"""Шаг 2 на настоящих выгрузках semgrep-core (tests/data, без Docker)."""
import json
from pathlib import Path

import pytest

from structsim.labels import (
    OPERATORS,
    _compound_sign,
    _number_label,
    to_program,
)

DATA = Path(__file__).parent / "data"

# Ожидаемая последовательность для фрагмента из разбора метода. Это главная
# проверка шага 2: числа и метки взяты из документа, а не из нашего вывода.
RELAX = (
    "FUNC P4 IF ID IDX ID + ID < ID IDX ID ; "
    "ID IDX ID = ID IDX ID + ID END END"
)


def load(name):
    return json.loads((DATA / name).read_text(encoding="utf-8"))


def program(name, language):
    return to_program(load(f"{name}.json"), language=language, source=name)


# --- главная проверка: совпадение с разбором метода -----------------------


@pytest.mark.parametrize(
    ("name", "language"), [("relax.cpp", "cpp"), ("relax.py", "python")]
)
def test_matches_the_documented_sequence(name, language):
    assert " ".join(program(name, language).labels) == RELAX


def test_cpp_and_python_give_the_same_sequence():
    """Главное свойство метода: один код на двух языках — одна
    последовательность. У C++ в дереве есть типы (`int`, `int*`, `void`),
    которых у Python нет, но типы по таблице дают «ничего»."""
    cpp = program("relax.cpp", "cpp")
    python = program("relax.py", "python")
    assert cpp.labels == python.labels
    assert len(cpp.labels) == 24


# --- блоки ----------------------------------------------------------------


def test_top_level_function_becomes_a_named_block():
    cpp = program("relax.cpp", "cpp")
    assert [(b.name, len(b)) for b in cpp.blocks] == [("relax", 24)]


def test_blocks_cover_the_whole_sequence():
    rich = program("rich.py", "python")
    joined = tuple(label for block in rich.blocks for label in block.labels)
    assert joined == rich.labels


# --- позиции --------------------------------------------------------------


def test_positions_are_parallel_to_labels():
    cpp = program("relax.cpp", "cpp")
    assert len(cpp.positions) == len(cpp.labels)


def test_label_points_at_what_produced_it():
    cpp = program("relax.cpp", "cpp")
    by_label = dict(zip(cpp.labels, cpp.positions))
    assert by_label["FUNC"].line == 1
    assert by_label["FUNC"].text == "relax"
    assert by_label["IF"].line == 2
    assert by_label["IF"].text == "if"
    # IDX должен указывать на скобку, а не на левый операнд.
    assert by_label["IDX"].text == "["


def test_synthesised_labels_have_no_position():
    """`P4` и `END` в исходнике не стоят нигде — позиции у них нет."""
    cpp = program("relax.cpp", "cpp")
    for label, span in zip(cpp.labels, cpp.positions):
        if label in ("P4", "END"):
            assert span is None


def test_span_of_gives_line_range():
    cpp = program("relax.cpp", "cpp")
    assert cpp.span_of(0, len(cpp.labels)) == (1, 3)


def test_span_of_without_positions_is_none():
    cpp = program("relax.cpp", "cpp")
    # Отрезок из одного END — позиций в нём нет.
    end = len(cpp.labels) - 1
    assert cpp.span_of(end, 1) is None


# --- таблица «узел → метки» ------------------------------------------------


def test_numbers_keep_only_zero_one_two():
    assert _number_label(0) == "N0"
    assert _number_label(1) == "N1"
    assert _number_label(2) == "N2"
    assert _number_label(3) == "NUM"
    assert _number_label(100) == "NUM"


def test_compound_assignment_signs():
    assert _compound_sign("Plus") == "+="
    assert _compound_sign("Minus") == "-="
    assert _compound_sign("Mult") == "*="
    # Incr уже содержит знак присваивания — удваивать нельзя.
    assert _compound_sign("Incr") == "+="


def test_eq_in_assignment_position_is_plain_assignment():
    """Так Semgrep подаёт `:=` в Go. Равенства в этой позиции не бывает."""
    assert _compound_sign("Eq") == "="


def test_go_short_declaration_matches_python_assignment():
    """`total := 0` в Go и `total = 0` в Python дают одни и те же метки.

    Регрессия: `Eq` в позиции присваивания давал `==`, и Go расходился с
    Python на каждом объявлении. Общую сходимость языков проверяет
    `test_convergence.py`; здесь зафиксирована именно эта причина.
    """
    go = program("langs/s.go", "go")
    python = program("langs/s.py", "python")
    assert go.labels == python.labels
    assert "; ID = N0" in " ".join(go.labels)
    assert "==" not in go.labels


def test_unknown_compound_operator_does_not_become_equality():
    """Неизвестный оператор не должен замаскироваться под `==`."""
    assert _compound_sign("ЧтоТоНовое") == "OP="
    assert _compound_sign("") == "OP="


def test_operators_are_language_independent_signs():
    assert OPERATORS["Plus"] == "+"
    assert OPERATORS["Lt"] == "<"
    # `&&` и `and` оба приходят как And и становятся одной меткой.
    assert OPERATORS["And"] == "AND"


@pytest.mark.parametrize(
    ("label", "почему"),
    [
        ("LOOP", "for и while оба дают LOOP"),
        ("ELIF", "ветка elif"),
        ("ELSE", "ветка else"),
        ("CONT", "continue"),
        ("BRK", "break"),
        ("RET", "return"),
        ("RECCALL", "вызов самой себя"),
        ("TUPLE", "кортеж (s, flag)"),
        ("NEWLIST", "новый список [s, 0]"),
        ("STR", "строка \"done\""),
        ("FALSE", "логическое False"),
        ("+=", "s += items[i]"),
        ("N0", "число 0 сохраняется"),
        ("N1", "число 1 сохраняется"),
        ("N2", "число 2 сохраняется"),
        ("NUM", "число 100 обезличивается"),
        ("AND", "and"),
    ],
)
def test_rich_file_covers_the_table(label, почему):
    assert label in program("rich.py", "python").labels, почему


@pytest.mark.parametrize(
    ("fragment", "почему"),
    [
        ("; ID = NULL", "None даёт NULL"),
        ("; ID = TRUE", "True даёт TRUE"),
        ("; ID = CALL ID ID", "min(a, b) — вызов с двумя аргументами"),
        ("; ID = - ID", "унарный оператор пишется знаком вперёд"),
        ("; ID = NEWLIST", "словарь и множество — новый объект"),
    ],
)
def test_check_file_matches_the_table(fragment, почему):
    labels = " ".join(program("check.py", "python").labels)
    assert fragment in labels, почему


def test_loop_header_is_dropped_but_while_condition_kept():
    """`for i in range(len(items))` не оставляет следа от заголовка, а условие
    `while s > 100` сохраняется."""
    labels = program("rich.py", "python").labels
    joined = " ".join(labels)
    # Заголовок for содержал два вызова (range и len) — их в метках нет.
    assert "LOOP IF" in joined          # сразу после LOOP идёт тело
    assert "LOOP ID > NUM" in joined    # а у while условие на месте


def test_each_branch_closes_with_its_own_end():
    labels = program("rich.py", "python").labels
    assert labels.count("END") == (
        labels.count("IF")
        + labels.count("ELIF")
        + labels.count("ELSE")
        + labels.count("LOOP")
        + labels.count("FUNC")
    )


def test_parameters_are_counted_not_listed():
    """Параметры уходят в `Pn`, а именами в последовательность не попадают."""
    cpp = program("relax.cpp", "cpp")
    assert cpp.labels[0:2] == ("FUNC", "P4")
    # Четыре параметра не дали четырёх ID: сразу после P4 идёт тело.
    assert cpp.labels[2] == "IF"
