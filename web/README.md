
# IntegrityLens Web

Frontend приложения IntegrityLens.

## Стек

- React 18
- TypeScript
- Vite
- React Router
- TanStack Query
- Tailwind CSS
- MSW (Mock Service Worker)
- Vitest

## Запуск

Требуется Node.js 24.

```bash
cd web
npm ci
npm run dev
```

Frontend: http://localhost:5173

По умолчанию используются MSW-моки.

Для подключения существующего backend:

```bash
npm run dev:api
```

Backend должен быть запущен на localhost:8080.

## API-контракты

На этапе миграции поддерживаются два контракта.

### Текущий API

Источник:

`api/openapi.yaml`

Сгенерированные типы:

`web/src/api/schema.ts`

Генерация:

```bash
npm run gen:api
```

Используется существующими экранами.

### Новый API

Источник:

`docs/openapi.yaml`

Сгенерированные типы:

`web/src/api/target/schema.ts`

Генерация:

```bash
npm run gen:api:target
```

Связанные файлы:

- `src/api/target/types.ts` — типы
- `src/api/target/client.ts` — HTTP-клиент
- `src/api/target/hooks.ts` — React Query

Сгенерированные schema.ts не редактируются вручную.

## Моки нового API

MSW-обработчики:

`src/mocks/target-handlers.ts`

Тестовые данные:

`src/mocks/target-fixtures.ts`

Автоматические тесты:

`src/mocks/target-handlers.test.ts`

### Сценарии

| Сценарий | Описание |
|---|---|
| success | Проверка завершена |
| empty | Нет сдач |
| pending | Анализ выполняется |
| error | Ошибка анализа |

В новом API Structsim и AI имеют независимые состояния.

Статусы Score:

- `ok` — результат готов
- `pending` — анализ выполняется
- `failed` — ошибка анализа
- `not_applicable` — оценка неприменима

При `ok` значение находится в диапазоне 0–1.

При остальных статусах `value` равен null.

Для `failed` указывается причина ошибки.

### Демо-задания

MSW предоставляет фиксированные задания для каждого
из четырёх сценариев.

Основные маршруты:

- GET /api/assignments
- GET /api/assignments/{assignmentId}
- GET /api/assignments/{assignmentId}/submissions
- GET /api/assignments/{assignmentId}/submissions/{submissionId}
- GET /api/assignments/{assignmentId}/pairs/{a}/{b}

Для проверки HTTP-ошибки списка заданий используется
параметр `mockHttpError=1`.

Для пустого списка заданий — `mockScenario=empty`.

Тесты проверяют успешные, пустые, выполняющиеся и
ошибочные состояния.

Остальные операции нового API добавляются по мере
реализации соответствующей функциональности.

## Проверки

```bash
npm run typecheck
npm test
npm run build
```

## Docker

С моками:

```bash
docker build -t integritylens-web ./web
```

С настоящим API:

```bash
docker build --build-arg VITE_USE_MOCKS=false -t integritylens-web ./web
```

## CI

GitHub Actions выполняет:

1. Установку зависимостей.
2. Генерацию типов текущего API.
3. Генерацию типов нового API.
4. Проверку синхронизации схем.
5. Проверку TypeScript.
6. Запуск тестов.
7. Production build.
8. Сборку Docker-образа.

При расхождении сгенерированных типов с OpenAPI CI
завершится ошибкой.
