"""Метод на входе, ответ для которого известен заранее.

«Запустился» и «сработал» — разные вещи: JPlag без компилятора молча не разбирает ни
одного файла, а формат выгрузки Semgrep может поехать без ошибки. Проверка идёт через
весь core и запускается при сборке образа. Запуск заодно служит прогревом для AOT-кэша JVM.

    python -m worker.core.selfcheck jplag
"""
import json
import sys
import tempfile
from itertools import combinations
from pathlib import Path
from uuid import UUID

from .contract import CONTRACT, validate_result
from .engine import run

KNOWN = Path(__file__).parent / "known"
# Имя файла → ID. Порядок ID совпадает с порядком в списке, поэтому пары читаются легко.
IDS = {name: str(UUID(int=number + 1, version=4)) for number, name in
       enumerate(("base", "broken", "other", "partial", "renamed", "russian", "templated_first", "templated_second",
                  "tiny"))}
# Число меток structsim на base.java: если поедет формат выгрузки Semgrep, оно изменится.
BASE_LABELS = 172


def _job(input_dir, method):
    job = {"contract": CONTRACT, "runId": str(UUID(int=1, version=4)), "method": method, "params": {},
           "submissions": []}
    for name, sid in IDS.items():
        destination = input_dir / "submissions" / sid / "Main.java"
        destination.parent.mkdir(parents=True)
        destination.write_bytes((KNOWN / f"{name}.java").read_bytes())
        job["submissions"].append({"id": sid, "language": "java", "files": ["Main.java"]})
    (input_dir / "job.json").write_text(json.dumps(job), encoding="utf-8")
    return job


def _check_templates(submissions, pairs):
    """Набор шаблонов на месте и работает: стёртое не входит в совпавшие куски."""
    first, second = (submissions[name]["stats"]["templateLines"] for name in ("templated_first", "templated_second"))
    assert first and second, "шаблонные функции не найдены"
    assert not submissions["base"]["stats"]["templateLines"], "своя функция принята за шаблон"
    for fragment in pairs["templated_first", "templated_second"]["fragments"]:
        for side, lines in ((fragment["a"], first), (fragment["b"], second)):
            assert all(side[1] < start or end < side[0] for start, end in lines), (fragment, lines)


def check(method):
    with tempfile.TemporaryDirectory() as folder:
        root = Path(folder)
        input_dir = root / "input"
        input_dir.mkdir()
        job = _job(input_dir, method)
        result = run(input_dir, root / "output")
    validate_result(job, result)
    names = {sid: name for name, sid in IDS.items()}
    submissions = {names[s["id"]]: s for s in result["submissions"]}
    pairs = {(names[p["a"]], names[p["b"]]): p for p in result["pairs"]}
    assert len(pairs) == len(list(combinations(IDS, 2))), "не все пары"
    assert submissions["broken"]["status"] == "failed", "сломанный файл считается разобранным"
    assert all(submissions[name]["status"] == "ok" for name in ("base", "renamed", "other", "partial", "russian")), \
        submissions
    assert pairs["base", "renamed"]["value"] == 1.0, pairs["base", "renamed"]
    assert pairs["base", "renamed"]["fragments"], "у переименованной копии нет совпавших кусков"
    assert pairs["base", "other"]["value"] < pairs["base", "partial"]["value"] < 1.0, "неверный порядок оценок"
    assert all(line >= 1 for f in pairs["base", "renamed"]["fragments"] for line in f["a"] + f["b"])
    assert pairs["base", "broken"]["value"] is None and pairs["base", "broken"]["reason"]["code"] == "NOT_PARSED"
    _check_templates(submissions, pairs)
    if method == "structsim":
        assert submissions["base"]["stats"]["labels"] == BASE_LABELS, submissions["base"]["stats"]
    else:
        assert submissions["tiny"]["status"] == "failed", "слишком короткая работа считается разобранной"
    return result["methodVersion"]


if __name__ == "__main__":
    print(f"selfcheck passed: {check(sys.argv[1])}")
