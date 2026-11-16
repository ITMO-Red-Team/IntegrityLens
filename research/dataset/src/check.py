"""Проверить собранный датасет на соответствие требованиям.

    python3 research/dataset/src/check.py

Проверяет то, что видно без запуска инструментов: раскладку, ограничения на
файл работы, согласованность разметки. Падает на первой же группе ошибок.
"""

from __future__ import annotations

import csv
import hashlib
import uuid
from collections import Counter, defaultdict
from pathlib import Path

import spec

ROOT = Path(__file__).resolve().parents[1]
MAX_BYTES = 1024 * 1024
KINDS = {"honest": 0, "donor": 0, "plagiarism": 1, "ai": 1}


def read(name: str) -> list[dict[str, str]]:
    with (ROOT / name).open() as handle:
        return list(csv.DictReader(handle))


def main() -> None:
    errors: list[str] = []
    assignments = {row["assignment_id"]: row for row in read("assignments.csv")}
    labels = read("labels.csv")
    pairs = read("pairs.csv")
    by_assignment: dict[str, dict[str, dict[str, str]]] = defaultdict(dict)
    for row in labels:
        by_assignment[row["assignment_id"]][row["work_id"]] = row

    if by_assignment.keys() != assignments.keys():
        errors.append("наборы заданий в assignments.csv и labels.csv расходятся")
    if len({row["work_id"] for row in labels}) != len(labels):
        errors.append("повторяется work_id")

    for aid, works in by_assignment.items():
        meta = assignments.get(aid, {})
        base = ROOT / "assignments" / aid
        is_source = meta.get("split") == "source"
        if not is_source and not (base / "statement.md").is_file():
            errors.append(f"{aid}: нет statement.md")
        on_disk = {p.name for p in (base / "works").iterdir()} if (base / "works").is_dir() else set()
        if on_disk != works.keys():
            errors.append(f"{aid}: работы на диске и в labels.csv расходятся")
        if meta.get("split") not in ("dev", "test", "source"):
            errors.append(f"{aid}: split должен быть dev, test или source")
        source = meta.get("source_assignment_id")
        if source and source not in assignments:
            errors.append(f"{aid}: нет задания-источника {source}")

        positive = 0
        cases: Counter[str] = Counter()
        for wid, row in works.items():
            where = f"{aid}/{wid}"
            try:
                if uuid.UUID(wid).version != 4:
                    raise ValueError
            except ValueError:
                errors.append(f"{where}: work_id не UUID v4")
            files = sorted((base / "works" / wid).glob("*")) if wid in on_disk else []
            if [f.name for f in files] != ["Main.java"]:
                errors.append(f"{where}: в работе должен быть один файл Main.java")
                continue
            raw = files[0].read_bytes()
            if len(raw) > MAX_BYTES:
                errors.append(f"{where}: файл больше 1 МБ")
            try:
                text = raw.decode("utf-8")
            except UnicodeDecodeError:
                errors.append(f"{where}: не UTF-8")
                continue
            lines = len(text.splitlines())
            if not spec.MIN_LINES <= lines <= spec.MAX_LINES:
                errors.append(f"{where}: {lines} строк, вне {spec.MIN_LINES}–{spec.MAX_LINES}")
            if str(lines) != row["lines"] or hashlib.sha256(raw).hexdigest() != row["sha256"]:
                errors.append(f"{where}: lines или sha256 не совпадают с файлом")

            kind, label = row["kind"], int(row["label"])
            if KINDS.get(kind) != label:
                errors.append(f"{where}: kind={kind} не согласуется с label={label}")
            positive += label
            if kind == "plagiarism":
                if not row["case_id"]:
                    errors.append(f"{where}: у списанной работы нет case_id")
                cases[row["case_id"]] += 1
            elif row["case_id"] and kind in ("honest", "ai"):
                errors.append(f"{where}: у работы вида {kind} есть case_id")
            ref = row["source_work_id"]
            if ref:
                pool = works.keys() | by_assignment.get(source or "", {}).keys()
                if ref not in pool:
                    errors.append(f"{where}: source_work_id не найден")

        # Кейс списывания внутри потока — минимум две работы, если источник
        # не вынесен в задание прошлого потока.
        for case, count in cases.items():
            if count < 2 and not any(
                r["source_work_id"] for r in works.values() if r["case_id"] == case
            ):
                errors.append(f"{aid}: в кейсе {case} одна работа и нет источника")
        if meta and (str(len(works)) != meta["works"] or str(positive) != meta["positive"]):
            errors.append(f"{aid}: works/positive в assignments.csv не совпадают с labels.csv")
        if is_source:
            if positive:
                errors.append(f"{aid}: в задании-источнике есть несамостоятельные работы")
        elif positive == 0 or positive * 2 >= len(works):
            errors.append(f"{aid}: несамостоятельных {positive} из {len(works)} — метрика не имеет смысла")

    for row in pairs:
        aid = row["assignment_id"]
        source = assignments.get(aid, {}).get("source_assignment_id", "")
        works = {**by_assignment.get(aid, {}), **by_assignment.get(source, {})}
        a, b = row["work_a"], row["work_b"]
        if a not in works or b not in works or not a < b:
            errors.append(f"pairs.csv: неверная пара {a} {b}")
            continue
        same = works[a]["case_id"] and works[a]["case_id"] == works[b]["case_id"]
        if row["label"] == "1" and not same:
            errors.append(f"pairs.csv: пара {a} {b} списана, но работы не в одном кейсе")

    if errors:
        raise SystemExit("\n".join(errors))
    ranked = [r for r in labels if assignments[r["assignment_id"]]["split"] != "source"]
    kinds = Counter(r["kind"] for r in ranked)
    print(
        f"ок: заданий {sum(a['split'] != 'source' for a in assignments.values())}, "
        f"работ {len(ranked)} (честных {kinds['honest']}, списанных {kinds['plagiarism']}, "
        f"ИИ {kinds['ai']}), в заданиях-источниках {len(labels) - len(ranked)}, пар {len(pairs)}"
    )


if __name__ == "__main__":
    main()
