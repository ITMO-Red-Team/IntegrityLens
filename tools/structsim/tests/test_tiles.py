import pytest

from structsim.compare import order_similarity
from structsim.model import Program, Tile
from structsim.tiles import (
    MIN_TILE_LENGTH,
    covered,
    greedy_string_tiling,
    tiles_of,
)


def seq(*parts):
    """Последовательность из уникальных меток: a0, a1, ... — чтобы не было
    случайных совпадений там, где их не ждём."""
    out = []
    for prefix, count in parts:
        out.extend(f"{prefix}{i}" for i in range(count))
    return tuple(out)


def gst(a, b, min_length=3):
    return greedy_string_tiling(a, b, min_length)


# --- базовое поведение ----------------------------------------------------


def test_identical_sequences_give_one_tile():
    a = seq(("x", 10))
    tiles = gst(a, a)
    assert tiles == [Tile(start_a=0, start_b=0, length=10)]


def test_nothing_in_common_gives_no_tiles():
    assert gst(seq(("x", 10)), seq(("y", 10))) == []


def test_match_shorter_than_min_length_is_dropped():
    common = seq(("c", 2))
    a = common + seq(("x", 5))
    b = common + seq(("y", 5))
    assert gst(a, b, min_length=3) == []


def test_match_exactly_min_length_is_kept():
    common = seq(("c", 3))
    a = common + seq(("x", 5))
    b = common + seq(("y", 5))
    assert gst(a, b, min_length=3) == [Tile(0, 0, 3)]


def test_positions_point_into_both_sequences():
    common = seq(("c", 4))
    a = seq(("x", 2)) + common
    b = seq(("y", 5)) + common
    (tile,) = gst(a, b)
    assert (tile.start_a, tile.start_b, tile.length) == (2, 5, 4)


# --- то, ради чего взят GST ------------------------------------------------


def test_swapped_blocks_both_become_tiles():
    """Главное свойство: переставленные куски оба попадают в список.

    difflib на этом же входе удержал бы только один из двух — он ищет куски
    с сохранением порядка.
    """
    first = seq(("f", 8))
    second = seq(("s", 8))
    a = first + second
    b = second + first

    tiles = gst(a, b)

    assert len(tiles) == 2
    assert covered(tiles) == 16
    # Оба куска целиком, просто в разных местах.
    assert {(t.length, t.start_a, t.start_b) for t in tiles} == {
        (8, 0, 8),
        (8, 8, 0),
    }


def test_swapped_blocks_beat_difflib():
    """Та же пара: тайлы покрывают всё, мера 1 видит только половину."""
    first = seq(("f", 8))
    second = seq(("s", 8))
    a = first + second
    b = second + first

    assert covered(gst(a, b)) == len(a)
    assert order_similarity(a, b) == 0.5


# --- жадность и неперекрытие ----------------------------------------------


def test_longest_match_is_taken_first():
    long_part = seq(("l", 6))
    short_part = seq(("s", 3))
    a = short_part + long_part
    b = long_part + short_part

    tiles = gst(a, b, min_length=3)

    assert [t.length for t in tiles] == [6, 3]


def test_label_is_covered_at_most_once():
    """Кусок из A не сопоставляется двум местам в B."""
    block = seq(("c", 4))
    a = block
    b = block + block

    tiles = gst(a, b, min_length=4)

    assert covered(tiles) == 4
    assert len(tiles) == 1


def test_tiles_do_not_overlap_in_either_sequence():
    a = seq(("p", 5)) + seq(("q", 5)) + seq(("r", 5))
    b = seq(("r", 5)) + seq(("p", 5)) + seq(("q", 5))

    tiles = gst(a, b, min_length=3)

    taken_a: set[int] = set()
    taken_b: set[int] = set()
    for tile in tiles:
        span_a = set(range(tile.start_a, tile.start_a + tile.length))
        span_b = set(range(tile.start_b, tile.start_b + tile.length))
        assert not (span_a & taken_a)
        assert not (span_b & taken_b)
        taken_a |= span_a
        taken_b |= span_b


def test_one_extra_label_splits_a_tile():
    """Кусок обязан быть непрерывным."""
    a = seq(("c", 8))
    b = a[:4] + ("ЛИШНЯЯ",) + a[4:]

    tiles = gst(a, b, min_length=4)

    assert sorted(t.length for t in tiles) == [4, 4]


def test_insertion_below_min_length_loses_both_halves():
    a = seq(("c", 8))
    b = a[:4] + ("ЛИШНЯЯ",) + a[4:]
    assert gst(a, b, min_length=5) == []


# --- интерфейс ------------------------------------------------------------


def test_tiles_of_reads_programs():
    labels = seq(("c", 12))
    a = Program(labels=labels)
    b = Program(labels=labels)
    assert tiles_of(a, b) == [Tile(0, 0, 12)]


def test_tile_labels_can_be_read_back():
    labels = seq(("c", 12))
    a = Program(labels=labels)
    (tile,) = tiles_of(a, Program(labels=labels))
    assert tile.labels(a) == labels


def test_empty_sequences_give_no_tiles():
    assert gst((), ()) == []
    assert gst(seq(("x", 5)), ()) == []


def test_min_length_must_be_positive():
    with pytest.raises(ValueError):
        greedy_string_tiling(seq(("x", 5)), seq(("x", 5)), min_length=0)


def test_default_min_length_is_documented_as_ten():
    assert MIN_TILE_LENGTH == 10
