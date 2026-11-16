"""Шаг 3 и тайлы на Rust — те же числа, но на порядки быстрее.

`compare.py` и `tiles.py` остаются эталоном: `native/` повторяет их поведение бит
в бит, и это проверяет `tests/test_native.py`. Здесь только запуск бинарника и
перевод программ в его формат. Формат и замеры — `docs/native.md`.

Python-ядро от этого модуля не зависит: нет бинарника, значит `find_binary()`
вернёт None, а `compare` и `tiles` работают как раньше.
"""

from __future__ import annotations

import os
import shutil
import subprocess
from collections.abc import Sequence
from dataclasses import dataclass
from pathlib import Path

from structsim.model import Program, Tile
from structsim.tiles import MIN_TILE_LENGTH

PROTOCOL = "structsim-native/1"
BINARY_ENV = "STRUCTSIM_NATIVE"
BINARY_NAME = "structsim-native"
_LOCAL_BUILD = Path(__file__).resolve().parents[2] / "native" / "target" / "release" / BINARY_NAME

# Сколько меток отведено под номер метки в куске меры 2. Настоящий алфавит — это
# несколько десятков меток, предел важен только как проверка входа.
MAX_ALPHABET = 1 << 12


class NativeError(RuntimeError):
    """Бинарник не запустился или вернул не то, что ожидалось."""


@dataclass(frozen=True)
class ShownTile:
    """Тайл и его диапазоны строк. None — ни у одной метки куска нет позиции."""

    tile: Tile
    lines_a: tuple[int, int] | None
    lines_b: tuple[int, int] | None


@dataclass(frozen=True)
class NativePair:
    """Оценки пары: те же поля, что у `compare.explain`, плюс тайлы."""

    a: int
    b: int
    order: float
    shingle: float
    blocks: float
    combined: float
    tiles: tuple[ShownTile, ...]


def find_binary() -> str | None:
    """Путь к бинарнику: переменная окружения, PATH, сборка из `native/`. `off` — не искать."""
    configured = os.environ.get(BINARY_ENV)
    if configured == "off":
        return None
    if configured:
        return configured
    found = shutil.which(BINARY_NAME)
    if found:
        return found
    return str(_LOCAL_BUILD) if _LOCAL_BUILD.is_file() else None


def _encode(programs: Sequence[Program], min_tile: int, threads: int) -> str:
    ids: dict[str, int] = {}

    def numbered(labels: tuple[str, ...]) -> list[str]:
        return [str(ids.setdefault(label, len(ids))) for label in labels]

    parts = []
    for program in programs:
        parts.append(str(len(program.labels)))
        parts.extend(numbered(program.labels))
        if program.positions:
            parts.extend("0" if span is None else str(span.line) for span in program.positions)
        else:
            parts.extend("0" * len(program.labels))
        parts.append(str(len(program.blocks)))
        for block in program.blocks:
            parts.append(str(len(block.labels)))
            parts.extend(numbered(block.labels))
    if len(ids) > MAX_ALPHABET:
        raise NativeError(f"меток {len(ids)}, поддерживается до {MAX_ALPHABET}")
    # Алфавит известен только после обхода программ, поэтому заголовок в конце.
    header = f"{PROTOCOL} {len(programs)} {len(ids)} {min_tile} {threads} "
    return header + " ".join(parts)


def _span(first: str, last: str) -> tuple[int, int] | None:
    return None if first == "0" else (int(first), int(last))


def _decode(line: str) -> NativePair:
    fields = line.split()
    tiles = []
    for at in range(7, len(fields), 7):
        start_a, start_b, length, first_a, last_a, first_b, last_b = fields[at : at + 7]
        tiles.append(
            ShownTile(
                Tile(int(start_a), int(start_b), int(length)),
                _span(first_a, last_a),
                _span(first_b, last_b),
            )
        )
    return NativePair(
        a=int(fields[0]),
        b=int(fields[1]),
        order=float(fields[2]),
        shingle=float(fields[3]),
        blocks=float(fields[4]),
        combined=float(fields[5]),
        tiles=tuple(tiles),
    )


def score_all(
    programs: Sequence[Program],
    min_tile: int = MIN_TILE_LENGTH,
    threads: int = 0,
    binary: str | None = None,
) -> list[NativePair]:
    """Шаг 3 и тайлы по всем парам, в том же порядке, что `combinations(programs, 2)`.

    `min_tile=0` — без тайлов. `threads=0` — столько потоков, сколько доступно
    процессу. Направление пары как у `combinations`: `a` раньше `b` в `programs`,
    и `order` считается от `a` к `b`, как в `compare.explain(a, b)`.
    """
    binary = binary or find_binary()
    if binary is None:
        raise NativeError(f"нет бинарника {BINARY_NAME}: см. docs/native.md")
    process = subprocess.run(
        [binary],
        input=_encode(programs, min_tile, threads),
        capture_output=True,
        text=True,
    )
    if process.returncode != 0:
        raise NativeError(f"{BINARY_NAME} вернул {process.returncode}: {process.stderr.strip()[:500]}")
    pairs = [_decode(line) for line in process.stdout.splitlines()]
    expected = len(programs) * (len(programs) - 1) // 2
    if len(pairs) != expected:
        raise NativeError(f"{BINARY_NAME} вернул {len(pairs)} пар вместо {expected}")
    return pairs
