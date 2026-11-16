"""Собрать датасет из исходных наборов по спецификации `spec.py`.

    python3 research/dataset/src/build.py

Пишет `assignments/<id>/works/<uuid>/Main.java`, `labels.csv`, `pairs.csv`,
`assignments.csv`. Файлы работ копируются побайтно, без правок.
`assignments/<id>/statement.md` пишется руками и сборкой не трогается.
Работы, написанные ИИ, берутся из `ai/<id>/*.java`.
"""

from __future__ import annotations

import csv
import hashlib
import random
import re
import shutil
import uuid
from collections import defaultdict
from dataclasses import dataclass
from pathlib import Path

import sources
import spec

ROOT = sources.ROOT
CONPLAG_VERSION = "version_1"   # как сдано, с шаблонами участников

LABEL_FIELDS = (
    "assignment_id", "work_id", "label", "kind", "role", "case_id",
    "source_work_id", "obfuscation", "label_source", "origin", "lines", "sha256",
)
PAIR_FIELDS = ("assignment_id", "work_a", "work_b", "label", "label_source", "note")
ASSIGNMENT_FIELDS = (
    "assignment_id", "title", "split", "source_assignment_id",
    "works", "positive", "plagiarism", "ai", "cases", "origin",
)


@dataclass(frozen=True)
class Submission:
    id: str
    raw: bytes

    @property
    def lines(self) -> int:
        return len(self.raw.decode("utf-8", errors="replace").splitlines())

    @property
    def is_utf8(self) -> bool:
        try:
            self.raw.decode("utf-8")
        except UnicodeDecodeError:
            return False
        return True

    @property
    def squeezed(self) -> bytes:
        """Содержимое без пробельных символов: для поиска одинаковых работ."""
        return re.sub(rb"\s+", b"", self.raw)


class Union:
    def __init__(self) -> None:
        self.parent: dict[str, str] = {}

    def find(self, x: str) -> str:
        self.parent.setdefault(x, x)
        while self.parent[x] != x:
            self.parent[x] = self.parent[self.parent[x]]
            x = self.parent[x]
        return x

    def join(self, a: str, b: str) -> None:
        self.parent[self.find(a)] = self.find(b)


def load_conplag(problem: int):
    """Решения задачи и размеченные пары: {id: Submission}, {frozenset: 0/1}."""
    versions = sources.fetch(sources.CONPLAG) / "versions"
    variants: dict[str, dict[str, bytes]] = defaultdict(dict)
    labeled: dict[frozenset[str], int] = {}
    with (versions / "labels.csv").open() as handle:
        for row in csv.DictReader(handle):
            if int(row["problem"]) != problem:
                continue
            a, b = row["sub1"], row["sub2"]
            labeled[frozenset((a, b))] = int(row["verdict"])
            for sub in (a, b):
                raw = (versions / CONPLAG_VERSION / f"{a}_{b}" / f"{sub}.java").read_bytes()
                variants[sub][hashlib.sha256(raw).hexdigest()] = raw
    # Решение лежит копией в директории каждой своей пары, и у нескольких
    # решений копии расходятся в пробелах. Берём копию с наименьшим sha256.
    subs = {sub: Submission(sub, by_hash[min(by_hash)]) for sub, by_hash in variants.items()}
    return subs, labeled


def build_assignment(a: spec.Assignment):
    subs, labeled = load_conplag(a.problem)
    rng = random.Random(a.seed)
    past_id = f"{a.id}-prev"

    reviews = {
        frozenset((r.a, r.b)): r for r in spec.REVIEWS if r.problem == a.problem
    }
    for pair in reviews:
        if pair in labeled:
            raise SystemExit(f"{a.id}: пара {sorted(pair)} уже размечена в ConPlag")
        if not pair <= subs.keys():
            raise SystemExit(f"{a.id}: в проверке неизвестное решение {sorted(pair)}")

    # Источник метки для каждой положительной пары.
    positive: dict[frozenset[str], str] = {}
    for pair, verdict in labeled.items():
        if verdict:
            positive[pair] = "conplag"
    for pair, review in reviews.items():
        if review.plagiarism:
            positive[pair] = "manual"
    by_content: dict[bytes, list[str]] = defaultdict(list)
    for sub in sorted(subs):
        by_content[subs[sub].squeezed].append(sub)
    for same in by_content.values():
        for other in same[1:]:
            positive.setdefault(frozenset((same[0], other)), "identical")

    union = Union()
    for pair in positive:
        union.join(*sorted(pair))
    members: dict[str, list[str]] = defaultdict(list)
    for sub in sorted(union.parent):
        members[union.find(sub)].append(sub)
    clusters = sorted(members.values())

    def usable(sub: str) -> bool:
        s = subs[sub]
        return s.is_utf8 and spec.MIN_LINES <= s.lines <= spec.MAX_LINES

    # Кейс берётся только целиком: работа без своей пары в пуле — не кейс.
    candidates = [c for c in clusters if all(usable(s) for s in c)]
    rng.shuffle(candidates)

    # «Прошлый поток»: у кейса-пары одна работа уходит в задание-источник,
    # вторая остаётся в текущем задании как списанная у прошлого потока.
    moved = [c for c in candidates if len(c) == 2][: a.past_cases]
    if len(moved) < a.past_cases:
        raise SystemExit(f"{a.id}: не хватает кейсов-пар для задания-источника")
    donor_of = {}
    for case in moved:
        donor = rng.choice(case)
        (copy,) = (s for s in case if s != donor)
        donor_of[copy] = donor
    cases = list(moved)
    taken = len(moved)
    for cluster in candidates:
        if cluster not in moved and taken + len(cluster) <= a.max_positive:
            cases.append(cluster)
            taken += len(cluster)

    generated = sorted((ROOT / "ai" / a.id).glob("*.java"))

    involved = {s for c in clusters for s in c}
    honest_pool = sorted(s for s in subs if s not in involved and usable(s))
    honest = rng.sample(honest_pool, min(a.size - taken - len(generated), len(honest_pool)))
    spare = [s for s in honest_pool if s not in honest]
    past_honest = (
        rng.sample(spare, min(a.past_size - len(moved), len(spare))) if moved else []
    )

    donors = set(donor_of.values())
    current = sorted(honest + [s for c in cases for s in c if s not in donors])
    past = sorted(past_honest + list(donors))

    def new_id() -> str:
        return str(uuid.UUID(int=rng.getrandbits(128), version=4))

    ids = {sub: new_id() for sub in current}
    ai_ids = {path: new_id() for path in generated}
    ids.update({sub: new_id() for sub in past})

    for aid in (a.id, past_id):
        works = ROOT / "assignments" / aid / "works"
        if works.exists():
            shutil.rmtree(works)

    def put(aid: str, work_id: str, raw: bytes) -> None:
        target = ROOT / "assignments" / aid / "works" / work_id / "Main.java"
        target.parent.mkdir(parents=True)
        target.write_bytes(raw)

    def row(aid: str, work_id: str, raw: bytes, **fields) -> dict:
        base = {
            "assignment_id": aid, "work_id": work_id, "label": 0, "kind": "honest",
            "role": "", "case_id": "", "source_work_id": "", "obfuscation": "",
            "label_source": "conplag", "origin": "",
            "lines": len(raw.decode("utf-8").splitlines()),
            "sha256": hashlib.sha256(raw).hexdigest(),
        }
        return {**base, **fields}

    case_of = {s: f"{a.id}-c{n:02d}" for n, c in enumerate(sorted(cases), 1) for s in c}
    source_of = {}
    for pair, source in positive.items():
        for sub in pair:
            # «conplag» важнее остальных: метка авторов набора.
            if source_of.get(sub) != "conplag":
                source_of[sub] = source

    label_rows = []
    for sub in current + past:
        aid = a.id if sub in current else past_id
        origin = f"conplag:{CONPLAG_VERSION}:problem-{a.problem}:{sub}"
        put(aid, ids[sub], subs[sub].raw)
        if sub in donors:
            fields = dict(kind="donor", role="donor", case_id=case_of[sub],
                          label_source=source_of[sub])
        elif sub in case_of:
            # Кто у кого списал, в ConPlag не указано. Для пар, разнесённых
            # по потокам, направление назначено: источник — прошлый поток.
            fields = dict(
                label=1, kind="plagiarism", case_id=case_of[sub],
                role="copy" if sub in donor_of else "unknown",
                source_work_id=ids[donor_of[sub]] if sub in donor_of else "",
                obfuscation="unknown", label_source=source_of[sub],
            )
        else:
            fields = {}
        label_rows.append(row(aid, ids[sub], subs[sub].raw, origin=origin, **fields))
    for path, work_id in ai_ids.items():
        raw = path.read_bytes()
        put(a.id, work_id, raw)
        label_rows.append(row(
            a.id, work_id, raw, label=1, kind="ai", label_source="generated",
            origin=f"llm:{spec.AI_MODEL}:ai/{a.id}/{path.name}",
        ))

    pair_rows = []
    everything = {**{p: (v, "conplag", "") for p, v in labeled.items()}}
    for pair, review in reviews.items():
        everything[pair] = (int(review.plagiarism), "manual", review.note)
    for pair, source in positive.items():
        if source == "identical":
            everything[pair] = (1, "identical", "совпадают с точностью до пробелов")
    for pair, (verdict, source, note) in everything.items():
        if pair <= ids.keys():
            x, y = sorted(ids[s] for s in pair)
            pair_rows.append({
                "assignment_id": past_id if pair <= set(past) else a.id,
                "work_a": x, "work_b": y,
                "label": verdict, "label_source": source, "note": note,
            })

    origin = f"conplag:{CONPLAG_VERSION}:problem-{a.problem}"
    summaries = [{
        "assignment_id": a.id, "title": a.title, "split": a.split,
        "source_assignment_id": past_id if past else "",
        "works": len(current) + len(generated),
        "positive": len(current) - len(honest) + len(generated),
        "plagiarism": len(current) - len(honest),
        "ai": len(generated),
        "cases": len(cases),
        "origin": origin,
    }]
    if past:
        summaries.append({
            "assignment_id": past_id, "title": f"{a.title} (прошлый поток)",
            "split": "source", "source_assignment_id": "",
            "works": len(past), "positive": 0, "plagiarism": 0, "ai": 0, "cases": 0,
            "origin": origin,
        })
    report = (
        f"{a.id}: работ {len(current) + len(generated)} — честных {len(honest)}, "
        f"списанных {len(current) - len(honest)} в {len(cases)} кейсах "
        f"(размеры {sorted((len(c) for c in cases), reverse=True)}), ИИ {len(generated)}; "
        f"прошлый поток {len(past)}; в источнике решений {len(subs)}, кейсов {len(clusters)}, "
        f"не прошли по размеру/кодировке {len(clusters) - len(candidates)}, "
        f"честных доступно {len(honest_pool)}"
    )
    return summaries, label_rows, pair_rows, report


def write(path: Path, fields, rows) -> None:
    with path.open("w", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fields, lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)


def main() -> None:
    summaries, labels, pairs = [], [], []
    for assignment in spec.ASSIGNMENTS:
        summary, label_rows, pair_rows, report = build_assignment(assignment)
        summaries += summary
        labels += label_rows
        pairs += pair_rows
        print(report)
    labels.sort(key=lambda r: (r["assignment_id"], r["work_id"]))
    pairs.sort(key=lambda r: (r["assignment_id"], r["work_a"], r["work_b"]))
    write(ROOT / "assignments.csv", ASSIGNMENT_FIELDS, summaries)
    write(ROOT / "labels.csv", LABEL_FIELDS, labels)
    write(ROOT / "pairs.csv", PAIR_FIELDS, pairs)


if __name__ == "__main__":
    main()
