"""Сборка конвейера и вывод. Шаги 1–2 не запускаются: Docker тут не нужен."""
import json
from pathlib import Path

import pytest

from structsim.analyze import (
    PairScore,
    Report,
    ShownTile,
    _labels_word,
    format_report,
    format_tiles,
    score_pairs,
    source_files,
)
from structsim.labels import to_program

DATA = Path(__file__).parent / "data"


def program(name):
    tree = json.loads((DATA / f"{name}.json").read_text(encoding="utf-8"))
    return to_program(tree, source=name)


# --- поиск файлов ---------------------------------------------------------


def test_source_files_picks_known_languages_only(tmp_path):
    (tmp_path / "a.py").write_text("x = 1", encoding="utf-8")
    (tmp_path / "b.cpp").write_text("int x;", encoding="utf-8")
    (tmp_path / "notes.txt").write_text("не код", encoding="utf-8")
    (tmp_path / "data.json").write_text("{}", encoding="utf-8")

    found = [p.name for p in source_files(tmp_path)]

    assert found == ["a.py", "b.cpp"]


def test_source_files_is_recursive_and_sorted(tmp_path):
    (tmp_path / "sub").mkdir()
    (tmp_path / "sub" / "z.py").write_text("x = 1", encoding="utf-8")
    (tmp_path / "a.py").write_text("x = 1", encoding="utf-8")

    assert [p.name for p in source_files(tmp_path)] == ["a.py", "z.py"]


# --- шаги 3 и 4 по набору -------------------------------------------------


def test_small_set_has_no_contrast_z():
    programs = {"relax.cpp": program("relax.cpp"), "relax.py": program("relax.py")}
    scores = score_pairs(programs)
    assert len(scores) == 1
    assert scores[0].contrast_z is None


def test_full_set_gets_contrast_z():
    base = program("relax.cpp")
    other = program("rich.py")
    programs = {f"p{i}.py": (base if i % 2 else other) for i in range(5)}
    scores = score_pairs(programs)
    assert len(scores) == 10
    assert all(score.contrast_z is not None for score in scores)


def test_translation_scores_above_unrelated():
    """Один код на двух языках должен обойти чужую программу."""
    cpp, py, rich = program("relax.cpp"), program("relax.py"), program("rich.py")
    programs = {"relax.cpp": cpp, "relax.py": py, "rich.py": rich}
    by_pair = {(s.a, s.b): s.combined for s in score_pairs(programs)}
    assert by_pair[("relax.cpp", "relax.py")] == 1.0
    assert by_pair[("relax.cpp", "rich.py")] < 0.6


# --- отчёт ----------------------------------------------------------------


def report_of(programs, skipped=None):
    return Report(
        programs=programs, scores=score_pairs(programs), skipped=skipped or {}
    )


def test_top_sorts_by_combined():
    programs = {
        "relax.cpp": program("relax.cpp"),
        "relax.py": program("relax.py"),
        "rich.py": program("rich.py"),
    }
    top = report_of(programs).top(3)
    assert [s.combined for s in top] == sorted(
        (s.combined for s in top), reverse=True
    )


def test_report_counts_skipped_files():
    programs = {"relax.py": program("relax.py")}
    text = format_report(report_of(programs, {"bad.py": "не разобрался"}))
    assert "пропущено файлов: 1" in text
    assert "bad.py" in text


def test_report_says_why_contrast_z_is_absent():
    programs = {"relax.cpp": program("relax.cpp"), "relax.py": program("relax.py")}
    text = format_report(report_of(programs))
    assert "contrast-z не считался" in text
    assert "хотя бы 5" in text


# --- тайлы ----------------------------------------------------------------


def test_tiles_carry_line_ranges():
    programs = {"relax.cpp": program("relax.cpp"), "relax.py": program("relax.py")}
    shown = report_of(programs).tiles("relax.cpp", "relax.py", min_length=10)
    assert len(shown) == 1
    tile = shown[0]
    assert tile.length == 24
    assert tile.lines_a == (1, 3)
    assert tile.lines_b == (1, 3)


def test_tiles_text_names_both_files_and_lines():
    programs = {"relax.cpp": program("relax.cpp"), "relax.py": program("relax.py")}
    text = format_tiles(report_of(programs), "relax.cpp", "relax.py", 10)
    assert "relax.cpp строки 1–3" in text
    assert "relax.py строки 1–3" in text


def test_no_tiles_suggests_lowering_the_threshold():
    programs = {"relax.cpp": program("relax.cpp"), "rich.py": program("rich.py")}
    text = format_tiles(report_of(programs), "relax.cpp", "rich.py", 60)
    assert "нет" in text
    assert "--min-tile" in text


def test_shown_tile_without_positions_says_so():
    tile = ShownTile(length=5, labels=("ID",), lines_a=None, lines_b=None)
    assert "строки неизвестны" in tile.describe("a.py", "b.py")


def test_single_line_tile_reads_as_one_line():
    tile = ShownTile(length=5, labels=("ID",), lines_a=(7, 7), lines_b=(2, 2))
    assert "строка 7" in tile.describe("a.py", "b.py")


# --- мелочь, которую видно пользователю -----------------------------------


@pytest.mark.parametrize(
    ("count", "word"),
    [(1, "метка"), (2, "метки"), (4, "метки"), (5, "меток"), (11, "меток"),
     (14, "меток"), (21, "метка"), (22, "метки"), (25, "меток"), (101, "метка")],
)
def test_labels_word_declines(count, word):
    assert _labels_word(count) == word


def test_pair_score_keeps_every_measure():
    score = PairScore(
        a="x", b="y", order=0.5, shingle=0.1, blocks=0.9, combined=0.5
    )
    assert score.contrast_z is None
