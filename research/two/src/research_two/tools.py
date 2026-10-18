"""Вызовы обоих инструментов так, как их запускает эксперимент.

**JPlag** — один контейнер на набор (задачу): оценки пар от остальных решений
набора не зависят, а запуск на пару стоил бы 911 стартов JVM на конфигурацию.

**structsim** — шаги 2–4 импортируются из `tools/structsim` как есть. Шаг 1
вызывается той же командой `semgrep-core`, что в `structsim.semgrep.dump_ast`,
но циклом внутри одного контейнера на задачу: `dump_ast` поднимает контейнер на
файл, и на 1950 файлах это часы (сам structsim называет это своим слабым
местом, docs/pipeline.md). Что выгрузка цикла байт в байт совпадает с
`dump_ast`, проверяет `doctor`.
"""

from __future__ import annotations

import csv
import json
import time
import zipfile
from dataclasses import dataclass
from pathlib import Path

from research_two import config, containers

# --- JPlag ----------------------------------------------------------------


@dataclass(frozen=True)
class JPlagResult:
    rows: list[dict[str, str]]
    failed: list[str]
    comparisons: int
    execution_ms: int      # из runInformation.json: сравнение пар, без разбора
    wall_s: float          # весь контейнер: старт JVM, разбор, сравнение


def run_jplag(
    submissions: Path,
    out_dir: Path,
    args: tuple[str, ...],
    timeout: float = 1800,
) -> JPlagResult:
    """JPlag на одном наборе: каждая поддиректория `submissions` — решение."""
    out_dir.mkdir(parents=True, exist_ok=True)
    started = time.perf_counter()
    result = containers.run(
        config.JPLAG_IMAGE,
        ["/in", "-r", "/out/report", *args],
        mounts=[
            containers.Mount(submissions, "/in"),
            containers.Mount(out_dir, "/out", read_only=False),
        ],
        timeout=timeout,
        check=False,
    )
    wall = time.perf_counter() - started
    # Шум JLine про restricted method на Java 25 к делу не относится
    # (tools/jplag/pitfalls.md, ловушка 3).
    log = "\n".join(
        line
        for line in (result.stdout + result.stderr).splitlines()
        if "restricted method" not in line and "--enable-native-access" not in line
    )
    (out_dir / "log.txt").write_text(log, encoding="utf-8")
    if result.returncode != 0:
        raise containers.DockerError(
            f"JPlag на {submissions.name} вернул {result.returncode}: {log[-1500:]}"
        )

    csv_path = out_dir / "report" / "results.csv"
    with csv_path.open(encoding="utf-8", newline="") as handle:
        rows = list(csv.DictReader(handle))
    with zipfile.ZipFile(out_dir / "report.jplag") as archive:
        info = json.loads(archive.read("runInformation.json"))
    return JPlagResult(
        rows=rows,
        failed=[str(item) for item in info.get("failedSubmissions", [])],
        comparisons=int(info.get("totalComparisons", len(rows))),
        execution_ms=int(info.get("executionTime", 0)),
        wall_s=wall,
    )


# --- structsim, шаг 1 -----------------------------------------------------

# Та же команда, что в structsim.semgrep.dump_ast. Время на файл меряется
# внутри контейнера, чтобы в него не попал старт контейнера. Образ на Alpine,
# у busybox `date` нет `%N`, поэтому часы — /proc/uptime с точностью 10 мс:
# разбор файла занимает секунды, этого хватает.
_SEMGREP_LOOP = r"""
set -u
lang="$1"; shift
now() { read up _ < /proc/uptime; echo "${up%.*}${up#*.}"; }
for rel in "$@"; do
  name=$(basename "$rel"); name="${name%.*}"
  t0=$(now)
  semgrep-core -lang "$lang" -json -full_token_info -dump_ast "/work/$rel" \
    > "/out/$name.json" 2> "/out/$name.err"
  rc=$?
  t1=$(now)
  echo "$name $rc $(( (t1 - t0) * 10 ))" >> /out/_status.txt
done
"""


@dataclass(frozen=True)
class ParseStatus:
    name: str
    ok: bool
    ms: int
    error: str = ""


def dump_trees(
    root: Path,
    files: list[str],
    out_dir: Path,
    language: str = config.STRUCTSIM_LANGUAGE,
    timeout: float = 7200,
) -> list[ParseStatus]:
    """Шаг 1 для пачки файлов одним контейнером.

    `files` — пути относительно `root`, в стиле POSIX. Дерево файла
    `<x>/<имя>.java` ложится в `out_dir/<имя>.json`; имена в пачке обязаны
    быть различны.
    """
    names = [Path(rel).stem for rel in files]
    if len(set(names)) != len(names):
        raise ValueError("имена файлов в пачке повторяются")
    out_dir.mkdir(parents=True, exist_ok=True)
    status_file = out_dir / "_status.txt"
    status_file.unlink(missing_ok=True)

    containers.run(
        config.SEMGREP_IMAGE,
        ["-c", _SEMGREP_LOOP, "loop", language, *files],
        mounts=[
            containers.Mount(root, "/work"),  # как в dump_ast: путь попадает в дерево
            containers.Mount(out_dir, "/out", read_only=False),
        ],
        entrypoint="sh",
        timeout=timeout,
    )

    statuses: list[ParseStatus] = []
    for line in status_file.read_text(encoding="utf-8").splitlines():
        name, rc, ms = line.split()
        tree = out_dir / f"{name}.json"
        err = out_dir / f"{name}.err"
        error = err.read_text(encoding="utf-8", errors="replace").strip() if err.exists() else ""
        ok = rc == "0" and tree.exists() and tree.stat().st_size > 0
        if ok:
            err.unlink(missing_ok=True)
        else:
            tree.unlink(missing_ok=True)
        statuses.append(ParseStatus(name, ok, int(ms), "" if ok else (error or f"код {rc}")[:500]))
    if len(statuses) != len(files):
        raise containers.DockerError(
            f"разобрано {len(statuses)} файлов из {len(files)}: контейнер оборвался"
        )
    return statuses
