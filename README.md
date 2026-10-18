# IntegrityLens

MVP бэкенда для сравнения Java-работ. API работает на Java 25 / Spring Boot, два Python-воркера
обрабатывают методы независимо. PostgreSQL хранит данные и очередь, MinIO — ZIP, исходники и результаты.
Сейчас оба метода возвращают детерминированные mock-оценки, не измеряющие сходство кода.

## Запуск

Из корня репозитория:

```sh
docker compose -f infra/docker-compose.yml up --build -d
```

Если Compose установлен отдельной программой, используйте `docker-compose` вместо `docker compose`.
Первый запуск собирает в том числе MinIO из официальных исходников. В x86 VM на ARM-хосте первая
компиляция может занять около 30 минут; последующие сборки используют кеш.

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>
- OpenAPI YAML: <http://localhost:8080/v3/api-docs.yaml>
- Проверка БД и хранилища: <http://localhost:8080/api/health>

```sh
curl -F archive=@works.zip -F language=java -F title=lab3 http://localhost:8080/api/batches
docker compose -f infra/docker-compose.yml logs -f api worker-jplag worker-structsim
docker compose -f infra/docker-compose.yml down
```

Данные сохраняются в Docker volumes при `down`. `down -v` удаляет данные этого Compose-проекта.
Настройки и тестирование описаны в [infra/README.md](infra/README.md), контракт API — в
[api/README.md](api/README.md), подключение настоящего анализа — в [core/CONTRACT.md](core/CONTRACT.md).

Фронт и авторизация в эту поставку не входят. API рассчитан на закрытое окружение; ограничение доступа
при публикации на демо обеспечивает внешний reverse proxy.
