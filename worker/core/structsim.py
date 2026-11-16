"""structsim: шаги 1–4 из tools/structsim.

Шаг 1 — `semgrep-core` напрямую, без контейнера на файл: воркер уже в контейнере, и
бинарник лежит в его образе. Шаг 2 — Python-код инструмента, он занимает миллисекунды.
Шаг 3 и тайлы — `native`, если он есть: числа те же, а времени в сотни раз меньше.
"""
import hashlib
import json
import os
import subprocess
import sys
from concurrent.futures import ThreadPoolExecutor
from itertools import combinations

from structsim import compare, contrast, native, tiles
from structsim.labels import to_program
from structsim.semgrep import _ANSI, SEMGREP_VERSION, language_of

from .engine import available_cpus, merged_params, positive_int, sources_of
from .result import assemble

STRUCTSIM_VERSION = "0.1.0"
METHOD_VERSION = f"structsim-{STRUCTSIM_VERSION}+semgrep-{SEMGREP_VERSION}"
DEFAULT_PARAMS = {"minTile": tiles.MIN_TILE_LENGTH}
FILE_TIMEOUT_SECONDS = 120
# Шаг 2 рекурсивно обходит дерево, а по умолчанию в Python глубина всего 1000: на этом падала
# бы цепочка из 300 сложений или вложенность в 160 условий. Кадры Python лежат в куче, и стек
# от такой глубины не страдает, а `json.loads` при слишком глубоком дереве сам сообщает об ошибке.
RECURSION_LIMIT = 100_000


class _NotParsed(Exception):
    pass


def _semgrep_tree(path):
    # Путь попадает в каждый токен выгрузки, поэтому он короткий. `./` — чтобы имя не приняли за ключ.
    command = [os.environ.get("SEMGREP_CORE", "semgrep-core"), "-lang", language_of(path), "-json",
               "-full_token_info", "-dump_ast", f"./{path.name}"]
    try:
        process = subprocess.run(command, cwd=path.parent, stdin=subprocess.DEVNULL, capture_output=True,
                                 timeout=FILE_TIMEOUT_SECONDS)
    except subprocess.TimeoutExpired as error:
        raise _NotParsed("Разбор файла не уложился во время") from error
    if process.returncode < 0:
        # Убит сигналом, например по памяти: файл тут ни при чём, попытку надо повторить.
        raise RuntimeError(f"semgrep-core killed by signal {-process.returncode}")
    if process.returncode != 0:
        reason = _ANSI.sub("", process.stderr.decode("utf-8", "replace")).strip()
        print(f"semgrep-core exited with {process.returncode}: {reason[:300]}", file=sys.stderr)
        raise _NotParsed("Файл не удалось разобрать")
    try:
        return json.loads(process.stdout)
    except (ValueError, RecursionError) as error:
        raise _NotParsed("Файл не удалось разобрать") from error


def _parse(path):
    """Шаги 1 и 2 одного файла: `(программа, причина)`, одно из двух — None."""
    try:
        program = to_program(_semgrep_tree(path), language=language_of(path), source=path.name)
    except _NotParsed as error:
        return None, str(error)
    except RecursionError:
        return None, "Файл не удалось разобрать"
    if not program.labels:
        return None, "В работе нечего сравнивать"
    return program, None


def _to_programs(sources, threads):
    # Точные копии в наборе не редкость, а старт semgrep-core — самая дорогая часть разбора.
    groups = {}
    for sid, path in sources.items():
        groups.setdefault(hashlib.sha256(path.read_bytes()).digest(), []).append(sid)
    with ThreadPoolExecutor(threads) as pool:
        parsed = pool.map(_parse, [sources[sids[0]] for sids in groups.values()])
        programs, failed = {}, {}
        for sids, (program, reason) in zip(groups.values(), parsed):
            for sid in sids:
                if program is None:
                    failed[sid] = reason
                else:
                    programs[sid] = program
    return programs, failed


def _fragments(shown):
    spans = [(item.lines_a, item.lines_b) for item in shown]
    fragments = [{"a": list(a), "b": list(b)} for a, b in spans if a is not None and b is not None]
    return sorted(fragments, key=lambda f: (f["a"], f["b"]))


def _reference_scores(programs, ids, min_tile):
    """Эталон на Python: те же числа, что у `native`. Для разработки без сборки Rust."""
    print("structsim-native not found, using slow reference implementation", file=sys.stderr)
    scores = {}
    for a, b in combinations(ids, 2):
        first, second = programs[a], programs[b]
        measures = compare.explain(first, second)
        shown = [native.ShownTile(tile, first.span_of(tile.start_a, tile.length),
                                  second.span_of(tile.start_b, tile.length))
                 for tile in tiles.tiles_of(first, second, min_tile)]
        scores[a, b] = (measures, _fragments(shown))
    return scores


def _native_scores(programs, ids, min_tile, threads):
    pairs = native.score_all([programs[sid] for sid in ids], min_tile=min_tile, threads=threads)
    return {(ids[pair.a], ids[pair.b]): ({"order": pair.order, "shingle": pair.shingle, "blocks": pair.blocks,
                                          "combined": pair.combined}, _fragments(pair.tiles))
            for pair in pairs}


def analyze(job, input_dir, artifacts, watch):
    params = merged_params(job, DEFAULT_PARAMS)
    min_tile = positive_int(params, "minTile")
    threads = available_cpus()
    sys.setrecursionlimit(RECURSION_LIMIT)
    programs, failed = _to_programs(sources_of(job, input_dir), threads)
    watch.lap("parse")
    ids = sorted(programs)
    if native.find_binary():
        scores = _native_scores(programs, ids, min_tile, threads)
    else:
        scores = _reference_scores(programs, ids, min_tile)
    watch.lap("compare")
    # Фон пары считается по всему набору; при малом наборе шаг 4 не определён.
    z_scores = {}
    if len(ids) >= contrast.MIN_SET_SIZE:
        z_scores = contrast.contrast_z({pair: measures["combined"] for pair, (measures, _) in scores.items()})
    scored = {}
    for pair, (measures, fragments) in scores.items():
        details = {"order": measures["order"], "shingle": measures["shingle"], "blocks": measures["blocks"]}
        if pair in z_scores:
            details["contrastZ"] = z_scores[pair]
        scored[pair] = {"value": measures["combined"], "details": details, "fragments": fragments}
    stats = {sid: {"labels": len(program.labels), "blocks": len(program.blocks)} for sid, program in programs.items()}
    result = assemble(job, METHOD_VERSION, params, scored, failed, stats)
    watch.lap("report")
    return result
