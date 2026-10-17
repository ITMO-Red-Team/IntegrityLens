"""e03 — устойчив ли вывод к настройке JPlag: порог `-t` и `--normalize`.

    uv run python experiments/e03_jplag_threshold/run.py

В e01 и e02 оба инструмента запускались по умолчанию. Но порог JPlag `-t 9`
рассчитан на программы крупнее учебных: на IR-Plag он обнуляет больше трети
пар уровня L3. Здесь JPlag прогоняется по сетке `-t` и с `--normalize`, а
structsim остаётся как был — его оценки берутся из e01/e02. Выбор лучшего `-t`
задним числом — оптимистичная оценка для JPlag, то есть проверка в его пользу.
Для каждой настройки считается не только AUC, но и разница structsim − JPlag с
интервалом парного бутстрэпа: точечный порядок может сохраниться, а значимость —
нет.
"""

from __future__ import annotations

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402
import pandas as pd  # noqa: E402

from research_one import datasets, evaluate, metrics, report  # noqa: E402
from research_one.paths import results_of, work_of  # noqa: E402

NAME = "e03_jplag_threshold"
THRESHOLDS = (3, 5, 7, 9, 12, 15)
SETS = {
    "IR-Plag": (datasets.irplag, "e01_irplag"),
    "ConPlag v1": (lambda: datasets.conplag(1), "e02_conplag/v1"),
    "ConPlag v2": (lambda: datasets.conplag(2), "e02_conplag/v2"),
}


def settings():
    for t in THRESHOLDS:
        yield f"t{t}", {"min_tokens": t}
    yield "t9-normalize", {"min_tokens": 9, "extra": ("--normalize",)}


def main() -> None:
    out = results_of(NAME)
    rows = []
    for set_name, (load, source) in SETS.items():
        dataset = load()
        base = pd.read_csv(results_of(source) / "scores.csv")
        base = base.dropna(subset=list(evaluate.PRIMARY)).reset_index(drop=True)
        keys = [(g, frozenset((a, b))) for g, a, b in zip(base["group"], base["a"], base["b"])]
        y = base["label"].to_numpy()
        ss = base["ss_combined"].to_numpy()
        rows.append({"set": set_name, "setting": "structsim", "t": None,
                     "auc_avg": metrics.auc(y, ss), "ap_avg": metrics.ap(y, ss),
                     "auc_max": None, "ap_max": None, "failed": 0})
        for label, args in settings():
            work = work_of(f"{NAME}/{set_name.replace(' ', '-')}/{label}")
            scores, failed, _ = evaluate.jplag_pairs(dataset, work, **args)
            missing = [k for k in keys if k not in scores]
            avg = [scores[k].average if k in scores else 0.0 for k in keys]
            mx = [scores[k].maximum if k in scores else 0.0 for k in keys]
            # structsim − JPlag при этой настройке: тот же парный бутстрэп, что в e01/e02
            # (2000 повторов, seed 0), поэтому строка `t9` совпадает с их summary.json.
            against_avg = metrics.paired_bootstrap(y, ss, avg)
            against_max = metrics.paired_bootstrap(y, ss, mx)
            rows.append({
                "set": set_name, "setting": label, "t": args["min_tokens"],
                "auc_avg": metrics.auc(y, avg), "ap_avg": metrics.ap(y, avg),
                "auc_max": metrics.auc(y, mx), "ap_max": metrics.ap(y, mx),
                "failed": sum(len(v) for v in failed.values()), "missing_pairs": len(missing),
                "ss_minus_avg": against_avg["diff"],
                "ss_minus_avg_low": against_avg["ci_low"], "ss_minus_avg_high": against_avg["ci_high"],
                "ss_minus_max": against_max["diff"],
                "ss_minus_max_low": against_max["ci_low"], "ss_minus_max_high": against_max["ci_high"],
            })
            print(rows[-1])
    table = pd.DataFrame(rows)
    table.to_csv(out / "grid.csv", index=False, float_format="%.4f")

    # Лучшая настройка JPlag против structsim — по каждому набору.
    best = []
    for set_name, part in table.groupby("set", sort=False):
        jp = part[part["setting"] != "structsim"]
        ss = part[part["setting"] == "structsim"].iloc[0]
        top_avg = jp.loc[jp["auc_avg"].idxmax()]
        top_max = jp.loc[jp["auc_max"].idxmax()]
        default = jp[jp["setting"] == "t9"].iloc[0]
        best.append({
            "set": set_name,
            "structsim_auc": ss["auc_avg"],
            "jplag_default_avg": default["auc_avg"], "jplag_default_max": default["auc_max"],
            "jplag_best_avg": top_avg["auc_avg"], "best_avg_setting": top_avg["setting"],
            "jplag_best_max": top_max["auc_max"], "best_max_setting": top_max["setting"],
            # structsim − лучший JPlag, ROC-AUC, с 95% интервалом бутстрэпа.
            "ss_minus_best_avg": top_avg["ss_minus_avg"],
            "ss_minus_best_avg_low": top_avg["ss_minus_avg_low"], "ss_minus_best_avg_high": top_avg["ss_minus_avg_high"],
            "ss_minus_best_max": top_max["ss_minus_max"],
            "ss_minus_best_max_low": top_max["ss_minus_max_low"], "ss_minus_best_max_high": top_max["ss_minus_max_high"],
        })
    best = pd.DataFrame(best).set_index("set")
    best.to_csv(out / "best.csv", float_format="%.4f")
    print(report.markdown(best))
    plot(table, out / "auc_by_threshold.png")


def plot(table: pd.DataFrame, path) -> None:
    sets = list(dict.fromkeys(table["set"]))
    fig, axes = plt.subplots(1, len(sets), figsize=(4.2 * len(sets), 3.8), facecolor=report.SURFACE)
    for ax, set_name in zip(axes, sets):
        report._style(ax)
        part = table[(table["set"] == set_name) & table["setting"].str.fullmatch(r"t\d+")]
        ss = table[(table["set"] == set_name) & (table["setting"] == "structsim")]["auc_avg"].iloc[0]
        ax.plot(part["t"], part["auc_avg"], color=report.COLORS["jplag"], linewidth=2, marker="o", markersize=5,
                label="JPlag averageSimilarity")
        ax.plot(part["t"], part["auc_max"], color=report.COLORS["jplag"], linewidth=2, linestyle="--", marker="s",
                markersize=5, label="JPlag maxSimilarity")
        ax.axhline(ss, color=report.COLORS["structsim"], linewidth=2, label="structsim COMBINED (без настройки)")
        ax.axvline(9, color=report.GRID, linewidth=1)
        ax.text(9.2, ax.get_ylim()[0], " -t 9 по умолч.", color=report.INK_2, fontsize=8, va="bottom")
        ax.set_xticks(THRESHOLDS)
        ax.set_xlabel("порог JPlag -t, метки", color=report.INK_2, fontsize=9)
        ax.set_ylabel("ROC-AUC", color=report.INK_2, fontsize=9)
        ax.set_title(set_name, color=report.INK, fontsize=11, loc="left")
    handles, labels = axes[0].get_legend_handles_labels()
    legend = fig.legend(handles, labels, frameon=False, fontsize=9, loc="lower center", ncol=3)
    for text in legend.get_texts():
        text.set_color(report.INK)
    fig.tight_layout(rect=(0, 0.08, 1, 1))
    fig.savefig(path, dpi=150, facecolor=report.SURFACE)
    plt.close(fig)


if __name__ == "__main__":
    main()
