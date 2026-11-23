# Ресурсы, время и сборка воркера AI

## Время

Замеры на машине разработки (Docker Desktop, 20 vCPU, лимит 5 ГБ,
float32-GEMM по умолчанию):

| | 4 потока | 12 потоков | 16 потоков (умолчание) | 20 потоков |
| --- | --- | --- | --- | --- |
| загрузка модели | ~6 с | ~6 с | ~6 с | ~6 с |
| работа 1.2–1.6 КБ (1 чанк ~420 токенов) | ~26 с | ~19 с | ~18 с | ~29 с |

Тот же прогон в прежней конфигурации (bfloat16, 4 потока) занимал ~81–160 с на
чанк; источник торможения и переключатель — `AICODE_COMPUTE` (см.
[tools/aicode/docs/how-it-works.md](../../../tools/aicode/docs/how-it-works.md)).
20 потоков на гибридном CPU (6P+8E) уже хуже 16: hyper-threading и E-ядра
спорят за GEMM.

Сквозной прогон шести работ из `docs/samples/works.zip` через стек: 2 мин 30 с
(было 9 мин 29 с), по 24–26 с на прогон вместе с загрузкой модели. Журнал
воркера на этот прогон:

```
worker-ai-1  | {"timestamp": "2026-11-23 20:45:07,673", "level": "INFO", "message": "Run started", "batchId": "2fece9ac-9ff1-4cb2-be56-00d14f669c90", "runId": "e8309869-6f1e-442e-8f07-3765f06e18d7", "timings": null}
worker-ai-1  | {"timestamp": "2026-11-23 20:45:30,484", "level": "INFO", "message": "Run completed", "batchId": "2fece9ac-9ff1-4cb2-be56-00d14f669c90", "runId": "e8309869-6f1e-442e-8f07-3765f06e18d7", "timings": {"downloadMs": 9.9, "coreMs": 22776.0, "validateMs": 0.1, "uploadMs": 8.2, "commitMs": 8.9}}
```

Сводка прогона в `GET /api/batches/{id}`:

```json
{"method": "ai", "methodVersion": "aicode-0.1.1+gigacheck-9fabe1503f+int2", "status": "done", "startedAt": "2026-11-23T20:43:04.552072Z", "finishedAt": "2026-11-23T20:45:30.480659Z", "parsed": 6, "failed": 0, "error": null}
```

Одна сдача ~1.5 КБ — около 25 с, 30 таких работ — около 13 мин (воркер один,
прогоны идут подряд). Память — 5 ГБ (пик ~5.3).

Таймаут прогона `AICODE_TIMEOUT_SECONDS` (1800 с) при 16 потоках вмещает
десятки полных чанков: границу по длине файла теперь задаёт не время счёта.
Ускорить дальше — `AICODE_THREADS` (до числа ядер), увеличить окно —
`AICODE_TIMEOUT_SECONDS`.

Прогон AI — один на сдачу (`runs.submission_id`, уникальный индекс);
повторный анализ той же сдачи другой версией модели потребует доработки схемы
(в целевой модели ключ — `(submission_id, method_version)`).

Рабочие файлы и отчёты удаляются после прогона (временный каталог воркера); в
MinIO уходит только `result.json`, в БД — результат, версия и параметры.

## Образ и модель

`worker/ai.Dockerfile`, цель `ai`: зависимости aicode (заморожены в `uv.lock`)
+ модель из [релиза репозитория](https://github.com/ITMO-Red-Team/IntegrityLens/releases/tag/aicode-model-v0.1.0)
(sha256 проверяется при скачивании). Сборка завершается selfcheck: мок
проверяет контракт, `real` — что модель грузится и результат воспроизводится
(нужно ~5.5 ГБ памяти на время сборки). Смена версии модели — build-args
`AICODE_MODEL_URL` и `AICODE_MODEL_SHA256` (или переменные того же имени в
окружении compose).

## Мок

Движок мокается отдельно от методов сравнения: `AICODE_ENGINE=mock` (у
воркеров сравнения — `CORE_ENGINE`), сценарии те же (`success`, `partial`,
`fail_once`, `tool_error`, `invalid_input`) — см.
[worker/README.md](../../README.md#моки). Проверка без модели:

```sh
python -m worker.ai.selfcheck mock
python -m unittest discover -s worker/ai/tests -v
```
