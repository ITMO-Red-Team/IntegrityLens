"""Весь конвейер: директория с кодом → оценки по парам и тайлы.

Собирает шаги вместе и больше ничего не решает: шаг 1 в `semgrep.py`, шаг 2 в
`labels.py`, шаг 3 в `compare.py`, шаг 4 в `contrast.py`, тайлы в `tiles.py`.

Устройство вывода подчинено одному: **оценки считаются по всем парам, тайлы —
только по запрошенным.** Тайлы растут кубически от длины программы, и считать
их для всех пар на большом наборе не нужно: это вывод для человека, а человек
смотрит десяток пар, а не десятки тысяч.
"""

from __future__ import annotations

from collections.abc import Iterable, Sequence
from dataclasses import dataclass, replace
from itertools import combinations
from pathlib import Path

from structsim import compare, contrast, semgrep, tiles
from structsim.labels import to_program
from structsim.model import Program


@dataclass(frozen=True)
class PairScore:
    """Оценки одной пары. `contrast_z` равен None, если набор меньше пяти."""

    a: str
    b: str
    order: float
    shingle: float
    blocks: float
    combined: float
    contrast_z: float | None = None


@dataclass(frozen=True)
class ShownTile:
    """Общий кусок в том виде, в котором его показывают человеку."""

    length: int
    labels: tuple[str, ...]
    lines_a: tuple[int, int] | None
    lines_b: tuple[int, int] | None

    def describe(self, name_a: str, name_b: str) -> str:
        return (
            f"{name_a} {_lines(self.lines_a)} ↔ {name_b} {_lines(self.lines_b)}"
            f" — {self.length} {_labels_word(self.length)}"
        )


def _labels_word(count: int) -> str:
    """«1 метка», «2 метки», «21 метка», «11 меток»."""
    if count % 100 // 10 == 1:
        return "меток"
    last = count % 10
    if last == 1:
        return "метка"
    if last in (2, 3, 4):
        return "метки"
    return "меток"


def _lines(span: tuple[int, int] | None) -> str:
    if span is None:
        return "строки неизвестны"
    first, last = span
    return f"строка {first}" if first == last else f"строки {first}–{last}"


@dataclass
class Report:
    """Что получилось: разобранные программы, оценки пар и причины пропусков."""

    programs: dict[str, Program]
    scores: list[PairScore]
    skipped: dict[str, str]

    def top(self, count: int = 10) -> list[PairScore]:
        """Пары, самые похожие сначала."""
        return sorted(self.scores, key=lambda s: s.combined, reverse=True)[:count]

    def tiles(
        self, a: str, b: str, min_length: int = tiles.MIN_TILE_LENGTH
    ) -> list[ShownTile]:
        """Общие куски пары — с диапазонами строк в обоих файлах.

        Считается по запросу, а не заранее: см. замеры в `docs/pipeline.md`.
        """
        first, second = self.programs[a], self.programs[b]
        shown = []
        for tile in tiles.tiles_of(first, second, min_length):
            shown.append(
                ShownTile(
                    length=tile.length,
                    labels=tile.labels(first),
                    lines_a=first.span_of(tile.start_a, tile.length),
                    lines_b=second.span_of(tile.start_b, tile.length),
                )
            )
        return shown


def source_files(directory: Path) -> list[Path]:
    """Файлы, язык которых метод знает, в устойчивом порядке."""
    directory = Path(directory)
    found = [
        path
        for path in sorted(directory.rglob("*"))
        if path.is_file() and path.suffix.lower() in semgrep.LANGUAGES
    ]
    return found


def to_programs(
    files: Iterable[Path],
    image: str = semgrep.SEMGREP_IMAGE,
) -> tuple[dict[str, Program], dict[str, str]]:
    """Шаги 1 и 2: файлы → `Program`, плюс причины пропусков.

    Имя программы — имя файла. Пропуск одного файла не роняет остальные: это
    нормальный исход, но его надо считать, иначе оценки посчитаются на меньшей
    выборке, чем кажется.
    """
    programs: dict[str, Program] = {}
    skipped: dict[str, str] = {}
    for path in files:
        path = Path(path)
        try:
            tree = semgrep.dump_ast(path, image=image)
        except semgrep.SemgrepError as exc:
            skipped[path.name] = str(exc)
            continue
        program = to_program(
            tree, language=semgrep.language_of(path), source=path.name
        )
        if not program.labels:
            skipped[path.name] = "разобрался, но меток не дал"
            continue
        programs[path.name] = program
    return programs, skipped


def score_pairs(programs: dict[str, Program]) -> list[PairScore]:
    """Шаги 3 и 4 по всем парам набора."""
    scores: list[PairScore] = []
    combined_by_pair: dict[contrast.PairKey, float] = {}

    for (name_a, first), (name_b, second) in combinations(programs.items(), 2):
        measures = compare.explain(first, second)
        scores.append(
            PairScore(
                a=name_a,
                b=name_b,
                order=measures["order"],
                shingle=measures["shingle"],
                blocks=measures["blocks"],
                combined=measures["combined"],
            )
        )
        combined_by_pair[contrast.pair_key(name_a, name_b)] = measures["combined"]

    if len(programs) < contrast.MIN_SET_SIZE:
        return scores

    z_by_pair = contrast.contrast_z(combined_by_pair)
    return [
        replace(score, contrast_z=z_by_pair.get(contrast.pair_key(score.a, score.b)))
        for score in scores
    ]


def analyze(
    directory: Path | str,
    image: str = semgrep.SEMGREP_IMAGE,
) -> Report:
    """Директория с решениями одной задачи → отчёт.

    Все программы набора должны решать одну задачу: иначе `contrast-z`
    бессмыслен, потому что фон считается по набору.
    """
    files = source_files(Path(directory))
    if not files:
        known = ", ".join(sorted(set(semgrep.LANGUAGES)))
        raise semgrep.SemgrepError(
            f"в {directory} нет файлов известных языков ({known})"
        )
    programs, skipped = to_programs(files, image=image)
    return Report(programs=programs, scores=score_pairs(programs), skipped=skipped)


def format_report(report: Report, top: int = 10) -> str:
    """Отчёт в виде, пригодном для чтения в терминале."""
    lines: list[str] = []
    lines.append(
        f"разобрано программ: {len(report.programs)}"
        f", пар: {len(report.scores)}"
        f", пропущено файлов: {len(report.skipped)}"
    )
    if report.skipped:
        lines.append("")
        lines.append("пропущены:")
        for name, reason in report.skipped.items():
            lines.append(f"  {name}: {reason.splitlines()[0][:100]}")

    best = report.top(top)
    if not best:
        return "\n".join(lines)

    lines.append("")
    has_z = any(score.contrast_z is not None for score in best)
    header = f"{'пара':<40} {'COMBINED':>9} {'поряд':>7} {'фрагм':>7} {'функц':>7}"
    if has_z:
        header += f" {'contrast-z':>11}"
    lines.append(header)
    for score in best:
        pair = f"{score.a} ↔ {score.b}"
        row = (
            f"{pair[:40]:<40} {score.combined:>9.3f} {score.order:>7.2f}"
            f" {score.shingle:>7.2f} {score.blocks:>7.2f}"
        )
        if has_z:
            row += (
                f" {score.contrast_z:>11.2f}"
                if score.contrast_z is not None
                else f" {'—':>11}"
            )
        lines.append(row)

    if not has_z:
        lines.append("")
        lines.append(
            f"contrast-z не считался: в наборе {len(report.programs)} программ,"
            f" нужно хотя бы {contrast.MIN_SET_SIZE}"
        )
    return "\n".join(lines)


def format_tiles(report: Report, a: str, b: str, min_length: int) -> str:
    shown = report.tiles(a, b, min_length)
    if not shown:
        return (
            f"общих кусков длиной от {min_length} меток у {a} и {b} нет."
            " Порог можно понизить ключом --min-tile."
        )

    lines = [f"общие куски {a} ↔ {b} (порог {min_length} меток):", ""]
    for tile in shown:
        lines.append("  " + tile.describe(a, b))
        lines.append("    " + " ".join(tile.labels))
    return "\n".join(lines)


def _program_names(report: Report, wanted: Sequence[str]) -> tuple[str, str]:
    missing = [name for name in wanted if name not in report.programs]
    if missing:
        known = ", ".join(sorted(report.programs))
        raise semgrep.SemgrepError(
            f"нет разобранных программ {missing}; есть: {known}"
        )
    return wanted[0], wanted[1]
