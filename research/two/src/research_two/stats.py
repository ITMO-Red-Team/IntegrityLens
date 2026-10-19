"""Статистика сравнения двух оценок на одной и той же выборке пар.

Обе оценки считаются на одних и тех же парах, поэтому сравнение парное:
независимые интервалы для двух AUC проигнорировали бы корреляцию между
оценками и завысили бы неопределённость разницы.

- `auc` — через ранги (Манн — Уитни), связки получают средний ранг, то есть
  дают 0.5 — так же, как `sklearn.metrics.roc_auc_score`;
- `delong` — аналитический парный тест для разницы двух AUC (DeLong et al.,
  1988), в быстрой форме Sun & Xu (2014);
- `bootstrap_diff` — парный бутстрэп: одни и те же индексы для обеих оценок.
  `groups` включает кластерный вариант: пересэмплируются задачи целиком.
"""

from __future__ import annotations

from dataclasses import dataclass

import numpy as np
from scipy import stats as sps


def auc(y: np.ndarray, s: np.ndarray) -> float:
    y = np.asarray(y, dtype=bool)
    ranks = sps.rankdata(s)
    n_pos = y.sum()
    n_neg = len(y) - n_pos
    if n_pos == 0 or n_neg == 0:
        return float("nan")
    return float((ranks[y].sum() - n_pos * (n_pos + 1) / 2) / (n_pos * n_neg))


def _midrank_components(pos: np.ndarray, neg: np.ndarray) -> tuple[float, np.ndarray, np.ndarray]:
    """AUC и структурные компоненты V10 (по положительным) и V01 (по отрицательным)."""
    m, n = len(pos), len(neg)
    both = np.concatenate([pos, neg])
    r_all = sps.rankdata(both)
    r_pos = sps.rankdata(pos)
    r_neg = sps.rankdata(neg)
    v10 = (r_all[:m] - r_pos) / n
    v01 = 1.0 - (r_all[m:] - r_neg) / m
    return float(v10.mean()), v10, v01


@dataclass(frozen=True)
class DeLong:
    auc_a: float
    auc_b: float
    diff: float
    se: float
    z: float
    p: float


def delong(y: np.ndarray, a: np.ndarray, b: np.ndarray) -> DeLong:
    y = np.asarray(y, dtype=bool)
    auc_a, v10_a, v01_a = _midrank_components(a[y], a[~y])
    auc_b, v10_b, v01_b = _midrank_components(b[y], b[~y])
    s10 = np.cov(np.vstack([v10_a, v10_b]))
    s01 = np.cov(np.vstack([v01_a, v01_b]))
    m, n = y.sum(), (~y).sum()
    cov = s10 / m + s01 / n
    var = cov[0, 0] + cov[1, 1] - 2 * cov[0, 1]
    diff = auc_a - auc_b
    se = float(np.sqrt(var)) if var > 0 else 0.0
    z = diff / se if se > 0 else float("inf") * np.sign(diff) if diff else 0.0
    p = float(2 * sps.norm.sf(abs(z))) if se > 0 else (0.0 if diff else 1.0)
    return DeLong(auc_a, auc_b, diff, se, float(z), p)


@dataclass(frozen=True)
class Bootstrap:
    diff: float
    low: float
    high: float
    p_le_zero: float  # доля повторов, где разница ≤ 0: односторонняя оценка


def bootstrap_diff(
    y: np.ndarray,
    a: np.ndarray,
    b: np.ndarray,
    iterations: int,
    seed: int,
    level: float,
    groups: np.ndarray | None = None,
) -> Bootstrap:
    """ДИ для AUC(a) − AUC(b). Повтор без одного из классов пропускается."""
    rng = np.random.default_rng(seed)
    y = np.asarray(y, dtype=bool)
    n = len(y)
    if groups is not None:
        labels, inverse = np.unique(groups, return_inverse=True)
        members = [np.flatnonzero(inverse == g) for g in range(len(labels))]
    diffs = []
    while len(diffs) < iterations:
        if groups is None:
            idx = rng.integers(0, n, n)
        else:
            picked = rng.integers(0, len(members), len(members))
            idx = np.concatenate([members[g] for g in picked])
        yy = y[idx]
        if yy.all() or not yy.any():
            continue
        diffs.append(auc(yy, a[idx]) - auc(yy, b[idx]))
    diffs = np.asarray(diffs)
    alpha = (1 - level) / 2
    return Bootstrap(
        diff=auc(y, a) - auc(y, b),
        low=float(np.quantile(diffs, alpha)),
        high=float(np.quantile(diffs, 1 - alpha)),
        p_le_zero=float((diffs <= 0).mean()),
    )


def bootstrap_auc(y, s, iterations: int, seed: int, level: float) -> tuple[float, float]:
    """ДИ для одной AUC — для таблицы метрик."""
    rng = np.random.default_rng(seed)
    y = np.asarray(y, dtype=bool)
    values = []
    while len(values) < iterations:
        idx = rng.integers(0, len(y), len(y))
        yy = y[idx]
        if yy.all() or not yy.any():
            continue
        values.append(auc(yy, s[idx]))
    alpha = (1 - level) / 2
    return float(np.quantile(values, alpha)), float(np.quantile(values, 1 - alpha))


def holm(pvalues: list[float]) -> list[float]:
    """Поправка Холма на множественные сравнения."""
    order = np.argsort(pvalues)
    m = len(pvalues)
    adjusted = np.empty(m)
    running = 0.0
    for rank, i in enumerate(order):
        running = max(running, (m - rank) * pvalues[i])
        adjusted[i] = min(1.0, running)
    return adjusted.tolist()
