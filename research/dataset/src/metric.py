"""Бизнес-метрика БТ-1 по оценкам риска.

    python3 research/dataset/src/metric.py scores.csv

`scores.csv`: столбцы `assignment_id`, `work_id`, `score`; пустой `score` —
анализ не удался, работа уходит в конец списка (UC-4, Н1).

Метрика задания: работы сортируются по убыванию оценки; k — сколько работ
сверху надо открыть, чтобы среди них оказалось 70% несамостоятельных;
результат — k / N. Цель — не больше 0.35. При равных оценках честные
считаются стоящими выше: порядок внутри группы система не гарантирует,
поэтому берётся худший для неё.
"""

from __future__ import annotations

import csv
import math
import sys
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RECALL = 0.7
TARGET = 0.35


def effort(scored: list[tuple[float | None, int]], recall: float = RECALL) -> float:
    """Доля работ, которые надо открыть. `scored` — пары (оценка, метка 0/1)."""
    total = sum(label for _, label in scored)
    if total == 0:
        raise ValueError("в задании нет несамостоятельных работ")
    need = math.ceil(recall * total)
    ranked = sorted(
        scored,
        key=lambda item: (item[0] is None, -(item[0] or 0.0), item[1]),
    )
    found = 0
    for opened, (_, label) in enumerate(ranked, 1):
        found += label
        if found == need:
            return opened / len(ranked)
    raise AssertionError


def random_effort(works: int, positive: int, recall: float = RECALL) -> float:
    """Ожидание той же метрики при случайном порядке проверки."""
    need = math.ceil(recall * positive)
    return need * (works + 1) / (positive + 1) / works


def labels() -> dict[str, dict[str, int]]:
    """Метки работ по заданиям. Задания-источники не ранжируются и сюда не входят."""
    with (ROOT / "assignments.csv").open() as handle:
        sources = {r["assignment_id"] for r in csv.DictReader(handle) if r["split"] == "source"}
    result: dict[str, dict[str, int]] = defaultdict(dict)
    with (ROOT / "labels.csv").open() as handle:
        for row in csv.DictReader(handle):
            if row["assignment_id"] not in sources:
                result[row["assignment_id"]][row["work_id"]] = int(row["label"])
    return result


def main() -> None:
    truth = labels()
    scores: dict[str, dict[str, float | None]] = defaultdict(dict)
    with open(sys.argv[1]) as handle:
        for row in csv.DictReader(handle):
            value = row["score"].strip()
            scores[row["assignment_id"]][row["work_id"]] = float(value) if value else None

    values = []
    print("задание  работ  несамост.  открыть  случайно  цель")
    if scores.keys() != truth.keys():
        raise SystemExit("набор заданий в оценках не совпадает с labels.csv")
    for assignment in sorted(scores):
        want, got = truth[assignment], scores[assignment]
        if want.keys() != got.keys():
            raise SystemExit(f"{assignment}: набор работ в оценках не совпадает с labels.csv")
        value = effort([(got[w], want[w]) for w in want])
        values.append(value)
        baseline = random_effort(len(want), sum(want.values()))
        mark = "да" if value <= TARGET else "нет"
        print(f"{assignment:7}  {len(want):5}  {sum(want.values()):9}  {value:7.2f}  {baseline:8.2f}  {mark}")
    print(f"среднее: {sum(values) / len(values):.3f} (цель <= {TARGET})")


if __name__ == "__main__":
    main()
