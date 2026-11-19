# Инфраструктура

Сервисы, версии образов и переменные с умолчаниями — в [docker-compose.yml](docker-compose.yml).

Настройки передавайте через окружение или незакоммиченный `.env`:

```sh
docker compose --env-file .env -f infra/docker-compose.yml up --build -d
```

Старый том PostgreSQL 16 требует переноса через `pg_dump`/restore перед запуском версии 18.
При `SIGILL` во время сборки в x86 VM на ARM задайте `JAVA_BUILD_OPTS=-Xint`.

В основном Compose-стеке запускается только `worker-structsim`. AI-анализатор
`tools/aicode` пока является отдельной пакетной CLI-командой: он читает файлы
из каталога и выдаёт текстовый или JSON-отчёт. У него нет адаптера очереди,
интеграции с API/хранилищем результатов или длительно работающего worker entrypoint,
поэтому он намеренно не объявлен как Compose worker. Для отдельного ручного
анализа см. [инструкцию aicode](../tools/aicode/README.md). Подключение к очереди
потребует реализации адаптера, определяющего входной контракт, передачу исходников
и сохранение отчёта.

## Проверки

Из корня, с Python и зависимостями из `infra/requirements.txt`:

```sh
API_PORT=18080 docker compose -p integritylens-check -f infra/docker-compose.yml up --build -d
python3 infra/smoke_test.py --base http://localhost:18080
python3 infra/export_openapi.py --base http://localhost:18080 --check
```

Сквозной тест идёт и на настоящих методах, и на моке с `CORE_ENGINE=mock MOCK_SCENARIO=success`,
и оставляет пакеты в тестовой БД. Для тестов очереди и методов дождитесь завершения заданий и
остановите воркеры:

```sh
docker compose -p integritylens-check -f infra/docker-compose.yml stop worker-structsim
docker compose -p integritylens-check -f infra/docker-compose.yml run --rm --no-deps worker-structsim \
  python -m unittest discover -s worker/core/tests -v
```

Удалить тестовый стек вместе с его данными:

```sh
docker compose -p integritylens-check -f infra/docker-compose.yml down -v
```

## Переход на новую архитектуру
Текущий API ещё создаёт прогоны JPlag; после удаления сервиса они останутся в очереди. Старый smoke_test.py и экспорт OpenAPI не являются проверками нового стека. Полноценный сквозной запуск требует задач #39, #40, #43 и #44.
