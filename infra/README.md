# Инфраструктура

Сервисы, версии образов и переменные с умолчаниями — в [docker-compose.yml](docker-compose.yml).

Настройки передавайте через окружение или незакоммиченный `.env`:

```sh
docker compose --env-file .env -f infra/docker-compose.yml up --build -d
```

Старый том PostgreSQL 16 требует переноса через `pg_dump`/restore перед запуском версии 18.
При `SIGILL` во время сборки в x86 VM на ARM задайте `JAVA_BUILD_OPTS=-Xint`.

`worker-ai` тянет модель GigaCheck (~1.6 ГБ) из [релиза репозитория](https://github.com/ITMO-Red-Team/IntegrityLens/releases/tag/aicode-model-v0.1.0)
при сборке; на самопроверку образа нужно ~4 ГБ памяти. Смена версии модели — переменные
`AICODE_MODEL_URL` и `AICODE_MODEL_SHA256`. Настоящий AI-движок считает ~2 мин на каждую
сдачу (нужно 4 ГБ памяти), а пакет остаётся `processing`, пока не завершатся все AI-прогоны,
поэтому в проверках ниже он работает на моке: `AICODE_ENGINE=mock` (не задевает методы
сравнения). Время и потоки — в [worker/ai/README.md](../worker/ai/README.md#ресурсы-и-время).

## Проверки

Из корня, с Python и зависимостями из `infra/requirements.txt`:

```sh
API_PORT=18080 AICODE_ENGINE=mock docker compose -p integritylens-check -f infra/docker-compose.yml up --build -d
python3 infra/smoke_test.py --base http://localhost:18080
python3 infra/export_openapi.py --base http://localhost:18080 --check
```

Сквозной тест идёт и на настоящих методах, и на моке с `CORE_ENGINE=mock MOCK_SCENARIO=success`,
и оставляет пакеты в тестовой БД. Для тестов очереди и методов дождитесь завершения заданий и
остановите воркеры:

```sh
docker compose -p integritylens-check -f infra/docker-compose.yml stop worker-jplag worker-structsim worker-ai
docker compose -p integritylens-check -f infra/docker-compose.yml run --rm --no-deps worker-jplag \
  python -m unittest discover -s worker/tests -v
docker compose -p integritylens-check -f infra/docker-compose.yml run --rm --no-deps worker-jplag \
  python -m unittest discover -s worker/core/tests -v
docker compose -p integritylens-check -f infra/docker-compose.yml run --rm --no-deps worker-structsim \
  python -m unittest discover -s worker/core/tests -v
```

Удалить тестовый стек вместе с его данными:

```sh
docker compose -p integritylens-check -f infra/docker-compose.yml down -v
```
