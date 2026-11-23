"""Известный ответ для движка ai.

    python -m worker.ai.selfcheck mock   # контракт целиком, работает в любом образе
    python -m worker.ai.selfcheck real   # + загрузка модели и воспроизводимость

«Запустился» и «сработал» — разные вещи: real грузит модель и дважды прогоняет
тот же файл — результаты обязаны совпасть (контракт: одинаковые файлы, версия и
параметры дают одинаковые оценки).
"""
import json
import sys
import tempfile
from pathlib import Path
from uuid import UUID

from .contract import validate_result
from .engine import run
from .version import method_version

BOM = b"\xef\xbb\xbf"
BASE = """class Base {
    static int solve(int[] values, int limit) {
        int total = 0;
        for (int value : values) {
            total += value < limit ? value : 0;
        }
        return total;
    }

    public static void main(String[] args) {
        System.out.println(solve(new int[] {1, 2, 3}, 2));
    }
}
""".encode("utf-8")
RUSSIAN = """// Поиск максимума: сравнение идёт попарно.
class Russian {
    static int max(int[] values) {
        int best = values[0];
        for (int value : values) {
            if (value > best) {
                best = value; // новый максимум
            }
        }
        return best;
    }
}
""".encode("utf-8")

# Имя файла → содержимое. Настоящему движку хватает одного файла: остальное дешевле проверить моком.
KNOWN = {"base": BASE, "russian": RUSSIAN, "bom": BOM + BASE}


def _job(root, params, known):
    input_dir = root / "input"
    job = {"contract": "integritylens.ai/1", "runId": str(UUID(int=1, version=4)), "methodVersion": known["version"],
           "params": params, "inputFiles": []}
    for name, sid in known["ids"].items():
        destination = input_dir / "submissions" / sid / "Main.java"
        destination.parent.mkdir(parents=True)
        destination.write_bytes(KNOWN[name])
        job["inputFiles"].append({"submissionId": sid, "path": f"input/submissions/{sid}/Main.java"})
    (input_dir / "job.json").write_text(json.dumps(job), encoding="utf-8")
    return job


def _assert_fragments(job, result, known):
    """Фрагменты — байтовые смещения UTF-8: внутри файла и по границам символов."""
    names = {sid: name for name, sid in known["ids"].items()}
    validate_result(job, result, {sid: KNOWN[name] for sid, name in names.items()})
    for file in result["files"]:
        data = KNOWN[names[file["submissionId"]]]
        for fragment in file["suspicious"]:
            start, end = fragment["startIndex"], fragment["endIndex"]
            data[:start].decode("utf-8")  # префикс декодируется строго, только если start — граница символа
            data[start:end].decode("utf-8")


def check(mode):
    names = list(KNOWN) if mode == "mock" else ["base"]
    known = {"version": "selfcheck" if mode == "mock" else method_version(),
             "ids": {name: str(UUID(int=number + 1, version=4)) for number, name in enumerate(names)}}
    with tempfile.TemporaryDirectory() as folder:
        root = Path(folder)
        params = {"mockScenario": "success", "delaySeconds": 0} if mode == "mock" else {}
        job = _job(root, params, known)
        first = run(root / "input", root / "output-first")
        second = run(root / "input", root / "output-second")
    assert first == second, "движок недетерминирован"
    _assert_fragments(job, first, known)
    assert all(file["status"] == "ok" for file in first["files"]), first["files"]
    return first["methodVersion"]


if __name__ == "__main__":
    print(f"selfcheck passed: {check(sys.argv[1])}")
