# infra

Общий `docker-compose.yml` запускает API, два воркера, PostgreSQL 16, MinIO и одноразовую инициализацию
bucket. Flyway применяет `db/migrations` при старте API; воркеры стартуют после успешного healthcheck API.
Наружу опубликован только порт API. БД и S3 доступны сервисам внутри Compose-сети.

## Настройки

Передавайте переменные окружения или используйте незакоммиченный `.env` в корне репозитория.
Для `.env` укажите `docker compose --env-file .env -f infra/docker-compose.yml ...`.

| Переменная | Значение по умолчанию |
| --- | --- |
| `API_PORT` | `8080` |
| `POSTGRES_USER` | `integritylens` |
| `POSTGRES_PASSWORD` | `integritylens-local` |
| `S3_ACCESS_KEY` | `integritylens` |
| `S3_SECRET_KEY` | `integritylens-local` |
| `MOCK_DELAY_SECONDS` | `2` |
| `MOCK_SCENARIO` | `success` |
| `JAVA_BUILD_OPTS` | пусто; при `SIGILL` в эмулируемой JVM задайте `-Xint` |

Значения по умолчанию предназначены для локального запуска. Bucket — `integritylens`.
Все сервисы пишут логи в stdout; API и воркеры используют JSON.

MinIO Community собирается из официального тега `RELEASE.2025-10-15T17-29-55Z`, SHA-256 архива
проверяется в Dockerfile. Готовые community-образы и бинарники на момент проверки недоступны.
Инициализация bucket использует тот же Python-образ, что и воркеры.

## Проверки

Из корня при запущенном стеке и установленном Python с зависимостями `worker/requirements.txt`:

```sh
python3 -m unittest discover -s core/tests -v
python3 infra/smoke_test.py
python3 infra/export_openapi.py --check
```

Сквозной тест создаёт пакет с пятью работами и проверяет конкурентную дедупликацию, результаты,
исходники, согласованность оценок, отзывы, ошибки и соответствие ответов сгенерированной OpenAPI.
Он рассчитан на `MOCK_SCENARIO=success`. Созданные им пакеты сохраняются в тестовой БД.

Изолированный стек можно запустить через `API_PORT=18080 docker compose -p integritylens-check
-f infra/docker-compose.yml up --build -d`, затем передать скриптам `--base http://localhost:18080`.

Тесты очереди требуют изолированного стека без других queued/running заданий. Остановите воркеры:

```sh
docker compose -p integritylens-check -f infra/docker-compose.yml stop worker-jplag worker-structsim
docker compose -p integritylens-check -f infra/docker-compose.yml run --rm --no-deps worker-jplag \
  python -m unittest discover -s worker/tests -v
```

Тесты создают и удаляют только собственные строки. Проверяются конкурирующие воркеры, истечение аренды,
защита от устаревшей попытки, исчерпание повторов и ошибка без повторов.

Если x86 Docker VM на ARM-хосте падает в JVM с `SIGILL`, используйте
`JAVA_BUILD_OPTS=-Xint docker compose -f infra/docker-compose.yml up --build -d`.
Этот аргумент действует только на Maven при сборке; runtime остаётся обычным JRE 25.
