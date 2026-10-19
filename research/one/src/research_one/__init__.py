"""Исследование 1: JPlag (baseline) против structsim на решениях одного языка.

Что где лежит — README.md рядом с pyproject.toml. Модули:

- `paths`   — где окружение, данные и результаты;
- `fetch`   — скачать jar JPlag и наборы данных, поставить Dolos; всё прибито;
- `jplag`   — запуск JPlag из локального jar, без Docker;
- `dolos`   — запуск Dolos CLI из node_modules, без Docker;
- `ssim`    — structsim с локальным semgrep-core, без Docker;
- `datasets`— раскладка IR-Plag и ConPlag в пары с разметкой;
- `metrics` — насколько оценка отделяет плагиат от неплагиата.
"""
