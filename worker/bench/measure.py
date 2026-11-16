"""Один запуск core отдельным процессом: стенные часы, CPU, память и время этапов."""
import json
import os
import statistics
import subprocess
import sys
import tempfile
import time
from pathlib import Path

from worker.bench.corpus import write_job


def run_core(input_dir, env=None):
    with tempfile.TemporaryDirectory(prefix="bench-") as folder:
        output = Path(folder)
        started = time.perf_counter()
        process = subprocess.Popen([sys.executable, "-m", "worker.core", "run", "--input", str(input_dir),
                                    "--output", str(output)], env={**os.environ, **(env or {})},
                                   stdout=subprocess.DEVNULL, stderr=subprocess.PIPE)
        stderr = process.stderr.read()
        _, status, usage = os.wait4(process.pid, 0)
        wall = time.perf_counter() - started
        if os.waitstatus_to_exitcode(status) != 0:
            raise RuntimeError(f"core: {stderr.decode('utf-8', 'replace')[-500:]}")
        timings = json.loads((output / "artifacts" / "timings.json").read_text())
        pairs = json.loads((output / "result.json").read_text())["pairs"]
    # ru_maxrss в килобайтах на Linux и в байтах на macOS. ru_maxrss детей — максимум, а не сумма.
    rss_mb = usage.ru_maxrss / (1024 if sys.platform == "linux" else 1024 * 1024)
    return {"wall_s": wall, "cpu_s": usage.ru_utime + usage.ru_stime, "rss_mb": rss_mb, "pairs": len(pairs), **timings}


def repeated(sources, method, repeats, env=None, params=None):
    """Медиана по повторам: первый запуск прогревает диск, он отбрасывается вместе с прочими выбросами."""
    with tempfile.TemporaryDirectory(prefix="bench-input-") as folder:
        write_job(folder, sources, method, params)
        runs = [run_core(folder, env) for _ in range(repeats)]
    return {key: round(statistics.median(run.get(key, 0) for run in runs), 3) for key in runs[0]}
