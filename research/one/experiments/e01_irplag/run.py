"""e01 — IR-Plag: JPlag против structsim на учебных решениях Java.

    uv run python experiments/e01_irplag/run.py

Протокол авторов набора: исходное решение каждой из 7 задач сравнивается с
каждым остальным. Положительный класс — 355 списанных (L1–L6), отрицательный —
105 написанных независимо. Оба инструмента с настройками по умолчанию.
Интерпретация и вывод — README.md рядом.
"""

from __future__ import annotations

from research_one import datasets, evaluate, metrics, report
from research_one.paths import results_of, work_of

NAME = "e01_irplag"
LEVELS = ("non", "L1", "L2", "L3", "L4", "L5", "L6")
LEVEL_NAMES = ("не плагиат", "L1", "L2", "L3", "L4", "L5", "L6")


def main() -> None:
    out = results_of(NAME)
    frame, coverage = evaluate.score(datasets.irplag(), work_of(NAME))
    evaluate.save(frame, coverage, out)
    print(coverage)

    both = frame.dropna(subset=list(evaluate.PRIMARY)).reset_index(drop=True)

    overall = report.table(both)
    (out / "metrics.csv").write_text(overall.to_csv(float_format="%.4f"))

    # Отделимость каждого уровня от неплагиата: AUC «Lk против всех non».
    level_rows = []
    for level in LEVELS[1:]:
        part = both[(both["level"] == level) | (both["label"] == 0)]
        row = {"level": level, "plag_pairs": int((part["level"] == level).sum())}
        for column in evaluate.SCORES:
            row[f"auc_{column}"] = metrics.auc(part["label"], part[column])
        level_rows.append(row)
    by_level_auc = report.pd.DataFrame(level_rows).set_index("level")
    (out / "auc_by_level.csv").write_text(by_level_auc.to_csv(float_format="%.4f"))

    means = both.groupby("level")[list(evaluate.SCORES)].agg(["mean", "median"]).reindex(LEVELS)
    (out / "score_by_level.csv").write_text(means.to_csv(float_format="%.4f"))

    loco = {column: report.leave_one_group_out(both, column) for column in evaluate.SCORES}
    per_case = report.per_group_auc(both, evaluate.SCORES)
    (out / "auc_by_case.csv").write_text(per_case.to_csv(float_format="%.4f"))

    summary = {
        "coverage": coverage,
        "pairs_compared": int(len(both)),
        "overall": overall.to_dict(orient="index"),
        "leave_one_case_out_f1": loco,
        "bootstrap_structsim_minus_jplag_avg": report.bootstrap(both, "ss_combined", "jplag_avg"),
        "bootstrap_structsim_minus_jplag_max": report.bootstrap(both, "ss_combined", "jplag_max"),
        "cases_structsim_auc_higher": int((per_case["ss_combined"] > per_case["jplag_avg"]).sum()),
        "cases_jplag_auc_higher": int((per_case["ss_combined"] < per_case["jplag_avg"]).sum()),
        "cases": int(len(per_case)),
    }
    report.write_json(out / "summary.json", summary)

    report.plot_by_level(both, LEVELS, LEVEL_NAMES, out / "score_by_level.png",
                         "IR-Plag: оценка пары «исходное ↔ другое» по уровню плагиата")

    print(report.markdown(overall[["auc", "ap", "best_f1", "mean_plag", "mean_non", "zero_share_non"]]))
    print(report.markdown(by_level_auc))
    print({k: (round(v["f1"], 3), round(v["precision"], 3), round(v["recall"], 3)) for k, v in loco.items()})
    print(summary["bootstrap_structsim_minus_jplag_avg"])
    print(summary["bootstrap_structsim_minus_jplag_max"])
    print(report.markdown(per_case))


if __name__ == "__main__":
    main()
