"""Где что лежит. Всё — внутри research/one/, кроме самих инструментов."""

from __future__ import annotations

from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
REPO = ROOT.parents[1]

VENDOR = ROOT / "vendor"      # jar JPlag, скачивается
DATA = ROOT / "data"          # наборы данных, скачиваются
WORK = ROOT / "work"          # промежуточное: деревья, отчёты JPlag
RESULTS = ROOT / "results"    # итоговые таблицы и рисунки — в git
DOLOS_BIN = ROOT / "node_modules" / ".bin" / "dolos"   # npm ci по package-lock.json

TOOLS = REPO / "tools"


def results_of(experiment: str) -> Path:
    path = RESULTS / experiment
    path.mkdir(parents=True, exist_ok=True)
    return path


def work_of(experiment: str) -> Path:
    path = WORK / experiment
    path.mkdir(parents=True, exist_ok=True)
    return path
