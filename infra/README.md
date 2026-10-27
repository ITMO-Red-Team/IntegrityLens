# Инфраструктура

Сервисы, версии образов и переменные с умолчаниями — в [docker-compose.yml](docker-compose.yml).

Настройки передавайте через окружение или незакоммиченный `.env`:

```sh
docker compose --env-file .env -f infra/docker-compose.yml up --build -d
```

Старый том PostgreSQL 16 требует переноса через `pg_dump`/restore перед запуском версии 18.
При `SIGILL` во время сборки в x86 VM на ARM задайте `JAVA_BUILD_OPTS=-Xint`.

## Проверки

Из корня, с Python и зависимостями из `worker/requirements.txt`:

```sh
API_PORT=18080 docker compose -p integritylens-check -f infra/docker-compose.yml up --build -d
python3 infra/smoke_test.py --base http://localhost:18080
python3 infra/export_openapi.py --base http://localhost:18080 --check
```

Сквозной тест требует `MOCK_SCENARIO=success` и оставляет пакеты в тестовой БД.
Для тестов очереди дождитесь завершения заданий и остановите воркеры:

```sh
docker compose -p integritylens-check -f infra/docker-compose.yml stop worker-jplag worker-structsim
docker compose -p integritylens-check -f infra/docker-compose.yml run --rm --no-deps worker-jplag \
  python -m unittest discover -s worker/tests -v
```

Удалить тестовый стек вместе с его данными:

```sh
docker compose -p integritylens-check -f infra/docker-compose.yml down -v
```
