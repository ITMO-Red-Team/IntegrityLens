# IntegrityLens

Бэкенд сравнения Java-работ: API, два воркера, PostgreSQL и MinIO.
Анализ пока возвращает детерминированные mock-оценки; сходство кода они не измеряют.

## Запуск

```sh
docker compose -f infra/docker-compose.yml up --build -d
curl -F archive=@works.zip -F language=java -F title=lab3 http://localhost:8080/api/batches
```

- [Swagger UI](http://localhost:8080/swagger-ui.html)
- [OpenAPI](http://localhost:8080/v3/api-docs)
- [Healthcheck](http://localhost:8080/api/health)

## Логи

```sh
docker compose -f infra/docker-compose.yml logs -f api worker-jplag worker-structsim
```

## Shutdown

С сохранением данных:

```sh
docker compose -f infra/docker-compose.yml down
```

С удалением данных:

```sh
docker compose -f infra/docker-compose.yml down -v
```

[Настройки и проверки](infra/README.md) · [API](api/README.md) ·
[Воркеры](worker/README.md) · [Контракт анализа](worker/core/CONTRACT.md) · [Исследования](research/README.md)
