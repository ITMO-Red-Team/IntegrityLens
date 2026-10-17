"""Таблицы метрик и рисунки — общие для экспериментов на размеченных парах."""

from __future__ import annotations

import json
from collections.abc import Sequence
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402
import numpy as np  # noqa: E402
import pandas as pd  # noqa: E402
from sklearn.metrics import roc_curve  # noqa: E402

from research_one import metrics  # noqa: E402
from research_one.evaluate import SCORES  # noqa: E402

# Палитра — слоты 1–3 эталонной категориальной палитры, проверена
# валидатором на всех парах (худшая CVD ΔE 9.2, обычное зрение 27.6). Слот 3
# (Dolos) ниже 3:1 по контрасту с фоном — поэтому подписи в легенде всегда.
COLORS = {"jplag": "#2a78d6", "structsim": "#eb6834", "dolos": "#1baf7a"}
TOOLS_BY_LEVEL = (
    ("jplag", "jplag_avg", "JPlag averageSimilarity"),
    ("structsim", "ss_combined", "structsim COMBINED"),
)
ROC_CURVES = (
    ("jplag", "jplag_avg", "JPlag avg", "-"),
    ("jplag", "jplag_max", "JPlag max", "--"),
    ("structsim", "ss_combined", "structsim", "-"),
)
SURFACE = "#fcfcfb"
INK = "#0b0b0b"
INK_2 = "#52514e"
GRID = "#e4e3df"


def table(frame: pd.DataFrame, columns: Sequence[str] = tuple(SCORES)) -> pd.DataFrame:
    """Метрики разделения по каждой оценке. Ничьи у AUC идут за половину."""
    rows = []
    y = frame["label"].to_numpy()
    for column in columns:
        s = frame[column]
        if s.isna().all():
            continue
        s = s.to_numpy(dtype=float)
        threshold, f1 = metrics.best_threshold(y, s)
        rows.append({
            "score": column,
            "auc": metrics.auc(y, s),
            "ap": metrics.ap(y, s),
            "best_f1": f1,
            "best_threshold": threshold,
            "mean_plag": float(s[y == 1].mean()),
            "mean_non": float(s[y == 0].mean()),
            "median_plag": float(np.median(s[y == 1])),
            "median_non": float(np.median(s[y == 0])),
            "zero_share_plag": float(np.mean(s[y == 1] == 0)),
            "zero_share_non": float(np.mean(s[y == 0] == 0)),
        })
    return pd.DataFrame(rows).set_index("score")


def leave_one_group_out(frame: pd.DataFrame, column: str) -> dict:
    """Порог по всем задачам, кроме одной, проверка — на ней; предсказания сводятся."""
    predicted = np.zeros(len(frame), dtype=bool)
    thresholds = {}
    for group in sorted(frame["group"].unique()):
        held = (frame["group"] == group).to_numpy()
        t, _ = metrics.best_threshold(frame.loc[~held, "label"], frame.loc[~held, column])
        thresholds[group] = t
        predicted[held] = frame.loc[held, column].to_numpy(dtype=float) >= t
    result = metrics.at_threshold(frame["label"], predicted.astype(float), 0.5)
    result.pop("threshold")
    result["thresholds"] = thresholds
    return result


def train_test(frame: pd.DataFrame, column: str) -> dict:
    """Порог по train, проверка — на test (разбиение авторов ConPlag)."""
    train = frame[frame["split"] == "train"]
    test = frame[frame["split"] == "test"]
    t, f1_train = metrics.best_threshold(train["label"], train[column])
    result = metrics.at_threshold(test["label"], test[column], t)
    result["f1_train"] = f1_train
    return result


def per_group_auc(frame: pd.DataFrame, columns: Sequence[str]) -> pd.DataFrame:
    rows = []
    for group, part in frame.groupby("group"):
        if part["label"].nunique() < 2:
            continue
        row = {"group": group, "pairs": len(part), "plag": int(part["label"].sum())}
        for column in columns:
            row[column] = metrics.auc(part["label"], part[column])
        rows.append(row)
    return pd.DataFrame(rows).set_index("group")


def bootstrap(frame: pd.DataFrame, first: str, second: str) -> dict:
    y = frame["label"].to_numpy()
    return {
        "auc": metrics.paired_bootstrap(y, frame[first], frame[second], metrics.auc),
        "ap": metrics.paired_bootstrap(y, frame[first], frame[second], metrics.ap),
    }


def markdown(df: pd.DataFrame, digits: int = 3) -> str:
    """Таблица в markdown без зависимости от tabulate."""
    df = df.reset_index()
    head = "| " + " | ".join(str(c) for c in df.columns) + " |"
    sep = "|" + "|".join("---" for _ in df.columns) + "|"
    lines = [head, sep]
    for _, row in df.iterrows():
        cells = []
        for value in row:
            if isinstance(value, float):
                cells.append(f"{value:.{digits}f}")
            else:
                cells.append(str(value))
        lines.append("| " + " | ".join(cells) + " |")
    return "\n".join(lines)


def write_json(path: Path, data) -> None:
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2, default=float) + "\n")


# --- рисунки ---------------------------------------------------------------

def _style(ax) -> None:
    ax.set_facecolor(SURFACE)
    for side in ("top", "right"):
        ax.spines[side].set_visible(False)
    for side in ("left", "bottom"):
        ax.spines[side].set_color(INK_2)
        ax.spines[side].set_linewidth(0.8)
    ax.tick_params(colors=INK_2, labelsize=9)
    ax.yaxis.grid(True, color=GRID, linewidth=0.8)
    ax.set_axisbelow(True)


def plot_by_level(
    frame: pd.DataFrame, levels: Sequence[str], labels: Sequence[str], path: Path, title: str,
    tools: Sequence[tuple[str, str, str]] = TOOLS_BY_LEVEL,
) -> None:
    """Распределение оценок по уровням: инструменты рядом, одна шкала 0..1."""
    fig, ax = plt.subplots(figsize=(9, 4.2), facecolor=SURFACE)
    _style(ax)
    rng = np.random.default_rng(0)
    width = 0.68 / len(tools)
    for k, (tool, column, name) in enumerate(tools):
        offset = (k - (len(tools) - 1) / 2) * (width + 0.04)
        for i, level in enumerate(levels):
            values = frame.loc[frame["level"] == level, column].to_numpy(dtype=float)
            x = i + offset
            ax.boxplot(
                [values], positions=[x], widths=width, showfliers=False, patch_artist=True,
                boxprops={"facecolor": "none", "edgecolor": COLORS[tool], "linewidth": 1.2},
                medianprops={"color": COLORS[tool], "linewidth": 2},
                whiskerprops={"color": COLORS[tool], "linewidth": 1},
                capprops={"color": COLORS[tool], "linewidth": 1},
            )
            jitter = rng.uniform(-width / 2.6, width / 2.6, size=values.size)
            ax.scatter(x + jitter, values, s=9, color=COLORS[tool], alpha=0.45, linewidths=0,
                       label=name if i == 0 else None, zorder=3)
    ax.set_xticks(range(len(levels)), labels, color=INK)
    ax.set_ylim(-0.03, 1.03)
    ax.set_ylabel("оценка сходства", color=INK_2, fontsize=9)
    ax.set_title(title, color=INK, fontsize=11, loc="left")
    legend = ax.legend(frameon=False, fontsize=9, loc="lower right", markerscale=2)
    for text in legend.get_texts():
        text.set_color(INK)
    fig.tight_layout()
    fig.savefig(path, dpi=150, facecolor=SURFACE)
    plt.close(fig)


def plot_roc(
    panels: dict[str, pd.DataFrame], path: Path,
    curves: Sequence[tuple[str, str, str, str]] = ROC_CURVES,
) -> None:
    """ROC-кривые инструментов, по панели на набор."""
    fig, axes = plt.subplots(1, len(panels), figsize=(4.2 * len(panels), 4.0), facecolor=SURFACE, squeeze=False)
    for ax, (name, frame) in zip(axes[0], panels.items()):
        _style(ax)
        ax.xaxis.grid(True, color=GRID, linewidth=0.8)
        ax.plot([0, 1], [0, 1], color=GRID, linewidth=1, linestyle="--")
        for tool, column, label, style in curves:
            fpr, tpr, _ = roc_curve(frame["label"], frame[column])
            auc = metrics.auc(frame["label"], frame[column])
            ax.plot(fpr, tpr, color=COLORS[tool], linewidth=2, linestyle=style, label=f"{label}, AUC {auc:.3f}")
        ax.set_xlim(0, 1)
        ax.set_ylim(0, 1.01)
        ax.set_xlabel("доля ложных срабатываний", color=INK_2, fontsize=9)
        ax.set_ylabel("доля найденного плагиата", color=INK_2, fontsize=9)
        ax.set_title(name, color=INK, fontsize=11, loc="left")
        legend = ax.legend(frameon=False, fontsize=9, loc="lower right")
        for text in legend.get_texts():
            text.set_color(INK)
    fig.tight_layout()
    fig.savefig(path, dpi=150, facecolor=SURFACE)
    plt.close(fig)
