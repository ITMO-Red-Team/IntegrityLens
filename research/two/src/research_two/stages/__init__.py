"""Стадии эксперимента: `uv run stage s00` … `uv run stage s50`, `uv run stage all`.

Каждая стадия — модуль `sNN_*.py` с функцией `run()`. Стадии идемпотентны и
запускаются по отдельности: падение на s20 не заставляет перезапускать s00.
"""

from __future__ import annotations

import argparse
import importlib
import pkgutil
import sys
import time


def _stages() -> dict[str, str]:
    found = {}
    for module in pkgutil.iter_modules(__path__):
        if module.name[:1] == "s" and module.name[1:3].isdigit():
            found[module.name[:3]] = module.name
    return dict(sorted(found.items()))


def main() -> int:
    for stream in (sys.stdout, sys.stderr):
        stream.reconfigure(encoding="utf-8", errors="replace")
    stages = _stages()
    parser = argparse.ArgumentParser(description="Стадии эксперимента 2")
    parser.add_argument("stage", nargs="+", choices=[*stages, "all"])
    args = parser.parse_args()
    wanted = list(stages) if "all" in args.stage else args.stage
    for key in wanted:
        module = importlib.import_module(f"{__name__}.{stages[key]}")
        print(f"=== {stages[key]}", flush=True)
        started = time.perf_counter()
        module.run()
        print(f"=== {stages[key]}: {time.perf_counter() - started:.1f} с", flush=True)
    return 0
