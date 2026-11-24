import json
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

import pytest

from structsim import treesitter
from structsim.labels import to_program

DATA = Path(__file__).parent / "data"
JAVA = sorted((DATA / "java").glob("*.java")) + [DATA / "langs" / "s.java"]


def semgrep_program(path):
    tree = json.loads(path.with_name(path.name + ".json").read_text(encoding="utf-8"))
    return to_program(tree, language="java", source=path.name)


def lines(program):
    return [None if span is None else span.line for span in program.positions]


@pytest.mark.parametrize("path", JAVA, ids=lambda p: p.name)
def test_same_labels_blocks_and_lines_as_semgrep(path):
    expected = semgrep_program(path)
    actual = treesitter.to_program_java(path.read_bytes(), source_name=path.name)
    assert actual.labels == expected.labels
    assert [(b.name, b.labels) for b in actual.blocks] == [(b.name, b.labels) for b in expected.blocks]
    assert lines(actual) == lines(expected)


def test_syntax_error_is_not_parsed():
    with pytest.raises(treesitter.NotParsed):
        treesitter.to_program_java(b"class A { void f( { }")


def test_numbers_follow_semgrep():
    assert treesitter._int_label("0") == "N0"
    assert treesitter._int_label("0x2") == "N2"
    assert treesitter._int_label("0L") == "NUM"
    assert treesitter._float_label("2.0") == "N2"
    assert treesitter._float_label("0.0d") == "N0"
    assert treesitter._float_label("0.5") == "NUM"


def test_local_declarators_are_reversed_and_fields_are_not():
    source = b"class A { int p = 1, q; void f() { int x = 0, y; } }"
    program = treesitter.to_program_java(source)
    rest = next(b for b in program.blocks if b.name is None)
    function = next(b for b in program.blocks if b.name == "f")
    assert rest.labels == (";", "ID", "=", "N1", ";", "ID")
    assert function.labels == ("FUNC", "P0", ";", "ID", ";", "ID", "=", "N0", "END")


def test_parallel_parsing_gives_the_same_programs():
    sources = [path.read_bytes() for path in JAVA] * 20
    expected = [treesitter.to_program_java(source).labels for source in sources]
    with ThreadPoolExecutor(8) as pool:
        actual = list(pool.map(lambda source: treesitter.to_program_java(source).labels, sources))
    assert actual == expected
