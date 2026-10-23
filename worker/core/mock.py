import hashlib
import json
import re
import time
from itertools import combinations
from pathlib import Path

from .contract import CONTRACT, validate_result, validate_schema


class InvalidInput(Exception):
    pass


# Ключевые токены, которые сохраняются при нормализации (аналогично мок-движку
# фронта): остальные идентификаторы (локальные переменные/методы) заменяются на
# "V", числа — на "N", строки — на "S". Так схожие по структуре куски кода с
# разными именами переменных совпадают и выделяются как подозрительные.
_KEEP = set(
    (
        "abstract assert boolean break byte case catch char class continue default do double else enum extends "
        "final finally float for if implements import instanceof int interface long native new package private "
        "protected public return short static super switch synchronized this throw throws try void volatile while "
        "var true false null String System out println print Math Integer Long Arrays Collections Objects Scanner "
        "List ArrayList LinkedList Map HashMap TreeMap Set HashSet TreeSet Queue Deque ArrayDeque PriorityQueue "
        "Iterator Comparator add addAll addFirst addLast get put poll pollFirst pollLast offer push pop peek remove "
        "size isEmpty contains containsKey sort fill length nextInt of forEach stream toList main args"
    ).split(" ")
)


def _tokenize(s):
    return re.findall(r'[A-Za-z_$][A-Za-z0-9_$]*|\d+|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|::|->|\+\+|--|[<>=!]=|&&|\|\||\S', s)


def _norm(line):
    """Нормализация строки для сравнения: локальные идентификаторы → V, числа → N, строки → S."""
    s = line.strip()
    if not s or not s.strip("{}();\t "):
        return ""
    toks = _tokenize(s)
    out = []
    for t in toks:
        if re.match(r"^[A-Za-z_$]", t):
            out.append(t if t in _KEEP else "V")
        elif re.match(r"^\d", t):
            out.append("N")
        elif re.match(r'^["\']', t):
            out.append("S")
        else:
            out.append(t)
    return " ".join(out)


def _find_fragments(source_a, source_b, min_len=4):
    """Ищет совпадающие последовательности строк между двумя исходниками.

    Возвращает список фрагментов {"a": [from, to], "b": [from, to]} — границы
    1-based, включены (как требует контракт). Ищем серии совпадающих
    нормализованных строк (без пустых и чисто-скобочных), непересекающиеся
    между собой. Пара со схожим кодом получит подозрительные участки, которые
    фронт подсветит при сравнении.
    """
    raw_a = source_a.decode("utf-8", "replace").splitlines()
    raw_b = source_b.decode("utf-8", "replace").splitlines()

    # Нормализуем заранее: строка -> (нормализованный текст) или None (пропуск).
    def prep(raw):
        out = []
        for k, line in enumerate(raw, start=1):
            t = _norm(line)
            if t:
                out.append((k, t))
        return out

    a = prep(raw_a)
    b = prep(raw_b)

    fragments = []
    used_a = set()
    used_b = set()

    # Жадный поиск: для каждого старта в a ищем самую длинную серию с b,
    # берём только серии >= min_len и не пересекающиеся с уже взятыми.
    for ia in range(len(a)):
        if a[ia][0] in used_a:
            continue
        for ib in range(len(b)):
            if b[ib][0] in used_b:
                continue
            if a[ia][1] != b[ib][1]:
                continue
            # Продлеваем серию.
            L = 0
            while ia + L < len(a) and ib + L < len(b):
                if a[ia + L][0] in used_a or b[ib + L][0] in used_b:
                    break
                if a[ia + L][1] != b[ib + L][1]:
                    break
                L += 1
            if L < min_len:
                continue
            # Помечаем использованные строки серии.
            for k in range(L):
                used_a.add(a[ia + k][0])
                used_b.add(b[ib + k][0])
            fragments.append({
                "a": [a[ia][0], a[ia + L - 1][0]],
                "b": [b[ib][0], b[ib + L - 1][0]],
            })
            break
    return fragments


def run(input_dir, output_dir):
    input_dir = Path(input_dir)
    output_dir = Path(output_dir)
    try:
        job = json.loads((input_dir / "job.json").read_text(encoding="utf-8"))
        validate_schema("job", job)
        if len({s["id"] for s in job["submissions"]}) != len(job["submissions"]):
            raise InvalidInput()
        hashes = {}
        sources = {}
        for submission in job["submissions"]:
            name = submission["files"][0]
            source = input_dir / "submissions" / submission["id"] / name
            hashes[submission["id"]] = hashlib.sha256(source.read_bytes()).hexdigest()
            sources[submission["id"]] = source.read_bytes()
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
            fragments = _find_fragments(sources[a], sources[b])
            pairs.append({"a": a, "b": b, "value": value, "details": {}, "fragments": fragments})
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
