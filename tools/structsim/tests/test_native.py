"""`native/` обязан совпадать с эталоном на Python бит в бит.

Оба реализуют один метод, и расхождение в последнем знаке тоже считается
ошибкой: иначе оценки перестали бы быть теми, на которых получены числа в
исследованиях. Поэтому сравнение здесь через `==`, без допусков.
"""

from __future__ import annotations

import json
import random
from itertools import combinations
from pathlib import Path

import pytest

from structsim import compare, native, tiles
from structsim.labels import to_program
from structsim.model import Block, Program, Span

pytestmark = pytest.mark.skipif(native.find_binary() is None, reason="native не собран")

DATA = Path(__file__).parent / "data"
TREES = [*DATA.glob("*.json"), *(DATA / "langs").glob("*.json")]
ALPHABET = ["ID", ";", "=", "END", "IF", "LOOP", "CALL", "+", "<", "IDX", "RET", "N0", "N1"]


def _mutated(rng: random.Random, labels: list[str]) -> list[str]:
    """Правки, которые делает плагиат: замены, вставки, удаления, перестановка кусков."""
    out = list(labels)
    for _ in range(rng.randint(0, 6)):
        if not out:
            break
        at = rng.randrange(len(out))
        match rng.randint(0, 3):
            case 0:
                out[at] = rng.choice(ALPHABET)
            case 1:
                out.insert(at, rng.choice(ALPHABET))
            case 2:
                del out[at:at + rng.randint(1, 8)]
            case 3:
                piece = rng.randint(0, len(out))
                out = out[piece:] + out[:piece]
    return out


def _random_program(rng: random.Random, pool: list[list[str]]) -> Program:
    style = rng.randint(0, 3)
    if style == 0:
        alphabet = ALPHABET[: rng.randint(1, len(ALPHABET))]
        labels = [rng.choice(alphabet) for _ in range(rng.randint(0, 120))]
    elif style == 1 and pool:
        labels = _mutated(rng, rng.choice(pool))
    elif style == 2:
        # Повторы: на них жадный выбор куска и порядок проходов GST важнее всего.
        unit = [rng.choice(ALPHABET) for _ in range(rng.randint(1, 5))]
        labels = (unit * rng.randint(1, 40))[: rng.randint(0, 150)]
    else:
        labels = [rng.choice(ALPHABET[:3]) for _ in range(rng.randint(0, 60))]
    pool.append(labels)

    blocks = []
    cut = 0
    while cut < len(labels):
        size = rng.randint(1, max(1, len(labels) // 2))
        blocks.append(Block(labels=tuple(labels[cut : cut + size]), name=None))
        cut += size
    if rng.random() < 0.2:
        blocks.append(Block(labels=(), name="empty"))
    if rng.random() < 0.1:
        blocks = []
    positions = tuple(
        None if rng.random() < 0.15 else Span(line=1 + index // 4, column=0)
        for index in range(len(labels))
    )
    if rng.random() < 0.1:
        positions = ()
    return Program(labels=tuple(labels), blocks=tuple(blocks), positions=positions)


def _assert_same(programs: list[Program], min_tile: int, threads: int = 2) -> None:
    got = native.score_all(programs, min_tile=min_tile, threads=threads)
    pairs = list(combinations(range(len(programs)), 2))
    assert [(pair.a, pair.b) for pair in got] == pairs
    for pair in got:
        first, second = programs[pair.a], programs[pair.b]
        assert compare.explain(first, second) == {
            "order": pair.order,
            "shingle": pair.shingle,
            "blocks": pair.blocks,
            "combined": pair.combined,
        }
        expected = tiles.tiles_of(first, second, min_tile)
        assert [item.tile for item in pair.tiles] == expected
        for item in pair.tiles:
            tile = item.tile
            assert item.lines_a == first.span_of(tile.start_a, tile.length)
            assert item.lines_b == second.span_of(tile.start_b, tile.length)


@pytest.mark.parametrize("seed", range(40))
def test_random_programs_match_reference(seed: int) -> None:
    rng = random.Random(seed)
    pool: list[list[str]] = []
    programs = [_random_program(rng, pool) for _ in range(rng.randint(2, 7))]
    _assert_same(programs, min_tile=rng.choice([1, 2, 3, 5, 10]))


def test_real_trees_match_reference() -> None:
    programs = [
        to_program(json.loads(path.read_text(encoding="utf-8")), source=path.name)
        for path in sorted(TREES)
    ]
    assert len(programs) > 5
    _assert_same(programs, min_tile=tiles.MIN_TILE_LENGTH)
    _assert_same(programs, min_tile=3)


def test_no_tiles_when_disabled() -> None:
    program = Program(labels=tuple(ALPHABET * 3), blocks=(Block(labels=tuple(ALPHABET)),))
    assert native.score_all([program, program], min_tile=0)[0].tiles == ()


def test_thread_count_does_not_change_scores() -> None:
    rng = random.Random(7)
    pool: list[list[str]] = []
    programs = [_random_program(rng, pool) for _ in range(12)]
    assert native.score_all(programs, threads=1) == native.score_all(programs, threads=4)


def test_broken_input_is_an_error(tmp_path: Path) -> None:
    script = tmp_path / "fake"
    script.write_text("#!/bin/sh\necho oops >&2\nexit 3\n")
    script.chmod(0o755)
    with pytest.raises(native.NativeError, match="oops"):
        native.score_all([Program(labels=("ID",))], binary=str(script))


def test_lost_pairs_are_an_error(tmp_path: Path) -> None:
    script = tmp_path / "fake"
    script.write_text("#!/bin/sh\ncat > /dev/null\n")
    script.chmod(0o755)
    programs = [Program(labels=("ID",)), Program(labels=("ID",))]
    with pytest.raises(native.NativeError, match="0 пар вместо 1"):
        native.score_all(programs, binary=str(script))
