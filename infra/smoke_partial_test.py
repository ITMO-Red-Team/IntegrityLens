"""Regression: a malformed Java input must not invalidate its peers' scores."""
import argparse
from uuid import uuid4

from smoke_test import Api, JAVA


def check(base, timeout):
    api = Api(base)
    suffix = uuid4().hex[:12]
    _, teacher = api.auth(f"partial-teacher-{suffix}@example.org", "teacher", "Partial teacher")
    students = [api.auth(f"partial-{n}-{suffix}@example.org", "student", f"Partial {n}")[1]
                for n in range(4)]
    assignment, link = api.assignment(teacher, "Partial comparison regression")
    path = "/api/assignments/" + assignment
    good = api.submit(link, students[0])["submissionId"]
    bad = api.submit(link, students[1], b"Hello world!")["submissionId"]
    first = api.wait_scores(path, teacher, good, ("not_applicable", "ok"), timeout)
    assert first["similarity"] == {
        "status": "not_applicable", "value": None, "comparisons": {"successful": 0, "skipped": 1}
    }, first
    broken = api.wait_scores(path, teacher, bad, ("failed", "ok"), timeout)
    assert broken["similarity"]["reason"]["code"] == "NOT_PARSED"
    peer = api.submit(link, students[2])["submissionId"]
    for work in (good, peer):
        result = api.wait_scores(path, teacher, work, ("ok", "ok"), timeout)
        assert result["similarity"]["value"] == 1.0
        assert result["similarity"]["comparisons"] == {"successful": 1, "skipped": 1}
    pair = api.call("GET", f"{path}/pairs/{good}/{bad}", 200, session=teacher)[0]
    assert pair["score"]["status"] == "failed" and pair["methods"]["structsim"]["fragments"] == []
    summary = api.call("GET", path, 200, session=teacher)[0]
    assert summary["analysis"] == {"completed": 3, "failed": 1}, summary

    # Every input is malformed: each fails independently.
    all_bad, all_bad_link = api.assignment(teacher, "All malformed")
    for session in students[:2]:
        work = api.submit(all_bad_link, session, b"Hello world!")["submissionId"]
    rows = "/api/assignments/" + all_bad
    api.wait_scores(rows, teacher, work, ("failed", "ok"), timeout)
    assert all(row["similarity"]["status"] == "failed" for row in api.rows(rows, teacher)["items"])

    # A bad direct source only reduces coverage in the target assignment.
    target, target_link = api.assignment(teacher, "Bad source", [all_bad])
    for session in students[2:]:
        work = api.submit(target_link, session, JAVA)["submissionId"]
    rows = "/api/assignments/" + target
    result = api.wait_scores(rows, teacher, work, ("ok", "ok"), timeout)
    assert result["similarity"]["comparisons"] == {"successful": 1, "skipped": 2}
    assert result["similarity"]["value"] == 1.0
    print("Partial comparison, valid-only-invalid peers, invalid sources and all-invalid inputs checked", flush=True)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--base", default="http://localhost:8080")
    parser.add_argument("--timeout", type=float, default=300)
    args = parser.parse_args()
    check(args.base, args.timeout)
