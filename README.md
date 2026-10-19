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

```sh
docker compose -f infra/docker-compose.yml logs -f api worker-jplag worker-structsim
docker compose -f infra/docker-compose.yml down
```

`down` сохраняет данные, `down -v` удаляет их. Для отдельной установки Compose замените
`docker compose` на `docker-compose`.

[Настройки и проверки](infra/README.md) · [API](api/README.md) ·
[Воркеры](worker/README.md) · [Контракт анализа](core/CONTRACT.md) · [Исследования](research/README.md)
