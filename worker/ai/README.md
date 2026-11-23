# Воркер AI

Движок метода `ai` по контракту
[`integritylens.ai/1`](../../docs/contracts.md#контракты-воркеров): получает
задание, оценивает каждый файл работы отдельно и пишет `result.json` или
ошибку файла. Под моделью — [aicode](../../tools/aicode/README.md) (GigaCheck,
int2, CPU); воркер — общий из [`worker/`](../README.md), движок запускается
отдельным процессом `python -m worker.ai run --input … --output …`.

## Быстрый старт

Стек с полным движком и запрос с архивом для образца:

```sh
docker compose -f infra/docker-compose.yml up --build -d
curl -F archive=@docs/samples/works.zip -F language=java -F title=lab3 http://localhost:8080/api/batches
docker compose -f infra/docker-compose.yml logs --tail 20 worker-ai
```

На каждую работу ставится свой прогон `ai`, воркер берёт их по одному. Замер
на машине разработки (Docker Desktop, 20 vCPU, 6 работ по 1.2–1.6 КБ): все
шесть AI-прогонов — за 2 мин 30 с (22–26 с на прогон вместе с загрузкой
модели), память — 5 ГБ. Оценки шести работ — от 0.46 до 0.54.

## Оценки

`score` работы — доля байтов, попавших в «подозрительные» фрагменты; каждый
фрагмент — байтовое смещение с confidence. Порог по умолчанию — 0.05:
инструментальный 0.8 на int2-кванте не пересекается ни одним confidence на
реальном коде, то есть даёт пустые `suspicious`; 0.05 даёт сигнал, но шумный.
HTTP API отдаёт статус и оценку, фрагменты — в `runs.result` и в MinIO
(`runs/{runId}/attempt-{n}/result.json`).

Ошибка отдельного файла не валят прогон: файл получает `status: failed` и
`reason`, прогон завершается `done`.

## Что внутри

| | |
|---|---|
| [docs/scoring.md](docs/scoring.md) | задание и результат, как считаются оценки, порог, детерминизм |
| [docs/runtime.md](docs/runtime.md) | ресурсы и время, сборка образа, мок-движок |
| [aicode](../../tools/aicode/README.md) | сама модель: устройство, ограничения, качество |

## Проверка без модели

```sh
python -m worker.ai.selfcheck mock
python -m unittest discover -s worker/ai/tests -v
```
