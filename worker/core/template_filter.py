"""Шаблонные алгоритмы: то, что при сравнении не учитывается вообще.

Бинарный поиск или Дейкстра у двух студентов похожи не потому, что один списал у другого.
Такие функции стираются из копии исходника до запуска метода, поэтому и JPlag, и structsim
видят программу так, будто их в ней не было: они не входят ни в оценку, ни в совпавшие куски.
Строки при этом не сдвигаются. Набор — файлы в `templates/`, замеры — в worker/bench/README.md.
"""
import hashlib
import re
from collections import Counter, defaultdict
from difflib import SequenceMatcher
from pathlib import Path

CODE = Path(__file__).parent / "templates"
# Пороги подобраны на обучающей части ConPlag, см. worker/bench/README.md. Короткая функция
# становится похожей на шаблон от пары токенов, а отличается от него как раз ими.
SIMILARITY = 0.7
SHORT_SIMILARITY = 0.85
SHORT_TOKENS = 60
# Сравнение дорого, поэтому сначала отбор: общих кусков формы должно быть хотя бы столько.
GRAM = 3
CANDIDATE_SIMILARITY = 0.4

# Незакрытые комментарий и строка забирают всё до конца файла и строки. Иначе разбор начинался бы
# заново с каждого следующего `/*` и на файле из одних таких скобок занимал бы квадратичное время.
_TOKEN = re.compile(rb"""
    (?P<space> \xef\xbb\xbf | \s+ | //[^\n]* | /\*(?:.*?\*/|.*\Z))
  | (?P<text> \"\"\"(?:.*?\"\"\"|.*\Z)
            | "[^"\\\n]*(?:\\.[^"\\\n]*)*"?
            | '[^'\\\n]*(?:\\.[^'\\\n]*)*'?)
  | (?P<number> \.?[0-9][\w.]*)
  | (?P<word> [A-Za-z_$\x80-\xff][\w$\x80-\xff]*)
  | (?P<mark> .)
""", re.DOTALL | re.VERBOSE)
_MODIFIERS = frozenset((b"public", b"protected", b"private", b"static", b"final", b"abstract", b"synchronized",
                        b"native", b"transient", b"volatile", b"strictfp", b"default"))
_PRIMITIVES = frozenset((b"int", b"long", b"short", b"byte", b"char", b"float", b"double", b"boolean", b"void"))
_KEYWORDS = frozenset((b"for", b"while", b"do", b"if", b"else", b"switch", b"case", b"break", b"continue", b"return",
                       b"new", b"try", b"catch", b"finally", b"throw", b"throws", b"this", b"super", b"null", b"true",
                       b"false", b"instanceof", b"class", b"interface", b"enum", b"extends", b"implements"))
_DECLARES_TYPE = frozenset((b"class", b"interface", b"enum"))
# Оформление: `if (x) y;` и `if (x) { y; }` — один и тот же код.
_LAYOUT = frozenset((b"{", b"}", b";", b","))
_BLANK = bytes(byte if byte in b"\r\n" else 0x20 for byte in range(256))


def _tokens(source):
    """Токены без пробелов и комментариев: `(текст, вид, начало, конец)`."""
    return [(match.group(), match.lastgroup, match.start(), match.end())
            for match in _TOKEN.finditer(source) if match.lastgroup != "space"]


def _closing(tokens, opening):
    """Индекс скобки, закрывающей `{` в `opening`. None, если скобки не сходятся."""
    depth = 0
    for index in range(opening, len(tokens)):
        text, kind = tokens[index][:2]
        if kind == "mark":
            if text == b"{":
                depth += 1
            elif text == b"}":
                depth -= 1
                if depth == 0:
                    return index
    return None


def _declares_record(tokens, index):
    """`record` — слово контекстное: объявление — это `record Имя(` или `record Имя<`."""
    return (tokens[index][0] == b"record" and index + 2 < len(tokens) and tokens[index + 1][1] == "word"
            and tokens[index + 2][0] in (b"(", b"<"))


def _methods(tokens):
    """Методы и конструкторы как `(первый токен, последний токен)`, на любой вложенности классов.

    Настоящий разбор здесь не нужен: ошибка стоит одного не найденного метода, потому что
    стереть можно только то, что целиком лежит между своими скобками.
    """
    found = []
    start = index = parens = 0
    declared = None
    assigns = throws = False
    # По элементу на открытый тип: True, пока идут константы перечисления. Константа с телом
    # выглядит как конструктор, но это не метод: вместе с ней стёрлись бы и соседние константы.
    constants = []
    while index < len(tokens):
        text, kind, _, _ = tokens[index]
        next_member = False
        if kind == "word":
            # `Foo.class` — это не объявление.
            if declared is None and (text in _DECLARES_TYPE and tokens[index - 1][0] != b"."
                                     or _declares_record(tokens, index)):
                declared = text
            throws = throws or text == b"throws"
        elif kind == "mark":
            if text == b"(":
                parens += 1
            elif text == b")":
                parens -= 1
            elif text == b"=":
                assigns = assigns or not parens
            elif text == b";":
                next_member = True
                if constants and not parens:
                    constants[-1] = False
            elif text == b"}":
                next_member = True
                if constants:
                    constants.pop()
            elif text == b"{":
                if declared and not parens:
                    next_member = True
                    constants.append(declared == b"enum")
                else:
                    closing = _closing(tokens, index)
                    if closing is None:
                        return found
                    # Инициализатор поля и аргумент аннотации: само объявление идёт дальше.
                    if not assigns and not parens:
                        next_member = True
                        is_constant = constants and constants[-1]
                        if (throws or tokens[index - 1][0] == b")") and index > start and not is_constant:
                            found.append((start, closing))
                    index = closing
        if next_member:
            start = index + 1
            parens = 0
            declared = None
            assigns = throws = False
        index += 1
    return found


def _is_entry_point(tokens):
    """`main` — это сама программа, а не вставленный в неё алгоритм, как бы он ни был похож."""
    for current, following in zip(tokens, tokens[1:]):
        if current[0] == b"{":
            return False
        if current[0] == b"main" and following[0] == b"(":
            return True
    return False


def _shape(tokens):
    """Форма кода: от имён, чисел и оформления она не зависит."""
    shape = []
    annotation = False
    for text, kind, _, _ in tokens:
        if annotation:
            annotation = False
        elif kind == "word":
            if text in _KEYWORDS:
                shape.append(text)
            elif text in _PRIMITIVES:
                shape.append(b"T")
            elif text not in _MODIFIERS:
                shape.append(b"I")
        elif kind == "number":
            shape.append(b"N")
        elif kind == "text":
            shape.append(b"S")
        elif text == b"@":
            annotation = True
        elif text not in _LAYOUT:
            shape.append(text)
    return shape


def _grams(shape):
    if len(shape) < GRAM:
        return {tuple(shape)}
    return {tuple(shape[index:index + GRAM]) for index in range(len(shape) - GRAM + 1)}


def _common_length(shape, positions, size):
    """Длина наибольшей общей подпоследовательности, битовый алгоритм Хююрё: по целому числу на строку таблицы."""
    full = (1 << size) - 1
    row = full
    for token in shape:
        low = row & positions.get(token, 0)
        row = ((row + low) | (row - low)) & full
    return size - row.bit_count()


class TemplateIndex:
    """Формы функций набора. Отбор идёт по кускам формы, и размер набора на его время почти не влияет."""

    def __init__(self, sources, similarity=SIMILARITY, short_similarity=SHORT_SIMILARITY, short_tokens=SHORT_TOKENS):
        self.similarity = similarity
        self.short_similarity = short_similarity
        self.short_tokens = short_tokens
        self.shapes = []
        self.sizes = []
        self.positions = []
        self.postings = defaultdict(list)
        self.known = {}
        for source in sources:
            tokens = _tokens(source)
            for first, last in _methods(tokens):
                shape = _shape(tokens[first:last + 1])
                grams = _grams(shape)
                for piece in grams:
                    self.postings[piece].append(len(self.shapes))
                self.shapes.append(shape)
                self.sizes.append(len(grams))
                positions = defaultdict(int)
                for position, token in enumerate(shape):
                    positions[token] |= 1 << position
                self.positions.append(positions)
        # Длиннее этого на функцию набора не похоже при любом совпадении: 2b / (a + b) < порога.
        self.longest = max(map(len, self.shapes), default=0) * (2 - similarity) / similarity
        self.matchers = [None] * len(self.shapes)

    def _candidates(self, shape):
        grams = _grams(shape)
        hits = Counter()
        for piece in grams:
            hits.update(self.postings.get(piece, ()))
        scored = ((2 * count / (len(grams) + self.sizes[number]), number) for number, count in hits.items())
        return sorted((pair for pair in scored if pair[0] >= CANDIDATE_SIMILARITY), reverse=True)

    def _matches(self, shape, number):
        size = len(self.shapes[number])
        threshold = self.short_similarity if len(shape) < self.short_tokens else self.similarity
        # `ratio` не больше доли общей подпоследовательности, а считается она на порядок быстрее.
        if 2 * _common_length(shape, self.positions[number], size) < threshold * (len(shape) + size):
            return False
        # Таблица вхождений строится по второй последовательности: она у функции набора одна на весь запуск.
        matcher = self.matchers[number]
        if matcher is None:
            matcher = self.matchers[number] = SequenceMatcher(None, (), self.shapes[number], autojunk=False)
        matcher.set_seq1(shape)
        return matcher.ratio() >= threshold

    def is_template(self, shape):
        """Похожа ли функция на функцию набора. Один и тот же ввод встречается почти в каждой работе."""
        key = tuple(shape)
        if key not in self.known:
            self.known[key] = len(shape) <= self.longest and any(
                self._matches(shape, number) for _, number in self._candidates(shape))
        return self.known[key]


def default_index():
    return TemplateIndex(path.read_bytes() for path in sorted(CODE.glob("*.java")))


def version():
    """Версия набора. Результат зависит и от файлов, и от этого правила, и должен воспроизводиться."""
    digest = hashlib.sha256(Path(__file__).read_bytes())
    for path in sorted(CODE.glob("*.java")):
        digest.update(path.name.encode() + b"\0" + path.read_bytes() + b"\0")
    return digest.hexdigest()[:8]


def strip(source, index):
    """Исходник без шаблонных функций и их строки: `(байты, [[первая, последняя], ...])`.

    Стёртое заменяется пробелами, переводы строк остаются: номера строк в ответе не меняются.
    """
    tokens = _tokens(source)
    stripped = None
    lines = []
    line, counted, previous = 1, 0, None
    for first, last in _methods(tokens):
        method = tokens[first:last + 1]
        if not _is_entry_point(method) and index.is_template(_shape(method)):
            start, end = tokens[first][2], tokens[last][3]
            line += source.count(b"\n", counted, start)
            last_line = line + source.count(b"\n", start, end)
            # Функции подряд — один участок: между ними нет кода, только пробелы и комментарии.
            if first - 1 == previous:
                lines[-1][1] = last_line
            else:
                lines.append([line, last_line])
            line, counted, previous = last_line, end, last
            if stripped is None:
                stripped = bytearray(source)
            stripped[start:end] = source[start:end].translate(_BLANK)
    return source if stripped is None else bytes(stripped), lines


def _outside(span, lines):
    """Части диапазона строк вне стёртых участков."""
    pieces = []
    start, end = span
    for first, last in lines:
        if first > end:
            break
        if last >= start:
            if first > start:
                pieces.append([start, first - 1])
            start = last + 1
    if start <= end:
        pieces.append([start, end])
    return pieces


def split_fragment(first, second, lines_first, lines_second):
    """Совпавший кусок без стёртых строк: `[(строки в одной работе, строки в другой), ...]`.

    После стирания код вокруг него стоит подряд, и метод видит один кусок поверх стёртых строк,
    а показывать их как совпавшие нельзя. Кусков с двух сторон поровну, значит они идут в том же
    порядке. Иначе соответствие неизвестно, и остаётся самая длинная часть с каждой стороны.
    """
    pieces_first, pieces_second = _outside(first, lines_first), _outside(second, lines_second)
    if not pieces_first or not pieces_second:
        return []
    if len(pieces_first) != len(pieces_second):
        pieces_first = [max(pieces_first, key=lambda piece: piece[1] - piece[0])]
        pieces_second = [max(pieces_second, key=lambda piece: piece[1] - piece[0])]
    return list(zip(pieces_first, pieces_second))


def strip_all(sources, folder):
    """Копии работ без шаблонов в раскладке входа core и их стёртые строки по работам."""
    index = default_index()
    done, stripped_lines = {}, {}
    for sid, path in sources.items():
        source = path.read_bytes()
        # Точные копии в наборе не редкость.
        if source not in done:
            done[source] = strip(source, index)
        clean, stripped_lines[sid] = done[source]
        copy = folder / "submissions" / sid / path.name
        copy.parent.mkdir(parents=True)
        copy.write_bytes(clean)
    return stripped_lines
