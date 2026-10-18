"""Статистика проверяется на числах, которые известны заранее."""

import numpy as np
import pytest
from sklearn.metrics import roc_auc_score

from research_two import stats


@pytest.fixture
def data():
    rng = np.random.default_rng(0)
    y = rng.random(300) < 0.3
    a = y * 0.8 + rng.normal(0, 0.6, 300)
    b = y * 0.3 + rng.normal(0, 0.6, 300)
    # Связки, как у JPlag: часть оценок ровно 0.
    b[rng.random(300) < 0.3] = 0.0
    return y, a, b


def test_auc_matches_sklearn_with_ties(data):
    y, a, b = data
    assert stats.auc(y, a) == pytest.approx(roc_auc_score(y, a))
    assert stats.auc(y, b) == pytest.approx(roc_auc_score(y, b))


def test_auc_perfect_and_constant():
    y = np.array([0, 0, 1, 1])
    assert stats.auc(y, np.array([0.1, 0.2, 0.8, 0.9])) == 1.0
    assert stats.auc(y, np.zeros(4)) == 0.5


def test_delong_identical_scores():
    y = np.array([0, 1, 0, 1, 1, 0, 0])
    s = np.array([0.1, 0.9, 0.3, 0.7, 0.4, 0.5, 0.2])
    result = stats.delong(y, s, s)
    assert result.diff == 0 and result.p == 1.0


def test_delong_auc_and_known_variance(data):
    y, a, b = data
    result = stats.delong(y, a, b)
    assert result.auc_a == pytest.approx(roc_auc_score(y, a))
    assert result.auc_b == pytest.approx(roc_auc_score(y, b))
    # Сильно лучшая оценка — значимая разница.
    assert result.diff > 0 and result.p < 0.01


def test_delong_agrees_with_bootstrap(data):
    """Аналитическая SE и разброс бутстрэпа должны быть одного порядка."""
    y, a, b = data
    d = stats.delong(y, a, b)
    boot = stats.bootstrap_diff(y, a, b, 2000, 1, 0.95)
    width = (boot.high - boot.low) / (2 * 1.96)
    assert width == pytest.approx(d.se, rel=0.25)
    assert boot.diff == pytest.approx(d.diff)


def test_bootstrap_is_reproducible(data):
    y, a, b = data
    first = stats.bootstrap_diff(y, a, b, 500, 7, 0.95)
    second = stats.bootstrap_diff(y, a, b, 500, 7, 0.95)
    assert first == second


def test_cluster_bootstrap_is_wider_when_clusters_correlate():
    rng = np.random.default_rng(3)
    groups = np.repeat(np.arange(20), 30)
    shift = rng.normal(0, 1.0, 20)[groups]   # общий для кластера сдвиг разницы
    y = rng.random(600) < 0.3
    a = y + rng.normal(0, 1, 600) + shift * y
    b = y + rng.normal(0, 1, 600)
    plain = stats.bootstrap_diff(y, a, b, 1000, 1, 0.95)
    clustered = stats.bootstrap_diff(y, a, b, 1000, 1, 0.95, groups=groups)
    assert clustered.high - clustered.low > plain.high - plain.low


def test_holm():
    assert stats.holm([0.01, 0.04, 0.03]) == pytest.approx([0.03, 0.06, 0.06])
