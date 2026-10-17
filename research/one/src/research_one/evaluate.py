"""Прогнать оба инструмента по набору и свести оценки размеченных пар в одну таблицу.

Сравнение честное в одном смысле: пары, которые хоть один инструмент не смог
оценить (файл не разобрался), выпадают из сравнения у обоих — и сколько их,
записывается рядом.
"""

from __future__ import annotations

import json
import os
from concurrent.futures import ProcessPoolExecutor, ThreadPoolExecutor
from pathlib import Path

import pandas as pd

from research_one import jplag, ssim
from research_one.datasets import Dataset

SCORES = {
    "jplag_avg": "JPlag averageSimilarity",
    "jplag_max": "JPlag maxSimilarity",
    "ss_combined": "structsim COMBINED",
    "ss_order": "structsim мера 1 (порядок)",
    "ss_shingle": "structsim мера 2 (фрагменты)",
    "ss_blocks": "structsim мера 3 (функции)",
    "ss_contrast_z": "structsim contrast-z",
}
PRIMARY = ("jplag_avg", "ss_combined")


def jplag_pairs(
    dataset: Dataset, work: Path, *, workers: int = 4, **jplag_args
) -> tuple[dict[tuple[str, frozenset], jplag.Pair], dict[str, list[str]], dict[tuple[str, str], int]]:
    """JPlag: один запуск на задачу, оценки всех пар задачи.

    Пара не зависит от остальных решений в запуске (нет `-bc`), поэтому
    раскладывать по паре не нужно. Запуски независимы и идут параллельно.
    """
    dirs = dataset.lay_out(work / "jplag-in")
    scores: dict[tuple[str, frozenset], jplag.Pair] = {}
    failed: dict[str, list[str]] = {}
    tokens: dict[tuple[str, str], int] = {}

    def one(item):
        group, directory = item
        return group, jplag.run(directory, work / "jplag-out" / group, **jplag_args)

    with ThreadPoolExecutor(max_workers=workers) as pool:
        for group, run in pool.map(one, dirs.items()):
            if run.failed:
                failed[group] = run.failed
            for key, pair in run.by_pair().items():
                scores[(group, key)] = pair
            for name, count in run.tokens.items():
                tokens[(group, name)] = count
    return scores, failed, tokens


def _contrast(programs) -> dict:
    return ssim.contrast_of_set(programs)


def score(dataset: Dataset, work: Path, *, with_contrast: bool = True, jplag_args: dict | None = None) -> tuple[pd.DataFrame, dict]:
    jplag_scores, failed, jplag_tokens = jplag_pairs(dataset, work, **(jplag_args or {}))
    coverage: dict = {"jplag_failed": failed, "structsim_skipped": {}}

    # --- structsim: разбор каждого файла один раз ------------------------
    programs_by_group = {}
    for group, files in dataset.files.items():
        programs, skipped = ssim.to_programs(files, workers=os.cpu_count() or 8)
        programs_by_group[group] = programs
        if skipped:
            coverage["structsim_skipped"][group] = skipped

    contrast_by_group: dict[str, dict] = {}
    if with_contrast:
        groups = sorted(programs_by_group)
        with ProcessPoolExecutor() as pool:
            for group, z in zip(groups, pool.map(_contrast, [programs_by_group[g] for g in groups])):
                contrast_by_group[group] = z

    rows = []
    for pair in dataset.pairs:
        key = frozenset((pair.a, pair.b))
        programs = programs_by_group[pair.group]
        j = jplag_scores.get((pair.group, key))
        s = ssim.explain_pairs(programs, [(pair.a, pair.b)]).get(key)
        rows.append({
            "group": pair.group, "a": pair.a, "b": pair.b,
            "label": pair.label, "level": pair.level, "split": pair.split,
            "jplag_avg": j.average if j else None,
            "jplag_max": j.maximum if j else None,
            "ss_combined": s["combined"] if s else None,
            "ss_order": s["order"] if s else None,
            "ss_shingle": s["shingle"] if s else None,
            "ss_blocks": s["blocks"] if s else None,
            "ss_contrast_z": contrast_by_group.get(pair.group, {}).get(key) if s else None,
            # Длины в метках каждого инструмента: у JPlag метки крупнее.
            "len_a": len(programs[pair.a]) if pair.a in programs else None,
            "len_b": len(programs[pair.b]) if pair.b in programs else None,
            "tokens_a": jplag_tokens.get((pair.group, pair.a)),
            "tokens_b": jplag_tokens.get((pair.group, pair.b)),
        })
    frame = pd.DataFrame(rows)
    both = frame["jplag_avg"].notna() & frame["ss_combined"].notna()
    coverage["pairs_total"] = int(len(frame))
    coverage["pairs_scored_by_both"] = int(both.sum())
    coverage["pairs_missing_jplag"] = int(frame["jplag_avg"].isna().sum())
    coverage["pairs_missing_structsim"] = int(frame["ss_combined"].isna().sum())
    return frame, coverage


def save(frame: pd.DataFrame, coverage: dict, out: Path) -> None:
    frame.to_csv(out / "scores.csv", index=False, float_format="%.6f")
    (out / "coverage.json").write_text(json.dumps(coverage, ensure_ascii=False, indent=2) + "\n")
