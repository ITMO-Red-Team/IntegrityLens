# Инфраструктура

Compose запускает API, PostgreSQL, MinIO и воркеры Structsim и AI. Для v2 используются чистые тома PostgreSQL и MinIO. Имя проекта по умолчанию — `integritylens-v2`.

Передайте настройки через окружение или отдельный `.env`. Для ссылок сдачи задайте `INTEGRITYLENS_PUBLIC_BASE_URL`. По умолчанию API доступен на `127.0.0.1:8080`. На сервере разместите HTTPS reverse proxy перед API, чтобы браузер отправлял Secure-cookie.

```sh
docker compose --env-file .env -f infra/docker-compose.yml up --build -d --wait
```

AI-образ скачивает проверяемый архив с моделью при сборке. Дайте Docker не менее 6 ГиБ памяти для сборки AI-образа и запуска модели. При смене артефакта обновите `AICODE_MODEL_URL`, `AICODE_MODEL_SHA256` и `AI_METHOD_VERSION`. При выпуске новой версии Structsim обновите `STRUCTSIM_METHOD_VERSION`, чтобы воркер и API использовали одну версию.

## Сквозная проверка

Из корня репозитория:

```sh
python3 -m pip install -r infra/requirements.txt
API_PORT=18080 MAINTENANCE_INTERVAL=PT2S docker compose -p integritylens-check \
  -f infra/docker-compose.yml up --build -d --wait
python3 infra/smoke_test.py --base http://localhost:18080
```

Сквозной тест проверяет все операции OpenAPI на поднятом приложении и оставляет тестовые задания и пользователей в выбранной БД. На CI используется реальный Structsim и тестовый AI. Для такого запуска задайте `AI_BUILD_TARGET=ai-mock-worker`, `AI_ENGINE=mock` и `AI_METHOD_VERSION=aicode-mock/1`. При `AI_MOCK_SCENARIO=partial` сквозной тест запускается с флагом `--expect-ai-failure`.

Тесты аренды и удаления объектов запускаются против того же тестового Compose:

```sh
API_PORT=18080 docker compose -p integritylens-check -f infra/docker-compose.yml \
  run --rm --no-deps worker-structsim python -m unittest discover -s worker/tests -v
```

Удаление отдельного тестового окружения вместе с его томами:

```sh
docker compose -p integritylens-check -f infra/docker-compose.yml down -v
```
