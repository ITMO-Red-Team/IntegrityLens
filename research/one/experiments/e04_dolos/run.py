"""e04 — Dolos как третий инструмент: те же три набора и те же пары, что в e01/e02.

    uv run python experiments/e04_dolos/run.py

Dolos запускается заново, один раз на задачу, с настройками по умолчанию.
JPlag и structsim не пересчитываются: их оценки берутся из
results/e01_irplag/scores.csv и results/e02_conplag/v{1,2}/scores.csv, и
сравнение идёт ровно на тех парах, на которых e01/e02 сравнивали JPlag со
structsim. Порог для F1 переносится так же: IR-Plag — leave-one-case-out,
ConPlag — с train на test. Интерпретация и вывод — README.md рядом.
"""

from __future__ import annotations

import json
import random
from concurrent.futures import ThreadPoolExecutor

import pandas as pd

from research_one import datasets, dolos, metrics, report
from research_one.datasets import Dataset
from research_one.paths import results_of, work_of

NAME = "e04_dolos"
SETS = (
    # имя, набор, откуда оценки JPlag и structsim, как переносить порог
    ("irplag", "IR-Plag", datasets.irplag, "e01_irplag", "loco"),
    ("conplag-v1", "ConPlag v1", lambda: datasets.conplag(1), "e02_conplag/v1", "split"),
    ("conplag-v2", "ConPlag v2", lambda: datasets.conplag(2), "e02_conplag/v2", "split"),
)
SCORES = {
    "dolos": "Dolos similarity",
    "dolos_max": "Dolos max (выведена из pairs.csv)",
    "jplag_avg": "JPlag averageSimilarity",
    "jplag_max": "JPlag maxSimilarity",
    "ss_combined": "structsim COMBINED",
}
RIVALS = ("jplag_avg", "jplag_max", "ss_combined")
LEVELS = ("non", "L1", "L2", "L3", "L4", "L5", "L6")
LEVEL_NAMES = ("не плагиат", "L1", "L2", "L3", "L4", "L5", "L6")
SOLO_PAIRS = 20     # пар на набор для проверки «оценка не зависит от состава запуска»


def dolos_pairs(dataset: Dataset, work) -> tuple[dict, dict]:
    """Dolos: один запуск на задачу, оценки всех пар задачи. Запуски параллельно."""
    dirs = dataset.lay_out(work / "dolos-in")

    def one(item):
        group, directory = item
        return group, dolos.run(directory, work / "dolos-out" / group)

    scores: dict[tuple[str, frozenset], dolos.Pair] = {}
    coverage: dict = {"dolos_failed": {}, "dolos_empty": {}}
    with ThreadPoolExecutor(max_workers=4) as pool:
        for group, run in pool.map(one, dirs.items()):
            if run.failed:
                coverage["dolos_failed"][group] = run.failed
            if run.empty:
                coverage["dolos_empty"][group] = run.empty
            for key, pair in run.by_pair().items():
                scores[(group, key)] = pair
    return scores, coverage


def solo_check(dataset: Dataset, frame: pd.DataFrame, work, seed: int = 0) -> dict:
    """Те же пары, но каждая — отдельным запуском из двух файлов.

    Без `-M`/`-m` оценка пары не должна зависеть от остальных решений задачи
    (ловушка 1 в dolos.py). Если зависит — запуск на задачу нечестен.
    """
    rows = random.Random(seed).sample(range(len(frame)), min(SOLO_PAIRS, len(frame)))

    def one(i):
        row = frame.iloc[i]
        root = work / "dolos-solo" / f"{i:04d}"
        for name in (row["a"], row["b"]):
            source = dataset.files[row["group"]][name]
            target = root / "in" / name / source.name
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(source.read_bytes())
        (pair,) = dolos.run(root / "in", root / "out").pairs
        return abs(pair.similarity - row["dolos"])

    with ThreadPoolExecutor(max_workers=4) as pool:
        diffs = list(pool.map(one, rows))
    return {"pairs": len(diffs), "max_abs_diff": float(max(diffs))}


def transfer(frame: pd.DataFrame, column: str, protocol: str) -> dict:
    if protocol == "loco":
        return report.leave_one_group_out(frame, column)
    return report.train_test(frame, column)


def one(key: str, title: str, make, source: str, protocol: str) -> tuple[pd.DataFrame, dict]:
    dataset = make()
    out = results_of(f"{NAME}/{key}")
    work = work_of(f"{NAME}/{key}")

    scored, coverage = dolos_pairs(dataset, work)
    previous_path = results_of(source) / "scores.csv"
    if not previous_path.exists():
        raise SystemExit(f"нет {previous_path}: сначала прогнать {source.split('/')[0]}")
    previous = pd.read_csv(previous_path)

    # Пары e01/e02 и пары, которые даёт набор сейчас, должны совпасть один в один.
    now = {(p.group, frozenset((p.a, p.b))) for p in dataset.pairs}
    then = {(r.group, frozenset((r.a, r.b))) for r in previous.itertuples()}
    if now != then:
        raise SystemExit(f"{title}: пары набора разошлись с {previous_path}")

    columns = ["dolos", "dolos_max", "dolos_longest", "fp_a", "fp_b"]
    values = []
    for row in previous.itertuples():
        pair = scored[(row.group, frozenset((row.a, row.b)))]
        flip = pair.a != row.a
        values.append((
            pair.similarity, pair.maximum, pair.longest,
            pair.total_b if flip else pair.total_a,
            pair.total_a if flip else pair.total_b,
        ))
    frame = previous[["group", "a", "b", "label", "level", "split", *RIVALS, "len_a", "len_b"]].copy()
    frame[columns] = pd.DataFrame(values, columns=columns)

    # Те же пары, что сравнивались в e01/e02: оценку дали JPlag и structsim.
    both = frame.dropna(subset=["jplag_avg", "ss_combined"]).reset_index(drop=True)
    shaky = set()
    for group, names in coverage["dolos_failed"].items():
        shaky |= {(group, name) for name in names}
    touches = both.apply(lambda r: (r.group, r.a) in shaky or (r.group, r.b) in shaky, axis=1)
    coverage.update({
        "pairs_total": int(len(frame)),
        "pairs_compared": int(len(both)),
        "pairs_compared_with_dolos_error_node": int(touches.sum()),
        "solo_runs": solo_check(dataset, both, work),
        "dolos": dolos.version(),
    })

    frame.to_csv(out / "scores.csv", index=False, float_format="%.6f")
    overall = report.table(both, SCORES)
    (out / "metrics.csv").write_text(overall.to_csv(float_format="%.4f"))
    thresholds = {column: transfer(both, column, protocol) for column in SCORES}
    boot = {
        f"dolos_minus_{rival}": report.bootstrap(both, "dolos", rival) for rival in RIVALS
    }
    boot["dolos_max_minus_dolos"] = report.bootstrap(both, "dolos_max", "dolos")
    boot["dolos_max_minus_jplag_max"] = report.bootstrap(both, "dolos_max", "jplag_max")
    per_group = report.per_group_auc(both, SCORES)
    (out / "auc_by_group.csv").write_text(per_group.to_csv(float_format="%.4f"))

    summary = {
        "coverage": coverage,
        "overall": overall.to_dict(orient="index"),
        "transferred_threshold_f1": {
            "protocol": "leave-one-case-out" if protocol == "loco" else "train → test",
            **thresholds,
        },
        "bootstrap": boot,
        "groups": int(len(per_group)),
        "groups_dolos_auc_higher_than": {
            rival: int((per_group["dolos"] > per_group[rival]).sum()) for rival in RIVALS
        },
        "groups_dolos_auc_lower_than": {
            rival: int((per_group["dolos"] < per_group[rival]).sum()) for rival in RIVALS
        },
    }

    if key == "irplag":
        rows = []
        for level in LEVELS[1:]:
            part = both[(both["level"] == level) | (both["label"] == 0)]
            row = {"level": level, "plag_pairs": int((part["level"] == level).sum())}
            for column in SCORES:
                row[column] = metrics.auc(part["label"], part[column])
            rows.append(row)
        cut = pd.DataFrame(rows).set_index("level")
        (out / "auc_by_level.csv").write_text(cut.to_csv(float_format="%.4f"))
        report.plot_by_level(
            both, LEVELS, LEVEL_NAMES, out / "score_by_level.png",
            "IR-Plag: оценка пары «исходное ↔ другое» по уровню плагиата",
            tools=(*report.TOOLS_BY_LEVEL, ("dolos", "dolos", "Dolos similarity")),
        )
    else:
        # Тот же разрез, что в e02: частичное заимствование в своём шаблоне.
        ratio = both[["len_a", "len_b"]].max(axis=1) / both[["len_a", "len_b"]].min(axis=1)
        rows = []
        for name, mask in (("длины близки (≤ 1.5)", ratio <= 1.5), ("длины расходятся (> 1.5)", ratio > 1.5)):
            part = both[mask]
            row = {"pairs": name, "n": int(len(part)), "plag": int(part["label"].sum())}
            for column in SCORES:
                row[column] = metrics.auc(part["label"], part[column])
            rows.append(row)
        cut = pd.DataFrame(rows).set_index("pairs")
        (out / "auc_by_length_ratio.csv").write_text(cut.to_csv(float_format="%.4f"))
    summary["cut"] = cut.to_dict(orient="index")
    report.write_json(out / "summary.json", summary)
    (out / "coverage.json").write_text(json.dumps(coverage, ensure_ascii=False, indent=2) + "\n")

    print(f"\n=== {title}: сравнивается {len(both)} пар из {len(frame)}; "
          f"с узлом ERROR у Dolos: {coverage['pairs_compared_with_dolos_error_node']}; "
          f"отдельные запуски: {coverage['solo_runs']}")
    print(report.markdown(overall[["auc", "ap", "best_f1", "mean_plag", "mean_non", "zero_share_plag", "zero_share_non"]]))
    print({k: (round(v["f1"], 3), round(v["precision"], 3), round(v["recall"], 3))
           for k, v in thresholds.items()})
    for name, value in boot.items():
        print(name, {m: {k: round(x, 3) for k, x in d.items()} for m, d in value.items()})
    print(report.markdown(cut))
    return both, summary


def main() -> None:
    panels: dict[str, pd.DataFrame] = {}
    rows, rows_boot = [], []
    for key, title, make, source, protocol in SETS:
        both, summary = one(key, title, make, source, protocol)
        panels[title] = both
        for column in SCORES:
            m = summary["overall"][column]
            t = summary["transferred_threshold_f1"][column]
            rows.append({
                "set": title, "score": column, "pairs": len(both), "plag": int(both["label"].sum()),
                "auc": m["auc"], "ap": m["ap"],
                "f1_transferred": t["f1"], "precision": t["precision"], "recall": t["recall"],
                "mean_plag": m["mean_plag"], "mean_non": m["mean_non"],
                "zero_share_non": m["zero_share_non"],
            })
        for name, value in summary["bootstrap"].items():
            for metric, d in value.items():
                rows_boot.append({"set": title, "comparison": name, "metric": metric, **d})
    out = results_of(NAME)
    pd.DataFrame(rows).to_csv(out / "metrics.csv", index=False, float_format="%.4f")
    pd.DataFrame(rows_boot).to_csv(out / "bootstrap.csv", index=False, float_format="%.4f")
    report.plot_roc(panels, out / "roc.png", curves=(
        *report.ROC_CURVES,
        ("dolos", "dolos", "Dolos", "-"),
        ("dolos", "dolos_max", "Dolos max", "--"),
    ))


if __name__ == "__main__":
    main()
