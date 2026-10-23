import json
from itertools import combinations
from pathlib import Path

from jsonschema import Draft202012Validator, FormatChecker

CONTRACT = "integritylens.core/1"
SCHEMAS = Path(__file__).parent / "schemas"


def validate_schema(name, value):
    schema = json.loads((SCHEMAS / f"{name}.schema.json").read_text())
    Draft202012Validator(schema, format_checker=FormatChecker()).validate(value)


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
    for pair in result["pairs"]:
        if pair["value"] is not None and any(by_id[s]["status"] == "failed" for s in (pair["a"], pair["b"])):
            raise ValueError("Scored pair contains a failed submission")
    for submission in result["submissions"]:
        if submission["status"] != "ok":
            continue
        values = [p["value"] for p in result["pairs"]
                  if submission["id"] in (p["a"], p["b"]) and p["value"] is not None]
        if submission["score"] != (max(values) if values else None):
            raise ValueError("Submission score is inconsistent with pairs")
        matches = submission["matches"]
        if len({m["with"] for m in matches}) != len(matches):
            raise ValueError("Duplicate matches")
        for match in matches:
            key = tuple(sorted((submission["id"], match["with"])))
            if key not in by_pair or by_pair[key]["value"] != match["score"]:
                raise ValueError("Match is inconsistent with pairs")
