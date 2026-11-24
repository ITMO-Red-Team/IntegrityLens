from __future__ import annotations

import re
import threading
from functools import lru_cache

from structsim.labels import MAIN_NAMES
from structsim.model import Block, Program, Span

BINARY = {
    "+": "+", "-": "-", "*": "*", "/": "/", "%": "%",
    "==": "==", "!=": "!=", "<": "<", "<=": "<=", ">": ">", ">=": ">=",
    "&&": "AND", "||": "OR", "&": "&", "|": "|", "^": "^",
    "<<": "<<", ">>": ">>", ">>>": ">>",
}
UNARY = {"-": "-", "+": "+", "!": "NOT", "~": "~"}
COMPOUND = {
    "=": "=", "+=": "+=", "-=": "-=", "*=": "*=", "/=": "/=", "%=": "%=",
    "&=": "&=", "|=": "|=", "^=": "^=", "<<=": "<<=", ">>=": ">>=", ">>>=": ">>=",
}
TYPE_DECLARATIONS = frozenset({
    "class_declaration", "interface_declaration", "enum_declaration", "record_declaration",
    "annotation_type_declaration",
})
SKIPPED = frozenset({
    "modifiers", "type_identifier", "integral_type", "floating_point_type", "boolean_type", "void_type",
    "generic_type", "array_type", "scoped_type_identifier", "type_arguments", "type_parameters",
    "dimensions", "annotation", "marker_annotation", "formal_parameters", "superclass",
    "super_interfaces", "extends_interfaces", "throws", "line_comment", "block_comment",
    "catch_formal_parameter", "inferred_parameters", "type_list", "permits", "import_declaration",
    "package_declaration",
})
_OCAML_INT = re.compile(r"[0-9][0-9_]*|0[xX][0-9a-fA-F_]+|0[oO][0-7_]+|0[bB][01_]+")


class NotParsed(ValueError):
    pass


_LOCAL = threading.local()


@lru_cache(maxsize=1)
def _language():
    import tree_sitter_java
    from tree_sitter import Language

    return Language(tree_sitter_java.language())


def _parser():
    parser = getattr(_LOCAL, "parser", None)
    if parser is None:
        from tree_sitter import Parser

        parser = _LOCAL.parser = Parser(_language())
    return parser


def _int_label(text: str) -> str:
    if not _OCAML_INT.fullmatch(text):
        return "NUM"
    value = int(text.replace("_", ""), 0) if text[:2].lower() in ("0x", "0o", "0b") else int(text.replace("_", ""))
    if value >= 1 << 62:
        return "NUM"
    return f"N{value}" if value in (0, 1, 2) else "NUM"


def _float_label(text: str) -> str:
    if text[-1:] in "fFdD" and not text.lower().startswith("0x"):
        text = text[:-1]
    try:
        value = float(text) if not text.lower().startswith("0x") else float.fromhex(text)
    except ValueError:
        return "NUM"
    return f"N{int(value)}" if value in (0.0, 1.0, 2.0) else "NUM"


def _line(node) -> int:
    return node.start_point[0] + 1


def _named(node):
    return [child for child in node.named_children if child.type not in ("line_comment", "block_comment")]


def _text(node) -> str:
    return node.text.decode("utf-8", "replace")


def _is_name(node) -> bool:
    if node.type in ("identifier", "this", "super"):
        return True
    if node.type == "field_access":
        obj = node.child_by_field_name("object")
        return obj is not None and _is_name(obj)
    return False


def _leftmost_name(node) -> str | None:
    while node is not None:
        if node.type == "identifier":
            return _text(node)
        if node.type in ("this", "super"):
            return None
        if node.type == "field_access":
            node = node.child_by_field_name("object")
        elif node.type == "method_invocation":
            obj = node.child_by_field_name("object")
            node = obj if obj is not None else node.child_by_field_name("name")
        else:
            return None
    return None


class _Walker:
    def __init__(self, self_name: str | None) -> None:
        self.labels: list[str] = []
        self.spans: list[Span | None] = []
        self.self_name = self_name

    def emit(self, label: str, line: int | None) -> None:
        self.labels.append(label)
        self.spans.append(None if line is None else Span(line=line, column=0, text=""))

    def walk(self, node) -> None:
        if node is None or node.type in SKIPPED:
            return
        handler = getattr(self, f"_n_{node.type}", None)
        if handler is not None:
            handler(node)
            return
        for child in _named(node):
            self.walk(child)

    def walk_all(self, nodes) -> None:
        for node in nodes:
            self.walk(node)

    def _n_identifier(self, node) -> None:
        self.emit("ID", _line(node))

    def _n_this(self, node) -> None:
        self.emit("ID", _line(node))

    def _n_super(self, node) -> None:
        return

    def _n_field_access(self, node) -> None:
        if _is_name(node):
            self.emit("ID", _near(node.child_by_field_name("object")) or _dot_line(node))
            return
        self.walk(node.child_by_field_name("object"))

    def _n_class_literal(self, node) -> None:
        return

    def _n_method_reference(self, node) -> None:
        target = _named(node)[0] if _named(node) else None
        if target is not None and target.type in ("array_type", "integral_type", "floating_point_type",
                                                  "boolean_type", "generic_type"):
            return
        self.emit("ID", _line(node))

    def _literal(self, label: str, node) -> None:
        self.emit(label, _line(node))

    def _n_decimal_integer_literal(self, node) -> None:
        self._literal(_int_label(_text(node)), node)

    _n_hex_integer_literal = _n_decimal_integer_literal
    _n_octal_integer_literal = _n_decimal_integer_literal
    _n_binary_integer_literal = _n_decimal_integer_literal

    def _n_decimal_floating_point_literal(self, node) -> None:
        self._literal(_float_label(_text(node)), node)

    _n_hex_floating_point_literal = _n_decimal_floating_point_literal

    def _n_character_literal(self, node) -> None:
        self._literal("NUM", node)

    def _n_string_literal(self, node) -> None:
        self._literal("STR", node)

    _n_text_block = _n_string_literal

    def _n_true(self, node) -> None:
        self._literal("TRUE", node)

    def _n_false(self, node) -> None:
        self._literal("FALSE", node)

    def _n_null_literal(self, node) -> None:
        self._literal("NULL", node)

    def _n_array_access(self, node) -> None:
        self.walk(node.child_by_field_name("array"))
        self.emit("IDX", _line(node))
        self.walk(node.child_by_field_name("index"))

    def _n_assignment_expression(self, node) -> None:
        left = node.child_by_field_name("left")
        self.walk(left)
        operator = _text(node.child_by_field_name("operator"))
        line = _line(node.child_by_field_name("operator"))
        if operator == "=":
            line = _near(left) or line
        self.emit(COMPOUND.get(operator, "OP="), line)
        self.walk(node.child_by_field_name("right"))

    def _n_binary_expression(self, node) -> None:
        operator = node.child_by_field_name("operator")
        self.walk(node.child_by_field_name("left"))
        self.emit(BINARY.get(_text(operator), _text(operator)), _line(operator))
        self.walk(node.child_by_field_name("right"))

    def _n_unary_expression(self, node) -> None:
        operator = node.child_by_field_name("operator")
        self.emit(UNARY.get(_text(operator), _text(operator)), _line(operator))
        self.walk(node.child_by_field_name("operand"))

    def _n_update_expression(self, node) -> None:
        operator = next((c for c in node.children if c.type in ("++", "--")), node)
        self.emit("CALL", _line(operator))
        self.walk_all(_named(node))

    def _n_instanceof_expression(self, node) -> None:
        self.emit("CALL", None)
        self.walk(node.child_by_field_name("left"))

    def _n_ternary_expression(self, node) -> None:
        parts = [node.child_by_field_name(f) for f in ("condition", "consequence", "alternative")]
        self.emit("IF", next((line for line in map(_near, parts) if line), None))
        self.walk(node.child_by_field_name("condition"))
        self.walk(node.child_by_field_name("consequence"))
        self.walk(node.child_by_field_name("alternative"))
        self.emit("END", None)

    def _n_cast_expression(self, node) -> None:
        self.walk(node.child_by_field_name("value"))

    def _n_method_invocation(self, node) -> None:
        obj = node.child_by_field_name("object")
        name = _leftmost_name(obj) if obj is not None else _text(node.child_by_field_name("name"))
        recursive = name is not None and name == self.self_name
        self.emit("RECCALL" if recursive else "CALL", _call_line(node))
        self.walk(node.child_by_field_name("arguments"))

    def _n_explicit_constructor_invocation(self, node) -> None:
        self.emit(";", _line(node))
        self.emit("CALL", _line(node))
        self.walk(node.child_by_field_name("arguments"))

    def _n_object_creation_expression(self, node) -> None:
        body = next((c for c in _named(node) if c.type == "class_body"), None)
        self.emit("CALL" if body is not None else "NEWLIST", _line(node))
        self.walk(node.child_by_field_name("arguments"))

    def _n_array_creation_expression(self, node) -> None:
        self.emit("NEWLIST", _line(node))
        for child in _named(node):
            if child.type == "dimensions_expr":
                self.walk_all(_named(child))
            elif child.type == "array_initializer":
                self.walk(child)

    def _n_array_initializer(self, node) -> None:
        self.emit("NEWLIST", _line(node))
        self.walk_all(_named(node))

    def _n_lambda_expression(self, node) -> None:
        body = node.child_by_field_name("body")
        if body is None:
            return
        if body.type == "block":
            self.walk(body)
        else:
            self.emit(";", _near(body))
            self.walk(body)

    def _n_expression_statement(self, node) -> None:
        self.emit(";", _statement_line(node))
        self.walk_all(_named(node))

    def _declarators(self, node, reverse: bool) -> None:
        declarators = [c for c in _named(node) if c.type == "variable_declarator"]
        for declarator in reversed(declarators) if reverse else declarators:
            self.emit(";", None)
            self.emit("ID", None)
            value = declarator.child_by_field_name("value")
            if value is not None:
                self.emit("=", None)
                self.walk(value)

    def _n_local_variable_declaration(self, node) -> None:
        self._declarators(node, reverse=True)

    def _n_field_declaration(self, node) -> None:
        self._declarators(node, reverse=False)

    _n_constant_declaration = _n_field_declaration

    def _n_resource(self, node) -> None:
        value = node.child_by_field_name("value")
        if value is None:
            self.walk_all(_named(node))
            return
        self.emit(";", None)
        self.emit("ID", None)
        self.emit("=", None)
        self.walk(value)

    def _n_if_statement(self, node) -> None:
        self._if_chain(node, "IF")

    def _if_chain(self, node, head: str, line: int | None = None) -> None:
        self.emit(head, _line(node) if line is None else line)
        self.walk(node.child_by_field_name("condition"))
        self.walk(node.child_by_field_name("consequence"))
        self.emit("END", None)
        otherwise = node.child_by_field_name("alternative")
        if otherwise is None:
            return
        nested = _nested_if(otherwise)
        if nested is not None:
            self._if_chain(nested, "ELIF", _line(otherwise))
        else:
            self.emit("ELSE", _line(otherwise))
            self.walk(otherwise)
            self.emit("END", None)

    def _loop(self, node, *parts) -> None:
        self.emit("LOOP", _line(node))
        for part in parts:
            self.walk(part)
        self.emit("END", None)

    def _n_while_statement(self, node) -> None:
        self._loop(node, node.child_by_field_name("condition"), node.child_by_field_name("body"))

    def _n_do_statement(self, node) -> None:
        self._loop(node, node.child_by_field_name("body"), node.child_by_field_name("condition"))

    def _n_for_statement(self, node) -> None:
        init = node.child_by_field_name("init")
        update = node.child_by_field_name("update")
        condition = node.child_by_field_name("condition")
        header = condition if init is None and update is None else None
        self._loop(node, header, node.child_by_field_name("body"))

    def _n_enhanced_for_statement(self, node) -> None:
        self._loop(node, node.child_by_field_name("body"))

    def _n_return_statement(self, node) -> None:
        self.emit("RET", _line(node))
        self.walk_all(_named(node))

    def _n_break_statement(self, node) -> None:
        self.emit("BRK", _line(node))

    def _n_continue_statement(self, node) -> None:
        self.emit("CONT", _line(node))

    def _n_labeled_statement(self, node) -> None:
        self.walk_all(c for c in _named(node) if c.type != "identifier")

    def _n_switch_label(self, node) -> None:
        for child in _named(node):
            if child.type == "guard":
                self.walk_all(_named(child))
            else:
                self._pattern(child)

    def _pattern(self, node) -> None:
        if node.type == "type_pattern":
            name = next((c for c in _named(node) if c.type == "identifier"), None)
            if name is not None:
                self.emit("ID", _line(name))
            return
        if node.type == "record_pattern_component":
            names = [c for c in _named(node) if c.type == "identifier"]
            if names:
                self.emit("ID", _line(names[-1]))
            for child in _named(node):
                if child.type == "record_pattern":
                    self._pattern(child)
            return
        if node.type in ("pattern", "record_pattern", "record_pattern_body"):
            for child in _named(node):
                if child.type != "identifier" or node.type != "record_pattern":
                    self._pattern(child)

    def _n_yield_statement(self, node) -> None:
        self.emit("RET", _line(node))
        self.walk_all(_named(node))

    def _n_switch_rule(self, node) -> None:
        for child in _named(node):
            if child.type == "switch_label":
                self._n_switch_label(child)
                continue
            if child.type == "expression_statement" or child.type in ("block", "throw_statement"):
                self.walk(child)
            else:
                self.emit(";", _line(child))
                self.walk(child)

    def _n_catch_clause(self, node) -> None:
        self.walk(node.child_by_field_name("body"))

    def _n_class_declaration(self, node) -> None:
        self.walk(node.child_by_field_name("body"))

    _n_interface_declaration = _n_class_declaration
    _n_enum_declaration = _n_class_declaration
    _n_record_declaration = _n_class_declaration
    _n_annotation_type_declaration = _n_class_declaration

    def _n_method_declaration(self, node) -> None:
        self.walk(node.child_by_field_name("body"))

    _n_constructor_declaration = _n_method_declaration

    def _n_static_initializer(self, node) -> None:
        self.walk_all(_named(node))

    def _n_enum_constant(self, node) -> None:
        self.walk(node.child_by_field_name("arguments"))
        self.walk(node.child_by_field_name("body"))


def _call_line(node) -> int:
    if node.child_by_field_name("object") is not None:
        name = node.child_by_field_name("name")
        dot = name.prev_sibling if name is not None else None
        while dot is not None and dot.type != ".":
            dot = dot.prev_sibling
        if dot is not None:
            return _line(dot)
    arguments = node.child_by_field_name("arguments")
    return _line(arguments) if arguments is not None else _line(node)


def _dot_line(node) -> int:
    dot = next((c for c in node.children if c.type == "."), None)
    return _line(dot if dot is not None else node)


def _near(node) -> int | None:
    while node is not None and node.type == "parenthesized_expression":
        inner = _named(node)
        node = inner[0] if inner else None
    if node is None:
        return None
    if node.type == "field_access":
        return _dot_line(node)
    if node.type in ("object_creation_expression", "cast_expression", "this", "array_creation_expression", "null_literal"):
        return _line(node)
    if node.type == "assignment_expression":
        operator = node.child_by_field_name("operator")
        if _text(operator) == "=":
            return _line(operator)
    return None


def _statement_line(node) -> int:
    expression = _named(node)[0] if _named(node) else None
    line = _near(expression) if expression is not None and expression.type != "field_access" else None
    if line is not None:
        return line
    last = node.children[-1] if node.children else node
    return _line(last)


def _nested_if(node):
    if node.type == "if_statement":
        return node
    if node.type == "block":
        inner = _named(node)
        if len(inner) == 1 and inner[0].type == "if_statement":
            return inner[0]
    return None


def _parameter_count(node) -> int:
    params = node.child_by_field_name("parameters")
    if params is None:
        return 0
    return sum(1 for c in _named(params) if c.type in ("formal_parameter", "spread_parameter"))


def _members(declaration):
    body = declaration.child_by_field_name("body")
    if body is None:
        return []
    members = []
    for child in _named(body):
        if child.type == "enum_body_declarations":
            members.extend(_named(child))
        else:
            members.append(child)
    return members


MEMBER_KINDS = frozenset({
    "field_declaration", "method_declaration", "constructor_declaration", "constant_declaration",
    "enum_constant", "compact_constructor_declaration", *TYPE_DECLARATIONS,
})


def to_program_java(source: bytes, source_name: str | None = None, strict: bool = True) -> Program:
    tree = _parser().parse(source)
    if strict and tree.root_node.has_error:
        raise NotParsed("Файл не удалось разобрать")
    functions = []
    rest = []
    for statement in _named(tree.root_node):
        if statement.type in TYPE_DECLARATIONS:
            for member in _members(statement):
                if member.type not in MEMBER_KINDS:
                    continue
                if member.type in ("method_declaration", "constructor_declaration", "compact_constructor_declaration"):
                    name = _text(member.child_by_field_name("name"))
                    if name not in MAIN_NAMES:
                        functions.append((name, member))
                        continue
                rest.append(member)
        else:
            rest.append(statement)

    blocks: list[Block] = []
    labels: list[str] = []
    spans: list[Span | None] = []
    parts = [(name, [node]) for name, node in functions]
    if rest:
        parts.append((None, rest))
    for name, nodes in parts:
        walker = _Walker(self_name=name)
        if name is not None:
            node = nodes[0]
            walker.emit("FUNC", _line(node.child_by_field_name("name")))
            walker.emit(f"P{_parameter_count(node)}", None)
            walker.walk(node.child_by_field_name("body"))
            walker.emit("END", None)
        else:
            for node in nodes:
                walker.walk(node)
        if not walker.labels:
            continue
        blocks.append(Block(labels=tuple(walker.labels), name=name))
        labels.extend(walker.labels)
        spans.extend(walker.spans)
    return Program(
        labels=tuple(labels),
        blocks=tuple(blocks),
        language="java",
        source=source_name,
        positions=tuple(spans),
    )


def java_program(path) -> Program | None:
    try:
        return to_program_java(path.read_bytes(), source_name=path.name)
    except (ImportError, NotParsed):
        return None
