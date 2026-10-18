"""s10: JPlag — все конфигурации на обеих версиях, один запуск на задачу.

Что получается:

- `work/jplag/<версия>/<прогон>/<задача>/` — отчёт JPlag и `run.json`;
- `results/scores/jplag.csv` — оценки размеченных пар: `version, run, pair,
  avg, max`. Пара, которую JPlag не оценил (решение не разобралось), — пустая;
- `results/tables/jplag_runs.csv` — по запуску: сколько решений не разобрано,
  сколько пар сравнено, время сравнения и время контейнера целиком.

Оценки пар берутся только из `results.csv`: архив `.jplag` режет пары по `-n`
(tools/jplag/pitfalls.md, ловушка 1). Порядок имён в строке не гарантирован,
поэтому пара сопоставляется неупорядоченно.
"""

from __future__ import annotations

import csv
import json
from collections import defaultdict
from concurrent.futures import ThreadPoolExecutor, as_completed

from research_two import config, paths, tools


def _job(version: str, run: config.JPlagRun, problem: str) -> dict:
    out = paths.JPLAG_WORK / version / run.name / problem
    marker = out / "run.json"
    if marker.is_file():
        return json.loads(marker.read_text(encoding="utf-8"))
    result = tools.run_jplag(paths.SETS_DIR / version / problem, out, run.args)
    info = {
        "version": version,
        "run": run.name,
        "problem": int(problem),
        "submissions": sum(1 for d in (paths.SETS_DIR / version / problem).iterdir() if d.is_dir()),
        "failed": result.failed,
        "comparisons": result.comparisons,
        "csv_rows": len(result.rows),
        "execution_ms": result.execution_ms,
        "wall_s": round(result.wall_s, 3),
    }
    marker.write_text(json.dumps(info, ensure_ascii=False), encoding="utf-8")
    return info


def _scores(version: str, run: str, problem: str) -> dict[frozenset, tuple[str, str]]:
    path = paths.JPLAG_WORK / version / run / problem / "report" / "results.csv"
    with path.open(encoding="utf-8", newline="") as handle:
        return {
            frozenset((r["submissionName1"], r["submissionName2"])): (
                r["averageSimilarity"],
                r["maxSimilarity"],
            )
            for r in csv.DictReader(handle)
        }


def run() -> None:
    with paths.PAIRS_CSV.open(encoding="utf-8", newline="") as handle:
        pairs = list(csv.DictReader(handle))
    by_problem: dict[str, list[dict]] = defaultdict(list)
    for p in pairs:
        by_problem[f"{int(p['problem']):02d}"].append(p)

    jobs = [
        (version, run, problem)
        for version in config.DATASET_VERSIONS
        for run in config.ALL_JPLAG_RUNS
        for problem in sorted(by_problem)
    ]
    infos = []
    with ThreadPoolExecutor(config.JPLAG_PARALLEL) as pool:
        futures = {pool.submit(_job, *job): job for job in jobs}
        for done, future in enumerate(as_completed(futures), 1):
            version, run_, problem = futures[future]
            info = future.result()
            infos.append(info)
            if done % 21 == 0 or info["failed"]:
                print(f"  [{done}/{len(jobs)}] {version} {run_.name} задача {problem}: "
                      f"пар {info['comparisons']}, не разобрано {len(info['failed'])}", flush=True)

    infos.sort(key=lambda i: (i["version"], i["run"], i["problem"]))
    with (paths.TABLES_DIR / "jplag_runs.csv").open("w", encoding="utf-8", newline="") as handle:
        writer = csv.writer(handle)
        writer.writerow(["version", "run", "problem", "submissions", "failed", "comparisons",
                         "execution_ms", "wall_s"])
        for i in infos:
            writer.writerow([i["version"], i["run"], i["problem"], i["submissions"],
                             " ".join(i["failed"]), i["comparisons"], i["execution_ms"], i["wall_s"]])

    missing = 0
    with paths.JPLAG_CSV.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.writer(handle)
        writer.writerow(["version", "run", "pair", "avg", "max"])
        for version in config.DATASET_VERSIONS:
            for run_ in config.ALL_JPLAG_RUNS:
                for problem, group in sorted(by_problem.items()):
                    scores = _scores(version, run_.name, problem)
                    for p in group:
                        avg, mx = scores.get(frozenset((p["sub1"], p["sub2"])), ("", ""))
                        missing += avg == ""
                        writer.writerow([version, run_.name, p["pair"], avg, mx])
    failed = sum(len(i["failed"]) for i in infos)
    print(f"записано: {paths.JPLAG_CSV.name}; не разобрано решений (по всем прогонам) {failed}, "
          f"размеченных пар без оценки {missing}")
