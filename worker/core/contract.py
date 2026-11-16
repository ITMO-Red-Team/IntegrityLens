import json
from itertools import combinations
from pathlib import Path

import fastjsonschema

CONTRACT = "integritylens.core/1"
SCHEMAS = Path(__file__).parent / "schemas"
FORMATS = {"uuid": r"^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"}
_VALIDATORS = {}


class InvalidInput(Exception):
    pass


def validate_schema(name, value):
    # Схема компилируется в код: проверка результата на порядок быстрее, чем у jsonschema
    # (замеры в worker/bench/README.md). Схемы не меняются, поэтому компиляция одна на процесс.
    validate = _VALIDATORS.get(name)
    if validate is None:
        schema = json.loads((SCHEMAS / f"{name}.schema.json").read_text())
        validate = _VALIDATORS[name] = fastjsonschema.compile(schema, formats=FORMATS)
    validate(value)


def validate_result(job, result):
    validate_schema("result", result)
    if result["method"] != job["method"]:
        raise ValueError("Result method mismatch")
    expected = {s["id"] for s in job["submissions"]}
    actual = [s["id"] for s in result["submissions"]]
    if len(actual) != len(expected) or set(actual) != expected:
        raise ValueError("Result submissions mismatch")
    pairs = [(p["a"], p["b"]) for p in result["pairs"]]
    if len(pairs) != len(set(pairs)) or set(pairs) != set(combinations(sorted(expected), 2)):
        raise ValueError("Result pairs mismatch")
    by_id = {s["id"]: s for s in result["submissions"]}
    by_pair = {(p["a"], p["b"]): p for p in result["pairs"]}
    best = {}
    for pair in result["pairs"]:
        value = pair["value"]
        if value is None:
            continue
        for sid in (pair["a"], pair["b"]):
            if by_id[sid]["status"] == "failed":
                raise ValueError("Scored pair contains a failed submission")
            if value > best.get(sid, -1):
                best[sid] = value
    for submission in result["submissions"]:
        if submission["status"] != "ok":
            continue
        if submission["score"] != best.get(submission["id"]):
            raise ValueError("Submission score is inconsistent with pairs")
        matches = submission["matches"]
        if len({m["with"] for m in matches}) != len(matches):
            raise ValueError("Duplicate matches")
        for match in matches:
            key = tuple(sorted((submission["id"], match["with"])))
            if key not in by_pair or by_pair[key]["value"] != match["score"]:
                raise ValueError("Match is inconsistent with pairs")
