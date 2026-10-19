"""Командная строка structsim.

    structsim <директория>                  — оценки по всем парам
    structsim <директория> --tiles a.py b.py — общие куски пары, строками
    structsim <директория> --csv out.csv     — оценки в файл

Все программы в директории должны решать одну задачу: `contrast-z` считает фон
по набору, и на смеси задач он бессмыслен.
"""

from __future__ import annotations

import argparse
import csv
import sys
from pathlib import Path

from structsim import analyze as pipeline
from structsim import semgrep, tiles


def _write_csv(report: pipeline.Report, path: Path) -> None:
    with path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.writer(handle)
        writer.writerow(
            ["a", "b", "combined", "order", "shingle", "blocks", "contrast_z"]
        )
        for score in sorted(report.scores, key=lambda s: -s.combined):
            writer.writerow(
                [
                    score.a,
                    score.b,
                    f"{score.combined:.6f}",
                    f"{score.order:.6f}",
                    f"{score.shingle:.6f}",
                    f"{score.blocks:.6f}",
                    "" if score.contrast_z is None else f"{score.contrast_z:.6f}",
                ]
            )


def main(argv: list[str] | None = None) -> int:
    # Кодировка консоли Windows по умолчанию не UTF-8, и кириллица в выводе
    # превращается в мусор. Переключаем поток явно.
    for stream in (sys.stdout, sys.stderr):
        if hasattr(stream, "reconfigure"):
            stream.reconfigure(encoding="utf-8", errors="replace")

    parser = argparse.ArgumentParser(
        prog="structsim",
        description="Структурное сходство программ, в том числе между языками.",
    )
    parser.add_argument("directory", type=Path, help="директория с решениями одной задачи")
    parser.add_argument(
        "--top", type=int, default=10, help="сколько самых похожих пар показать"
    )
    parser.add_argument(
        "--tiles",
        nargs=2,
        metavar=("A", "B"),
        help="показать общие куски этой пары, с диапазонами строк",
    )
    parser.add_argument(
        "--min-tile",
        type=int,
        default=tiles.MIN_TILE_LENGTH,
        help=f"минимальная длина куска в метках (по умолчанию {tiles.MIN_TILE_LENGTH})",
    )
    parser.add_argument("--csv", type=Path, help="записать оценки всех пар в файл")
    parser.add_argument(
        "--image", default=semgrep.SEMGREP_IMAGE, help="образ Semgrep"
    )
    args = parser.parse_args(argv)

    try:
        report = pipeline.analyze(args.directory, image=args.image)
    except semgrep.SemgrepError as exc:
        print(f"не вышло: {exc}", file=sys.stderr)
        return 1

    if args.tiles:
        try:
            first, second = pipeline._program_names(report, args.tiles)
        except semgrep.SemgrepError as exc:
            print(f"не вышло: {exc}", file=sys.stderr)
            return 1
        print(pipeline.format_tiles(report, first, second, args.min_tile))
        return 0

    print(pipeline.format_report(report, top=args.top))
    if args.csv:
        _write_csv(report, args.csv)
        print(f"\nоценки всех пар записаны в {args.csv}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
