"""s20: structsim — шаги 1–4 на обеих версиях, по наборам задач.

Шаги 2–4 — функции `tools/structsim` как есть. Шаг 1 — та же команда
`semgrep-core` циклом в одном контейнере на задачу (почему — `tools.py`).

`contrast-z` считается по всем парам набора задачи, а не только по
размеченным: фон пары — это сходство каждого из двух решений со всеми
остальными решениями задачи. Поэтому `COMBINED` считается для всех 31 770 пар
наборов, а в `results/` уходят только 911 размеченных.

Что получается:

- `work/trees/<версия>/<задача>/<решение>.json` — деревья шага 1;
- `work/structsim/<версия>/<задача>.csv` — все пары набора;
- `results/scores/structsim.csv` — размеченные пары: `version, pair,
  combined, contrast_z, order, shingle, blocks, ms`; `ms` — время
  `compare.combined` на пару;
- `results/tables/structsim_parse.csv` — по решению: разобралось ли, время
  шага 1 и шага 2, число меток.
"""

from __future__ import annotations

import csv
import json
import time
from collections import defaultdict
from concurrent.futures import ProcessPoolExecutor, ThreadPoolExecutor
from functools import lru_cache
from itertools import combinations
from pathlib import Path

from structsim import compare, contrast
from structsim.labels import to_program
from structsim.model import Program

from research_two import config, paths, tools

CHUNK = 150


# --- шаг 1 ----------------------------------------------------------------


def _parse_problem(version: str, problem: str) -> list[tools.ParseStatus]:
    root = paths.SETS_DIR / version / problem
    out = paths.TREES_DIR / version / problem
    files = sorted(f"{d.name}/{d.name}.java" for d in root.iterdir() if d.is_dir())
    status_file = out / "_status.txt"
    if status_file.is_file() and len(status_file.read_text().splitlines()) == len(files):
        statuses = []
        for line in status_file.read_text(encoding="utf-8").splitlines():
            name, rc, ms = line.split()
            ok = rc == "0" and (out / f"{name}.json").is_file()
            err = out / f"{name}.err"
            error = "" if ok else (err.read_text(errors="replace").strip()[:500] if err.exists() else f"код {rc}")
            statuses.append(tools.ParseStatus(name, ok, int(ms), error))
        return statuses
    return tools.dump_trees(root, files, out)


# --- шаги 2 и 3 в процессах -------------------------------------------------


@lru_cache(maxsize=None)
def _program(tree_path: str) -> Program:
    tree = json.loads(Path(tree_path).read_text(encoding="utf-8"))
    return to_program(tree, language=config.STRUCTSIM_LANGUAGE, source=Path(tree_path).name)


def _score_chunk(tree_dir: str, chunk: list[tuple[str, str]]) -> list[tuple]:
    rows = []
    for a, b in chunk:
        pa, pb = _program(f"{tree_dir}/{a}.json"), _program(f"{tree_dir}/{b}.json")
        started = time.perf_counter()
        combined = compare.combined(pa, pb)
        ms = (time.perf_counter() - started) * 1000
        rows.append((
            a, b, combined,
            compare.order_similarity(pa.labels, pb.labels),
            compare.shingle_similarity(pa.labels, pb.labels),
            compare.block_similarity(pa, pb),
            ms,
        ))
    return rows


def _labels_info(tree_path: Path) -> tuple[int, float]:
    started = time.perf_counter()
    program = to_program(json.loads(tree_path.read_text(encoding="utf-8")),
                         language=config.STRUCTSIM_LANGUAGE, source=tree_path.name)
    return len(program.labels), (time.perf_counter() - started) * 1000


# --- стадия ---------------------------------------------------------------

FIELDS = ["a", "b", "combined", "order", "shingle", "blocks", "ms", "contrast_z"]


def _score_problem(pool: ProcessPoolExecutor, version: str, problem: str, parsed: list[str]) -> Path:
    out = paths.STRUCTSIM_WORK / version / f"{problem}.csv"
    if out.is_file():
        return out
    tree_dir = str(paths.TREES_DIR / version / problem)
    pairs = list(combinations(sorted(parsed), 2))
    chunks = [pairs[i:i + CHUNK] for i in range(0, len(pairs), CHUNK)]
    rows = [row for part in pool.map(_score_chunk, [tree_dir] * len(chunks), chunks) for row in part]

    by_pair = {contrast.pair_key(r[0], r[1]): r[2] for r in rows}
    z = contrast.contrast_z(by_pair) if len(parsed) >= contrast.MIN_SET_SIZE else {}
    out.parent.mkdir(parents=True, exist_ok=True)
    tmp = out.with_suffix(".part")
    with tmp.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.writer(handle)
        writer.writerow(FIELDS)
        for a, b, combined, order, shingle, blocks, ms in rows:
            zz = z.get(contrast.pair_key(a, b))
            writer.writerow([a, b, f"{combined:.6f}", f"{order:.6f}", f"{shingle:.6f}",
                             f"{blocks:.6f}", f"{ms:.2f}", "" if zz is None else f"{zz:.6f}"])
    tmp.replace(out)
    return out


def run() -> None:
    with paths.PAIRS_CSV.open(encoding="utf-8", newline="") as handle:
        pairs = list(csv.DictReader(handle))
    by_problem: dict[str, list[dict]] = defaultdict(list)
    for p in pairs:
        by_problem[f"{int(p['problem']):02d}"].append(p)
    problems = sorted(by_problem)

    # Шаг 1: контейнеры параллельно, по задаче на контейнер.
    jobs = [(v, p) for v in config.DATASET_VERSIONS for p in problems]
    with ThreadPoolExecutor(config.SEMGREP_PARALLEL) as pool:
        statuses = dict(zip(jobs, pool.map(lambda job: _parse_problem(*job), jobs)))
    for (version, problem), st in statuses.items():
        bad = [s.name for s in st if not s.ok]
        if bad:
            print(f"  {version} задача {problem}: не разобрано {bad}")
    print("шаг 1 готов", flush=True)

    # Шаг 2 отдельно — только чтобы отчитаться о метках и времени.
    with (paths.TABLES_DIR / "structsim_parse.csv").open("w", encoding="utf-8", newline="") as handle:
        writer = csv.writer(handle)
        writer.writerow(["version", "problem", "submission", "parsed", "step1_ms", "step2_ms", "labels", "error"])
        for (version, problem), st in statuses.items():
            for s in st:
                n, ms2 = _labels_info(paths.TREES_DIR / version / problem / f"{s.name}.json") if s.ok else (0, 0.0)
                writer.writerow([version, int(problem), s.name, int(s.ok), s.ms, f"{ms2:.1f}", n,
                                 s.error.splitlines()[0] if s.error else ""])

    # Шаги 3 и 4.
    results: dict[str, dict[frozenset, dict]] = {}
    with ProcessPoolExecutor(config.STRUCTSIM_WORKERS) as pool:
        for version in config.DATASET_VERSIONS:
            results[version] = {}
            for problem in problems:
                parsed = [s.name for s in statuses[(version, problem)] if s.ok]
                started = time.perf_counter()
                out = _score_problem(pool, version, problem, parsed)
                with out.open(encoding="utf-8", newline="") as handle:
                    for r in csv.DictReader(handle):
                        results[version][frozenset((r["a"], r["b"]))] = r
                print(f"  {version} задача {problem}: {len(parsed)} решений, "
                      f"{time.perf_counter() - started:.0f} с", flush=True)

    missing = 0
    with paths.STRUCTSIM_CSV.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.writer(handle)
        writer.writerow(["version", "pair", "combined", "contrast_z", "order", "shingle", "blocks", "ms"])
        for version in config.DATASET_VERSIONS:
            for problem in problems:
                for p in by_problem[problem]:
                    r = results[version].get(frozenset((p["sub1"], p["sub2"])))
                    if r is None:
                        missing += 1
                        writer.writerow([version, p["pair"], "", "", "", "", "", ""])
                        continue
                    writer.writerow([version, p["pair"], r["combined"], r["contrast_z"], r["order"],
                                     r["shingle"], r["blocks"], r["ms"]])
    print(f"записано: {paths.STRUCTSIM_CSV.name}; размеченных пар без оценки {missing}")
