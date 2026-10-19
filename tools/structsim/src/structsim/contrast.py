"""Шаг 4: сравнение пары с фоном набора — `contrast-z`.

`COMBINED` ничего не знает про остальные программы, поэтому не отвечает на
вопрос «а 0.62 — это много для этой задачи?». Каноническое решение похоже на
многие другие сразу, у него высокий фон, и даже большой `COMBINED` не должен
удивлять. Пара, похожая только друг на друга, наоборот, выделяется.

Для пары `a` и `b`:

1. `m_a` — среднее сходство `a` со всеми остальными программами набора,
   кроме `b`; так же `m_b`;
2. фон пары = `(m_a + m_b) / 2`;
3. `σ` — разброс `COMBINED` по всем парам набора;
4. `contrast-z` = (`COMBINED` − фон) / `σ`.

Читается так: около 0 — пара похожа как обычно для этой задачи; `+2` — «на два
обычных разброса сильнее, чем эти двое похожи на остальных»; меньше 0 — слабее
обычного.

**Мера зависит от набора.** Добавили или убрали программы — `contrast-z`
изменится, а `COMBINED` нет. Все программы набора должны решать одну задачу,
и их должно быть не меньше пяти, иначе фон считать не на чем.
"""

from __future__ import annotations

from collections.abc import Mapping
from statistics import fmean, pstdev

PairKey = tuple[str, str]

MIN_SET_SIZE = 5


class TooSmallSet(ValueError):
    """В наборе меньше `MIN_SET_SIZE` программ — фон считать не на чем."""


def pair_key(a: str, b: str) -> PairKey:
    """Ключ пары, не зависящий от порядка."""
    if a == b:
        raise ValueError(f"пара из одной и той же программы: {a!r}")
    return (a, b) if a <= b else (b, a)


def z_score(combined: float, background: float, sigma: float) -> float:
    """Сама формула шага 4, отдельно — чтобы её можно было проверить на числах.

    При `sigma == 0` все пары набора похожи одинаково, выделять нечего, и
    возвращается 0.0: делить на ноль здесь нечего, а «неопределённо» для
    сортировки хуже, чем «ничем не выделяется».
    """
    if sigma == 0:
        return 0.0
    return (combined - background) / sigma


def contrast_z(scores: Mapping[PairKey, float]) -> dict[PairKey, float]:
    """`contrast-z` для каждой пары набора.

    `scores` — `COMBINED` по всем парам, ключ из `pair_key`. Набор программ
    выводится из ключей. Если какой-то пары в `scores` нет, она не участвует
    ни в фоне, ни в `σ`: так метрику можно считать и там, где часть программ
    не разобралась.
    """
    programs = sorted({name for key in scores for name in key})
    if len(programs) < MIN_SET_SIZE:
        raise TooSmallSet(
            f"в наборе {len(programs)} программ, нужно хотя бы {MIN_SET_SIZE}"
        )

    sigma = pstdev(scores.values()) if len(scores) > 1 else 0.0

    def mean_against_others(one: str, excluded: str) -> float:
        values = [
            scores[pair_key(one, other)]
            for other in programs
            if other not in (one, excluded) and pair_key(one, other) in scores
        ]
        return fmean(values) if values else 0.0

    result: dict[PairKey, float] = {}
    for a, b in scores:
        background = (mean_against_others(a, b) + mean_against_others(b, a)) / 2
        result[(a, b)] = z_score(scores[(a, b)], background, sigma)
    return result
