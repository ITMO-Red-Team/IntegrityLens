"""Цена одной пары от длины программы: эталон на Python против native.

Отдельно от `run.py`, потому что мерит не весь core, а шаг 3 и тайлы: эталон на длинных
программах слишком медленный, чтобы гонять его на всех парах набора.

    python -m worker.bench.pair_cost --out pair_cost.csv
"""
import argparse
import csv
import math
import tempfile
import time
from pathlib import Path

from structsim import compare, native, tiles

from worker.bench.corpus import synthetic
from worker.core.structsim import _parse

REFERENCE_MAX_LINES = 800


def programs_of(lines, directory):
    # Четвёртая программа — списанная третья: так есть и чужая пара, и похожая.
    parsed = []
    for number, source in enumerate(synthetic(4, lines)):
        path = Path(directory) / str(number) / "Main.java"
        path.parent.mkdir(parents=True)
        path.write_text(source, encoding="utf-8")
        program, reason = _parse(path)
        if program is None:
            raise RuntimeError(reason)
        parsed.append(program)
    return parsed


def best_of(function, repeats=3):
    best = math.inf
    for _ in range(repeats):
        started = time.perf_counter()
        function()
        best = min(best, time.perf_counter() - started)
    return best * 1000


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--out", required=True)
    args = parser.parse_args()
    with Path(args.out).open("w", newline="", encoding="utf-8") as handle:
        writer = csv.writer(handle)
        writer.writerow(["lines", "labels", "pair", "reference_score_ms", "reference_tiles_ms", "native_score_ms",
                         "native_tiles_ms"])
        for lines in (100, 200, 400, 800, 1600, 3200, 6400, 12800, 25600):
            with tempfile.TemporaryDirectory() as directory:
                programs = programs_of(lines, directory)
            for name, (a, b) in (("чужие", programs[:2]), ("списанная", programs[2:])):
                pair = [a, b]
                score = best_of(lambda: native.score_all(pair, min_tile=0, threads=1))
                both = best_of(lambda: native.score_all(pair, threads=1))
                reference_score = reference_tiles = ""
                if lines <= REFERENCE_MAX_LINES:
                    reference_score = round(best_of(lambda: compare.explain(a, b), 1), 2)
                    reference_tiles = round(best_of(lambda: tiles.tiles_of(a, b), 1), 2)
                row = [lines, len(a.labels), name, reference_score, reference_tiles, round(score, 2),
                       round(max(both - score, 0), 2)]
                writer.writerow(row)
                handle.flush()
                print(row, flush=True)


if __name__ == "__main__":
    main()
