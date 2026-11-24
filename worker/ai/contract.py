import json
from pathlib import Path

import fastjsonschema

CONTRACT = "integritylens.ai/1"
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


def validate_result(job, result, sources=None):
    """Формат результата и его согласованность с заданием.

    `sources` — содержимое входных файлов по submissionId; с ним фрагменты проверяются
    ещё и по файлу: внутри него и по границам символов UTF-8.
    """
    validate_schema("result", result)
    if result["runId"] != job["runId"]:
        raise ValueError("Result run mismatch")
    if result["methodVersion"] != job["methodVersion"]:
        raise ValueError("Result method version mismatch")
    expected = {f["submissionId"] for f in job["inputFiles"]}
    actual = [f["submissionId"] for f in result["files"]]
    if len(actual) != len(expected) or set(actual) != expected:
        raise ValueError("Result files mismatch")
    for file in result["files"]:
        if file["status"] != "ok" and (file["score"] is not None or file["suspicious"]):
            raise ValueError("Failed file must not carry an assessment")
        data = None if sources is None else sources.get(file["submissionId"])
        previous_end = 0
        for fragment in file["suspicious"]:
            start, end = fragment["startIndex"], fragment["endIndex"]
            if start >= end:
                raise ValueError("Empty fragment")
            if start < previous_end:
                raise ValueError("Fragments overlap or are unordered")
            previous_end = end
            if data is not None and not (_starts_character(data, start) and _starts_character(data, end)):
                raise ValueError("Fragment is outside the file or splits a character")


def _starts_character(data, offset):
    """Смещение в пределах файла и не внутри многобайтового символа: байт
    продолжения UTF-8 имеет вид 10xxxxxx."""
    return offset == len(data) or offset < len(data) and data[offset] & 0xC0 != 0x80
