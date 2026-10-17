"""JPlag из локального jar, без Docker.

Делает то же, что команда из tools/jplag/usage.md, только `java -jar` вместо
`docker run`. Ключи те же: `--mode RUN` (иначе JPlag поднимает просмотрщик),
`--csv-export` (единственный полный источник оценок — ловушка 1),
`--cluster-skip`, `--overwrite`.

Две проверки, которых у Docker-версии нет, потому что там их обеспечивает
образ: Java не ниже 25 и наличие `javac` рядом с `java` — на JRE JPlag
разбирает ноль файлов и не объясняет почему (tools/jplag/usage.md).
"""

from __future__ import annotations

import csv
import json
import os
import re
import shutil
import subprocess
import zipfile
from dataclasses import dataclass, field
from pathlib import Path

from research_one.fetch import JPLAG_JAR, JPLAG_VERSION

MIN_JAVA = 25


class JplagError(RuntimeError):
    pass


@dataclass(frozen=True)
class Pair:
    a: str
    b: str
    average: float
    maximum: float


@dataclass
class Run:
    """Итог одного запуска: пары из `results.csv` и сведения о прогоне."""

    pairs: list[Pair]
    failed: list[str]
    submissions: int
    stderr: str = ""
    options: dict = field(default_factory=dict)
    tokens: dict[str, int] = field(default_factory=dict)   # решение → число меток JPlag

    def by_pair(self) -> dict[frozenset[str], Pair]:
        """Соединять надо по неупорядоченной паре: порядок имён не гарантирован."""
        return {frozenset((p.a, p.b)): p for p in self.pairs}


def java() -> Path:
    """`java` из JAVA_HOME или PATH, с проверкой версии и наличия javac."""
    home = os.environ.get("JAVA_HOME")
    exe = Path(home) / "bin" / "java" if home else shutil.which("java")
    if exe is None or not Path(exe).exists():
        raise JplagError("java не найдена: нужен JDK >= 25 в JAVA_HOME или PATH")
    exe = Path(exe)
    out = subprocess.run(
        [exe, "-version"], capture_output=True, text=True, check=True
    ).stderr
    match = re.search(r'version "(\d+)', out)
    if not match or int(match.group(1)) < MIN_JAVA:
        raise JplagError(f"JPlag {JPLAG_VERSION} требует Java >= {MIN_JAVA}, есть: {out!r}")
    javac = exe.with_name("javac")
    if not javac.exists() and shutil.which("javac") is None:
        raise JplagError("рядом с java нет javac: это JRE, а JPlag нужен JDK")
    return exe


# Предупреждения Java 25 о нативном доступе от JLine — шум, ловушка 3.
_NOISE = re.compile(r"^WARNING: .*$", re.MULTILINE)


def run(
    submissions: Path,
    out: Path,
    *,
    min_tokens: int | None = None,
    language: str = "java",
    extra: tuple[str, ...] = (),
    timeout: float = 1800,
) -> Run:
    """Прогнать JPlag по директории, где каждая поддиректория — одно решение."""
    if not JPLAG_JAR.exists():
        raise JplagError(f"нет {JPLAG_JAR}; скачать: uv run python -m research_one.fetch")
    out.mkdir(parents=True, exist_ok=True)
    report = out / "report"
    command = [
        str(java()), "-jar", str(JPLAG_JAR), "--mode", "RUN",
        str(submissions), "-l", language,
        "-r", str(report), "--csv-export", "--cluster-skip", "--overwrite",
        # Оценки берутся из CSV, архив нужен только ради runInformation.json;
        # не раздуваем его всеми парами.
        "-n", "0",
    ]
    if min_tokens is not None:
        command += ["-t", str(min_tokens)]
    command += list(extra)

    result = subprocess.run(
        command, capture_output=True, text=True, timeout=timeout
    )
    stderr = _NOISE.sub("", result.stderr).strip()
    if result.returncode != 0:
        raise JplagError(
            f"JPlag вернул {result.returncode} на {submissions}:\n"
            f"{result.stdout[-2000:]}\n{stderr[-2000:]}"
        )

    csv_path = report / "results.csv"
    pairs = []
    with csv_path.open(encoding="utf-8") as handle:
        for row in csv.DictReader(handle):
            pairs.append(
                Pair(
                    a=row["submissionName1"],
                    b=row["submissionName2"],
                    average=float(row["averageSimilarity"]),
                    maximum=float(row["maxSimilarity"]),
                )
            )

    with zipfile.ZipFile(report.with_suffix(".jplag")) as archive:
        info = json.loads(archive.read("runInformation.json"))
        options = json.loads(archive.read("options.json"))
        index = json.loads(archive.read("submissionFileIndex.json"))["fileIndexes"]
    tokens = {
        name: sum(entry["tokenCount"] for entry in files.values())
        for name, files in index.items()
    }
    failed = [str(item) for item in info.get("failedSubmissions", [])]
    submitted = sum(1 for path in submissions.iterdir() if path.is_dir())
    return Run(pairs=pairs, failed=failed, submissions=submitted, stderr=stderr, options=options, tokens=tokens)
