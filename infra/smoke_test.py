"""Проверяет backend v2 через HTTP и валидирует ответы по согласованному OpenAPI."""

import argparse
import json
import re
import time
import urllib.error
import urllib.parse
import urllib.request
from concurrent.futures import ThreadPoolExecutor
from http.cookies import SimpleCookie
from pathlib import Path
from uuid import uuid4

import yaml
from jsonschema import Draft202012Validator, FormatChecker

REPO_ROOT = Path(__file__).resolve().parents[1]
SPEC_PATH = REPO_ROOT / "docs" / "openapi.yaml"
PASSWORD = "  тест smoke  "
JAVA = """class Example {
    static long evaluate(int limit) {
        long sum = 0;
        for (int i = 0; i < limit; i++) {
            int value = i * i;
            if (value % 3 == 0) {
                sum += value;
            }
        }
        return sum;
    }

    public static void main(String[] args) {
        System.out.println(evaluate(42)); // Сумма
    }
}
""".encode()
METHODS = {"get", "post", "put", "delete", "patch"}


class Api:
    def __init__(self, base):
        self.base = base.rstrip("/")
        self.spec = yaml.safe_load(SPEC_PATH.read_text(encoding="utf-8"))
        self.routes = [(re.compile(re.sub(r"\{[^}]+\}", "[^/]+", path)), operations)
                       for path, operations in self.spec["paths"].items()]
        self.validators = {}
        self.visited = set()

    def call(self, method, path, status, *, session=None, body=None, files=None, code=None):
        headers = {}
        if session:
            headers["Cookie"] = f"session={session}"
        if body is not None:
            headers["Content-Type"] = "application/json"
            payload = json.dumps(body).encode()
        elif files is not None:
            boundary = uuid4().hex
            headers["Content-Type"] = f"multipart/form-data; boundary={boundary}"
            payload = multipart(boundary, files)
        else:
            payload = None
        request = urllib.request.Request(self.base + path, payload, headers, method=method)
        try:
            response = urllib.request.urlopen(request, timeout=90)
        except urllib.error.HTTPError as error:
            response = error
        with response:
            raw = response.read()
            response_headers = response.headers
            expected = (status,) if isinstance(status, int) else status
            assert response.status in expected, (
                f"{method} {path}: expected {status}, got {response.status}: {raw!r}"
            )
            status = response.status

        if not raw:
            data = None
        elif response_headers.get_content_type() == "application/json":
            data = json.loads(raw)
        else:
            data = raw
        if code is not None:
            assert data["code"] == code, (code, data)
        self.validate(method.lower(), path.split("?", 1)[0], status, response_headers, data)
        return data, response_headers

    def validate(self, method, path, status, headers, data):
        operation = next((ops[method] for pattern, ops in self.routes
                          if method in ops and pattern.fullmatch(path)), None)
        if operation is None:
            return
        name = operation["operationId"]
        self.visited.add(name)
        response = operation["responses"][str(status)]
        if "$ref" in response:
            response = self.spec["components"]["responses"][response["$ref"].rsplit("/", 1)[-1]]
        if "content" not in response:
            assert data is None, (name, status, data)
            return
        content = response["content"]
        if "application/json" in content:
            assert headers.get_content_type() == "application/json", (name, status, headers)
            key = name, status
            if key not in self.validators:
                schema = {"components": self.spec["components"], "allOf": [content["application/json"]["schema"]]}
                self.validators[key] = Draft202012Validator(schema, format_checker=FormatChecker())
            self.validators[key].validate(data)
        else:
            assert headers.get_content_type() == "text/plain"
            assert headers.get_content_charset() == "utf-8"

    def auth(self, email, role, name):
        user, headers = self.call("POST", "/api/auth/register", 201,
                                  body={"email": email, "role": role, "fullName": name, "password": PASSWORD})
        assert user["fullName"] == name and user["role"] == role
        assert headers["Cache-Control"] == "no-store" and "Set-Cookie" not in headers
        return user, self.login(email, expected=user)

    def login(self, email, *, previous=None, expected=None):
        result, headers = self.call("POST", "/api/auth/login", 200,
                                    session=previous, body={"email": email, "password": PASSWORD})
        assert result == expected and headers["Cache-Control"] == "no-store"
        return session_cookie(headers, 86400).value

    def assignment(self, session, title, sources=None):
        request = {"title": title}
        if sources is not None:
            request["sourceAssignmentIds"] = sources
        assignment, _ = self.call("POST", "/api/assignments", 201, session=session, body=request)
        share_url = urllib.parse.urlparse(assignment["submissionUrl"])
        assert share_url.scheme in ("http", "https") and share_url.netloc and "/submit/" in share_url.path
        token = share_url.path.rsplit("/", 1)[-1]
        return assignment["id"], "/api/submit/" + token

    def submit(self, link, session, data=JAVA, status=201, code=None):
        receipt, _ = self.call("POST", link, status, session=session, files=[("file", "local.java", data)], code=code)
        return receipt

    def rows(self, assignment, teacher, query=""):
        return self.call("GET", assignment + "/submissions" + query, 200, session=teacher)[0]

    def wait_scores(self, assignment, teacher, work, expected, timeout):
        deadline = time.monotonic() + timeout
        while True:
            result, _ = self.call("GET", f"{assignment}/submissions/{work}", 200, session=teacher)
            actual = result["similarity"]["status"], result["ai"]["status"]
            if actual == expected:
                return result
            if any(a != e and a != "pending" for a, e in zip(actual, expected)) or time.monotonic() >= deadline:
                raise AssertionError(f"Expected Structsim and AI {expected}, got {actual}: {result}")
            time.sleep(1)

    def check_coverage(self):
        expected = {operation["operationId"] for path in self.spec["paths"].values()
                    for method, operation in path.items() if method in METHODS}
        assert self.visited == expected, expected - self.visited


def multipart(boundary, files):
    chunks = []
    for field, name, data in files:
        chunks.extend((f"--{boundary}\r\n".encode(),
                       f'Content-Disposition: form-data; name="{field}"; filename="{name}"\r\n'.encode(),
                       b"Content-Type: application/octet-stream\r\n\r\n", data, b"\r\n"))
    chunks.append(f"--{boundary}--\r\n".encode())
    return b"".join(chunks)


def session_cookie(headers, age):
    cookie = SimpleCookie()
    cookie.load(headers["Set-Cookie"])
    assert set(cookie) == {"session"}
    session = cookie["session"]
    assert session["path"] == "/" and session["secure"] and session["httponly"]
    assert session["samesite"].lower() == "lax" and int(session["max-age"]) == age
    assert not session.value if age == 0 else bool(session.value)
    return session


def smoke(api, timeout, expect_ai_failure=False):
    api.call("GET", "/api/health", 200)
    published, _ = api.call("GET", "/openapi.yaml", 200)
    assert published == SPEC_PATH.read_bytes()
    runtime, _ = api.call("GET", "/v3/api-docs", 200)
    declared = {(path, method) for path, ops in api.spec["paths"].items() for method in ops if method in METHODS}
    mapped = {(path, method) for path, ops in runtime["paths"].items() for method in ops if method in METHODS}
    assert mapped - {("/api/health", "get")} == declared, (declared - mapped, mapped - declared)

    api.call("GET", "/api/auth/me", 401, code="AUTHENTICATION_REQUIRED")
    api.call("GET", "/api/assignments", 401, code="AUTHENTICATION_REQUIRED")
    api.call("POST", "/api/auth/register", 422, body={}, code="VALIDATION_ERROR")
    api.call("POST", "/api/auth/login", 422, body={"email": "wrong"}, code="VALIDATION_ERROR")
    suffix = uuid4().hex[:12]
    email = f"teacher-{suffix}@example.org"
    teacher_user, other_device = api.auth(" " + email.upper() + " ", "teacher", "Преподаватель")
    api.call("POST", "/api/auth/register", 409, code="EMAIL_ALREADY_REGISTERED",
             body={"email": email, "role": "student", "fullName": "Дубль", "password": PASSWORD})
    api.call("POST", "/api/auth/login", 401, session=other_device, code="INVALID_CREDENTIALS",
             body={"email": email, "password": "тест smoke"})
    assert api.call("GET", "/api/auth/me", 200, session=other_device)[0] == teacher_user
    browser = api.login(email, expected=teacher_user)
    teacher = api.login(email, previous=browser, expected=teacher_user)
    api.call("GET", "/api/auth/me", 401, session=browser, code="AUTHENTICATION_REQUIRED")
    api.call("GET", "/api/auth/me", 200, session=other_device)
    assert api.call("GET", "/api/assignments", 200, session=teacher)[0] == []

    _, outsider = api.auth(f"other-{suffix}@example.org", "teacher", "Другой преподаватель")
    first_user, first = api.auth(f"first-{suffix}@example.org", "student", "Первый студент")
    _, second = api.auth(f"second-{suffix}@example.org", "student", "Второй студент")
    api.call("GET", "/api/assignments", 403, session=first)
    api.call("POST", "/api/assignments", 422, session=teacher, body={"title": "  "})
    api.call("POST", "/api/assignments", 404, session=teacher,
             body={"title": "Нет источника", "sourceAssignmentIds": [str(uuid4())]})
    assert api.call("GET", "/api/assignments", 200, session=teacher)[0] == []

    source_id, source_link = api.assignment(teacher, "  Источник  ")
    target_id, target_link = api.assignment(teacher, "Текущее задание", [source_id])
    source = "/api/assignments/" + source_id
    target = "/api/assignments/" + target_id
    initial, _ = api.call("GET", target, 200, session=teacher)
    assert initial["sourceAssignmentIds"] == [source_id] and initial["analysis"] == {"completed": 0, "failed": 0}
    assert api.call("GET", source, 200, session=teacher)[0]["title"] == "Источник"
    api.call("GET", target, 404, session=outsider)
    api.call("POST", "/api/assignments", 404, session=outsider,
             body={"title": "Чужой источник", "sourceAssignmentIds": [source_id]})
    api.call("PUT", target + "/sources", 422, session=teacher, body={"sourceAssignmentIds": [target_id]})
    api.call("PUT", target + "/sources", 422, session=teacher, body={"sourceAssignmentIds": [source_id, source_id]})
    api.call("PUT", target + "/sources", 404, session=outsider, body={"sourceAssignmentIds": []})
    assert api.rows(target, teacher)["items"] == []
    api.call("GET", target + "/submissions?pageSize=101", 422, session=teacher)
    api.call("GET", target + "/submissions?sort=wrong", 422, session=teacher)
    print("Authentication, permissions, assignments and OpenAPI routes checked", flush=True)

    api.call("GET", target_link, 401)
    api.call("GET", target_link, 403, session=teacher)
    api.call("GET", "/api/submit/unknown", 404, session=first)
    form, _ = api.call("GET", target_link, 200, session=first)
    assert form == {"title": "Текущее задание", "teacherFullName": "Преподаватель",
                    "canSubmit": True, "currentSubmission": None}
    api.call("POST", target_link, 422, session=first, code="FILE_REQUIRED")
    api.call("POST", target_link, 422, session=first, code="MULTIPLE_FILES",
             files=[("file", "a.java", JAVA), ("file", "b.java", JAVA)])
    api.call("POST", target_link, 422, session=first, code="INVALID_EXTENSION", files=[("file", "a.txt", JAVA)])
    api.submit(target_link, first, b"", 422, "EMPTY_FILE")
    api.submit(target_link, first, b"\xf0\x28\x8c\x28", 422, "INVALID_ENCODING")
    api.submit(target_link, first, b"a" * 1_000_001, 413, "FILE_TOO_LARGE")
    assert api.call("GET", target_link, 200, session=first)[0] == form

    historical = api.submit(source_link, first)
    single, _ = api.call("GET", f"{source}/submissions/{historical['submissionId']}", 200, session=teacher)
    assert single["similarity"] == {"status": "not_applicable", "value": None}

    with ThreadPoolExecutor(2) as pool:
        attempts = list(pool.map(lambda _: api.submit(target_link, first, status=(201, 409)), range(2)))
    receipts = [attempt for attempt in attempts if "submissionId" in attempt]
    assert len(receipts) == 1 and any(row.get("code") == "SUBMISSION_ALREADY_ACCEPTED" for row in attempts)
    accepted = receipts[0]
    api.submit(target_link, first, status=409, code="SUBMISSION_ALREADY_ACCEPTED")
    locked, _ = api.call("GET", target_link, 200, session=first)
    assert not locked["canSubmit"] and locked["currentSubmission"] == accepted

    peer = api.submit(target_link, second, JAVA.replace(b"evaluate(42)", b"evaluate(47)"))
    source_peer = api.submit(source_link, second)
    a, b, old_a, old_b = (entry["submissionId"] for entry in (accepted, peer, historical, source_peer))
    api.call("GET", f"{target}/pairs/{old_a}/{old_b}", 404, session=teacher)
    api.call("GET", f"{target}/submissions/{old_a}", 404, session=teacher)
    api.call("GET", f"{target}/submissions/{a}", 404, session=outsider)
    api.call("GET", f"/api/submissions/{a}/source", 404, session=outsider)
    api.call("GET", f"/api/submissions/{a}/source", 403, session=first)
    assert api.call("GET", f"/api/submissions/{a}/source", 200, session=teacher)[0] == JAVA

    # Вторая ссылка формирует цикл. В обоих направлениях читаются только прямые текущие сдачи.
    api.call("PUT", source + "/sources", 200, session=teacher, body={"sourceAssignmentIds": [target_id]})
    unchanged, _ = api.call("PUT", target + "/sources", 200, session=teacher, body={"sourceAssignmentIds": [source_id]})
    assert unchanged["submissionUrl"] == initial["submissionUrl"]
    ai_state = "failed" if expect_ai_failure else "ok"
    detail = api.wait_scores(target, teacher, a, ("ok", ai_state), timeout)
    api.wait_scores(target, teacher, b, ("ok", ai_state), timeout)
    assert {item["submissionId"] for item in detail["similar"]} == {b, old_a, old_b}
    assert detail["similarity"]["value"] == max(item["score"]["value"] for item in detail["similar"])
    assert {item["assignmentId"] for item in detail["similar"]} == {target_id, source_id}
    assert detail["studentId"] == first_user["id"] and detail["fullName"] == "Первый студент"
    direct = api.call("GET", f"{target}/pairs/{a}/{old_a}", 200, session=teacher)[0]
    reverse = api.call("GET", f"{target}/pairs/{old_a}/{a}", 200, session=teacher)[0]
    assert direct["a"] == a and direct["b"] == old_a and reverse["a"] == old_a and reverse["b"] == a
    assert direct["score"] == reverse["score"] == direct["methods"]["structsim"]["score"]
    fragments = direct["methods"]["structsim"]["fragments"]
    assert fragments and reverse["methods"]["structsim"]["fragments"] == [
        {"a": fragment["b"], "b": fragment["a"]} for fragment in fragments
    ]
    score_sort = "similarity" if expect_ai_failure else "ai"
    first_page = api.rows(target, teacher, f"?sort={score_sort}&pageSize=1")
    second_page = api.rows(target, teacher, f"?sort={score_sort}&pageSize=1&page=2")
    assert first_page["total"] == second_page["total"] == 2
    both = [first_page["items"][0], second_page["items"][0]]
    assert both == sorted(both, key=lambda row: (-row[score_sort]["value"], row["submissionId"]))
    assert api.rows(target, teacher, "?page=10000")["items"] == []
    summary, _ = api.call("GET", target, 200, session=teacher)
    assert summary["submissionCount"] == 2 and summary["analysis"] == {
        "completed": 2, "failed": 2 if expect_ai_failure else 0
    }
    print("Uploads, direct-source comparison, Structsim results and AI states checked", flush=True)

    reset = f"{target}/students/{first_user['id']}/reset"
    api.call("POST", reset, 409, session=teacher, code="SUBMISSION_CHANGED", body={"expectedSubmissionId": b})
    for _ in range(2):
        api.call("POST", reset, 204, session=teacher, body={"expectedSubmissionId": a})
    reopened, _ = api.call("GET", target_link, 200, session=first)
    assert reopened["canSubmit"] and reopened["currentSubmission"] == accepted
    replacement = api.submit(target_link, first, JAVA.replace(b"evaluate(42)", b"evaluate(45)"))["submissionId"]
    assert replacement not in (a, b)
    api.call("POST", reset, 409, session=teacher, code="SUBMISSION_CHANGED", body={"expectedSubmissionId": a})
    api.call("GET", f"/api/submissions/{a}/source", 404, session=teacher)
    api.call("GET", f"{target}/submissions/{a}", 404, session=teacher)
    api.call("GET", f"{target}/pairs/{a}/{b}", 404, session=teacher)
    api.call("GET", f"{source}/pairs/{a}/{old_a}", 404, session=teacher)
    api.wait_scores(source, teacher, old_a, ("ok", ai_state), timeout)
    replaced = api.wait_scores(target, teacher, replacement, ("ok", ai_state), timeout)

    for _ in range(2):
        api.call("DELETE", source, 204, session=teacher)
    api.call("GET", source, 404, session=teacher)
    api.call("GET", source_link, 404, session=first)
    api.call("GET", f"/api/submissions/{old_a}/source", 404, session=teacher)
    api.call("GET", f"{target}/pairs/{replacement}/{old_a}", 404, session=teacher)
    current, _ = api.call("GET", target, 200, session=teacher)
    assert current["sourceAssignmentIds"] == [] and current["submissionUrl"] == initial["submissionUrl"]
    refreshed = api.wait_scores(target, teacher, replacement, ("ok", ai_state), timeout)
    assert [item["submissionId"] for item in refreshed["similar"]] == [b]
    assert refreshed["ai"] == replaced["ai"]

    mixed_id, mixed_link = api.assignment(teacher, "Задание с неразобранной работой")
    mixed = "/api/assignments/" + mixed_id
    good = api.submit(mixed_link, first)["submissionId"]
    malformed_java = (REPO_ROOT / "worker/core/known/broken.java").read_bytes()
    malformed = api.submit(mixed_link, second, malformed_java)["submissionId"]
    api.wait_scores(mixed, teacher, good, ("not_applicable", ai_state), timeout)

    _, third = api.auth(f"third-{suffix}@example.org", "student", "Третий студент")
    good_peer = api.submit(mixed_link, third, JAVA.replace(b"evaluate(42)", b"evaluate(47)"))["submissionId"]
    good_detail = api.wait_scores(mixed, teacher, good, ("ok", ai_state), timeout)
    expected_pair, _ = api.call("GET", f"{mixed}/pairs/{good}/{good_peer}", 200, session=teacher)
    assert good_detail["similarity"] == expected_pair["score"]
    scores = {row["submissionId"]: row["similarity"] for row in api.rows(mixed, teacher)["items"]}
    assert scores[good] == scores[good_peer] == expected_pair["score"]
    assert scores[malformed]["status"] == "failed" and scores[malformed]["reason"]["code"] == "NOT_PARSED"

    for active in (teacher, None):
        _, headers = api.call("POST", "/api/auth/logout", 204, session=active)
        assert headers["Cache-Control"] == "no-store"
        session_cookie(headers, 0)
    api.call("GET", "/api/auth/me", 401, session=teacher)
    api.call("GET", "/api/auth/me", 200, session=other_device)
    api.check_coverage()
    print("Resubmission, revision propagation, deletion and logout checked", flush=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base", default="http://localhost:8080")
    parser.add_argument("--timeout", type=float, default=300, help="Maximum wait per submission in seconds")
    parser.add_argument("--expect-ai-failure", action="store_true")
    args = parser.parse_args()
    smoke(Api(args.base), args.timeout, args.expect_ai_failure)


if __name__ == "__main__":
    main()
