"""Шаг 3: сравнение пары очищенных программ.

Три меры, каждая от 0 до 1, и `COMBINED` — их среднее:

1. **по порядку** — сколько меток удаётся выровнять с сохранением порядка;
2. **по фрагментам** — доля общих кусков по 5 меток, порядок кусков неважен;
3. **по функциям** — блок сопоставляется с самым похожим блоком.

Меры отвечают на разные вопросы и ломаются по-разному: первая страдает от
перестановки кода, вторая — нет, но она строгая (одна отличающаяся метка
портит сразу 5 кусков); третья не страдает от выноса кода в функцию.

`COMBINED` считается в обе стороны и усредняется: мера 1 немного зависит от
того, какая программа подана первой.
"""

from __future__ import annotations

from difflib import SequenceMatcher

from structsim.model import Program

SHINGLE_SIZE = 5


def order_similarity(a: tuple[str, ...], b: tuple[str, ...]) -> float:
    """Мера 1, «по порядку»: 2 × совпало / (длина A + длина B).

    Это в точности `SequenceMatcher.ratio()`, но **обязательно** с
    `autojunk=False`. Эвристика autojunk объявляет «мусором» элементы, которые
    встречаются чаще, чем в 1 % позиций, если последовательность длиннее 200
    элементов. Наши последовательности именно такие: `ID`, `;` и `END`
    занимают в них десятки процентов. С включённым autojunk оценка на
    программах длиннее 200 меток поехала бы вниз, причём молча и только на
    длинных входах.
    """
    if not a and not b:
        return 1.0
    if not a or not b:
        return 0.0
    return SequenceMatcher(None, a, b, autojunk=False).ratio()


def shingles(labels: tuple[str, ...], size: int = SHINGLE_SIZE) -> set[tuple[str, ...]]:
    """Все куски из `size` подряд идущих меток, как множество различных."""
    if len(labels) < size:
        return set()
    return {labels[i : i + size] for i in range(len(labels) - size + 1)}


def shingle_similarity(
    a: tuple[str, ...], b: tuple[str, ...], size: int = SHINGLE_SIZE
) -> float:
    """Мера 2, «по фрагментам»: общих кусков / всех различных кусков двух программ."""
    sa, sb = shingles(a, size), shingles(b, size)
    union = sa | sb
    if not union:
        # Обе программы короче куска — общего измерить нечем.
        return 0.0
    return len(sa & sb) / len(union)


def _best_match(block: tuple[str, ...], others: tuple[tuple[str, ...], ...]) -> float:
    return max((order_similarity(block, other) for other in others), default=0.0)


def _blocks_one_way(
    a_blocks: tuple[tuple[str, ...], ...], b_blocks: tuple[tuple[str, ...], ...]
) -> float:
    """Каждому блоку A — самый похожий блок B, среднее с весом по длине блока."""
    total_weight = sum(len(block) for block in a_blocks)
    if total_weight == 0:
        return 0.0
    weighted = sum(
        len(block) * _best_match(block, b_blocks) for block in a_blocks
    )
    return weighted / total_weight


def block_similarity(a: Program, b: Program) -> float:
    """Мера 3, «по функциям»: сопоставление блоков, усреднённое в обе стороны."""
    a_blocks = tuple(block.labels for block in a.blocks if block.labels)
    b_blocks = tuple(block.labels for block in b.blocks if block.labels)
    if not a_blocks or not b_blocks:
        return 0.0
    return (
        _blocks_one_way(a_blocks, b_blocks) + _blocks_one_way(b_blocks, a_blocks)
    ) / 2


def _combined_one_way(a: Program, b: Program) -> float:
    return (
        order_similarity(a.labels, b.labels)
        + shingle_similarity(a.labels, b.labels)
        + block_similarity(a, b)
    ) / 3


def combined(a: Program, b: Program) -> float:
    """`COMBINED`: среднее трёх мер, посчитанное в обе стороны и усреднённое."""
    return (_combined_one_way(a, b) + _combined_one_way(b, a)) / 2


def explain(a: Program, b: Program) -> dict[str, float]:
    """Все три меры и `COMBINED` разом — чтобы было видно, что откуда взялось.

    Меры 1 и 2 приводятся в одну сторону, как в разборе метода; `COMBINED`
    усреднён по обеим.
    """
    return {
        "order": order_similarity(a.labels, b.labels),
        "shingle": shingle_similarity(a.labels, b.labels),
        "blocks": block_similarity(a, b),
        "combined": combined(a, b),
    }
