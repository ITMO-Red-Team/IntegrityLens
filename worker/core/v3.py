"""Файловый контракт Structsim для снимка ревизии задания."""

import argparse
import hashlib
import json
import math
import sys
import tempfile
import time
from itertools import combinations
from pathlib import Path
from uuid import UUID

from . import template_filter
from .contract import InvalidInput, validate_schema
from .engine import Stopwatch, merged_params, sources_of
from .mock import _find_fragments

CONTRACT = "integritylens.core/3"
MOCK_VERSION = "structsim-mock/3"
IDENTITY = ("contract", "runId", "revision", "method", "methodVersion")


def method_version():
    from .structsim import METHOD_VERSION
    return f"{METHOD_VERSION}+core3+templates-{template_filter.version()}"


def _targets(job):
    return {s["id"] for s in job["submissions"] if s["scope"] == "target"}


def _expected_pairs(job):
    targets = _targets(job)
    return [(a, b) for a, b in combinations(sorted(s["id"] for s in job["submissions"]), 2)
            if a in targets or b in targets]


def validate_job(job):
    validate_schema("job-v3", job)
    ids = [s["id"] for s in job["submissions"]]
    if len(set(ids)) != len(ids) or any(str(UUID(sid)) != sid for sid in ids) or not _targets(job):
        raise InvalidInput()


def validate_result(job, result):
    validate_schema("result-v3", result)
    if any(job[key] != result[key] for key in IDENTITY):
        raise ValueError("Result identity mismatch")
    pairs = [(p["a"], p["b"]) for p in result["pairs"]]
    if sorted(pairs) != _expected_pairs(job):
        raise ValueError("Result pair membership or coverage mismatch")
    failed = [entry["id"] for entry in result["failedInputs"]]
    if len(set(failed)) != len(failed) or not set(failed) <= {s["id"] for s in job["submissions"]}:
        raise ValueError("Invalid failed input membership")
    for pair in result["pairs"]:
        invalid_input = pair["a"] in failed or pair["b"] in failed
        if invalid_input and (pair["value"] is not None or pair.get("reason", {}).get("code") != "NOT_PARSED"):
            raise ValueError("A failed input must fail all its pairs")
        if not invalid_input and pair.get("reason", {}).get("code") == "NOT_PARSED":
            raise ValueError("A parse failure requires a failed input")
        if pair["value"] is None:
            if "reason" not in pair or pair["fragments"]:
                raise ValueError("A failed pair requires a reason and empty fragments")
        elif "reason" in pair or not math.isfinite(pair["value"]):
            raise ValueError("Invalid successful pair")
        if any(not math.isfinite(value) for value in pair["details"].values()):
            raise ValueError("Non-finite details")
        for fragment in pair["fragments"]:
            if fragment["a"][0] > fragment["a"][1] or fragment["b"][0] > fragment["b"][1]:
                raise ValueError("Reversed fragment")


def _real(job, input_dir):
    from .structsim import DEFAULT_PARAMS, compare_sources

    if job["methodVersion"] != method_version():
        raise InvalidInput()
    params = merged_params(job, DEFAULT_PARAMS)
    if not isinstance(params["ignoreTemplates"], bool):
        raise InvalidInput()
    sources = sources_of(job, input_dir)
    watch = Stopwatch()
    if params["ignoreTemplates"]:
        with tempfile.TemporaryDirectory(prefix="core-") as folder:
            stripped = template_filter.strip_all(sources, Path(folder))
            _, scored, failed = compare_sources(sources_of(job, folder), params, watch, _targets(job))
        for (a, b), pair in scored.items():
            pair["fragments"] = [
                {"a": first, "b": second} for fragment in pair["fragments"]
                for first, second in template_filter.split_fragment(fragment["a"], fragment["b"],
                                                                    stripped[a], stripped[b])
            ]
    else:
        _, scored, failed = compare_sources(sources, params, watch, _targets(job))
    return params, scored, failed


def _mock(job, input_dir):
    if job["methodVersion"] != MOCK_VERSION:
        raise InvalidInput()
    params = {"mockScenario": "success", "delaySeconds": 0, **job["params"]}
    scenario = params["mockScenario"]
    if scenario == "invalid_input":
        raise InvalidInput()
    if scenario == "tool_error":
        raise RuntimeError("Mock tool error")
    time.sleep(float(params["delaySeconds"]))
    sources = {sid: path.read_bytes() for sid, path in sources_of(job, input_dir).items()}
    failed = {min(_targets(job)): "Mock parse failure"} if scenario == "partial" else {}
    hashes = {sid: hashlib.sha256(data).hexdigest() for sid, data in sources.items()}
    scored = {}
    for a, b in _expected_pairs(job):
        if a in failed or b in failed:
            continue
        seed = ":".join([MOCK_VERSION, *sorted((hashes[a], hashes[b]))])
        value = round(int(hashlib.sha256(seed.encode()).hexdigest()[:8], 16) / 0xFFFFFFFF, 4)
        scored[a, b] = {"value": value, "details": {}, "fragments": _find_fragments(sources[a], sources[b])}
    return params, scored, failed


def run(input_dir, output_dir):
    try:
        job = json.loads((input_dir / "job.json").read_text(encoding="utf-8"))
        validate_job(job)
    except (OSError, ValueError) as error:
        raise InvalidInput() from error
    analyze = _mock if "mockScenario" in job["params"] else _real
    params, scored, failed = analyze(job, input_dir)
    pairs = []
    for a, b in _expected_pairs(job):
        if a in failed or b in failed:
            pair = {"value": None, "details": {}, "fragments": [], "reason": {"code": "NOT_PARSED"}}
        else:
            pair = scored[a, b]
        pairs.append({"a": a, "b": b, **pair})
    result = {**{key: job[key] for key in IDENTITY}, "params": params, "pairs": pairs,
              "failedInputs": [{"id": sid, "reason": {"code": "NOT_PARSED"}} for sid in sorted(failed)]}
    validate_result(job, result)
    output_dir.mkdir(parents=True, exist_ok=True)
    (output_dir / "result.json").write_text(json.dumps(result, allow_nan=False), encoding="utf-8")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    try:
        run(args.input, args.output)
    except InvalidInput:
        return 2
    except Exception as error:
        print(f"Structsim failed: {type(error).__name__}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
