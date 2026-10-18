import argparse
import concurrent.futures
import io
import json
import time
import urllib.error
import urllib.request
import uuid
import zipfile

from jsonschema import Draft202012Validator, FormatChecker


def request(base, path, method="GET", data=None, headers=None):
    req = urllib.request.Request(base + path, data=data, method=method, headers=headers or {})
    try:
        response = urllib.request.urlopen(req, timeout=90)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        raw = response.read()
        value = json.loads(raw) if "json" in response.headers.get("Content-Type", "") else raw
        return response.status, value


def archive(marker):
    output = io.BytesIO()
    with zipfile.ZipFile(output, "w", zipfile.ZIP_DEFLATED) as zipped:
        for i in range(5):
            zipped.writestr(f"folder/Main{i}.java", f"// {marker}\nclass Main{i} {{ int value = {i}; }}\n")
    return output.getvalue()


def upload(base, data, title="smoke test"):
    boundary = "IntegrityLensBoundary"
    body = (f"--{boundary}\r\nContent-Disposition: form-data; name=\"language\"\r\n\r\njava\r\n"
            f"--{boundary}\r\nContent-Disposition: form-data; name=\"title\"\r\n\r\n{title}\r\n"
            f"--{boundary}\r\nContent-Disposition: form-data; name=\"archive\"; filename=\"demo.zip\"\r\n"
            "Content-Type: application/zip\r\n\r\n").encode() + data + f"\r\n--{boundary}--\r\n".encode()
    return request(base, "/api/batches", "POST", body, {"Content-Type": f"multipart/form-data; boundary={boundary}"})


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base", default="http://localhost:8080")
    args = parser.parse_args()
    base = args.base.rstrip("/")
    status, spec = request(base, "/v3/api-docs")
    assert status == 200
    assert spec["openapi"].startswith("3.1")
    assert request(base, "/swagger-ui/index.html")[0] == 200
    form = spec["paths"]["/api/batches"]["post"]["requestBody"]["content"]["multipart/form-data"]["schema"]
    assert {"archive", "language", "title"} <= set(form["properties"])

    def validate(value, name):
        schema = {"$ref": f"#/components/schemas/{name}", "components": spec["components"]}
        Draft202012Validator(schema, format_checker=FormatChecker()).validate(value)

    assert request(base, "/api/health")[0] == 200
    before = len(request(base, "/api/batches")[1])
    data = archive(str(uuid.uuid4()))
    with concurrent.futures.ThreadPoolExecutor(max_workers=6) as executor:
        responses = list(executor.map(lambda _: upload(base, data), range(6)))
    assert sorted(s for s, _ in responses) == [200, 200, 200, 200, 200, 202], responses
    ids = {b["id"] for _, b in responses}
    assert len(ids) == 1
    batch_id = ids.pop()
    for _, value in responses:
        validate(value, "Batch")
    assert len(request(base, "/api/batches")[1]) == before + 1
    assert upload(base, data, "changed title")[1]["title"] == "smoke test"
    for _ in range(120):
        status, batch = request(base, f"/api/batches/{batch_id}")
        assert status == 200
        validate(batch, "Batch")
        if batch["status"] in ("done", "failed"):
            break
        time.sleep(1)
    assert batch["status"] == "done", batch
    assert all(r["status"] == "done" and r["methodVersion"] == "mock-v1" for r in batch["runs"]), batch
    assert all(r["parsed"] == 5 and r["failed"] == 0 for r in batch["runs"])
    status, submissions = request(base, f"/api/batches/{batch_id}/submissions")
    validate(submissions, "Submissions")
    assert status == 200 and submissions["total"] == 5
    for submission in submissions["submissions"]:
        uuid.UUID(submission["id"])
        status, source = request(base, f"/api/submissions/{submission['id']}/source")
        assert status == 200 and "class Main" in source.decode("utf-8")
    status, pairs = request(base, f"/api/batches/{batch_id}/pairs")
    validate(pairs, "Pairs")
    assert status == 200 and pairs["total"] == 10
    assert len({p["id"] for p in pairs["pairs"]}) == 10
    assert pairs == request(base, f"/api/batches/{batch_id}/pairs")[1]
    for submission in submissions["submissions"]:
        for method, result in submission["methods"].items():
            scores = [p["scores"][method]["value"] for p in pairs["pairs"] if submission["id"] in (p["a"], p["b"])]
            assert result["score"] == max(scores)
            assert [m["score"] for m in result["matches"]] == sorted(scores, reverse=True)[:3]
    pair_id = pairs["pairs"][0]["id"]
    status, pair = request(base, f"/api/pairs/{pair_id}")
    validate(pair, "PairDetail")
    assert status == 200 and pair["feedback"] is None
    for decision in ("confirmed", "acceptable"):
        status, feedback = request(base, f"/api/pairs/{pair_id}/feedback", "PUT",
                                   json.dumps({"decision": decision, "comment": "Проверено"}).encode(),
                                   {"Content-Type": "application/json"})
        validate(feedback, "Feedback")
        assert status == 200 and feedback["decision"] == decision
    assert request(base, f"/api/pairs/{pair_id}")[1]["feedback"]["comment"] == "Проверено"
    assert request(base, f"/api/batches/{batch_id}/pairs")[1]["pairs"][0]["decision"] == "acceptable"
    status, error = upload(base, b"not a zip")
    validate(error, "Error")
    assert status == 400 and error["code"] == "NOT_A_ZIP"
    status, error = upload(base, b"x" * (10 * 1024 * 1024 + 1))
    assert status == 413 and error["code"] == "ARCHIVE_TOO_LARGE"
    status, error = request(base, f"/api/pairs/{pair_id}/feedback", "PUT", b'{"decision":"unknown"}',
                            {"Content-Type": "application/json"})
    assert status == 400 and error["code"] == "VALIDATION_ERROR"
    assert request(base, f"/api/batches/{uuid.uuid4()}")[0] == 404
    assert request(base, "/api/submissions/invalid/source")[0] == 400
    assert request(base, f"/api/submissions/{uuid.uuid4()}/source")[0] == 404
    assert request(base, "/api/pairs/invalid")[0] == 404
    assert len(request(base, "/api/batches")[1]) == before + 1
    print(f"PASS: concurrent deduplication, analysis, sources, scores, feedback, errors, OpenAPI ({batch_id})")


if __name__ == "__main__":
    main()
