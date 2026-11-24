# Движок AI

Модуль выполняет файловый контракт `integritylens.ai/1`: читает `job.json`,
анализирует исходники и записывает `result.json`. Инференс использует
[tools/aicode](../../tools/aicode/README.md), GigaCheck int2 на CPU.

Постановка заданий, захват очереди, скачивание исходников и сохранение результата
в PostgreSQL относятся к адаптеру новой системы. Этот модуль не изменяет API,
схему БД и frontend. В общий Compose он пока не подключён.

## Запуск

Собрать образ из корня репозитория:

```sh
docker build -f worker/ai.Dockerfile --target ai -t integritylens-worker-ai:0.1.1 .
```

Подготовить рабочий каталог:

```text
work/
  input/job.json
  input/submissions/<submissionId>/source.java
  output/
```

`inputFiles[].path` в задании — `input/submissions/<submissionId>/source.java`,
относительно `work/`. `output/` должен быть доступен пользователю контейнера
на запись. Образ содержит модель; при запуске она не скачивается.

```sh
docker run --rm --init --memory=5g -v "$PWD/work:/work" \
  integritylens-worker-ai:0.1.1 run --input /work/input --output /work/output
```

Без Docker, с зависимостями aicode и fastjsonschema:

```sh
python -m worker.ai run --input work/input --output work/output
```

Коды выхода: 0 — результат записан (включая ошибки отдельных файлов),
1 — сбой инструмента, 2 — неверный вход. Адаптер проверяет результат перед
публикацией. Версия модели и фактические параметры находятся в результате.

## Оценки

`score` — доля байтов в подозрительных фрагментах; фрагменты содержат смещения
UTF-8 и confidence. Порог по умолчанию 0.05 даёт шумный сигнал на int2-модели;
это не доказательство происхождения кода. Подробности:
[расчёт оценок](docs/scoring.md), [ресурсы](docs/runtime.md).

## Проверка без модели

```sh
python -m worker.ai.selfcheck mock
python -m unittest discover -s worker/ai/tests -v
```
