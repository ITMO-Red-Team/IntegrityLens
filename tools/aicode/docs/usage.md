# Запуск и сборка aicode

Всё, что нужно после быстрого старта из [README](../README.md): готовые
артефакты модели, локальный экспорт, варианты сборки образа и запуск без
Docker. Устройство модели и ограничения — в
[how-it-works.md](how-it-works.md).

## Готовые артефакты из релиза

Чтобы не экспортировать модель на каждой машине, артефакты выложены ассетом
релиза [`aicode-model-v0.1.0`](https://github.com/ITMO-Red-Team/IntegrityLens/releases/tag/aicode-model-v0.1.0)
— `gigacheck-int2.zip`, 1.59 GB,
sha256 `afd7277e650d08055c6deca54375dabc8423cdc289405b9c08343058a750c28c`.
`fetch_model.py` — standalone-скрипт на стандартной библиотеке Python (нужен
любой Python 3, без зависимостей и без venv): он скачивает архив, сверяет
сумму при загрузке и распаковывает в `artifacts/gigacheck-int2`, пропуская
скачивание, если артефакты уже на месте (флаги: `--force`, `--url`, `--sha256`,
`--output`). Артефакты из релиза и артефакты локального экспорта
взаимозаменяемы: сборка образа устроена одинаково.

Сопровождающим — выпуск нового релиза артефактов: упаковка должна
воспроизводить layout «один каталог верхнего уровня с файлами модели», а
после упаковки нужно обновить `ARTIFACT_SHA256` в `fetch_model.py` и сумму в
этом файле. Перезапакованный без обновления суммы архив не пройдёт проверку:

```bash
cd tools/aicode/artifacts
python -m zipfile -c gigacheck-int2.zip gigacheck-int2
gh release create aicode-model-v0.2.0 gigacheck-int2.zip \
    --title "aicode model artifacts v0.2.0" \
    --notes "Quantized GigaCheck-Detector-Multi (int2) artifacts for tools/aicode."
```

Сам zip в git не хранится: он лежит в игнорируемом `artifacts/` и исключён из
build context через `.dockerignore`.

## Экспорт модели локально

По умолчанию модель скачивается и квантуется заранее, на машине сборки, а
Docker-образ собирается из готовых файлов и модель из сети не скачивает.
Экспорт требует Python 3.11+ и `uv` (см. «Запуск без Docker») и около 14 GB
места под исходный checkpoint; результат пишется в пустой каталог:

```bash
cd tools/aicode
HF_HOME="$PWD/.hf-cache" uv run python -m aicode.export_model --output artifacts/gigacheck-int2
docker compose -f ../docker-compose.yml build aicode
docker run --rm -v /path/to/source:/work integritylens/aicode:0.1.1 /work
```

Экспорт проверяется локально без Docker:

```bash
uv run aicode src --model artifacts/gigacheck-int2 --local-files-only
```

Загрузка таких артефактов использует C++-опы Quanto над упакованными весами:
на Linux расширение собирается при сборке образа из установленного `g++`, а на
Windows локальный запуск без MSVC Build Tools падает на компиляции расширения —
на этой платформе проверяйте экспорт через Docker-образ.

Все пути, куда экспорт что-то скачал, записываются в файл
`artifacts/gigacheck-int2.downloads.txt`: исходный checkpoint попадает в кэш
Hugging Face, и без этого файла его легко потерять из виду. Каждая строка
манифеста — полный путь; после проверки экспорта скачанное из кэша можно
удалить. Если путь до домашней папки содержит не-ASCII символы (например,
кириллицу в имени пользователя Windows), sentencepiece не откроет файлы
токенизатора из такого кэша — поэтому в командах выше задан `HF_HOME` с путём
без них; этот каталог тоже попадает в манифест.

Чтобы собрать образ из другого каталога артефактов, задайте `AICODE_MODEL_DIR`
(путь внутри `tools/aicode`, по умолчанию `artifacts/gigacheck-int2`).
Контейнер запускается офлайн с уже включённым checkpoint; до сервера нужно
передать готовый образ (например, опубликовать его в registry или перенести
через `docker save` / `docker load`). Сборка проверяет, что скопированные веса
загружаются, — образ с испорченным экспортом не соберётся. Режим квантования
записан внутри артефактов (`aicode_model.json`), поэтому отчёт показывает
фактический режим без дополнительных переменных окружения.

Для компромисса между размером и качеством экспортируйте int4-вариант и
соберите его:

```bash
uv run python -m aicode.export_model --output artifacts/gigacheck-int4 --quantization int4
AICODE_MODEL_DIR=artifacts/gigacheck-int4 docker compose -f ../docker-compose.yml build aicode
```

Если экспорт на машине сборки невозможен, остался вариант, при котором модель
скачивается и квантуется прямо при сборке образа (тогда Python и `uv` на хосте
не нужны). В этом режиме пути загрузок в манифест не попадают: файлы оседают
в кэше сборки Docker, который освобождается через `docker builder prune`.

```bash
docker build -f tools/aicode/aicode.Dockerfile --target image-hub \
    -t integritylens/aicode:0.1.1 tools/aicode
# для int4 добавьте --build-arg AICODE_QUANTIZATION=int4
```

## Запуск без Docker

Нужны Python 3.11+, `uv` и доступ в интернет для установки зависимостей и
первого скачивания checkpoint. Сначала установите CPU-only зависимости:

```bash
cd tools/aicode
uv sync
uv pip install --no-deps "git+https://github.com/ai-forever/gigacheck.git@8cdbe6a1ce628b49b4aa0794edf7a425a47a774c"
```

Для `torch` в `pyproject.toml` закреплена версия 2.7.1 из официального CPU
индекса PyTorch. Пакет `gigacheck` ставится без его полного набора зависимостей
для обучения: inference использует только объявленные здесь библиотеки.

```bash
uv run aicode src/main.py
uv run aicode src
uv run aicode src --json --output report.json
uv run aicode src --threshold 0.9 --quantization int4
uv run aicode src --quantization none --dtype float32
```

`--quantization int2` — значение по умолчанию и самый экономный по памяти
режим. `int4` уменьшает потери качества, но не подходит как гарантия запуска в
5 GB RAM. При обычном запуске quantization применяется при загрузке весов на
CPU; исходный checkpoint и его кэш на диске остаются около 14 GB.
`--quantization none` отключает PTQ.

Если вы повторно запускаете `uv sync`, после него снова установите runtime-пакет
`gigacheck`: он намеренно не входит в зависимости обучения upstream-проекта.

Модель можно заранее скачать и затем работать без доступа к сети:

```bash
uv run aicode src
uv run aicode src --local-files-only
```

При не-ASCII пути домашней папки (см. «Экспорт модели локально») и этот запуск
требует `HF_HOME` без таких символов; где именно осели скачанные файлы, проще
всего проверить через манифест экспорта.
