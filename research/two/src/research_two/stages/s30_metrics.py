"""s30: метрики каждой оценки на каждой версии.

- **AUC** по всем общим парам (911 минус те, что не оценил кто-то из
  инструментов) — основная метрика; 95% ДИ — бутстрэп по парам;
- **PR-AUC** (average precision) — положительных пар 27.5 %, и ROC на таком
  перекосе льстит обоим инструментам;
- **F1, точность, полнота** на test с порогом, максимизирующим F1 на train;
- **доля оценок ровно 0.0** среди положительных и отрицательных: у JPlag пары
  без общего куска длиной `-t` получают ровно ноль, и положительная пара с
  нулём — чистый промах, который никакой порог не спасёт.

Пишет `results/tables/metrics.csv` и `results/tables/metrics.md`.
"""

from __future__ import annotations

import numpy as np
import pandas as pd
from sklearn.metrics import average_precision_score, f1_score, precision_score, recall_score

from research_two import config, paths, scores, stats


def best_threshold(y: np.ndarray, s: np.ndarray) -> float:
    """Порог `s >= t`, дающий максимум F1 на этой выборке."""
    best_t, best_f1 = float("inf"), -1.0
    for t in np.unique(s):
        f1 = f1_score(y, s >= t, zero_division=0)
        if f1 > best_f1:
            best_t, best_f1 = float(t), f1
    return best_t


def metrics_row(part: pd.DataFrame, column: str) -> dict:
    y = part["label"].to_numpy().astype(bool)
    s = part[column].to_numpy(dtype=float)
    train = (part["split"] == "train").to_numpy()
    test = (part["split"] == "test").to_numpy()
    low, high = stats.bootstrap_auc(y, s, 2000, config.BOOTSTRAP_SEED, config.CONFIDENCE_LEVEL)
    t = best_threshold(y[train], s[train])
    pred = s[test] >= t
    return {
        "score": column,
        "n": len(y),
        "auc": stats.auc(y, s),
        "auc_low": low,
        "auc_high": high,
        "pr_auc": average_precision_score(y, s),
        "auc_test": stats.auc(y[test], s[test]),
        "threshold": t,
        "f1_test": f1_score(y[test], pred, zero_division=0),
        "precision_test": precision_score(y[test], pred, zero_division=0),
        "recall_test": recall_score(y[test], pred, zero_division=0),
        "zero_pos": float((s[y] == 0).mean()),
        "zero_neg": float((s[~y] == 0).mean()),
    }


def to_markdown(table: pd.DataFrame) -> str:
    lines = []
    for version, group in table.groupby("version", sort=False):
        lines.append(f"### {version}\n")
        lines.append("| оценка | AUC [95% ДИ] | PR-AUC | F1 test | P | R | порог | 0.0 у плагиата | 0.0 у не плагиата |")
        lines.append("|---|---|---|---|---|---|---|---|---|")
        for r in group.itertuples():
            lines.append(
                f"| `{r.score}` | {r.auc:.3f} [{r.auc_low:.3f}; {r.auc_high:.3f}] | {r.pr_auc:.3f} | "
                f"{r.f1_test:.3f} | {r.precision_test:.3f} | {r.recall_test:.3f} | {r.threshold:.3f} | "
                f"{r.zero_pos:.1%} | {r.zero_neg:.1%} |"
            )
        lines.append("")
    return "\n".join(lines)


def run() -> None:
    table = scores.load()
    columns = scores.score_columns(table)
    rows = []
    for version in config.DATASET_VERSIONS:
        part, dropped = scores.common(table, version)
        print(f"{version}: общих пар {len(part)}, отброшено {dropped}")
        for column in columns:
            rows.append({"version": version, "dropped": dropped, **metrics_row(part, column)})
    result = pd.DataFrame(rows)
    result.to_csv(paths.TABLES_DIR / "metrics.csv", index=False, float_format="%.6f")
    (paths.TABLES_DIR / "metrics.md").write_text(to_markdown(result), encoding="utf-8")
    head = result[result["score"].isin([config.HEAD_STRUCTSIM, config.HEAD_JPLAG])]
    print(head[["version", "score", "auc", "pr_auc", "f1_test"]].to_string(index=False))
