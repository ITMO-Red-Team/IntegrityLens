# Производительность воркеров

Apple M4 Max, Docker `linux/arm64`, `--cpus 2 -m 4g`. На x86 не мерили. Вход по умолчанию —
50 различных решений ConPlag как сданы (3.8 КиБ, 380 меток на файл), 1225 пар. Данные — [`results/`](results/).
Все разделы, кроме [шаблонов](#шаблонные-алгоритмы), — с `ignoreTemplates: false`.

## Итог

| 50 работ, 2 ядра | Было | Стало |
|---|---|---|
| structsim, core | 61.5 с | 2.01 с |
| JPlag, core | 1.63 с | 0.68 с |
| подхват задания | 1561 мс | 87 мс |
| проверка результата | 132 мс | 21 мс |

«Было»: structsim — Python из `tools/structsim`, JPlag — CLI с ключами по умолчанию. Оценки не
изменились. Живой стек, от захвата до записи в БД ([`e2e.csv`](results/e2e.csv)): JPlag 0.87 с,
structsim 2.13 с; со стиранием шаблонов 0.92 и 2.09 с.

## Куда уходит время

| Этап | structsim | JPlag |
|---|---|---|
| скачивание, `job.json` | 50 мс | 50 мс |
| `semgrep-core` | 1780 мс (63 мс на файл, 58 из них — старт) | |
| метки, Python | 85 мс | |
| шаг 3 и тайлы | 74 мс | |
| JPlag | | 640 мс |
| сборка, проверка, S3, БД | 70 мс | 60 мс |

По ядрам ([`*_cpus.csv`](results/)): structsim 3.78 / 2.01 / 1.15 / 0.67 с на 1 / 2 / 4 / 8;
JPlag 1.11 / 0.68 / 0.62 / 0.62 с.

## Асимптотика

`n` — работ, `T` — меток в работе, `P = n(n−1)/2`.

| Этап | Было | Стало |
|---|---|---|
| подхват | опрос, до 2 с | `LISTEN/NOTIFY` |
| `semgrep-core` | `n·(c + αT)` | то же, по ядрам; `c ≈ 58 мс` |
| шаг 3 | `P·T²` | `P·T^1.5` |
| тайлы | `P·T^2.7` | `P·T^1.9` |
| проверка результата | `n·P` | `P` |
| JPlag | `c + P·T^1.8` | то же, меньше `c` |

От `n` ([`*_by_count.csv`](results/)): при `n = 50` шаг 3 — 74 мс из 2 с, остальное линейно по `n`.

| Работ | 5 | 10 | 20 | 30 | 40 | 50 |
|---|---|---|---|---|---|---|
| structsim | 0.25 с | 0.40 с | 0.79 с | 1.20 с | 1.63 с | 2.03 с |
| JPlag | 0.24 с | 0.28 с | 0.37 с | 0.48 с | 0.57 с | 0.68 с |

От длины, 10 работ, синтетика ([`*_by_size.csv`](results/)):

| Строк | 100 | 400 | 1600 | 6400 | 25600 (~1 МиБ) |
|---|---|---|---|---|---|
| structsim | 0.41 с | 0.58 с | 1.24 с | 4.29 с | 21.8 с |
| JPlag | 0.29 с | 0.32 с | 0.55 с | 5.28 с | — |

Предел API — 50 файлов по 1 МиБ. Оценка: structsim ~5 мин, JPlag ~30 мин, больше
`CORE_TIMEOUT_SECONDS` = 600. Измерено: 50 × 6400 строк у JPlag — 98 с.

## Что сделано

| Что | Эффект |
|---|---|
| шаг 3 и тайлы на Rust ([native](../../tools/structsim/docs/native.md)) | 58 с → 0.07 с |
| разбор параллельно по лимиту контейнера | 3.5 → 1.9 с |
| одинаковые файлы разбираются один раз | −63 мс на копию |
| короткий путь в `semgrep-core` | выгрузка в 1.4 раза меньше |
| без GC в процессе core | `json.loads` 197 → 94 мс |
| JPlag без проверки версии на GitHub | −0.46 с, нет зависания |
| JVM: SerialGC, AOT-кэш, только C1 до 1 МиБ входа | 1.16 → 0.68 с |
| `fastjsonschema`, одна проверка без `n³` | 132 → 21 мс |
| `LISTEN/NOTIFY` вместо опроса | 1561 → 87 мс |
| ожидание core без `sleep(0.2)` | до −200 мс |
| свои JVM (jlink) и `semgrep-core` из wheel | образы 620–640 МБ вместо 650 МБ + 1.6 ГБ |

C1 выключается выше 1 МиБ: на 2.9 МБ оба компилятора быстрее (6.34 с против 7.16 с).

## Шаблонные алгоритмы

Функции, похожие на функции из [`core/templates/`](../core/templates/) (9 файлов, 214 функций),
стираются из копии исходника до запуска метода ([контракт](../core/CONTRACT.md#шаблонные-алгоритмы)).
Сравнение: токены функции с обезличенными именами, числами и типами, `SequenceMatcher.ratio`;
порог 0.85 для функций короче 60 токенов, иначе 0.7. `main` не стирается.

ROC-AUC, [`templates_quality.csv`](results/templates_quality.csv). Правило и добавки к набору
подбирались по train ConPlag и IR-Plag, итог — по test.

| Набор | Оценка | Было | Стало | Прирост, 95% |
|---|---|---|---|---|
| ConPlag как сдано, test | JPlag `max` | 0.906 | 0.927 | [+0.008; +0.033] |
| | JPlag `avg` | 0.860 | 0.907 | [+0.029; +0.067] |
| | structsim | 0.852 | 0.895 | [+0.026; +0.064] |
| ConPlag вычищенный, test | JPlag `max` | 0.933 | 0.935 | не значим |
| | structsim | 0.962 | 0.963 | не значим |
| IR-Plag | JPlag `max` | 0.544 | 0.544 | не значим |
| | structsim | 0.611 | 0.618 | [+0.003; +0.013] |

Стирается 26% строк сданных работ, 3.4% вычищенных, 0.9% IR-Plag; ни одна работа не выпала.

Отвергнуто:
- `-bc` JPlag: на вычищенном коде AUC 0.939 → 0.910 ([ловушка 8](../../tools/jplag/pitfalls.md));
- куски меток structsim от 20 меток: полнота 28% против 65% у функций при той же точности;
- плоский порог 0.7: на IR-Plag стирает 7.2% строк и выбивает 6 работ; 0.6 стирает настоящие `solve`.

Цена: 63 мс на 50 работ ([`templates_speed.csv`](results/templates_speed.csv)). JPlag 0.69 → 0.73 с,
structsim 1.96 → 1.85 с (меньше кода на разбор). Линейно по длине, 2.3 МБ — 0.21 с. Перед `ratio`
отсечка по длине НОП битовым алгоритмом: вдвое быстрее, решения те же на 2937 формах ConPlag.

Не покрыто: алгоритм внутри `main`; задание, где шаблон и есть решение (`ignoreTemplates` из API не задать).

## Что не сделано

| Идея | Почему |
|---|---|
| ускорить старт `semgrep-core` | только через `LD_PRELOAD`-форк чужого бинарника |
| метки на Rust | 1.7 мс на файл против 62 мс `semgrep-core` |
| постоянная JVM | ~−0.4 с ценой 400 МБ памяти постоянно |
| Java API JPlag вместо CLI | ~−0.15 с, привязка к внутренним классам |
| параллельный S3 | 30 мс против 32 мс |
| отбор пар | контракт требует все пары |

## Как повторить

```bash
docker compose -f infra/docker-compose.yml build worker-jplag worker-structsim
mkdir -p results && chmod 777 results
VERSIONS=research/one/data/conplag/versions
IRPLAG=research/one/data/irplag/IR-Plag-Dataset
```

```bash
docker run --rm --cpus 2 -m 4g -e BENCH_CPUS=2 -v "$PWD/$VERSIONS/version_1:/conplag:ro" -v "$PWD/results:/results" integritylens-worker-jplag:0.2.0 python -m worker.bench.run before_after --conplag /conplag --out /results/jplag_before_after.csv --repeats 7
```

```bash
docker run --rm --cpus 4 -m 6g -v "$PWD/$VERSIONS:/conplag:ro" -v "$PWD/$IRPLAG:/irplag:ro" -v "$PWD/results:/results" integritylens-worker-jplag:0.2.0 python -m worker.bench.templates --conplag /conplag --irplag /irplag --out /results/templates_quality.csv
```

Для structsim — образ `integritylens-worker-structsim:0.2.0`.

| Эксперимент | Что меняет | Время |
|---|---|---|
| `run before_after` | Python/Rust, ключи JVM | structsim 6 мин, JPlag 1 мин |
| `run by_count` | число работ | 1 мин |
| `run by_size --count 10` | длина, синтетика | 3–5 мин |
| `run single` | запускать с разным `--cpus` | 1 мин |
| `run templates` | цена стирания | 1 мин |
| `bench.templates` | качество стирания | 5 мин |
| `bench.pair_cost` | цена пары, эталон и native | 4 мин |
| `bench.e2e` | путь через API, нужен стек | 1 мин |

Медиана повторов. Синтетика — [`corpus.py`](corpus.py), по зерну.
