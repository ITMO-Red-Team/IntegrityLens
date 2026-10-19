"""Шаг 2: дерево → последовательность меток.

Дерево обходится сверху вниз и слева направо, каждый узел заменяется на одну
или несколько коротких меток, и всё, что зависит от автора или языка, при этом
пропадает: имена становятся `ID`, числа — `N0`/`N1`/`N2`/`NUM`, вид цикла
теряется, типы и импорты исчезают совсем.

**Порядок инфиксный, а не древесный.** В дереве оператор стоит впереди своих
операндов (`Call(Op Lt, a, b)`), а в последовательности он идёт между ними:
`a < b`. Так задано в разборе метода, и так получается проверяемая на нём
последовательность. То же у обращения по индексу и у присваивания.

Выход — `Program`: вся последовательность, её разбиение на блоки (каждая
функция верхнего уровня плюс один блок на весь остальной код) и параллельный
массив позиций.

Формы узлов взяты из настоящей выгрузки `semgrep-core -json
-full_token_info -dump_ast`, см. `docs/step1.md`.
"""

from __future__ import annotations

from typing import Any

from structsim.model import Block, Program, Span

# Служебные поля выгрузки: к устройству программы отношения не имеют.
NOISE = frozenset(
    {
        "token", "OriginTok", "FakeTok", "str", "line", "column", "bytepos",
        "file", "id_info_id", "id_flags", "id_resolved",
        "id_resolved_alternative", "id_type", "id_svalue", "ref@", "attrs",
        "t_attrs", "pattrs", "tparams", "pinfo",
    }
)

# Типы, импорты и директивы по таблице метода дают «ничего».
SKIPPED = frozenset({"ptype", "frettype", "fkind", "DirectiveStmt", "ImportAs",
                     "ImportFrom", "ImportAll", "Package", "TyN", "TyPointer",
                     "TyArray", "TyApply", "TyFun", "TyVar", "TyAny", "t",
                     # Параметры в метки не идут: их число уходит в `Pn`.
                     "fparams"})

# Знаки операторов — «общий для всех языков знак». Имена слева приходят от
# Semgrep, он их уже привёл к одному виду независимо от языка.
OPERATORS: dict[str, str] = {
    "Plus": "+", "Minus": "-", "Mult": "*", "Div": "/", "Mod": "%",
    "Pow": "**", "Eq": "==", "NotEq": "!=", "PhysEq": "==", "NotPhysEq": "!=",
    "Lt": "<", "LtE": "<=", "Gt": ">", "GtE": ">=",
    "And": "AND", "Or": "OR", "Not": "NOT", "Xor": "XOR",
    "BitAnd": "&", "BitOr": "|", "BitXor": "^", "BitNot": "~",
    "LSL": "<<", "LSR": ">>", "ASR": ">>",
    "Range": "..", "In": "IN", "NotIn": "NOT_IN", "Is": "IS", "NotIs": "NOT_IS",
    "Incr": "+=", "Decr": "-=",
}

# Функция main особая: по разбору метода её тело считается основным кодом
# программы, как код верхнего уровня в Python, и FUNC на неё не ставится.
MAIN_NAMES = frozenset({"main", "Main"})

# Насколько глубоко искать «свой» токен узла. Не до конца поддерева: полный
# поиск сделал бы шаг 2 квадратичным.
#
# Четыре уровня — намеренно мало. Глубже поиск начинает брать токен ребёнка
# вместо своего: `IDX` стал бы указывать на левый операнд вместо `[`, а `=` —
# на левую часть вместо знака. Там, где своё имя лежит глубже (`FUNC`),
# глубина задаётся на месте вызова.
_TOKEN_SEARCH_DEPTH = 4
_NAME_SEARCH_DEPTH = 8


def _fields(node: Any) -> list[tuple[str, Any]]:
    """Значимые пары ключ-значение узла."""
    if not isinstance(node, dict):
        return []
    return [(k, v) for k, v in node.items() if k not in NOISE]


def _own_span(node: Any, depth: int = _TOKEN_SEARCH_DEPTH) -> Span | None:
    """Позиция самого узла: первый `OriginTok` в пределах нескольких уровней.

    Глубина ограничена намеренно. Поиск по всему поддереву сделал бы шаг 2
    квадратичным, а нужен тут именно *свой* токен узла — для `If` это слово
    `if`, для `Id` само имя, и оба лежат рядом. У достроенных узлов
    (`FakeTok`) позиции нет, и это нормально.
    """
    if depth < 0:
        return None
    if isinstance(node, dict):
        token = node.get("token")
        if isinstance(token, dict):
            origin = token.get("OriginTok")
            if isinstance(origin, dict) and "line" in origin:
                return Span(
                    line=origin["line"],
                    column=origin.get("column", 0),
                    text=origin.get("str", ""),
                )
        for _, value in _fields(node):
            found = _own_span(value, depth - 1)
            if found is not None:
                return found
    elif isinstance(node, list):
        for item in node:
            found = _own_span(item, depth - 1)
            if found is not None:
                return found
    return None


def _number_label(value: Any) -> str:
    """Числа: 0, 1 и 2 сохраняются, остальные обезличиваются."""
    if isinstance(value, bool):
        return "TRUE" if value else "FALSE"
    if isinstance(value, int) and value in (0, 1, 2):
        return f"N{value}"
    return "NUM"


class _Walker:
    def __init__(self, self_name: str | None) -> None:
        self.labels: list[str] = []
        self.spans: list[Span | None] = []
        self.self_name = self_name

    def emit(
        self, label: str, node: Any = None, depth: int = _TOKEN_SEARCH_DEPTH
    ) -> None:
        self.labels.append(label)
        self.spans.append(_own_span(node, depth) if node is not None else None)

    # --- узлы ------------------------------------------------------------

    def walk(self, node: Any) -> None:
        if isinstance(node, list):
            for item in node:
                self.walk(item)
            return
        if not isinstance(node, dict):
            return

        for key, value in _fields(node):
            if key in SKIPPED:
                continue
            handler = getattr(self, f"_n_{key}", None)
            if handler is not None:
                handler(value, node)
            else:
                self.walk(value)

    # имена ---------------------------------------------------------------

    def _n_N(self, value: Any, node: Any) -> None:
        self.emit("ID", value)

    def _n_PatId(self, value: Any, node: Any) -> None:
        self.emit("ID", value)

    # литералы ------------------------------------------------------------

    def _n_L(self, value: Any, node: Any) -> None:
        for kind, payload in _fields(value):
            if kind == "Int" or kind == "Float":
                self.emit(_number_label(_unwrap_some(payload)), value)
            elif kind == "String":
                self.emit("STR", value)
            elif kind == "Bool":
                raw = _first_string(payload)
                self.emit("TRUE" if raw == "true" else "FALSE", value)
            elif kind in ("Null", "Unit"):
                self.emit("NULL", value)
            else:
                self.emit("NUM", value)
            return
        self.emit("NUM", value)

    # обращение по индексу ------------------------------------------------

    def _n_ArrayAccess(self, value: Any, node: Any) -> None:
        parts = value if isinstance(value, list) else [value]
        if parts:
            self.walk(parts[0])
        self.emit("IDX", node)
        for rest in parts[1:]:
            self.walk(_unwrap_single_index(rest))

    # присваивание --------------------------------------------------------

    def _n_Assign(self, value: Any, node: Any) -> None:
        parts = value if isinstance(value, list) else [value]
        if parts:
            self.walk(parts[0])
        self.emit("=", node)
        for rest in parts[1:]:
            self.walk(rest)

    def _n_AssignOp(self, value: Any, node: Any) -> None:
        """Составное присваивание: `s += x` → `ID += ID`.

        Оператор приходит отдельно от знака присваивания (`AssignOp(lhs,
        ["Plus"], rhs)`), поэтому метка собирается из знака и `=`.
        """
        parts = value if isinstance(value, list) else [value]
        if parts:
            self.walk(parts[0])
        name = _first_string(parts[1]) if len(parts) > 1 else ""
        self.emit(_compound_sign(name), node)
        for rest in parts[2:]:
            self.walk(rest)

    # вызовы и операторы --------------------------------------------------

    def _n_Call(self, value: Any, node: Any) -> None:
        parts = value if isinstance(value, list) else [value]
        callee = parts[0] if parts else None
        raw_args = parts[1] if len(parts) > 1 else None
        args = _arguments(raw_args)
        sign = _operator_sign(callee)

        # Обращение по индексу, записанное как вызов. В Swift `items[i]`
        # приходит узлом `Call`, а не `ArrayAccess`, и от вызова `items(i)` в
        # дереве не отличается вовсе. Отличает их скобка в исходнике, и она
        # лежит в токене списка аргументов.
        if sign is None and _opening_bracket(raw_args) == "[":
            self.walk(callee)
            self.emit("IDX", node)
            for arg in args:
                self.walk(arg)
            return

        if sign is not None and len(args) == 2:
            # Инфиксная запись: левый операнд, знак, правый.
            self.walk(args[0])
            self.emit(sign, node)
            self.walk(args[1])
            return
        if sign is not None:
            self.emit(sign, node)
            for arg in args:
                self.walk(arg)
            return

        name = _called_name(callee)
        recursive = name is not None and name == self.self_name
        self.emit("RECCALL" if recursive else "CALL", node)
        for arg in args:
            self.walk(arg)

    # условия -------------------------------------------------------------

    def _n_If(self, value: Any, node: Any) -> None:
        self._if_chain(value, node, "IF")

    def _if_chain(self, value: Any, node: Any, head: str) -> None:
        """`IF … END`, затем ветки `ELIF … END` и `ELSE … END`.

        В дереве `elif` — это не отдельный вид узла, а `If`, лежащий в
        else-ветке предыдущего `If`. Поэтому цепочка разбирается рекурсивно, и
        у каждой ветки своё `END`.
        """
        parts = [
            part
            for part in (value if isinstance(value, list) else [value])
            if not _is_only_token(part)
        ]
        condition = parts[0] if parts else None
        body = parts[1] if len(parts) > 1 else None
        otherwise = parts[2] if len(parts) > 2 else None

        self.emit(head, node)
        self.walk(condition)
        self.walk(body)
        self.emit("END", None)

        if otherwise is None:
            return
        inner = _unwrap_option(otherwise)
        nested = _nested_if(inner)
        if nested is not None:
            self._if_chain(nested, inner, "ELIF")
        else:
            self.emit("ELSE", inner)
            self.walk(inner)
            self.emit("END", None)

    def _n_Conditional(self, value: Any, node: Any) -> None:
        self.emit("IF", node)
        self.walk(value)
        self.emit("END", None)

    # циклы ---------------------------------------------------------------

    def _n_For(self, value: Any, node: Any) -> None:
        self.emit("LOOP", node)
        self.walk(value)
        self.emit("END", None)

    def _n_While(self, value: Any, node: Any) -> None:
        self.emit("LOOP", node)
        self.walk(value)
        self.emit("END", None)

    def _n_DoWhile(self, value: Any, node: Any) -> None:
        self.emit("LOOP", node)
        self.walk(value)
        self.emit("END", None)

    def _n_ForEach(self, value: Any, node: Any) -> None:
        # Заголовок for отбрасывается: по разбору метода `for i in range(n)` и
        # `for x in items` после очистки неотличимы. Переменная и источник
        # обхода в последовательность не идут.
        return

    def _n_ForClassic(self, value: Any, node: Any) -> None:
        return

    # переходы ------------------------------------------------------------

    def _n_Return(self, value: Any, node: Any) -> None:
        self.emit("RET", node)
        self.walk(value)

    def _n_Continue(self, value: Any, node: Any) -> None:
        self.emit("CONT", node)

    def _n_Break(self, value: Any, node: Any) -> None:
        self.emit("BRK", node)

    # операторы как единица -----------------------------------------------

    def _n_ExprStmt(self, value: Any, node: Any) -> None:
        """`;` — начало оператора-выражения.

        Если внутри `StmtExpr`, метка не ставится: так Semgrep помечает
        оператор, стоящий в позиции выражения. В Kotlin `if` и `return` —
        выражения, и приходят как `ExprStmt(StmtExpr(If))`, тогда как в Python
        тот же `if` приходит узлом `If` напрямую. Без этого Kotlin получал бы
        лишний `;` перед каждым таким оператором, а своя метка (`IF`, `RET`) у
        него и так есть.
        """
        if _find_key(value, "StmtExpr", depth=2) is None:
            self.emit(";", node)
        self.walk(value)

    def _n_DefStmt(self, value: Any, node: Any) -> None:
        """Объявление. Для переменной — то же, что присваивание.

        `int total = 0;` приходит как `DefStmt(name, VarDef)`, а не как
        `ExprStmt(Assign)`. Без выравнивания `total = 0` в Python давало бы
        `; ID = N0`, а объявление — что-то другое, и программы расходились бы
        на каждой такой строке.

        Имя берётся **здесь**, а не внутри `VarDef`: у самого `VarDef` имени
        нет, оно лежит в `name` этого узла. И форма у него разная — в Java это
        `EN(Id)`, в Rust `PatId`. Если бы `VarDef` выдавал метку сам, а имя
        при этом обходилось как обычный узел, в Rust вышло бы два `ID` на одно
        объявление.

        Тип отбрасывается: типы по таблице дают «ничего».
        """
        vardef = _find_key(value, "VarDef", depth=2)
        if vardef is None:
            # Не объявление переменной (например функция) — обычный обход.
            self.walk(value)
            return
        self.emit(";", node)
        self.emit("ID", node)
        initial = _find_key(vardef, "vinit", depth=2)
        if initial is not None:
            self.emit("=", node)
            self.walk(initial)

    # контейнеры ----------------------------------------------------------

    def _n_Container(self, value: Any, node: Any) -> None:
        parts = value if isinstance(value, list) else [value]
        kind = _first_string(parts[0]) if parts else ""
        self.emit("TUPLE" if kind == "Tuple" else "NEWLIST", node)
        for rest in parts[1:]:
            self.walk(rest)

    def _n_New(self, value: Any, node: Any) -> None:
        self.emit("NEWLIST", node)
        self.walk(value)


def _unwrap_some(node: Any) -> Any:
    """`{"some": X}` → `X`; списки разворачиваются."""
    if isinstance(node, list):
        for item in node:
            got = _unwrap_some(item)
            if got is not None:
                return got
        return None
    if isinstance(node, dict):
        if "some" in node:
            return _unwrap_some(node["some"])
        for _, value in _fields(node):
            return _unwrap_some(value)
        return None
    return node


def _opening_bracket(args_node: Any) -> str:
    """Какой скобкой открыт список аргументов: `(` у вызова, `[` у индекса.

    Нужно там, где дерево различие потеряло: у Swift обращение по индексу и
    вызов функции — один и тот же узел `Call`. Токен при этом настоящий, из
    исходника, поэтому различие берём из него.
    """
    if not isinstance(args_node, list):
        return ""
    for item in args_node:
        if not _is_only_token(item):
            continue
        token = item.get("token") if isinstance(item, dict) else None
        if isinstance(token, dict):
            origin = token.get("OriginTok")
            if isinstance(origin, dict):
                return origin.get("str", "")
    return ""


def _unwrap_single_index(node: Any) -> Any:
    """Индекс из одного элемента — без обёртки-кортежа.

    В языках, где индексатор принимает несколько аргументов (`a[i, j]` в C#),
    Semgrep кладёт индекс в `Container(["Tuple", …])` даже когда он один. У
    Python индекс лежит голым, и без разворачивания `items[i]` давало бы
    `ID IDX TUPLE ID` против `ID IDX ID` — расхождение на каждом обращении к
    массиву.

    Кортеж из нескольких элементов не разворачивается: там он осмыслен,
    `a[i, j]` и `a[i]` — разные действия.
    """
    container = _find_key(node, "Container", depth=3)
    if container is None:
        return node
    parts = container if isinstance(container, list) else [container]
    if not parts or _first_string(parts[0]) != "Tuple":
        return node
    elements = _arguments(parts[1] if len(parts) > 1 else None)
    return elements[0] if len(elements) == 1 else node


def _compound_sign(op_name: str) -> str:
    """Метка составного присваивания по имени оператора.

    `Eq` здесь означает **обычное присваивание**, а не равенство: так Semgrep
    подаёт `:=` в Go. Равенство в позиции составного присваивания не
    встречается, поэтому `Eq` → `=`. Без этого `total := 0` давало бы
    `ID == N0`, то есть сравнение, и Go расходился бы с Python и C++ на каждом
    объявлении.

    Неизвестный оператор даёт `OP=`: если бы знаком по умолчанию было `=`, с
    дописанным `=` вышло бы `==`, и неизвестный оператор замаскировался бы под
    сравнение.
    """
    if op_name in ("Eq", "PhysEq"):
        return "="
    sign = OPERATORS.get(op_name) if op_name else None
    if sign is None:
        return "OP="
    # `Incr`/`Decr` уже содержат знак присваивания.
    return sign if sign.endswith("=") else f"{sign}="


def _unwrap_option(node: Any) -> Any:
    """`{"some": X}` → `X`, но узлом, а не строкой."""
    current = node
    for _ in range(4):
        if isinstance(current, dict) and "some" in current:
            current = current["some"]
        elif isinstance(current, list) and len(current) == 1:
            current = current[0]
        else:
            break
    return current


def _nested_if(node: Any) -> Any | None:
    """Полезная нагрузка `If`, если else-ветка — это снова `If`, то есть elif.

    В дереве elif лежит внутри `Block` из одного оператора, поэтому блок надо
    развернуть. Обратная сторона: `elif cond:` и `else:` с вложенным `if`
    внутри дают **одинаковое** дерево, и различить их нельзя. Считаем такую
    ветку elif: он встречается несравнимо чаще.
    """
    current = node
    for _ in range(6):
        if isinstance(current, dict):
            if "If" in current:
                return current["If"]
            if "Block" in current:
                current = current["Block"]
                continue
            fields = _fields(current)
            if len(fields) == 1:
                current = fields[0][1]
                continue
            return None
        if isinstance(current, list):
            meaningful = [item for item in current if not _is_only_token(item)]
            if len(meaningful) != 1:
                return None
            current = meaningful[0]
            continue
        return None
    return None


def _first_string(node: Any) -> str:
    value = _unwrap_some(node)
    return value if isinstance(value, str) else ""


def _is_only_token(node: Any) -> bool:
    """Узел, в котором нет ничего кроме позиции: токен скобки, запятой и т.п."""
    return isinstance(node, dict) and not _fields(node)


def _arguments(node: Any) -> list[Any]:
    """Аргументы вызова.

    В выгрузке аргументы лежат не отдельным списком, а вперемешку с токенами
    скобок: `[ {token: "("}, [ {Arg}, {Arg} ], {token: ")"} ]`. Токены надо
    выбросить, иначе число аргументов получается неверным — и тогда
    бинарный оператор перестаёт распознаваться как бинарный.
    """
    if node is None:
        return []
    current = node
    while True:
        if _is_only_token(current):
            return []
        if isinstance(current, list):
            meaningful = [item for item in current if not _is_only_token(item)]
            if len(meaningful) == 1:
                current = meaningful[0]
                continue
            return [item for item in meaningful if isinstance(item, (dict, list))]
        return [current]


def _operator_sign(callee: Any) -> str | None:
    """Знак, если вызываемое — оператор (`Special` → `Op`)."""
    if not isinstance(callee, dict):
        return None
    special = callee.get("Special")
    if special is None:
        return None
    for item in special if isinstance(special, list) else [special]:
        if isinstance(item, dict) and "Op" in item:
            return OPERATORS.get(_first_string(item["Op"]), _first_string(item["Op"]))
    return None


def _called_name(callee: Any) -> str | None:
    """Имя вызываемой функции до очистки — только чтобы узнать рекурсию."""
    value = _unwrap_some(callee)
    return value if isinstance(value, str) else None


def _definition_name(node: Any, depth: int = 3) -> str | None:
    """Имя определения. Лежит не на верхнем уровне, а внутри `DefStmt`."""
    if depth < 0:
        return None
    if isinstance(node, dict):
        for key, value in _fields(node):
            if key == "name":
                return _first_string(value)
            found = _definition_name(value, depth - 1)
            if found is not None:
                return found
    elif isinstance(node, list):
        for item in node:
            found = _definition_name(item, depth - 1)
            if found is not None:
                return found
    return None


def _class_members(statement: Any) -> list[Any] | None:
    """Содержимое класса, если это объявление класса.

    В языках, где функция не бывает на верхнем уровне, метод класса и есть
    аналог функции верхнего уровня: `class E { int solve(…) }` в Java должен
    дать такой же блок, как `def solve(…)` в Python, иначе одинаковые по
    устройству программы на этих языках разойдутся по разбиению на блоки.
    """
    classdef = _find_key(statement, "ClassDef", depth=3)
    if classdef is None:
        return None
    body = _find_key(classdef, "cbody", depth=3)
    if body is None:
        return []
    members = []
    _collect_dicts(body, members)
    return members


def _find_key(node: Any, key: str, depth: int) -> Any | None:
    if depth < 0:
        return None
    if isinstance(node, dict):
        if key in node:
            return node[key]
        for _, value in _fields(node):
            found = _find_key(value, key, depth - 1)
            if found is not None:
                return found
    elif isinstance(node, list):
        for item in node:
            found = _find_key(item, key, depth - 1)
            if found is not None:
                return found
    return None


def _collect_dicts(node: Any, out: list[Any], depth: int = 4) -> None:
    """Собирает узлы-операторы из обёрток `F`, списков и `some`."""
    if depth < 0:
        return
    if isinstance(node, dict):
        fields = _fields(node)
        if any(key in ("DefStmt", "FuncDef", "VarDef") for key, _ in fields):
            out.append(node)
            return
        for _, value in fields:
            _collect_dicts(value, out, depth - 1)
    elif isinstance(node, list):
        for item in node:
            _collect_dicts(item, out, depth - 1)


def _top_level_definitions(tree: Any) -> list[tuple[str | None, Any]]:
    """Определения функций и весь остальной код.

    Возвращает список `(имя, узел)`; у куска «остальной код» имя None.
    Функциями считаются определения верхнего уровня и методы классов.
    """
    program = tree.get("Pr") if isinstance(tree, dict) else None
    statements = program if isinstance(program, list) else []
    functions: list[tuple[str | None, Any]] = []
    rest: list[Any] = []

    def take(statement: Any) -> None:
        definition = _find_funcdef(statement)
        name = _definition_name(statement) if definition is not None else None
        if definition is not None and name not in MAIN_NAMES:
            functions.append((name, statement))
        else:
            rest.append(statement)

    for statement in statements:
        members = _class_members(statement)
        if members is None:
            take(statement)
            continue
        # Объявление класса: блоками становятся его методы, остальное
        # содержимое уходит в «остальной код».
        for member in members:
            take(member)

    if rest:
        functions.append((None, rest))
    return functions


def _find_funcdef(node: Any, depth: int = 3) -> Any | None:
    if depth < 0:
        return None
    if isinstance(node, dict):
        if "FuncDef" in node:
            return node["FuncDef"]
        for _, value in _fields(node):
            found = _find_funcdef(value, depth - 1)
            if found is not None:
                return found
    elif isinstance(node, list):
        for item in node:
            found = _find_funcdef(item, depth - 1)
            if found is not None:
                return found
    return None


def _parameter_count(funcdef: Any) -> int:
    if not isinstance(funcdef, dict):
        return 0
    # `fparams` устроен как аргументы вызова: параметры вперемешку с токенами
    # скобок и запятых, поэтому разбираем тем же способом.
    # Параметр приходит в двух видах: `Param` с полем `pname` (Java, C++,
    # Python) и `ParamPattern` с образцом (Rust, где параметр — это шаблон
    # `items: &[i32]`). Считать надо оба, иначе у Rust вышло бы `P0`.
    return sum(
        1
        for item in _arguments(funcdef.get("fparams"))
        if isinstance(item, dict)
        and any(key in item for key in ("Param", "ParamPattern", "ParamRest"))
    )


def to_program(
    tree: dict[str, Any],
    language: str | None = None,
    source: str | None = None,
) -> Program:
    """Дерево шага 1 → `Program` шага 3."""
    blocks: list[Block] = []
    labels: list[str] = []
    spans: list[Span | None] = []

    for name, node in _top_level_definitions(tree):
        walker = _Walker(self_name=name)
        funcdef = _find_funcdef(node) if name is not None else None
        if funcdef is not None:
            # Имя функции лежит глубоко (`DefStmt → name → EN → Id`), поэтому
            # для FUNC ищем токен глубже обычного.
            walker.emit("FUNC", node, depth=_NAME_SEARCH_DEPTH)
            walker.emit(f"P{_parameter_count(funcdef)}", None)
            walker.walk(funcdef)
            walker.emit("END", None)
        else:
            walker.walk(node)

        if not walker.labels:
            continue
        blocks.append(Block(labels=tuple(walker.labels), name=name))
        labels.extend(walker.labels)
        spans.extend(walker.spans)

    return Program(
        labels=tuple(labels),
        blocks=tuple(blocks),
        language=language,
        source=source,
        positions=tuple(spans),
    )
