# Работа с документацией

Страницы находятся в `docs/`, меню и настройки — в `mkdocs.yml`.

## Локальный просмотр

Из корня репозитория с Python 3.12:

```bash
python -m venv .venv-docs
# Windows PowerShell:
.venv-docs/Scripts/Activate.ps1
# Linux/macOS: source .venv-docs/bin/activate
python -m pip install mkdocs==1.6.1 mkdocs-material==9.6.14
python -m mkdocs serve
```

Сайт откроется по адресу `http://127.0.0.1:8000/`.

```bash
python -m mkdocs build --strict
```

Сборка записывается в `site/`. Этот каталог не коммитится.

## GitHub Pages

В настройках репозитория откройте **Settings → Pages → Build and deployment**
и выберите **Source: GitHub Actions**.

Workflow `docs.yml` проверяет сборку в pull request и публикует сайт после
изменений в `main`. Из другой ветки выполняется только сборка. Для ручного
запуска выберите workflow **Documentation** в Actions.

Ожидаемый адрес после первой успешной публикации:
`https://itmo-red-team.github.io/IntegrityLens/`.

Добавляя страницу, включите её в `nav` в `mkdocs.yml`. Для схем используйте
блоки кода с языком `mermaid`.
