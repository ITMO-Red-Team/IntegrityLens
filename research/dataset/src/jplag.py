"""Прогоны JPlag: кандидаты на ручную проверку и контрольные оценки.

    python3 research/dataset/src/jplag.py review JAR PROBLEM
    python3 research/dataset/src/jplag.py scores JAR OUT.csv

`JAR` — jplag-6.3.0-jar-with-dependencies.jar (сумма — в
tools/jplag/jplag.Dockerfile). Нужен JDK 25: на JRE JPlag молча не разбирает
ни одного файла, поэтому число разобранных работ сверяется.

`review` печатает пары задачи ConPlag без метки, которые JPlag оценил не ниже
порога на сырой или очищенной от шаблонов версии и по которым в `spec.REVIEWS`
ещё нет решения. `scores` считает для каждой работы собранного датасета
наибольшее сходство с другой работой задания или задания-источника — оценку
риска «по JPlag».
"""

from __future__ import annotations

import csv
import hashlib
import subprocess
import sys
import tempfile
from collections import defaultdict
from pathlib import Path

import sources
import spec

ROOT = sources.ROOT
THRESHOLD = 0.8


def run(jar: str, files: dict[str, bytes], strict: bool = True) -> dict[frozenset[str], float]:
    """Среднее сходство JPlag для всех пар. `files`: имя работы → содержимое.

    `strict`: падать, если JPlag разобрал не все работы.
    """
    with tempfile.TemporaryDirectory() as tmp:
        root = Path(tmp) / "in"
        for name, raw in files.items():
            (root / name).mkdir(parents=True)
            (root / name / "Main.java").write_bytes(raw)
        out = Path(tmp) / "out"
        result = subprocess.run(
            ["java", "-jar", jar, "--mode", "RUN", "-l", "java", "--csv-export",
             "--cluster-skip", "--overwrite", "-n", "-1", "-r", str(out), str(root)],
            capture_output=True, text=True,
        )
        table = Path(tmp) / "out" / "results.csv"
        if result.returncode or not table.exists():
            raise SystemExit(f"JPlag не отработал:\n{result.stdout[-2000:]}\n{result.stderr[-2000:]}")
        pairs = {}
        with table.open() as handle:
            for row in csv.DictReader(handle):
                pair = frozenset((row["submissionName1"], row["submissionName2"]))
                pairs[pair] = float(row["averageSimilarity"])
    parsed = {name for pair in pairs for name in pair}
    if parsed != files.keys():
        message = f"JPlag разобрал {len(parsed)} работ из {len(files)}: {sorted(files.keys() - parsed)}"
        if strict:
            raise SystemExit(message)
        print(message)
    return pairs


def review(jar: str, problem: int) -> None:
    versions = sources.fetch(sources.CONPLAG) / "versions"
    labeled = set()
    group: dict[str, str] = {}

    def find(x: str) -> str:
        group.setdefault(x, x)
        while group[x] != x:
            group[x] = group[group[x]]
            x = group[x]
        return x

    files: dict[str, dict[str, dict[str, bytes]]] = {
        "version_1": defaultdict(dict), "version_2": defaultdict(dict),
    }
    with (versions / "labels.csv").open() as handle:
        for row in csv.DictReader(handle):
            if int(row["problem"]) != problem:
                continue
            a, b = row["sub1"], row["sub2"]
            labeled.add(frozenset((a, b)))
            if row["verdict"] == "1":
                group[find(a)] = find(b)
            for version, by_sub in files.items():
                for sub in (a, b):
                    raw = (versions / version / f"{a}_{b}" / f"{sub}.java").read_bytes()
                    by_sub[sub][hashlib.sha256(raw).hexdigest()] = raw
    decided = {frozenset((r.a, r.b)) for r in spec.REVIEWS if r.problem == problem}
    best: dict[frozenset[str], dict[str, float]] = defaultdict(dict)
    for version, by_sub in files.items():
        chosen = {sub: variants[min(variants)] for sub, variants in by_sub.items()}
        # В очищенной версии есть файлы, сломанные при вырезании шаблона.
        for pair, value in run(jar, chosen, strict=version == "version_1").items():
            if value >= THRESHOLD and pair not in labeled:
                best[pair][version] = value
    raw = {sub: v[min(v)] for sub, v in files["version_1"].items()}
    squeezed: dict[bytes, str] = {}
    for sub in sorted(raw):
        first = squeezed.setdefault(b"".join(raw[sub].split()), sub)
        group[find(sub)] = find(first)
    todo = 0
    for pair in sorted(best, key=lambda p: -max(best[p].values())):
        a, b = sorted(pair)
        if find(a) == find(b):
            status = "уже в одном кейсе"
        elif pair in decided:
            status = "решение есть в spec.REVIEWS"
        else:
            status = "НУЖНА ПРОВЕРКА"
            todo += 1
        print(a, b, {k: round(v, 3) for k, v in sorted(best[pair].items())}, status)
    print(f"задача {problem}: пар без метки с оценкой >= {THRESHOLD}: {len(best)}, без решения: {todo}")
    if todo:
        raise SystemExit(1)


def scores(jar: str, out: str) -> None:
    with (ROOT / "assignments.csv").open() as handle:
        source_of = {r["assignment_id"]: r["source_assignment_id"] for r in csv.DictReader(handle)
                     if r["split"] != "source"}
    works: dict[str, dict[str, bytes]] = defaultdict(dict)
    with (ROOT / "labels.csv").open() as handle:
        for row in csv.DictReader(handle):
            path = ROOT / "assignments" / row["assignment_id"] / "works" / row["work_id"] / "Main.java"
            works[row["assignment_id"]][row["work_id"]] = path.read_bytes()
    with open(out, "w", newline="") as handle:
        writer = csv.writer(handle, lineterminator="\n")
        writer.writerow(["assignment_id", "work_id", "score"])
        for assignment in sorted(source_of):
            # Работа сравнивается с работами своего задания и задания-источника (ФТ-3.2).
            pool = {**works[assignment], **works.get(source_of[assignment], {})}
            top: dict[str, float] = defaultdict(float)
            for pair, value in run(jar, pool).items():
                for work in pair:
                    top[work] = max(top[work], value)
            for work in sorted(works[assignment]):
                writer.writerow([assignment, work, f"{top[work]:.4f}"])


if __name__ == "__main__":
    if len(sys.argv) != 4 or sys.argv[1] not in ("review", "scores"):
        raise SystemExit(__doc__)
    if sys.argv[1] == "review":
        review(sys.argv[2], int(sys.argv[3]))
    else:
        scores(sys.argv[2], sys.argv[3])
