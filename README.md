# IntegrityLens

Backend для приёма Java-работ и анализа с помощью Structsim и локального AI-детектора. Исходники хранятся в MinIO, задания, очередь и результаты — в PostgreSQL.

## Запуск backend

```sh
docker compose -f infra/docker-compose.yml up --build -d --wait
```

Compose использует новые тома проекта `integritylens-v2`. Flyway применяет схему при первом старте API.

- [Swagger UI](http://localhost:8080/swagger-ui.html)
- [OpenAPI](http://localhost:8080/openapi.yaml)
- [Healthcheck](http://localhost:8080/api/health)

```sh
docker compose -f infra/docker-compose.yml logs -f api worker-structsim worker-ai
docker compose -f infra/docker-compose.yml down
```

Настройки чистого развёртывания и HTTP-проверка описаны в [infra/README.md](infra/README.md). Схема, архитектура и контракты доступны на [сайте документации](https://itmo-red-team.github.io/IntegrityLens/). Локальную сборку сайта описывает [docs/contributing.md](docs/contributing.md).
