# research/one — JPlag и Dolos против structsim на решениях одного языка

Исследование по issue
[ITMO-Red-Team/IntegrityLens#6](https://github.com/ITMO-Red-Team/IntegrityLens/issues/6).
Вопрос: как пользоваться инструментами из [`tools/`](../../tools/README.md) и
какой из них лучше на программах одного языка.

**Baseline — JPlag**, а не Dolos, как сказано в issue. JPlag цитируют
заметно чаще, он академический стандарт и легче Dolos. Dolos добавлен
третьим инструментом отдельным экспериментом
([e04](experiments/e04_dolos/README.md)) на тех же парах. На реальных
решениях он слабее JPlag, так что выбор baseline подтвердился.

## Ответ

Язык — Java, три набора с разметкой «плагиат / не плагиат», 2280 пар. Все
три инструмента запускались с настройками по умолчанию.

| Набор | Пар (плагиат) | JPlag avg | JPlag max | structsim | Dolos | Кто лучше |
|---|---|---|---|---|---|---|
| IR-Plag — учебные задачи, синтетический плагиат L1–L6 | 460 (355) | 0.556 | 0.544 | 0.611 | **0.670** | Dolos, значимо; structsim значимо лучше JPlag |
| ConPlag v1 — олимпиадные решения как сданы | 911 (251) | 0.865 | **0.909** | 0.855 | 0.838 | JPlag max, значимо; structsim с JPlag avg наравне, Dolos хуже JPlag avg |
| ConPlag v2 — те же, без шаблонного кода | 909 (250) | 0.940 | 0.939 | **0.964** | 0.916 | structsim, значимо; Dolos хуже обоих, значимо |

В ячейках — ROC-AUC: вероятность того, что пара-плагиат получит оценку выше
пары-неплагиата. «Значимо» означает, что 95% интервал парного бутстрэпа не
содержит нуля.

1. **Сам скор у structsim выше всегда**, у плагиата и у неплагиата. На
   плагиате 0.73 против 0.47 (IR-Plag), 0.66 против 0.57 (v1), 0.79 против
   0.68 (v2). Ноль structsim не выдаёт никогда, а JPlag выдаёт ровно 0.0
   от 13% до 48% честных пар. Шкалы разные, так что порог одного
   инструмента к другому не переносится, а «больше» в сыром числе ещё не
   значит «лучше».
2. **По качеству structsim лучше JPlag на двух наборах из трёх.** Выигрыш
   там, где плагиат переделан: переставлены операторы, вставлен код. Такие
   правки рвут длинные общие куски, на которых держится JPlag
   ([e01](experiments/e01_irplag/README.md), уровень L3: 0.743 против 0.539).
3. **structsim проигрывает на частичном заимствовании**: чужое решение
   вставлено в собственный шаблон. Все меры structsim симметричны, а
   `maxSimilarity` у JPlag — односторонняя мера включения
   ([e02](experiments/e02_conplag/README.md)). На парах близкой длины
   structsim лучше и на v1.
4. **Настройка JPlag порядок не меняет, но ослабляет вывод на IR-Plag**:
   при пороге `-t 5`, подобранном задним числом, перевес structsim там
   падает с +0.055 до +0.015 AUC [−0.016; +0.045] и перестаёт быть значимым.
   На v2 он сохраняется (+0.014 [+0.002; +0.027]), на v1 JPlag max впереди
   при любом пороге ([e03](experiments/e03_jplag_threshold/README.md)).
5. **Dolos ответа не меняет** ([e04](experiments/e04_dolos/README.md)). На
   обеих версиях ConPlag он значимо хуже JPlag (AUC −0.027 и −0.024 против
   avg). На v2 он значимо хуже и structsim, на v1 наравне с ним по AUC и
   хуже по AP. Лучший Dolos только на маленьком синтетическом IR-Plag: его
   k-грамма — около полутора строк, и после перестановки операторов общих
   k-грамм остаётся много. Односторонняя мера, выведенная из тех же
   отпечатков, поднимает Dolos на v1 с 0.838 до 0.891, как `maxSimilarity`
   поднимает JPlag с 0.865 до 0.909.

Что из этого следует для structsim: нужна односторонняя мера включения
(на v1 она добавляет JPlag и Dolos по 0.04–0.05 AUC) и исключение общего
кода, аналог `-bc`. В текущем виде `contrast-z` не улучшает ранжирование, а
мера 2 (фрагменты) по отдельности не хуже `COMBINED`.

## Эксперименты

Каждый эксперимент — это `run.py` и `README.md` (постановка, результат,
интерпретация, вывод) в `experiments/`. Числа и рисунки лежат в
`results/<эксперимент>/`.

| | Что | Время |
|---|---|---|
| [e00_smoke](experiments/e00_smoke/README.md) | инструменты без Docker считают то же, что в `tools/`, а Dolos стоит в прибитой версии: 14 проверок на входе с известным ответом | 3 с |
| [e01_irplag](experiments/e01_irplag/README.md) | IR-Plag: 7 учебных задач, плагиат по уровням L1–L6 | 1 мин |
| [e02_conplag](experiments/e02_conplag/README.md) | ConPlag: настоящие пары с Codeforces, с шаблонами и без | 5 мин |
| [e03_jplag_threshold](experiments/e03_jplag_threshold/README.md) | чувствительность к порогу JPlag `-t` и `--normalize`, с интервалами разницы при каждом пороге | 5 мин |
| [e04_dolos](experiments/e04_dolos/README.md) | Dolos третьим инструментом на тех же парах, что e01 и e02 | 1 мин |

## Как воспроизвести

Нужны [uv](https://docs.astral.sh/uv/), **JDK ≥ 25** и, для Dolos,
**Node.js ≥ 18 с npm**. Нужна именно JDK с `javac`, а не JRE, см.
[tools/jplag/usage.md](../../tools/jplag/usage.md#почему-именно-jdk).
Docker не нужен.

```bash
cd research/one
uv sync                                   # окружение, включая structsim из tools/ и semgrep 1.180.0
uv run python -m research_one.fetch       # jar JPlag, оба набора и Dolos (npm ci), всё прибито
uv run python experiments/e00_smoke/run.py
uv run python experiments/e01_irplag/run.py
uv run python experiments/e02_conplag/run.py
uv run python experiments/e03_jplag_threshold/run.py
uv run python experiments/e04_dolos/run.py
```

Скачивается около 96 МБ: jar JPlag 6.3.0 (84 МБ, sha256 тот же, что в
`tools/jplag/jplag.Dockerfile`), IR-Plag (0.4 МБ, коммит `f07e951`) и
ConPlag (12 МБ, Zenodo 7332790, md5 совпадает с опубликованным). Dolos
2.9.3 ставится `npm ci` по `package-lock.json` (версии и sha512 всех
пакетов) в `node_modules/`. Установка занимает около 30 с и 470 МБ: при ней
собираются грамматики tree-sitter всех языков Dolos.

## Как пользоваться инструментами без Docker

### JPlag

Та же команда, что в [tools/jplag/usage.md](../../tools/jplag/usage.md), но
`java -jar` вместо `docker run`:

```bash
java -jar vendor/jplag-6.3.0-jar-with-dependencies.jar --mode RUN \
  <корень: одна поддиректория = одно решение> -l java \
  -r <out>/report --csv-export --cluster-skip --overwrite
```

Оценки брать из `<out>/report/results.csv`. Не разобравшиеся файлы — в
`runInformation.json` внутри `<out>/report.jplag` (`failedSubmissions`).
Обёртка с проверкой версии Java и наличия `javac` —
[`src/research_one/jplag.py`](src/research_one/jplag.py).

### structsim

CLI `structsim` запускает шаг 1 (`semgrep-core`) только в контейнере. Без
Docker тот же бинарник той же версии есть внутри pip-пакета `semgrep==1.180.0`.
[`src/research_one/ssim.py`](src/research_one/ssim.py) подменяет только шаг 1
и вызывает его с теми же ключами. Шаги 2–4 берутся из `tools/structsim`
без изменений:

```python
from pathlib import Path
from research_one import ssim

programs, skipped = ssim.to_programs({"a": Path("a/Main.java"), "b": Path("b/Main.java")})
ssim.explain_pairs(programs, [("a", "b")])
# {frozenset({'a', 'b'}): {'order': …, 'shingle': …, 'blocks': …, 'combined': …}}
ssim.contrast_of_set(programs)   # contrast-z, если программ ≥ 5
```

Что подмена даёт те же метки, что контейнер, проверено в e00 на всех
выгрузках из `tools/structsim/tests/data` (9 языков).

### Dolos

В `tools/` его нет. Без Docker это CLI из `node_modules/`:

```bash
node_modules/.bin/dolos run -l java -f csv -o <out> <файл решения 1> <файл решения 2> …
```

Оценки брать из `<out>/pairs.csv` (`similarity`, плюс `leftCovered` и
`rightCovered`, из которых выводится односторонняя мера). В `<out>/files.csv`
лежит число отпечатков каждого файла и дерево: узел `ERROR` в нём значит, что
файл разобран не целиком. Отказа в разборе у Dolos не бывает. `<out>` не
должен существовать. Справка CLI пишет, что `-M` по умолчанию 0.9, но это не
так: по умолчанию ничего не отбрасывается. Обёртка, которая раскладывает
решения как для JPlag (одна поддиректория — одно решение из одного файла),
— [`src/research_one/dolos.py`](src/research_one/dolos.py).

## Устройство

```
research/one/
  README.md               — этот файл: ответ, как воспроизвести, как пользоваться
  pyproject.toml, uv.lock — окружение; structsim подключён из tools/ (editable)
  package.json, package-lock.json — Dolos 2.9.3 для npm ci
  src/research_one/
    paths.py              — где vendor/, data/, work/, results/
    fetch.py              — скачивание с прибитыми суммами
    jplag.py              — JPlag из локального jar
    dolos.py              — Dolos из node_modules
    ssim.py               — structsim с локальным semgrep-core
    datasets.py           — IR-Plag и ConPlag → размеченные пары и раскладка для JPlag
    evaluate.py           — JPlag и structsim по набору → одна таблица пар
    metrics.py            — ROC-AUC, AP, F1 при пороге, парный бутстрэп
    report.py             — таблицы и рисунки
  experiments/eNN_*/      — run.py + README.md с интерпретацией и выводом
  results/eNN_*/          — итоговые таблицы и рисунки (в git)
  vendor/, data/, work/, node_modules/ — скачиваемое и промежуточное (не в git)
```

## Ограничения

- **Один язык — Java.** Оба набора с разметкой на Java. На других языках
  результат может отличаться: у JPlag свой разборщик для каждого языка, у
  structsim сходимость языков проверена только на одном алгоритме.
- **IR-Plag мал и синтетичен**: программы по ~28 строк, плагиат сделан по
  заданию. Основной вес вывода — на ConPlag.
- **Порог в F1** выбирается на тех же наборах (leave-one-case-out для
  IR-Plag, train/test авторов для ConPlag), но размеченных наборов на Java всего
  два, и внешней проверки на третьем нет.
- **Dolos — только с настройками по умолчанию** (k = 23, окно 17).
  Чувствительность к ним, как e03 для JPlag, не изучалась.
- **Сравнивалась оценка пары, а не весь продукт.** Тайлы, отчёты и скорость
  не сравнивались. По времени все три укладываются в минуты на тысячу файлов.
  Почти всё время structsim уходит на `contrast-z` по всем парам задачи.
