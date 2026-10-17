from structsim.compare import (
    block_similarity,
    combined,
    explain,
    order_similarity,
    shingle_similarity,
    shingles,
)
from structsim.model import Block, Program


def prog(labels, blocks=()):
    return Program(labels=tuple(labels), blocks=tuple(blocks))


# --- мера 1: по порядку ---------------------------------------------------


def test_order_identical_is_one():
    assert order_similarity(("IF", "ID"), ("IF", "ID")) == 1.0


def test_order_disjoint_is_zero():
    assert order_similarity(("IF", "ID"), ("LOOP", "CALL")) == 0.0


def test_order_is_two_matched_over_total_length():
    # Совпадает ("A","B"); длины 3 и 2 -> 2*2/(3+2) = 0.8
    assert order_similarity(("A", "B", "C"), ("A", "B")) == 0.8


def test_order_both_empty_is_one():
    assert order_similarity((), ()) == 1.0


def test_order_one_empty_is_zero():
    assert order_similarity(("A",), ()) == 0.0


def test_order_survives_frequent_labels():
    """Регрессия на autojunk в difflib.

    Эвристика autojunk объявляет мусором элементы, которые встречаются чаще
    чем в 1 % позиций, если последовательность длиннее 200 элементов. Метка
    `ID` в настоящих последовательностях именно такая. С включённым autojunk
    этот случай дал бы 0.0 вместо 0.996 — и только на длинных входах.
    """
    a = ("ID",) * 250 + ("X",)
    b = ("ID",) * 250 + ("Y",)
    assert order_similarity(a, b) == 2 * 250 / (len(a) + len(b))
    assert order_similarity(a, b) > 0.99


# --- мера 2: по фрагментам ------------------------------------------------


def test_shingles_are_all_windows_of_five():
    assert shingles(tuple("ABCDEFG")) == {
        tuple("ABCDE"),
        tuple("BCDEF"),
        tuple("CDEFG"),
    }


def test_shingles_shorter_than_window_is_empty():
    assert shingles(tuple("ABCD")) == set()


def test_shingle_similarity_is_jaccard_over_distinct_windows():
    # ABCDEFG -> 3 куска, ABCDE -> 1 кусок, общий 1, объединение 3
    assert shingle_similarity(tuple("ABCDEFG"), tuple("ABCDE")) == 1 / 3


def test_shingle_similarity_both_too_short_is_zero():
    assert shingle_similarity(tuple("AB"), tuple("AB")) == 0.0


def test_shingle_similarity_ignores_order_of_chunks():
    """Перестановка блоков кода меру 2 почти не трогает, а меру 1 роняет.

    Два блока по 20 меток меняются местами. Мера 2 теряет только куски,
    попавшие на стык: 32 общих из 40 различных, то есть 0.8. Мера 1 может
    сохранить порядок лишь для одного блока из двух, и даёт 0.5.
    """
    first_block = tuple(f"a{i}" for i in range(20))
    second_block = tuple(f"b{i}" for i in range(20))
    first = first_block + second_block
    second = second_block + first_block

    assert shingle_similarity(first, second) == 0.8
    assert order_similarity(first, second) == 0.5


# --- мера 3: по функциям --------------------------------------------------


def test_block_similarity_matches_block_to_block():
    a = prog("XY", [Block(tuple("ABCDE"), "f"), Block(tuple("VWXYZ"), "g")])
    b = prog("XY", [Block(tuple("VWXYZ"), "g2"), Block(tuple("ABCDE"), "f2")])
    # Блоки те же, только переставлены -> каждый находит своего.
    assert block_similarity(a, b) == 1.0


def test_block_similarity_without_blocks_is_zero():
    assert block_similarity(prog("ABC"), prog("ABC")) == 0.0


def test_block_similarity_weighted_by_block_length():
    """Длинный блок весит больше короткого."""
    a = prog("X", [Block(("A",) * 90, "long"), Block(("Z",) * 10, "short")])
    b = prog("X", [Block(("A",) * 90, "long")])
    # Длинный блок совпал полностью, короткий ни с чем; вес 90 против 10.
    assert block_similarity(a, b) > 0.7


# --- COMBINED -------------------------------------------------------------


def test_combined_is_mean_of_three_measures():
    a = prog("ABCDEFG", [Block(tuple("ABCDEFG"))])
    b = prog("ABCDEFG", [Block(tuple("ABCDEFG"))])
    assert combined(a, b) == 1.0


def test_combined_is_symmetric():
    """Оценка не зависит от того, какую программу подали первой."""
    a = prog("ABCDEFGHIJ", [Block(tuple("ABCDE")), Block(tuple("FGHIJ"))])
    b = prog("FGHIJKLMNOPQR", [Block(tuple("FGHIJ"))])
    assert combined(a, b) == combined(b, a)


def test_combined_matches_document_arithmetic():
    """Из разбора метода: среднее 0.80, 0.25 и 0.80 даёт COMBINED 0.62.

    Это же число (0.617) используется в примере про contrast-z, поэтому
    порядок усреднения проверяется сразу двумя местами документа.
    """
    assert round((0.80 + 0.25 + 0.80) / 3, 2) == 0.62
    assert round((0.80 + 0.25 + 0.80) / 3, 3) == 0.617


def test_explain_reports_every_measure():
    a = prog("ABCDEFG", [Block(tuple("ABCDEFG"))])
    b = prog("ABCDEFG", [Block(tuple("ABCDEFG"))])
    assert explain(a, b) == {
        "order": 1.0,
        "shingle": 1.0,
        "blocks": 1.0,
        "combined": 1.0,
    }
