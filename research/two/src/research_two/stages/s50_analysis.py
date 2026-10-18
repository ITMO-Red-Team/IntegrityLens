"""s50: разбор — по задачам, кривая по `-t`, расхождения, H4, время, графики.

Таблицы (`results/tables/`):

- `per_problem.csv` — AUC головных оценок по каждой задаче, где есть оба класса;
- `t_sweep.csv` — AUC JPlag по `-t` (avg и max) рядом с `COMBINED`;
- `agreement.csv` — на test с порогами от train: сколько плагиата поймали
  оба, только один, никто; то же для ложных срабатываний;
- `disagreements.csv` — пары, где инструменты расходятся сильнее всего;
- `h4.csv` — комбинация оценок: логистическая регрессия, обученная на train, и
  среднее рангов без обучения; AUC на test против каждой оценки отдельно;
- `length_ratio.csv` — AUC по отношению длин программ пары;
- `timing.csv` — время на пару и на решение у обоих инструментов.

Графики (`results/figures/`): `roc_pr.png`, `t_sweep.png`,
`distributions.png`, `per_problem.png`.
"""

from __future__ import annotations

import matplotlib

matplotlib.use("Agg")

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from scipy import stats as sps
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import precision_recall_curve, roc_curve
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import StandardScaler

from research_two import config, paths, scores, stats
from research_two.stages.s30_metrics import best_threshold

# Палитра: категориальные слоты 1–3 эталонной палитры dataviz, светлая тема.
BLUE, ORANGE, AQUA = "#2a78d6", "#eb6834", "#1baf7a"
INK, INK_2, GRID, SURFACE = "#0b0b0b", "#52514e", "#e4e3df", "#fcfcfb"
NEUTRAL = "#a3a29c"

S, J = config.HEAD_STRUCTSIM, config.HEAD_JPLAG
LABEL = {S: "structsim COMBINED", J: "JPlag default (avg)"}


def _style() -> None:
    plt.rcParams.update({
        "figure.facecolor": SURFACE, "axes.facecolor": SURFACE, "savefig.facecolor": SURFACE,
        "axes.edgecolor": GRID, "axes.labelcolor": INK_2, "axes.titlecolor": INK,
        "xtick.color": INK_2, "ytick.color": INK_2, "text.color": INK,
        "axes.grid": True, "grid.color": GRID, "grid.linewidth": 0.8,
        "axes.spines.top": False, "axes.spines.right": False,
        "font.size": 10, "axes.titlesize": 11, "legend.frameon": False,
        "lines.linewidth": 2, "lines.solid_capstyle": "round",
    })


def _y(part: pd.DataFrame) -> np.ndarray:
    return part["label"].to_numpy().astype(bool)


# --- таблицы ---------------------------------------------------------------


def per_problem(parts: dict[str, pd.DataFrame]) -> pd.DataFrame:
    rows = []
    for version, part in parts.items():
        for problem, g in part.groupby("problem"):
            y = _y(g)
            if y.all() or not y.any():
                continue
            rows.append({
                "version": version, "problem": problem, "pairs": len(g), "positive": int(y.sum()),
                "structsim": stats.auc(y, g[S].to_numpy()),
                "jplag": stats.auc(y, g[J].to_numpy()),
            })
    table = pd.DataFrame(rows)
    table["diff"] = table["structsim"] - table["jplag"]
    return table


def t_sweep(parts: dict[str, pd.DataFrame]) -> pd.DataFrame:
    rows = []
    for version, part in parts.items():
        y = _y(part)
        for t in sorted((*config.JPLAG_MIN_TOKENS_SWEEP, config.JPLAG_DEFAULT_MIN_TOKENS)):
            run = "default" if t == config.JPLAG_DEFAULT_MIN_TOKENS else f"t{t:02d}"
            rows.append({
                "version": version, "t": t,
                "avg": stats.auc(y, part[f"jplag-{run}:avg"].to_numpy()),
                "max": stats.auc(y, part[f"jplag-{run}:max"].to_numpy()),
                "zero_pos_avg": float((part.loc[y, f"jplag-{run}:avg"] == 0).mean()),
                "structsim": stats.auc(y, part[S].to_numpy()),
            })
    return pd.DataFrame(rows)


def agreement(parts: dict[str, pd.DataFrame]) -> pd.DataFrame:
    rows = []
    for version, part in parts.items():
        train, test = part[part["split"] == "train"], part[part["split"] == "test"]
        hit = {}
        for col in (S, J):
            t = best_threshold(_y(train), train[col].to_numpy())
            hit[col] = test[col].to_numpy() >= t
        y = _y(test)
        for cls, mask in (("плагиат", y), ("не плагиат", ~y)):
            s, j = hit[S][mask], hit[J][mask]
            rows.append({
                "version": version, "class": cls, "pairs": int(mask.sum()),
                "both": int((s & j).sum()), "only_structsim": int((s & ~j).sum()),
                "only_jplag": int((~s & j).sum()), "neither": int((~s & ~j).sum()),
            })
    return pd.DataFrame(rows)


def disagreements(parts: dict[str, pd.DataFrame], top: int = 8) -> pd.DataFrame:
    rows = []
    for version, part in parts.items():
        rs = part[S].rank(pct=True)
        rj = part[J].rank(pct=True)
        frame = part.assign(rank_structsim=rs, rank_jplag=rj, gap=rs - rj)
        for direction, sub in (("structsim выше", frame.nlargest(top, "gap")),
                               ("JPlag выше", frame.nsmallest(top, "gap"))):
            for _, r in sub.iterrows():
                rows.append({
                    "version": version, "direction": direction, "pair": r["pair"], "problem": r["problem"],
                    "label": r["label"], "structsim": r[S], "jplag": r[J],
                    "rank_structsim": r["rank_structsim"], "rank_jplag": r["rank_jplag"],
                })
    return pd.DataFrame(rows)


def h4(parts: dict[str, pd.DataFrame]) -> pd.DataFrame:
    rows = []
    for version, part in parts.items():
        train, test = part[part["split"] == "train"], part[part["split"] == "test"]
        y_train, y_test = _y(train), _y(test)
        features = [S, J]
        model = make_pipeline(StandardScaler(), LogisticRegression())
        model.fit(train[features].to_numpy(), y_train)
        lr = model.predict_proba(test[features].to_numpy())[:, 1]
        coef = model[-1].coef_[0]
        # Без обучения: среднее процентильных рангов по всему набору.
        ranks = (part[S].rank(pct=True) + part[J].rank(pct=True)) / 2
        rank_mean = ranks[part["split"] == "test"].to_numpy()
        candidates = {"logreg(structsim, jplag)": lr, "rank-mean(structsim, jplag)": rank_mean}
        for name, combo in candidates.items():
            for base in (S, J):
                sb = test[base].to_numpy()
                d = stats.delong(y_test, combo, sb)
                boot = stats.bootstrap_diff(y_test, combo, sb, config.BOOTSTRAP_ITERATIONS,
                                            config.BOOTSTRAP_SEED, config.CONFIDENCE_LEVEL)
                rows.append({
                    "version": version, "combination": name, "vs": base, "n_test": len(y_test),
                    "auc_combo": d.auc_a, "auc_base": d.auc_b, "diff": d.diff,
                    "boot_low": boot.low, "boot_high": boot.high, "delong_p": d.p,
                    "coef_structsim": coef[0] if name.startswith("logreg") else np.nan,
                    "coef_jplag": coef[1] if name.startswith("logreg") else np.nan,
                })
    return pd.DataFrame(rows)


def length_ratio(parts: dict[str, pd.DataFrame]) -> pd.DataFrame:
    """AUC по корзинам отношения длин пары (короткая / длинная, в метках structsim).

    Проверка объяснения проигрыша на as-is: заготовка участника раздувает одну
    программу пары, симметричные меры это штрафуют, а `max` у JPlag меряет
    относительно короткой программы и заготовку не замечает.
    """
    parse = pd.read_csv(paths.TABLES_DIR / "structsim_parse.csv", dtype={"submission": str})
    cols = [S, "structsim:blocks", J, "jplag-default:max"]
    rows = []
    for version, part in parts.items():
        n = parse[parse["version"] == version].set_index("submission")["labels"]
        a, b = part["sub1"].map(n), part["sub2"].map(n)
        ratio = np.minimum(a, b) / np.maximum(a, b)
        buckets = pd.cut(ratio, [0, 0.5, 0.75, 1.0])
        for bucket, g in part.groupby(buckets, observed=True):
            y = _y(g)
            row = {"version": version, "ratio": str(bucket), "pairs": len(g), "positive": int(y.sum())}
            for col in cols:
                row[col] = stats.auc(y, g[col].to_numpy()) if 0 < y.sum() < len(y) else np.nan
            rows.append(row)
    return pd.DataFrame(rows)


def timing() -> pd.DataFrame:
    parse = pd.read_csv(paths.TABLES_DIR / "structsim_parse.csv")
    runs = pd.read_csv(paths.TABLES_DIR / "jplag_runs.csv")
    rows = []
    for version in config.DATASET_VERSIONS:
        pairs = pd.concat(pd.read_csv(f) for f in sorted((paths.STRUCTSIM_WORK / version).glob("*.csv")))
        p = parse[(parse["version"] == version) & (parse["parsed"] == 1)]
        r = runs[(runs["version"] == version) & (runs["run"] == "default")]
        rows += [
            {"version": version, "tool": "structsim", "what": "шаг 1, мс на файл",
             "median": p["step1_ms"].median(), "mean": p["step1_ms"].mean(), "total_s": p["step1_ms"].sum() / 1000},
            {"version": version, "tool": "structsim", "what": "шаг 2, мс на файл",
             "median": p["step2_ms"].median(), "mean": p["step2_ms"].mean(), "total_s": p["step2_ms"].sum() / 1000},
            {"version": version, "tool": "structsim", "what": "шаг 3 (COMBINED), мс на пару",
             "median": pairs["ms"].median(), "mean": pairs["ms"].mean(), "total_s": pairs["ms"].sum() / 1000},
            {"version": version, "tool": "structsim", "what": "метки на файл",
             "median": p["labels"].median(), "mean": p["labels"].mean(), "total_s": np.nan},
            {"version": version, "tool": "jplag", "what": "сравнение, мс на пару",
             "median": (r["execution_ms"] / r["comparisons"]).median(),
             "mean": r["execution_ms"].sum() / r["comparisons"].sum(), "total_s": r["execution_ms"].sum() / 1000},
            {"version": version, "tool": "jplag", "what": "запуск целиком, с на задачу",
             "median": r["wall_s"].median(), "mean": r["wall_s"].mean(), "total_s": r["wall_s"].sum()},
        ]
    return pd.DataFrame(rows)


# --- графики ---------------------------------------------------------------


def fig_roc_pr(parts: dict[str, pd.DataFrame]) -> None:
    fig, axes = plt.subplots(2, 2, figsize=(10, 9))
    for row, (version, part) in enumerate(parts.items()):
        y = _y(part)
        ax_roc, ax_pr = axes[row]
        for col, color in ((S, BLUE), (J, ORANGE)):
            s = part[col].to_numpy()
            fpr, tpr, _ = roc_curve(y, s)
            ax_roc.plot(fpr, tpr, color=color, label=f"{LABEL[col]} — AUC {stats.auc(y, s):.3f}")
            prec, rec, _ = precision_recall_curve(y, s)
            ax_pr.plot(rec, prec, color=color, label=LABEL[col])
        ax_roc.plot([0, 1], [0, 1], color=NEUTRAL, linewidth=1, linestyle=":")
        ax_pr.axhline(y.mean(), color=NEUTRAL, linewidth=1, linestyle=":")
        ax_roc.set(title=f"ROC, {version}", xlabel="доля ложных срабатываний", ylabel="полнота")
        ax_pr.set(title=f"Точность — полнота, {version}", xlabel="полнота", ylabel="точность", ylim=(0, 1.02))
        ax_roc.legend(loc="lower right")
        ax_pr.legend(loc="lower left")
    fig.tight_layout()
    fig.savefig(paths.FIGURES_DIR / "roc_pr.png", dpi=150)
    plt.close(fig)


def fig_t_sweep(sweep: pd.DataFrame) -> None:
    fig, axes = plt.subplots(1, 2, figsize=(10, 4), sharey=True)
    for ax, (version, g) in zip(axes, sweep.groupby("version", sort=False)):
        ax.plot(g["t"], g["avg"], color=ORANGE, marker="o", markersize=5, label="JPlag avg")
        ax.plot(g["t"], g["max"], color=AQUA, marker="o", markersize=5, label="JPlag max")
        ax.axhline(g["structsim"].iloc[0], color=BLUE, label="structsim COMBINED")
        ax.axvline(config.JPLAG_DEFAULT_MIN_TOKENS, color=NEUTRAL, linewidth=1, linestyle=":")
        ax.set(title=version, xlabel="-t, минимальная длина совпадения", xticks=g["t"])
        ax.legend(loc="lower left")
    axes[0].set_ylabel("AUC")
    fig.tight_layout()
    fig.savefig(paths.FIGURES_DIR / "t_sweep.png", dpi=150)
    plt.close(fig)


def fig_distributions(parts: dict[str, pd.DataFrame]) -> None:
    fig, axes = plt.subplots(2, 2, figsize=(10, 7))
    bins = np.linspace(0, 1, 26)
    for row, (version, part) in enumerate(parts.items()):
        y = _y(part)
        for ax, col in zip(axes[row], (S, J)):
            s = part[col].to_numpy()
            ax.hist(s[~y], bins=bins, color=NEUTRAL, alpha=0.8, label="не плагиат", edgecolor=SURFACE, linewidth=1)
            ax.hist(s[y], bins=bins, color=BLUE, alpha=0.8, label="плагиат", edgecolor=SURFACE, linewidth=1)
            ax.set(title=f"{LABEL[col]}, {version}", xlabel="оценка", ylabel="пар")
            ax.legend(loc="upper right")
    fig.tight_layout()
    fig.savefig(paths.FIGURES_DIR / "distributions.png", dpi=150)
    plt.close(fig)


def fig_per_problem(table: pd.DataFrame) -> None:
    fig, axes = plt.subplots(1, 2, figsize=(10, 6), sharey=True)
    for ax, (version, g) in zip(axes, table.groupby("version", sort=False)):
        g = g.sort_values("problem")
        ypos = np.arange(len(g))
        ax.hlines(ypos, g[["structsim", "jplag"]].min(axis=1), g[["structsim", "jplag"]].max(axis=1),
                  color=GRID, linewidth=2)
        ax.scatter(g["jplag"], ypos, color=ORANGE, s=36, zorder=3, label="JPlag default (avg)",
                   edgecolor=SURFACE, linewidth=1.5)
        ax.scatter(g["structsim"], ypos, color=BLUE, s=36, zorder=3, label="structsim COMBINED",
                   edgecolor=SURFACE, linewidth=1.5)
        ax.set(title=version, xlabel="AUC по задаче", yticks=ypos,
               yticklabels=[f"{p} ({n})" for p, n in zip(g["problem"], g["pairs"])], xlim=(0, 1.02))
        ax.axvline(0.5, color=NEUTRAL, linewidth=1, linestyle=":")
        ax.legend(loc="lower left")
    axes[0].set_ylabel("задача (размеченных пар)")
    fig.tight_layout()
    fig.savefig(paths.FIGURES_DIR / "per_problem.png", dpi=150)
    plt.close(fig)


def run() -> None:
    _style()
    table = scores.load()
    parts = {v: scores.common(table, v)[0] for v in config.DATASET_VERSIONS}

    out = {
        "per_problem": per_problem(parts),
        "t_sweep": t_sweep(parts),
        "agreement": agreement(parts),
        "disagreements": disagreements(parts),
        "h4": h4(parts),
        "length_ratio": length_ratio(parts),
        "timing": timing(),
    }
    for name, frame in out.items():
        frame.to_csv(paths.TABLES_DIR / f"{name}.csv", index=False, float_format="%.4f")

    pp = out["per_problem"]
    for version, g in pp.groupby("version", sort=False):
        w = sps.wilcoxon(g["structsim"], g["jplag"])
        print(f"  {version}: structsim лучше на {(g['diff'] > 0).sum()} задачах из {len(g)}, "
              f"медиана разницы {g['diff'].median():+.3f}, Уилкоксон p = {w.pvalue:.3g}")
    (paths.TABLES_DIR / "per_problem_summary.csv").write_text(
        "version,problems,structsim_better,median_diff,wilcoxon_p\n" + "".join(
            f"{v},{len(g)},{int((g['diff'] > 0).sum())},{g['diff'].median():.4f},"
            f"{sps.wilcoxon(g['structsim'], g['jplag']).pvalue:.4g}\n"
            for v, g in pp.groupby("version", sort=False)),
        encoding="utf-8")

    fig_roc_pr(parts)
    fig_t_sweep(out["t_sweep"])
    fig_distributions(parts)
    fig_per_problem(pp)
    print(out["h4"][["version", "combination", "vs", "auc_combo", "auc_base", "diff", "delong_p"]].to_string(index=False))
    print(out["timing"].to_string(index=False))
