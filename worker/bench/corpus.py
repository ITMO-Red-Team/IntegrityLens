"""Входы для замеров: синтетический набор заданного размера и настоящие решения.

Синтетика воспроизводится по зерну и нужна там, где важен размер: настоящих решений на
десятки тысяч строк нет. Настоящие решения — ConPlag, в репозиторий он не входит,
`research/one` скачивает его сам (`uv run python -m research_one.fetch`).
"""
import hashlib
import json
import random
import re
import uuid
from pathlib import Path

from worker.core.contract import CONTRACT

NAMES = ("total", "count", "items", "value", "limit", "index", "result", "left", "right", "step", "best", "next",
         "size", "width", "cost", "node", "edge", "weight", "depth", "level", "state", "score", "prev", "mark")


def _name(rng):
    return f"{rng.choice(NAMES)}{rng.randint(0, 99)}"


def _prefix_sums(rng, name):
    a, s, i = _name(rng), _name(rng), _name(rng)
    return [f"    static long[] {name}(int[] {a}) {{",
            f"        long[] {s} = new long[{a}.length + {rng.randint(1, 3)}];",
            f"        for (int {i} = 0; {i} < {a}.length; {i}++) {{",
            f"            {s}[{i} + 1] = {s}[{i}] + {a}[{i}];",
            "        }",
            f"        return {s};",
            "    }"]


def _binary_search(rng, name):
    a, x, lo, hi, mid = _name(rng), _name(rng), _name(rng), _name(rng), _name(rng)
    return [f"    static int {name}(int[] {a}, int {x}) {{",
            f"        int {lo} = 0;",
            f"        int {hi} = {a}.length - 1;",
            f"        while ({lo} <= {hi}) {{",
            f"            int {mid} = ({lo} + {hi}) >>> 1;",
            f"            if ({a}[{mid}] < {x}) {{",
            f"                {lo} = {mid} + 1;",
            f"            }} else if ({a}[{mid}] > {x}) {{",
            f"                {hi} = {mid} - 1;",
            "            } else {",
            f"                return {mid};",
            "            }",
            "        }",
            "        return -1;",
            "    }"]


def _matrix(rng, name):
    m, n, i, j, s = _name(rng), _name(rng), _name(rng), _name(rng), _name(rng)
    return [f"    static long {name}(int[][] {m}, int {n}) {{",
            f"        long {s} = {rng.randint(0, 9)};",
            f"        for (int {i} = 0; {i} < {n}; {i}++) {{",
            f"            for (int {j} = {i}; {j} < {n}; {j}++) {{",
            f"                if (({i} + {j}) % {rng.randint(2, 9)} == 0) {{",
            f"                    {s} += {m}[{i}][{j}] * {rng.randint(2, 99)};",
            "                } else {",
            f"                    {s} -= {m}[{j}][{i}];",
            "                }",
            "            }",
            "        }",
            f"        return {s};",
            "    }"]


def _graph(rng, name):
    g, d, q, u, v = _name(rng), _name(rng), _name(rng), _name(rng), _name(rng)
    return [f"    static int[] {name}(List<List<Integer>> {g}, int start) {{",
            f"        int[] {d} = new int[{g}.size()];",
            f"        Arrays.fill({d}, -1);",
            f"        Deque<Integer> {q} = new ArrayDeque<>();",
            f"        {d}[start] = 0;",
            f"        {q}.add(start);",
            f"        while (!{q}.isEmpty()) {{",
            f"            int {u} = {q}.poll();",
            f"            for (int {v} : {g}.get({u})) {{",
            f"                if ({d}[{v}] < 0) {{",
            f"                    {d}[{v}] = {d}[{u}] + 1;",
            f"                    {q}.add({v});",
            "                }",
            "            }",
            "        }",
            f"        return {d};",
            "    }"]


def _strings(rng, name):
    t, b, i, c = _name(rng), _name(rng), _name(rng), _name(rng)
    return [f"    static String {name}(String {t}) {{",
            f"        StringBuilder {b} = new StringBuilder();",
            f"        for (int {i} = {t}.length() - 1; {i} >= 0; {i}--) {{",
            f"            char {c} = {t}.charAt({i});",
            f"            if (Character.isLetter({c})) {{",
            f"                {b}.append(Character.toUpperCase({c}));",
            f"            }} else if ({c} != ' ') {{",
            f"                {b}.append('{rng.choice('xyz_#')}');",
            "            }",
            "        }",
            f"        return {b}.toString();",
            "    }"]


METHODS = (_prefix_sums, _binary_search, _matrix, _graph, _strings)
HEADER = ["import java.util.ArrayDeque;", "import java.util.Arrays;", "import java.util.Deque;", "import java.util.List;",
          "", "public class Main {"]
FOOTER = ["    public static void main(String[] args) {",
          "        System.out.println(\"done\");",
          "    }",
          "}"]


def _program(rng, lines):
    methods = []
    size = len(HEADER) + len(FOOTER)
    while size < lines:
        method = rng.choice(METHODS)(rng, f"method{len(methods)}")
        methods.append(method)
        size += len(method)
    return methods


def _source(methods):
    return "\n".join(HEADER + [line for method in methods for line in method] + FOOTER) + "\n"


def _disguised(rng, methods):
    """Плагиат: другие имена, другой порядок функций, вставка в пару мест."""
    names = {name: _name(rng) for name in {word for method in methods for line in method
                                           for word in re.findall(r"\b[a-z]+\d{1,2}\b", line)}}
    renamed = [[re.sub(r"\b[a-z]+\d{1,2}\b", lambda m: names[m.group()], line) for line in method]
               for method in methods]
    rng.shuffle(renamed)
    for method in renamed[:len(renamed) // 5]:
        method.insert(1, f"        int {_name(rng)} = {rng.randint(0, 9)};")
    return renamed


def synthetic(count, lines, seed=1):
    """`count` программ примерно по `lines` строк. Каждая четвёртая списана с предыдущей."""
    rng = random.Random(f"{seed}:{count}:{lines}")
    sources = []
    previous = None
    for number in range(count):
        methods = _disguised(rng, previous) if number % 4 == 3 else _program(rng, lines)
        previous = methods
        sources.append(_source(methods))
    return sources


def conplag(root, count, seed=7):
    """`count` различных решений ConPlag как есть. Пары плагиата берутся целиком, как в настоящей группе."""
    root = Path(root)
    pairs = sorted(path for path in root.iterdir() if path.is_dir())
    random.Random(seed).shuffle(pairs)
    seen, sources = set(), []
    for pair in pairs:
        for path in sorted(pair.glob("*.java")):
            data = path.read_bytes()
            digest = hashlib.sha256(data).digest()
            if digest not in seen:
                seen.add(digest)
                sources.append(data.decode("utf-8", "replace"))
            if len(sources) == count:
                return sources
    raise ValueError(f"в {root} только {len(sources)} различных решений")


def write_job(directory, sources, method, params=None):
    """Вход core: `job.json` и исходники. ID детерминированы, порядок пар от запуска не зависит."""
    directory = Path(directory)
    submissions = []
    rng = random.Random(0)
    for source in sources:
        sid = str(uuid.UUID(int=rng.getrandbits(128), version=4))
        path = directory / "submissions" / sid / "Main.java"
        path.parent.mkdir(parents=True)
        path.write_text(source, encoding="utf-8")
        submissions.append({"id": sid, "language": "java", "files": ["Main.java"]})
    job = {"contract": CONTRACT, "runId": str(uuid.UUID(int=rng.getrandbits(128), version=4)), "method": method,
           "params": params or {}, "submissions": submissions}
    (directory / "job.json").write_text(json.dumps(job), encoding="utf-8")
