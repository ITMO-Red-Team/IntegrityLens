"""Насколько оценка отделяет плагиат от неплагиата.

Шкалы у инструментов разные, поэтому сравнивать надо не сами числа, а то,
как они упорядочивают пары:

- **ROC-AUC** — вероятность, что случайная пара-плагиат получит оценку выше
  случайной пары-неплагиата. Не зависит от порога и от шкалы. Ничьи считаются
  за половину — важно для JPlag, у которого масса пар ровно в 0.0;
- **AP** (площадь под PR-кривой) — то же с упором на верх списка: что увидит
  проверяющий, если пойдёт сверху вниз;
- **F1 при пороге**, выбранном на одной части данных и проверенном на другой.
  Лучший F1 на тех же данных — оптимистичная оценка и подаётся только как
  потолок.
"""

from __future__ import annotations

from collections.abc import Sequence

import numpy as np
from sklearn.metrics import average_precision_score, f1_score, precision_score, recall_score, roc_auc_score


def auc(labels: Sequence[int], scores: Sequence[float]) -> float:
    return float(roc_auc_score(labels, scores))


def ap(labels: Sequence[int], scores: Sequence[float]) -> float:
    return float(average_precision_score(labels, scores))


def best_threshold(labels: Sequence[int], scores: Sequence[float]) -> tuple[float, float]:
    """Порог с лучшим F1: `(порог, F1)`. Плагиат — оценка >= порога."""
    y = np.asarray(labels)
    s = np.asarray(scores, dtype=float)
    best = (float("inf"), -1.0)
    for t in np.unique(s):
        f1 = f1_score(y, s >= t, zero_division=0)
        if f1 > best[1]:
            best = (float(t), float(f1))
    return best


def at_threshold(labels: Sequence[int], scores: Sequence[float], threshold: float) -> dict[str, float]:
    y = np.asarray(labels)
    p = np.asarray(scores, dtype=float) >= threshold
    return {
        "threshold": float(threshold),
        "f1": float(f1_score(y, p, zero_division=0)),
        "precision": float(precision_score(y, p, zero_division=0)),
        "recall": float(recall_score(y, p, zero_division=0)),
    }


def paired_bootstrap(
    labels: Sequence[int],
    first: Sequence[float],
    second: Sequence[float],
    metric=auc,
    rounds: int = 2000,
    seed: int = 0,
) -> dict[str, float]:
    """Разница `metric(first) - metric(second)` и её 95% интервал.

    Пары переизвлекаются с возвращением **одним и тем же** набором индексов
    для обоих инструментов (парный бутстрэп), отдельно внутри каждого класса,
    чтобы доля плагиата не гуляла. `p` — доля повторов, где разница сменила
    знак: грубая двусторонняя оценка.
    """
    y = np.asarray(labels)
    a = np.asarray(first, dtype=float)
    b = np.asarray(second, dtype=float)
    rng = np.random.default_rng(seed)
    pos, neg = np.flatnonzero(y == 1), np.flatnonzero(y == 0)
    diffs = np.empty(rounds)
    for i in range(rounds):
        idx = np.concatenate([rng.choice(pos, pos.size), rng.choice(neg, neg.size)])
        diffs[i] = metric(y[idx], a[idx]) - metric(y[idx], b[idx])
    observed = metric(y, a) - metric(y, b)
    low, high = np.percentile(diffs, [2.5, 97.5])
    flips = np.mean(diffs <= 0) if observed > 0 else np.mean(diffs >= 0)
    return {
        "diff": float(observed),
        "ci_low": float(low),
        "ci_high": float(high),
        "p_sign_flip": float(min(1.0, 2 * flips)),
    }
