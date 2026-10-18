import hashlib
import json
import time
from itertools import combinations
from pathlib import Path

from .contract import CONTRACT, validate_result, validate_schema


class InvalidInput(Exception):
    pass


def run(input_dir, output_dir):
    input_dir = Path(input_dir)
    output_dir = Path(output_dir)
    try:
        job = json.loads((input_dir / "job.json").read_text(encoding="utf-8"))
        validate_schema("job", job)
        if len({s["id"] for s in job["submissions"]}) != len(job["submissions"]):
            raise InvalidInput()
        hashes = {}
        for submission in job["submissions"]:
            name = submission["files"][0]
            source = input_dir / "submissions" / submission["id"] / name
            hashes[submission["id"]] = hashlib.sha256(source.read_bytes()).hexdigest()
    except Exception as exc:
        raise InvalidInput() from exc
    params = {"mockScenario": "success", "delaySeconds": 0, **job["params"]}
    scenario = params["mockScenario"]
    if scenario == "invalid_input":
        raise InvalidInput()
    if scenario == "tool_error":
        raise RuntimeError("Mock tool error")
    time.sleep(float(params["delaySeconds"]))
    ids = sorted(hashes)
    failed = {ids[0]} if scenario == "partial" else set()
    pairs = []
    for a, b in combinations(ids, 2):
        if a in failed or b in failed:
            pairs.append({"a": a, "b": b, "value": None,
                          "reason": {"code": "NOT_PARSED", "message": "Mock parse failure"}})
        else:
            seed = ":".join(["mock-v1", job["method"], *sorted([hashes[a], hashes[b]])])
            value = round(int(hashlib.sha256(seed.encode()).hexdigest()[:8], 16) / 0xFFFFFFFF, 4)
            pairs.append({"a": a, "b": b, "value": value, "details": {}, "fragments": []})
    submissions = []
    for sid in ids:
        if sid in failed:
            submissions.append({"id": sid, "status": "failed",
                                "reason": {"code": "NOT_PARSED", "message": "Mock parse failure"}})
            continue
        matches = [{"with": p["b"] if p["a"] == sid else p["a"], "score": p["value"], "fragments": []}
                   for p in pairs if sid in (p["a"], p["b"]) and p["value"] is not None]
        matches.sort(key=lambda m: (-m["score"], m["with"]))
        submissions.append({"id": sid, "status": "ok", "score": matches[0]["score"] if matches else None,
                            "matches": matches[:3], "stats": {}})
    result = {"contract": CONTRACT, "method": job["method"], "methodVersion": "mock-v1",
              "params": params, "submissions": submissions, "pairs": pairs,
              "warnings": ["MOCK_RESULTS: scores do not measure code similarity"]}
    validate_result(job, result)
    output_dir.mkdir(parents=True, exist_ok=True)
    (output_dir / "artifacts").mkdir(exist_ok=True)
    (output_dir / "result.json").write_text(json.dumps(result, ensure_ascii=False), encoding="utf-8")
    return result
