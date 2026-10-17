import pytest

from structsim.contrast import (
    MIN_SET_SIZE,
    TooSmallSet,
    contrast_z,
    pair_key,
    z_score,
)


def test_pair_key_ignores_order():
    assert pair_key("b", "a") == pair_key("a", "b") == ("a", "b")


def test_pair_key_rejects_self_pair():
    with pytest.raises(ValueError):
        pair_key("a", "a")


def test_z_score_matches_document_example():
    """Разобранный в документе пример: COMBINED 0.617, фон 0.353, σ 0.096.

    Документ печатает +2.74, формула на округлённых до трёх знаков входных
    даёт 2.75: напечатанное значение получено из неокруглённой σ (при
    σ ≈ 0.0964 выходит ровно 2.74). Проверяем арифметику формулы, а не
    типографику разбора.
    """
    assert z_score(0.617, 0.353, 0.096) == pytest.approx(2.75, abs=0.005)
    assert z_score(0.617, 0.353, 0.0964) == pytest.approx(2.74, abs=0.005)


def test_z_score_of_average_pair_is_zero():
    assert z_score(0.3, 0.3, 0.1) == 0.0


def test_z_score_below_background_is_negative():
    assert z_score(0.2, 0.3, 0.1) == pytest.approx(-1.0)


def test_z_score_without_spread_is_zero():
    """σ = 0: все пары похожи одинаково, выделять нечего."""
    assert z_score(0.9, 0.3, 0.0) == 0.0


# --- полный расчёт на наборе из пяти программ -----------------------------
#
# Девять пар по 0.2 и одна пара (p1, p2) = 0.8.
#   среднее = 0.26, дисперсия = 0.0324, σ = 0.18


def build_scores():
    names = ["p1", "p2", "p3", "p4", "p5"]
    scores = {}
    for i, a in enumerate(names):
        for b in names[i + 1 :]:
            scores[pair_key(a, b)] = 0.2
    scores[pair_key("p1", "p2")] = 0.8
    return scores


def test_outlier_pair_stands_out():
    result = contrast_z(build_scores())
    # Фон пары (p1, p2): обе похожи на p3..p5 ровно на 0.2.
    assert result[("p1", "p2")] == pytest.approx((0.8 - 0.2) / 0.18)


def test_ordinary_pair_is_about_zero():
    result = contrast_z(build_scores())
    assert result[("p3", "p4")] == pytest.approx(0.0)


def test_background_excludes_the_partner():
    """В фоне `p1` не учитывается его сходство с `p3` — это и есть пара."""
    result = contrast_z(build_scores())
    # m_p1 по p2, p4, p5 = (0.8 + 0.2 + 0.2) / 3 = 0.4
    # m_p3 по p2, p4, p5 = 0.2;  фон = 0.3
    assert result[("p1", "p3")] == pytest.approx((0.2 - 0.3) / 0.18)


def test_pair_with_high_background_is_not_rewarded():
    """У p1 фон выше, поэтому та же оценка 0.2 даёт отрицательный z."""
    result = contrast_z(build_scores())
    assert result[("p1", "p3")] < result[("p3", "p4")]


def test_small_set_is_rejected():
    names = ["p1", "p2", "p3", "p4"]
    scores = {
        pair_key(a, b): 0.5
        for i, a in enumerate(names)
        for b in names[i + 1 :]
    }
    with pytest.raises(TooSmallSet):
        contrast_z(scores)


def test_minimum_set_size_is_five():
    assert MIN_SET_SIZE == 5
