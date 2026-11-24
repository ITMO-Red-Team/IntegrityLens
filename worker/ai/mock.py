"""Мок движка ai: те же сценарии, что у core. Фрагменты — первая и последняя строки
файла, score — доля помеченных байтов, как у настоящего движка."""
import hashlib
import time

from .contract import CONTRACT, InvalidInput, validate_result
from .engine import DEFAULT_THRESHOLD, input_paths


def run(job, input_dir):
    params = {"threshold": DEFAULT_THRESHOLD, "mockScenario": "success", "delaySeconds": 0, **job["params"]}
    scenario = params["mockScenario"]
    if scenario == "invalid_input":
        raise InvalidInput()
    if scenario == "tool_error":
        raise RuntimeError("Mock tool error")
    time.sleep(float(params["delaySeconds"]))
    paths = input_paths(job, input_dir)
    sources, files = {}, []
    for number, entry in enumerate(job["inputFiles"]):
        sid = entry["submissionId"]
        data = sources[sid] = paths[sid].read_bytes()
        if scenario == "partial" and number == 0:
            files.append({"submissionId": sid, "status": "failed", "score": None, "suspicious": [],
                          "reason": {"code": "NOT_PARSED", "message": "Mock parse failure"}})
            continue
        fragments = _fragments(data)
        score = sum(f["endIndex"] - f["startIndex"] for f in fragments) / len(data) if data else 0.0
        files.append({"submissionId": sid, "status": "ok", "score": score, "suspicious": fragments})
    result = {"contract": CONTRACT, "runId": job["runId"], "methodVersion": job["methodVersion"],
              "params": params, "files": files}
    validate_result(job, result, sources)
    return result


def _fragments(data):
    """0–2 фрагмента по границам строк, confidence детерминирован содержимым."""
    if not data:
        return []
    confidence = round(int(hashlib.sha256(data).hexdigest()[:4], 16) / 0xFFFF, 4)
    lines = data.splitlines(keepends=True)
    fragments = [{"startIndex": 0, "endIndex": len(lines[0]), "confidence": confidence}]
    if len(lines) > 1:
        length = len(lines[-1])
        fragments.append({"startIndex": len(data) - length, "endIndex": len(data), "confidence": confidence})
    return fragments
