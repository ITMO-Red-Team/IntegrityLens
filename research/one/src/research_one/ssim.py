"""structsim с локальным semgrep-core, без Docker.

Шаги 2–4 берутся из tools/structsim как есть: `to_program`, `compare`,
`contrast`. Заменён только шаг 1 — `semgrep.dump_ast`, который в инструменте
всегда запускает контейнер. Здесь тот же бинарник `semgrep-core` той же версии
1.180.0 берётся из pip-пакета semgrep и вызывается с теми же ключами
(`-lang <язык> -json -full_token_info -dump_ast`), что и в
tools/structsim/src/structsim/semgrep.py.

Что локальная замена даёт то же самое — проверяется в e00_smoke: деревья из
tools/structsim/tests/data сняты в контейнере, и метки по ним должны совпасть
с метками по локальной выгрузке.
"""

from __future__ import annotations

import hashlib
import json
import pathlib
import subprocess
from collections.abc import Iterable, Mapping
from concurrent.futures import ThreadPoolExecutor
from itertools import combinations
from pathlib import Path
from typing import Any

import semgrep as semgrep_package
from structsim import compare, contrast
from structsim.labels import to_program
from structsim.model import Block, Program
from structsim.semgrep import _ANSI, NotParsed, SEMGREP_VERSION, language_of

from research_one.paths import WORK

SEMGREP_CORE = Path(semgrep_package.__file__).parent / "bin" / "semgrep-core"
CACHE = WORK / "ssim-cache"


def check_version() -> str:
    """Версия локального semgrep-core должна совпадать с той, что прибита в structsim."""
    out = subprocess.run(
        [SEMGREP_CORE, "-version"], capture_output=True, text=True, check=True
    ).stdout.strip()
    if SEMGREP_VERSION not in out:
        raise RuntimeError(f"semgrep-core {out!r}, а structsim прибит к {SEMGREP_VERSION}")
    return out


def dump_ast(file: Path, language: str | None = None, timeout: float = 120.0) -> dict[str, Any]:
    """То же, что `structsim.semgrep.dump_ast`, но без контейнера."""
    language = language or language_of(file)
    result = subprocess.run(
        [SEMGREP_CORE, "-lang", language, "-json", "-full_token_info", "-dump_ast", str(file)],
        capture_output=True, text=True, timeout=timeout,
        encoding="utf-8", errors="replace",
    )
    if result.returncode != 0:
        reason = _ANSI.sub("", result.stderr).strip()
        raise NotParsed(f"semgrep-core вернул {result.returncode} на {file.name}: {reason[:500]}")
    if not result.stdout.strip():
        raise NotParsed(f"пустая выгрузка на {file.name}")
    try:
        return json.loads(result.stdout)
    except json.JSONDecodeError as exc:
        raise NotParsed(f"выгрузка {file.name} — не JSON: {exc}") from exc


# Кеш хранит только то, на чём считается оценка: метки и блоки. Деревья
# объёмные (≈9 КБ JSON на пять строк кода) и пересчитываются дёшево.
def _cache_key(file: Path, language: str) -> Path:
    digest = hashlib.sha256(file.read_bytes() + f"\0{language}\0{SEMGREP_VERSION}".encode())
    return CACHE / f"{digest.hexdigest()}.json"


def _program(file: Path, language: str) -> Program:
    cached = _cache_key(file, language)
    if cached.exists():
        data = json.loads(cached.read_text())
        return Program(
            labels=tuple(data["labels"]),
            blocks=tuple(Block(labels=tuple(b["labels"]), name=b["name"]) for b in data["blocks"]),
            language=language,
            source=file.name,
        )
    program = to_program(dump_ast(file, language), language=language, source=file.name)
    CACHE.mkdir(parents=True, exist_ok=True)
    cached.write_text(json.dumps({
        "labels": program.labels,
        "blocks": [{"labels": b.labels, "name": b.name} for b in program.blocks],
    }))
    return program


def to_programs(
    files: Mapping[str, Path], workers: int = 8
) -> tuple[dict[str, Program], dict[str, str]]:
    """Шаги 1 и 2 по набору: `{имя: файл}` → программы и причины пропусков.

    Как `structsim.analyze.to_programs`, но имя задаёт вызывающий (имена
    файлов в наборах повторяются: `Main.java`, `T01.java`), и разбор идёт
    параллельно — это отдельные процессы semgrep-core.
    """
    def one(item: tuple[str, Path]) -> tuple[str, Program | None, str | None]:
        name, path = item
        try:
            program = _program(Path(path), language_of(Path(path)))
        except NotParsed as exc:
            return name, None, str(exc)
        if not program.labels:
            return name, None, "разобрался, но меток не дал"
        return name, program, None

    programs: dict[str, Program] = {}
    skipped: dict[str, str] = {}
    with ThreadPoolExecutor(max_workers=workers) as pool:
        for name, program, reason in pool.map(one, files.items()):
            if program is None:
                skipped[name] = reason or "?"
            else:
                programs[name] = program
    return programs, skipped


def explain_pairs(
    programs: Mapping[str, Program], pairs: Iterable[tuple[str, str]]
) -> dict[frozenset[str], dict[str, float]]:
    """Шаг 3 по выбранным парам: три меры и COMBINED (`structsim.compare.explain`)."""
    out = {}
    for a, b in pairs:
        if a in programs and b in programs:
            out[frozenset((a, b))] = compare.explain(programs[a], programs[b])
    return out


def contrast_of_set(programs: Mapping[str, Program]) -> dict[frozenset[str], float]:
    """Шаг 4 по набору одной задачи: COMBINED всех пар → contrast-z.

    Фон пары считается по всем парам набора, поэтому COMBINED нужен для всех,
    а не только для размеченных.
    """
    combined = {
        contrast.pair_key(a, b): compare.combined(programs[a], programs[b])
        for a, b in combinations(sorted(programs), 2)
    }
    if len(programs) < contrast.MIN_SET_SIZE:
        return {}
    return {frozenset(key): z for key, z in contrast.contrast_z(combined).items()}
