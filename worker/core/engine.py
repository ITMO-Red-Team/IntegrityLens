import json
import math
import os
import tempfile
import time
from pathlib import Path

from . import template_filter
from .contract import InvalidInput, validate_schema

IGNORE_TEMPLATES = "ignoreTemplates"


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
    defaults = {**defaults, IGNORE_TEMPLATES: True}
    if not set(job["params"]) <= set(defaults):
        raise InvalidInput()
    return {**defaults, **job["params"]}


def positive_int(params, name):
    value = params[name]
    if not isinstance(value, int) or isinstance(value, bool) or not 1 <= value < 2 ** 31:
        raise InvalidInput()
    return value


def _without_templates(job, input_dir, artifacts, watch, analyze):
    """Метод видит копию входа без шаблонных функций, а в ответе остаётся, что из него стёрто."""
    with tempfile.TemporaryDirectory(prefix="core-") as folder:
        stripped = template_filter.strip_all(sources_of(job, input_dir), Path(folder))
        watch.lap("templates")
        result = analyze(job, Path(folder), artifacts, watch)
    result["methodVersion"] += f"+templates-{template_filter.version()}"
    for pair in result["pairs"]:
        if pair.get("fragments"):
            pair["fragments"] = [{"a": a, "b": b} for fragment in pair["fragments"] for a, b in
                                 template_filter.split_fragment(fragment["a"], fragment["b"],
                                                                stripped[pair["a"]], stripped[pair["b"]])]
    for submission in result["submissions"]:
        if submission["status"] == "ok":
            own = stripped[submission["id"]]
            submission["stats"]["templateLines"] = own
            for match in submission["matches"]:
                match["fragments"] = [{"lines": lines, "withLines": other} for fragment in match["fragments"]
                                      for lines, other in template_filter.split_fragment(
                                          fragment["lines"], fragment["withLines"], own, stripped[match["with"]])]
    return result


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
    ignore_templates = job["params"].get(IGNORE_TEMPLATES, True)
    if not isinstance(ignore_templates, bool):
        raise InvalidInput()
    artifacts = output_dir / "artifacts"
    artifacts.mkdir(parents=True, exist_ok=True)
    watch = Stopwatch()
    if ignore_templates:
        result = _without_templates(job, input_dir, artifacts, watch, analyze)
    else:
        result = analyze(job, input_dir, artifacts, watch)
    (output_dir / "result.json").write_text(json.dumps(result, ensure_ascii=False), encoding="utf-8")
    watch.lap("write")
    (artifacts / "timings.json").write_text(json.dumps(watch.timings), encoding="utf-8")
    return result
