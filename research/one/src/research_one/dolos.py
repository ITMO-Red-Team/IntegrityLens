"""Dolos CLI из локального node_modules, без Docker.

Dolos (Maertens et al., 2022; Гентский университет) превращает программу в
последовательность типов узлов дерева tree-sitter — имена и литералы
отбрасываются, — режет её на k-граммы (k = 23) и оставляет отпечатки по
winnowing (окно 17). Оценка пары `similarity` — доля отпечатков обеих
программ, которые есть в другой: (покрыто у a + покрыто у b) / (всего у a +
всего у b). Мера симметричная, как averageSimilarity у JPlag.

Запуск: `dolos run -l java -f csv -o <out> <файлы>`, оценки — из
`<out>/pairs.csv`. Установка — `npm ci` по package-lock.json (fetch.py);
если Dolos не стоит, `cli()` ставит его сам.

Три ловушки:

1. **Справка врёт про `-M`.** `dolos run --help` пишет, что отпечатки, которые
   есть больше чем в 90% файлов, по умолчанию отбрасываются. В коде 2.9.3
   умолчания нет: `metadata.csv` прогона показывает
   `maxFingerprintPercentage null`. Без `-M`/`-m`/`-i` оценка пары не зависит
   от остальных файлов запуска — как у JPlag без `-bc`. Поэтому запуск на всю
   задачу даёт те же оценки пар, что запуск на каждую пару.
2. **Сравниваются файлы, а не решения.** Если в поддиректории решения
   несколько файлов, Dolos сравнит каждый с каждым по отдельности. Обёртка
   требует ровно один файл на решение.
3. **Неразобранных файлов не бывает.** tree-sitter строит дерево всегда: на
   месте синтаксической ошибки — узел `ERROR`, и оценка считается по тому,
   что разобралось. Такие решения обёртка возвращает в `failed`, но их пары
   остаются с оценкой. Отдельно `empty` — решения короче k меток: у них нет ни
   одного отпечатка, и все их пары получают 0.
"""

from __future__ import annotations

import csv
import functools
import json
import os
import re
import shutil
import subprocess
import sys
import threading
from dataclasses import dataclass, field
from pathlib import Path

from research_one.fetch import DOLOS_VERSION, install_dolos
from research_one.paths import DOLOS_BIN

MIN_NODE = 18          # engines в package.json самого Dolos
_INSTALL = threading.Lock()   # run() зовут из нескольких потоков, а npm ci — один


class DolosError(RuntimeError):
    pass


@dataclass(frozen=True)
class Pair:
    a: str
    b: str
    similarity: float
    covered_a: int      # отпечатки a, которые нашлись в b
    covered_b: int
    total_a: int        # все отпечатки a
    total_b: int
    longest: int        # самый длинный общий фрагмент, в k-граммах

    @property
    def maximum(self) -> float:
        """Доля той программы, которая покрыта сильнее, — аналог maxSimilarity у JPlag.

        У Dolos такой оценки нет, она выводится из тех же столбцов `pairs.csv`.
        Верна, пока ничего не отброшено (`-M`, `-m`, `-i` не заданы).
        """
        shares = [c / t for c, t in ((self.covered_a, self.total_a), (self.covered_b, self.total_b)) if t]
        return max(shares, default=0.0)


@dataclass
class Run:
    """Итог одного запуска: пары из `pairs.csv` и сведения о прогоне."""

    pairs: list[Pair]
    failed: list[str]                 # в дереве есть ERROR: оценены по разобранной части
    empty: list[str]                  # ни одного отпечатка: все пары — 0
    submissions: int
    stderr: str = ""
    options: dict = field(default_factory=dict)          # metadata.csv
    fingerprints: dict[str, int] = field(default_factory=dict)   # решение → число отпечатков

    def by_pair(self) -> dict[frozenset[str], Pair]:
        """Соединять надо по неупорядоченной паре: порядок имён не гарантирован."""
        return {frozenset((p.a, p.b)): p for p in self.pairs}


def cli() -> Path:
    """`dolos` из node_modules: поставить, если нет, и сверить версии Node и Dolos."""
    with _INSTALL:
        return _checked_cli()


@functools.cache
def _checked_cli() -> Path:
    node = shutil.which("node")
    if node is None:
        raise DolosError(f"node не найден: Dolos {DOLOS_VERSION} требует Node.js >= {MIN_NODE}")
    out = subprocess.run([node, "--version"], capture_output=True, text=True, check=True).stdout
    match = re.match(r"v(\d+)", out.strip())
    if not match or int(match.group(1)) < MIN_NODE:
        raise DolosError(f"Dolos {DOLOS_VERSION} требует Node.js >= {MIN_NODE}, есть: {out!r}")
    install_dolos()
    out = subprocess.run([DOLOS_BIN, "--version"], capture_output=True, text=True, check=True).stdout
    if f"Dolos v{DOLOS_VERSION}" not in out:
        raise DolosError(f"в node_modules не тот Dolos: {out!r}, ждали {DOLOS_VERSION}; npm ci")
    return DOLOS_BIN


def version() -> str:
    """`Dolos v… / Node v… / Tree-sitter …` одной строкой — для протокола прогона."""
    out = subprocess.run([cli(), "--version"], capture_output=True, text=True, check=True).stdout
    return " · ".join(line.strip() for line in out.splitlines() if line.strip())


def _submission_files(submissions: Path) -> dict[str, Path]:
    files = {}
    for directory in sorted(path for path in submissions.iterdir() if path.is_dir()):
        inside = sorted(path for path in directory.rglob("*") if path.is_file())
        if len(inside) != 1:
            raise DolosError(
                f"{directory}: файлов {len(inside)}, а Dolos сравнивает файлы, не директории"
            )
        files[directory.name] = inside[0].resolve()
    return files


def run(
    submissions: Path,
    out: Path,
    *,
    language: str = "java",
    extra: tuple[str, ...] = (),
    timeout: float = 1800,
) -> Run:
    """Прогнать Dolos по директории, где каждая поддиректория — одно решение из одного файла."""
    files = _submission_files(submissions)
    if len(files) < 2:
        raise DolosError(f"{submissions}: меньше двух решений")
    name_of = {str(path): name for name, path in files.items()}

    out.mkdir(parents=True, exist_ok=True)
    report = out / "report"
    # Dolos отказывается писать в существующую директорию.
    shutil.rmtree(report, ignore_errors=True)
    command = [
        str(cli()), "run", "-l", language, "-f", "csv", "-o", str(report),
        *extra, *(str(path) for path in files.values()),
    ]
    result = subprocess.run(
        command, capture_output=True, text=True, timeout=timeout,
        env={**os.environ, "FORCE_COLOR": "0"},
    )
    if result.returncode != 0 or not (report / "pairs.csv").exists():
        raise DolosError(
            f"Dolos вернул {result.returncode} на {submissions}:\n"
            f"{result.stdout[-2000:]}\n{result.stderr[-2000:]}"
        )

    # В files.csv лежат целиком исходник и дерево — поля длиннее умолчания csv.
    csv.field_size_limit(sys.maxsize)
    by_id: dict[str, str] = {}
    fingerprints: dict[str, int] = {}
    failed = []
    with (report / "files.csv").open(encoding="utf-8") as handle:
        for row in csv.DictReader(handle):
            name = name_of[row["path"]]
            by_id[row["id"]] = name
            fingerprints[name] = int(row["amountOfKgrams"])
            if "ERROR" in json.loads(row["ast"]):
                failed.append(name)

    pairs = []
    with (report / "pairs.csv").open(encoding="utf-8") as handle:
        for row in csv.DictReader(handle):
            a, b = by_id[row["leftFileId"]], by_id[row["rightFileId"]]
            pairs.append(
                Pair(
                    a=a, b=b,
                    similarity=float(row["similarity"]),
                    covered_a=int(row["leftCovered"]),
                    covered_b=int(row["rightCovered"]),
                    total_a=fingerprints[a],
                    total_b=fingerprints[b],
                    longest=int(row["longestFragment"]),
                )
            )
    expected = len(files) * (len(files) - 1) // 2
    if len(pairs) != expected:
        raise DolosError(f"{submissions}: в pairs.csv {len(pairs)} пар, ждали все {expected}")

    with (report / "metadata.csv").open(encoding="utf-8") as handle:
        options = {row["property"]: row["value"] for row in csv.DictReader(handle)}

    return Run(
        pairs=pairs,
        failed=sorted(failed),
        empty=sorted(name for name, count in fingerprints.items() if count == 0),
        submissions=len(files),
        stderr=result.stderr.strip(),
        options=options,
        fingerprints=fingerprints,
    )
