from itertools import combinations

from .contract import CONTRACT


def _flipped(fragments):
    return [{"lines": f["b"], "withLines": f["a"]} for f in fragments]


def _same_side(fragments):
    return [{"lines": f["a"], "withLines": f["b"]} for f in fragments]


def assemble(job, version, params, scored, failed, stats, warnings=()):
    """Результат контракта из оценок разобранных пар.

    `scored` — `{(a, b): {"value", "details", "fragments"}}` при `a < b`, `failed` —
    `{id: причина}`. Пары с неразобранной работой, `score` и `matches` следуют из них.
    """
    ids = sorted(s["id"] for s in job["submissions"])
    pairs = []
    matches = {sid: [] for sid in ids}
    for a, b in combinations(ids, 2):
        if a in failed or b in failed:
            message = failed[a] if a in failed else failed[b]
            pairs.append({"a": a, "b": b, "value": None, "reason": {"code": "NOT_PARSED", "message": message}})
            continue
        pair = scored[a, b]
        pairs.append({"a": a, "b": b, **pair})
        matches[a].append({"with": b, "score": pair["value"], "fragments": _same_side(pair["fragments"])})
        matches[b].append({"with": a, "score": pair["value"], "fragments": _flipped(pair["fragments"])})
    submissions = []
    for sid in ids:
        if sid in failed:
            submissions.append({"id": sid, "status": "failed", "reason": {"code": "NOT_PARSED", "message": failed[sid]}})
            continue
        best = sorted(matches[sid], key=lambda m: (-m["score"], m["with"]))[:3]
        submissions.append({"id": sid, "status": "ok", "score": best[0]["score"] if best else None, "matches": best,
                            "stats": stats.get(sid, {})})
    return {"contract": CONTRACT, "method": job["method"], "methodVersion": version, "params": params,
            "submissions": submissions, "pairs": pairs, "warnings": list(warnings)}
