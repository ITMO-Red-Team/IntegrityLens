# web

Страница: загрузка архива, таблица работ, просмотр пары файлов.

- Ничего не импортирует из проекта.
- Общается только с `api`. Контракт — `api/openapi.yaml`, типы фронта генерируются из него.
- Поднимается в Docker.

## Стек

React 18 + TypeScript, Vite, React Router, TanStack Query, Tailwind CSS. Моки API — MSW.

## Запуск

Нужен Node.js 24.

```bash
cd web
npm ci
npm run dev        # http://localhost:5173 — на моках, api не нужен
npm run dev:api    # http://localhost:5173 — с настоящим api на localhost:8080
```

Для `dev:api` бэкенд должен быть поднят: `docker compose -f infra/docker-compose.yml up --build -d`.

## Контракт с api

Когда меняется `api/openapi.yaml`:

```bash
npm run gen:api    # обновляет src/api/schema.ts
npm run typecheck  # покажет места, которые нужно поправить
```

CI падает, если `src/api/schema.ts` не совпадает с `api/openapi.yaml`.

## Проверки

```bash
npm run typecheck
npm test
npm run build
```

## Docker

```bash
docker build -t integritylens-web ./web                                   # демо на моках
docker build --build-arg VITE_USE_MOCKS=false -t integritylens-web ./web  # с api
```

В режиме с api nginx пересылает `/api/*` на сервис `api:8080` из `infra/docker-compose.yml`.

## Как устроено

| Папка | Что внутри |
|---|---|
| `src/api` | `schema.ts` (сгенерирован), короткие типы, клиент запросов, хуки React Query |
| `src/features/upload` | загрузка архива, страница пакета со статусами методов |
| `src/features/queue` | очередь: сортировка по методу, порог, K, кластеры |
| `src/features/compare` | два исходника пары рядом и оценки методов |
| `src/lib` | логика очереди, подписи работ S-001… |
| `src/mocks` | моки api в формате `api/openapi.yaml`, эталонный движок сходства |

- Оценки JPlag и structsim не объединяются: очередь сортируется по выбранному методу (`?method=`).
- В api работа — это UUID, а исходное имя файла может содержать ФИО. Поэтому в интерфейсе работы подписаны S-001, S-002… по порядку из api, а имя файла показывается по клику.
- Сортировку, порог и кластеры считает фронт, как требует контракт.