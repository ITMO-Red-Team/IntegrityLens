import json
from pathlib import Path

from .contract import InvalidInput, validate_schema

# Рабочее значение для int2-кванта GigaCheck: порог модельной карты (0.8) не
# пересекается ни одним confidence на реальном коде, 0.05 даёт сигнал (см. README).
DEFAULT_THRESHOLD = 0.05


def load_job(input_dir):
    try:
        job = json.loads((Path(input_dir) / "job.json").read_text(encoding="utf-8"))
        validate_schema("job", job)
    except Exception as exc:
        raise InvalidInput() from exc
    if len({f["submissionId"] for f in job["inputFiles"]}) != len(job["inputFiles"]):
        raise InvalidInput()
    return job


def input_paths(job, input_dir):
    """Пути входных файлов. Путь в задании — относительно рабочего каталога,
    то есть родителя `input/`. Файл без файла — неверный вход, а не сбой метода."""
    root = Path(input_dir).parent
    paths = {f["submissionId"]: root / f["path"] for f in job["inputFiles"]}
    if not all(path.is_file() for path in paths.values()):
        raise InvalidInput()
    return paths


def merged_params(job):
    """Параметры прогона. Неизвестный параметр нигде не применился бы, поэтому это ошибка."""
    defaults = {"threshold": DEFAULT_THRESHOLD}
    if not set(job["params"]) <= set(defaults):
        raise InvalidInput()
    params = {key: value for key, value in job["params"].items() if key in defaults}
    threshold = params.get("threshold", DEFAULT_THRESHOLD)
    if not isinstance(threshold, (int, float)) or isinstance(threshold, bool) or not 0 <= threshold <= 1:
        raise InvalidInput()
    return {**defaults, "threshold": float(threshold)}


def run(input_dir, output_dir):
    input_dir, output_dir = Path(input_dir), Path(output_dir)
    job = load_job(input_dir)
    if "mockScenario" in job["params"]:
        from . import mock

        result = mock.run(job, input_dir)
    else:
        from . import real

        result = real.analyze(job, merged_params(job), input_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    (output_dir / "result.json").write_text(json.dumps(result, ensure_ascii=False), encoding="utf-8")
    return result
