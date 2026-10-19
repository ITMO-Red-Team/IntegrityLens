"""Сходимость языков: один алгоритм на девяти языках должен дать одни метки.

Это единственная проверка, которая ловит ошибки таблицы шага 2. Они не
падают и не пишут в stderr — они молча снижают оценку, поэтому ни код
возврата, ни число пропущенных файлов их не показывают.

Все расхождения, найденные за счёт этой проверки, перечислены в
`docs/step2.md`. Добавляя язык, добавьте сюда его файл.
"""
import json
from pathlib import Path

import pytest

from structsim.compare import combined
from structsim.labels import to_program
from structsim.semgrep import language_of

LANGS = Path(__file__).parent / "data" / "langs"

# Эталон: тот же алгоритм на Python.
REFERENCE = (
    "FUNC P1 ; ID = N0 LOOP IF ID IDX ID > N0 ; "
    "ID = ID + ID IDX ID END END RET ID END"
)

# Языки, у которых метки обязаны совпасть с эталоном буква в букву.
CONVERGENT = ["s.cpp", "s.cs", "s.go", "s.java", "s.js", "s.kt", "s.py", "s.swift"]


def program(name):
    tree = json.loads((LANGS / f"{name}.json").read_text(encoding="utf-8"))
    return to_program(tree, language=language_of(LANGS / name), source=name)


def test_reference_is_python():
    assert " ".join(program("s.py").labels) == REFERENCE


@pytest.mark.parametrize("name", CONVERGENT)
def test_language_converges(name):
    """Если упало — в шаге 2 расхождение кодировки, а не «язык такой».

    Каждое прошлое падение этой проверки оказывалось одной конструкцией:
    `:=` в Go, объявление в Java, индекс в контейнере у C#, `StmtExpr` у
    Kotlin, подстрочник как вызов у Swift.
    """
    assert " ".join(program(name).labels) == REFERENCE


@pytest.mark.parametrize("name", CONVERGENT)
def test_convergent_language_scores_one(name):
    assert combined(program("s.py"), program(name)) == 1.0


def test_every_fixture_is_covered():
    """Файл в данных без строки в CONVERGENT — незамеченное расхождение."""
    present = {
        path.name
        for path in LANGS.iterdir()
        if path.suffix != ".json"
    }
    untested = present - set(CONVERGENT) - {"s.rs"}
    assert not untested, f"язык без проверки сходимости: {sorted(untested)}"


# --- известное расхождение ------------------------------------------------


def test_rust_differs_only_by_the_implicit_return():
    """Rust возвращает значение последним выражением, без `return`.

    В дереве это `ExprStmt` — то же, чем приходит вызов-без-результата в
    конце функции Python. Различить их деревом нельзя, поэтому расхождение
    остаётся: `; ID` вместо `RET ID`.

    Проверка намеренно фиксирует расхождение, а не обходит его: если Rust
    начнёт совпадать, тест упадёт и его надо будет перенести в CONVERGENT.
    """
    rust = " ".join(program("s.rs").labels)
    assert rust != REFERENCE
    assert rust.endswith("; ID END")
    assert REFERENCE.endswith("RET ID END")
    # Длина та же, и отличается ровно одна метка из двадцати шести: `;`
    # вместо `RET`. Всё остальное уже выровнено.
    assert len(program("s.rs").labels) == len(program("s.py").labels)
    different = sum(
        1
        for a, b in zip(program("s.rs").labels, program("s.py").labels)
        if a != b
    )
    assert different == 1
