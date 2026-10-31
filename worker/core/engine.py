import json
import math
import os
import time
from pathlib import Path

from .contract import InvalidInput, validate_schema


def available_cpus():
    """Сколько ядер можно занять. `os.cpu_count()` не видит лимит контейнера."""
    configured = os.environ.get("CORE_THREADS")
    if configured:
        return max(1, int(configured))
    try:
        cpus = len(os.sched_getaffinity(0))
    except AttributeError:
        cpus = os.cpu_count() or 1
    try:
        quota, period = Path("/sys/fs/cgroup/cpu.max").read_text().split()
        if quota != "max":
            cpus = min(cpus, max(1, math.ceil(int(quota) / int(period))))
    except (OSError, ValueError):
        pass
    return cpus


class Stopwatch:
    """Время этапов запуска. Попадает в артефакты: в `result.json` ему нельзя,
    результат должен воспроизводиться."""

    def __init__(self):
        self.timings = {}
        self.last = time.perf_counter()

    def lap(self, stage):
        now = time.perf_counter()
        self.timings[f"{stage}Ms"] = round((now - self.last) * 1000, 1)
        self.last = now


def load_job(input_dir):
    try:
        job = json.loads((Path(input_dir) / "job.json").read_text(encoding="utf-8"))
        validate_schema("job", job)
    except Exception as exc:
        raise InvalidInput() from exc
    if len({s["id"] for s in job["submissions"]}) != len(job["submissions"]):
        raise InvalidInput()
    return job


def sources_of(job, input_dir):
    """Пути исходников по ID. Работа без файла — неверный вход, а не сбой метода."""
    sources = {s["id"]: Path(input_dir) / "submissions" / s["id"] / s["files"][0] for s in job["submissions"]}
    if not all(path.is_file() for path in sources.values()):
        raise InvalidInput()
    return sources


def merged_params(job, defaults):
    """Параметры запуска. Неизвестный параметр нигде не применился бы, поэтому это ошибка."""
    if not set(job["params"]) <= set(defaults):
        raise InvalidInput()
    return {**defaults, **job["params"]}


def positive_int(params, name):
    value = params[name]
    if not isinstance(value, int) or isinstance(value, bool) or value < 1:
        raise InvalidInput()
    return value


def run(input_dir, output_dir):
    input_dir, output_dir = Path(input_dir), Path(output_dir)
    job = load_job(input_dir)
    if "mockScenario" in job["params"]:
        from . import mock
        return mock.run(input_dir, output_dir)
    # Модули методов импортируются по требованию: в образе каждого метода только его инструменты.
    if job["method"] == "jplag":
        from .jplag import analyze
    else:
        from .structsim import analyze
    artifacts = output_dir / "artifacts"
    artifacts.mkdir(parents=True, exist_ok=True)
    watch = Stopwatch()
    result = analyze(job, input_dir, artifacts, watch)
    (output_dir / "result.json").write_text(json.dumps(result, ensure_ascii=False), encoding="utf-8")
    watch.lap("write")
    (artifacts / "timings.json").write_text(json.dumps(watch.timings), encoding="utf-8")
    return result
