"""e02 — ConPlag: JPlag против structsim на настоящих олимпиадных решениях Java.

    uv run python experiments/e02_conplag/run.py

911 размеченных пар (251 плагиат) с Codeforces, 21 задача. Две версии
набора: 1 — как сдано, 2 — с вычищенным вручную шаблонным кодом. Порог для F1
выбирается на train-части авторского разбиения и проверяется на test.
Оба инструмента с настройками по умолчанию. Интерпретация и вывод — README.md
рядом.
"""

from __future__ import annotations

from research_one import datasets, evaluate, report
from research_one.paths import results_of, work_of

NAME = "e02_conplag"


def one(version: int) -> tuple[str, report.pd.DataFrame, dict]:
    dataset = datasets.conplag(version)
    out = results_of(f"{NAME}/v{version}")
    frame, coverage = evaluate.score(dataset, work_of(f"{NAME}/v{version}"))
    evaluate.save(frame, coverage, out)
    both = frame.dropna(subset=list(evaluate.PRIMARY)).reset_index(drop=True)
    # contrast-z может не посчитаться для пары, если в задаче меньше 5 программ.
    overall = report.table(both)
    (out / "metrics.csv").write_text(overall.to_csv(float_format="%.4f"))
    per_problem = report.per_group_auc(both, evaluate.SCORES)
    (out / "auc_by_problem.csv").write_text(per_problem.to_csv(float_format="%.4f"))
    split = {
        column: report.train_test(both.dropna(subset=[column]), column)
        for column in evaluate.SCORES
    }
    # Частичное заимствование: решение, обёрнутое своим шаблоном, заметно
    # длиннее оригинала. Симметричные оценки на таких парах проседают,
    # односторонняя (maxSimilarity) — нет. Разрез по соотношению длин
    # (в метках structsim) показывает, где именно расходятся инструменты.
    ratio = both[["len_a", "len_b"]].max(axis=1) / both[["len_a", "len_b"]].min(axis=1)
    by_ratio = []
    for name, mask in (("длины близки (≤ 1.5)", ratio <= 1.5), ("длины расходятся (> 1.5)", ratio > 1.5)):
        part = both[mask]
        row = {"pairs": name, "n": int(len(part)), "plag": int(part["label"].sum())}
        for column in ("jplag_avg", "jplag_max", "ss_combined"):
            row[column] = report.metrics.auc(part["label"], part[column])
        by_ratio.append(row)
    by_ratio = report.pd.DataFrame(by_ratio).set_index("pairs")
    (out / "auc_by_length_ratio.csv").write_text(by_ratio.to_csv(float_format="%.4f"))

    summary = {
        "coverage": coverage,
        "pairs_compared": int(len(both)),
        "overall": overall.to_dict(orient="index"),
        "train_threshold_test_f1": split,
        "bootstrap_structsim_minus_jplag_avg": report.bootstrap(both, "ss_combined", "jplag_avg"),
        "bootstrap_structsim_minus_jplag_max": report.bootstrap(both, "ss_combined", "jplag_max"),
        "problems_structsim_auc_higher": int((per_problem["ss_combined"] > per_problem["jplag_avg"]).sum()),
        "problems_jplag_auc_higher": int((per_problem["ss_combined"] < per_problem["jplag_avg"]).sum()),
        "problems": int(len(per_problem)),
    }
    report.write_json(out / "summary.json", summary)
    report.plot_by_level(
        both, ("non", "plag"), ("не плагиат", "плагиат"), out / "score_by_label.png",
        f"ConPlag, версия {version} ({'как сдано' if version == 1 else 'без шаблона'})",
    )

    print(f"\n=== ConPlag v{version}: {coverage}")
    print(report.markdown(overall[["auc", "ap", "best_f1", "mean_plag", "mean_non", "zero_share_non"]]))
    print({k: (round(v["f1"], 3), round(v["precision"], 3), round(v["recall"], 3), round(v["threshold"], 3)) for k, v in split.items()})
    print(summary["bootstrap_structsim_minus_jplag_avg"])
    print(summary["bootstrap_structsim_minus_jplag_max"])
    print("задач, где AUC structsim выше:", summary["problems_structsim_auc_higher"], "/", summary["problems"])
    print(report.markdown(by_ratio))
    return f"ConPlag v{version}", both, summary


def main() -> None:
    panels = {}
    for version in (1, 2):
        name, both, _ = one(version)
        panels[name] = both
    irplag = results_of("e01_irplag") / "scores.csv"
    if irplag.exists():
        panels = {"IR-Plag": report.pd.read_csv(irplag), **panels}
    report.plot_roc(panels, results_of(NAME) / "roc.png")


if __name__ == "__main__":
    main()
