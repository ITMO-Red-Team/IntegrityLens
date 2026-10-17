"""Шаг 1. Часть проверок требует Docker и собранного образа.

    uv run pytest                  — без них
    uv run pytest -m docker        — только они
"""
from pathlib import Path

import pytest

from structsim import semgrep
from structsim.labels import to_program

DATA = Path(__file__).parent / "data"

docker = pytest.mark.docker


# --- без Docker -----------------------------------------------------------


@pytest.mark.parametrize(
    ("name", "language"),
    [
        ("a.cpp", "cpp"), ("a.cc", "cpp"), ("a.cxx", "cpp"),
        ("a.py", "python"), ("a.java", "java"), ("a.go", "go"),
        ("a.rs", "rust"), ("a.swift", "swift"), ("a.kt", "kotlin"),
        ("a.cs", "csharp"), ("a.js", "js"),
    ],
)
def test_language_by_extension(name, language):
    assert semgrep.language_of(Path(name)) == language


def test_language_is_case_insensitive():
    assert semgrep.language_of(Path("A.PY")) == "python"


def test_unknown_extension_names_the_known_ones():
    with pytest.raises(semgrep.SemgrepError) as exc:
        semgrep.language_of(Path("notes.txt"))
    assert ".py" in str(exc.value)


def test_nine_languages_are_covered():
    assert set(semgrep.LANGUAGES.values()) == {
        "cpp", "python", "java", "go", "rust", "swift", "kotlin", "csharp", "js"
    }


def test_missing_file_is_reported_before_docker():
    with pytest.raises(semgrep.SemgrepError, match="нет файла"):
        semgrep.dump_ast(Path("нет-такого.py"))


# --- с Docker -------------------------------------------------------------


@docker
def test_dump_ast_gives_positions():
    tree = semgrep.dump_ast(DATA / "relax.cpp")
    raw = str(tree)
    assert "OriginTok" in raw
    assert "line" in raw


@docker
def test_dump_ast_reproduces_the_saved_tree():
    """Сохранённые в tests/data деревья должны совпадать с живой выгрузкой.

    Если упало — значит образ пересобран на другой версии Semgrep, и
    сохранённые данные надо обновить вместе с ожиданиями шага 2.
    """
    import json

    for name in ("relax.cpp", "relax.py", "rich.py"):
        live = semgrep.dump_ast(DATA / name)
        saved = json.loads((DATA / f"{name}.json").read_text(encoding="utf-8"))
        assert live == saved, f"{name}: живая выгрузка расходится с сохранённой"


@docker
def test_unparseable_file_raises_not_parsed(tmp_path):
    bad = tmp_path / "bad.py"
    bad.write_text("def f(:\n  @@@\n", encoding="utf-8")
    with pytest.raises(semgrep.NotParsed):
        semgrep.dump_ast(bad)


@docker
def test_cpp17_structured_bindings_are_not_parsed(tmp_path):
    """Известное ограничение Semgrep, проверенное на нашем образе."""
    source = tmp_path / "bindings.cpp"
    source.write_text(
        "#include <vector>\n"
        "void f(std::vector<std::pair<int,int>> p) {\n"
        "    for (auto [a, b] : p) { int c = a + b; }\n"
        "}\n",
        encoding="utf-8",
    )
    with pytest.raises(semgrep.NotParsed):
        semgrep.dump_ast(source)


@docker
def test_batch_skips_the_broken_and_keeps_the_rest(tmp_path):
    good = tmp_path / "good.py"
    good.write_text("def f(x):\n    return x + 1\n", encoding="utf-8")
    bad = tmp_path / "bad.py"
    bad.write_text("def f(:\n  @@@\n", encoding="utf-8")

    trees, skipped = semgrep.dump_many([good, bad])

    assert list(trees) == [good]
    assert list(skipped) == [bad]


@docker
def test_cpp_and_python_converge_end_to_end():
    """Сквозная проверка шагов 1–2 на живом Semgrep."""
    cpp = to_program(semgrep.dump_ast(DATA / "relax.cpp"), "cpp")
    python = to_program(semgrep.dump_ast(DATA / "relax.py"), "python")
    assert cpp.labels == python.labels
