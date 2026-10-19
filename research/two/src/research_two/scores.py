"""Оценки обоих инструментов в одной таблице — то, с чем работают s30–s50.

Столбец оценки называется `<инструмент>:<мера>`: `structsim:combined`,
`jplag-default:avg`, `jplag-t05:max` и т. д.

Метрики считаются только на парах, которые оценили **все** сравниваемые
оценки версии: парный бутстрэп и DeLong требуют одной и той же выборки.
Сколько пар при этом потеряно, сообщает `common()`.
"""

from __future__ import annotations

import pandas as pd

from research_two import config, paths


def load() -> pd.DataFrame:
    """Длинная → широкая: строка на (версия, пара), столбец на оценку."""
    pairs = pd.read_csv(paths.PAIRS_CSV, dtype={"sub1": str, "sub2": str})
    frames = []

    jplag = pd.read_csv(paths.JPLAG_CSV)
    for measure in config.JPLAG_SCORES:
        wide = jplag.pivot(index=["version", "pair"], columns="run", values=measure)
        wide.columns = [f"jplag-{run}:{measure}" for run in wide.columns]
        frames.append(wide)

    ours = pd.read_csv(paths.STRUCTSIM_CSV).set_index(["version", "pair"])
    ours = ours[list(config.STRUCTSIM_SCORES) + ["ms"]]
    ours.columns = [f"structsim:{c}" for c in ours.columns]
    frames.append(ours)

    table = pd.concat(frames, axis=1).reset_index()
    return table.merge(pairs, on="pair", how="left", validate="many_to_one")


def score_columns(table: pd.DataFrame) -> list[str]:
    """Все столбцы оценок в порядке отчёта: structsim, затем JPlag."""
    ours = [f"structsim:{s}" for s in config.STRUCTSIM_SCORES]
    runs = [r.name for r in config.ALL_JPLAG_RUNS]
    theirs = [f"jplag-{r}:{m}" for r in runs for m in config.JPLAG_SCORES]
    return [c for c in ours + theirs if c in table.columns]


def common(table: pd.DataFrame, version: str) -> tuple[pd.DataFrame, int]:
    """Пары версии, оценённые всеми оценками, и сколько пар отброшено."""
    part = table[table["version"] == version]
    kept = part.dropna(subset=score_columns(table))
    return kept.reset_index(drop=True), len(part) - len(kept)
