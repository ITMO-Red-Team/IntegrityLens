"""Шаг 1: исходный файл → синтаксическое дерево.

Единственная внешняя часть метода. Дерево берётся у `semgrep-core`, а не у
публичного `semgrep --dump-ast`: только core умеет `-full_token_info`, то есть
отдаёт позиции токенов, без которых тайлы нельзя показать строками исходника.
Подробности и замеры — `docs/step1.md`.

Контейнер запускается из Python, а не из shell-обёртки, по двум причинам:
монтирования задаются через `--mount` (для путей Windows это единственный
однозначный вид), и stdout разбирается сразу.
"""

from __future__ import annotations

import json
import re
import shutil
import subprocess
from collections.abc import Iterable
from pathlib import Path
from typing import Any

# semgrep-core раскрашивает свои сообщения, и в текст ошибки приезжают
# управляющие последовательности. В сообщении они только мешают.
_ANSI = re.compile(r"\x1b\[[0-9;]*m")

SEMGREP_VERSION = "1.180.0"
SEMGREP_IMAGE = f"integritylens/semgrep:{SEMGREP_VERSION}"

# Девять языков, на которых метод проверен. Значение — то, что идёт в -lang.
LANGUAGES: dict[str, str] = {
    ".cpp": "cpp",
    ".cc": "cpp",
    ".cxx": "cpp",
    ".py": "python",
    ".java": "java",
    ".go": "go",
    ".rs": "rust",
    ".swift": "swift",
    ".kt": "kotlin",
    ".cs": "csharp",
    ".js": "js",
}


class SemgrepError(RuntimeError):
    """Контейнер не запустился или вернул не то, что ожидалось."""


class NotParsed(SemgrepError):
    """Semgrep не смог разобрать файл.

    Отдельный тип, потому что это нормальный исход, а не поломка окружения:
    такой файл пропускается, но число пропусков надо считать и сообщать.
    """


def language_of(path: Path) -> str:
    """Язык по расширению файла.

    `-lang` у semgrep-core обязателен и по расширению не угадывается, поэтому
    сопоставление делаем сами и явно.
    """
    language = LANGUAGES.get(path.suffix.lower())
    if language is None:
        known = ", ".join(sorted(set(LANGUAGES)))
        raise SemgrepError(f"нет языка для {path.suffix!r}; известны: {known}")
    return language


def _docker() -> str:
    exe = shutil.which("docker")
    if exe is None:
        raise SemgrepError(
            "docker не найден в PATH. Шаг 1 запускает Semgrep только в контейнере."
        )
    return exe


def _mount(source: Path, target: str) -> list[str]:
    # type=bind вместо -v: в -v путь Windows с двоеточием после буквы диска
    # разбирается неоднозначно.
    return [
        "--mount",
        f"type=bind,source={source.resolve()},target={target},readonly",
    ]


def dump_ast(
    file: Path,
    language: str | None = None,
    image: str = SEMGREP_IMAGE,
    timeout: float = 120.0,
) -> dict[str, Any]:
    """Дерево одного файла, как его отдаёт `semgrep-core`.

    Монтируется директория файла, а не сам файл: так путь внутри контейнера
    остаётся простым и тем же для пачки файлов.
    """
    file = Path(file)
    if not file.is_file():
        raise SemgrepError(f"нет файла {file}")
    language = language or language_of(file)

    command = [
        _docker(),
        "run",
        "--rm",
        *_mount(file.parent, "/work"),
        image,
        "semgrep-core",
        "-lang",
        language,
        "-json",
        "-full_token_info",
        "-dump_ast",
        f"/work/{file.name}",
    ]
    try:
        result = subprocess.run(
            command,
            capture_output=True,
            text=True,
            timeout=timeout,
            encoding="utf-8",
            errors="replace",
        )
    except subprocess.TimeoutExpired as exc:
        raise SemgrepError(f"разбор {file.name} не уложился в {timeout} с") from exc

    if result.returncode != 0:
        reason = _ANSI.sub("", result.stderr).strip()
        raise NotParsed(
            f"semgrep-core вернул {result.returncode} на {file.name}: {reason[:500]}"
        )
    if not result.stdout.strip():
        raise NotParsed(f"пустая выгрузка на {file.name}")
    try:
        return json.loads(result.stdout)
    except json.JSONDecodeError as exc:
        raise NotParsed(f"выгрузка {file.name} — не JSON: {exc}") from exc


def dump_many(
    files: Iterable[Path],
    language: str | None = None,
    image: str = SEMGREP_IMAGE,
) -> tuple[dict[Path, dict[str, Any]], dict[Path, str]]:
    """Деревья нескольких файлов: `(разобранные, причины пропуска)`.

    Пока один контейнер на файл. Это главная плата за простоту: сброшенный
    `ENTRYPOINT` в образе существует ровно для того, чтобы гонять цикл внутри
    одного контейнера, и на тысячах файлов это станет обязательным. Сейчас
    важнее, чтобы ошибка на одном файле не роняла остальные.
    """
    trees: dict[Path, dict[str, Any]] = {}
    skipped: dict[Path, str] = {}
    for file in files:
        file = Path(file)
        try:
            trees[file] = dump_ast(file, language, image)
        except NotParsed as exc:
            skipped[file] = str(exc)
    return trees, skipped
