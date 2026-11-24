import unittest
from uuid import uuid4

from worker.ai.contract import CONTRACT, validate_result, validate_schema


def job():
    return {"contract": CONTRACT, "runId": str(uuid4()), "methodVersion": "aicode-0.1.0+gigacheck-9fabe1503f+int2",
            "params": {}, "inputFiles": [{"submissionId": str(uuid4()), "path": f"input/submissions/{uuid4()}/Main.java"}]}


def result(job, **file):
    entry = {"submissionId": job["inputFiles"][0]["submissionId"], "status": "ok", "score": 0.5,
             "suspicious": [{"startIndex": 0, "endIndex": 10, "confidence": 0.5}]}
    entry.update(file)
    return {"contract": CONTRACT, "runId": job["runId"], "methodVersion": job["methodVersion"],
            "params": job["params"], "files": [entry]}


class JobSchemaTest(unittest.TestCase):
    def test_valid(self):
        validate_schema("job", job())

    def test_rejects_unsafe_paths(self):
        for path in ("../Main.java", "input/../../Main.java", "/etc/Main.java", "a\\b.java", "Main.txt"):
            with self.subTest(path=path):
                invalid = job()
                invalid["inputFiles"][0]["path"] = path
                with self.assertRaises(Exception):
                    validate_schema("job", invalid)

    def test_rejects_extra_fields_and_bad_contract(self):
        for mutate in (lambda j: j.update(contract="integritylens.core/1"),
                       lambda j: j.update(extra=1),
                       lambda j: j["inputFiles"][0].update(language="java")):
            with self.subTest(mutate=mutate):
                invalid = job()
                mutate(invalid)
                with self.assertRaises(Exception):
                    validate_schema("job", invalid)


class ResultSchemaTest(unittest.TestCase):
    def test_valid(self):
        validate_schema("result", result(job()))

    def test_ok_requires_score_and_suspicious(self):
        base = job()
        for mutate in (lambda f: f.pop("score"), lambda f: f.pop("suspicious"), lambda f: f.update(score=None)):
            with self.subTest(mutate=mutate):
                invalid = result(base)
                mutate(invalid["files"][0])
                with self.assertRaises(Exception):
                    validate_schema("result", invalid)

    def test_failed_requires_reason_and_empty_result(self):
        base = job()
        invalid = result(base, status="failed", score=None, suspicious=[],
                         reason={"code": "NOT_PARSED", "message": "x"})
        validate_schema("result", invalid)
        for mutate in (lambda f: f.pop("reason"), lambda f: f.update(score=0.5),
                       lambda f: f.update(suspicious=[{"startIndex": 0, "endIndex": 1, "confidence": 0.1}])):
            with self.subTest(mutate=mutate):
                broken = result(base, status="failed", reason={"code": "NOT_PARSED"})
                mutate(broken["files"][0])
                with self.assertRaises(Exception):
                    validate_schema("result", broken)

    def test_score_and_confidence_bounds(self):
        base = job()
        for mutate in (lambda f: f.update(score=1.5),
                       lambda f: f["suspicious"][0].update(confidence=2)):
            with self.subTest(mutate=mutate):
                invalid = result(base)
                mutate(invalid["files"][0])
                with self.assertRaises(Exception):
                    validate_schema("result", invalid)


class ValidateResultTest(unittest.TestCase):
    def test_consistent(self):
        base = job()
        validate_result(base, result(base), {base["inputFiles"][0]["submissionId"]: b"x" * 100})

    def test_rejects_mismatches(self):
        base = job()
        for mutate in (lambda r: r.update(runId=str(uuid4())),
                       lambda r: r.update(methodVersion="other"),
                       lambda r: r["files"][0].update(submissionId=str(uuid4()))):
            with self.subTest(mutate=mutate):
                invalid = result(base)
                mutate(invalid)
                with self.assertRaises(ValueError):
                    validate_result(base, invalid)

    def test_rejects_failed_file_with_assessment(self):
        base = job()
        invalid = result(base, status="failed", score=None, suspicious=[],
                         reason={"code": "NOT_PARSED"})
        invalid["files"][0]["score"] = 0.5
        with self.assertRaises(ValueError):
            validate_result(base, invalid)

    def test_rejects_bad_fragments(self):
        def fragment(start, end):
            return {"startIndex": start, "endIndex": end, "confidence": 0.5}

        base = job()
        sid = base["inputFiles"][0]["submissionId"]
        source = b"x" * 100
        russian = "// \u043a\u043e\u043c".encode("utf-8")  # к = D0 BA, о = D0 BE, м = D0 BC: байты 3..8
        self.assertEqual(len(russian), 9)
        for suspicious, data in (([fragment(10, 5)], source),              # пустой
                                 ([fragment(5, 10), fragment(7, 12)], source),  # пересечение
                                 ([fragment(20, 12), fragment(5, 10)], source),  # беспорядок
                                 ([fragment(90, 100)], b"x" * 95),        # вне файла
                                 ([fragment(0, 4)], russian),             # конец внутри «к»
                                 ([fragment(1, 6)], russian),             # конец внутри «о»
                                 ([fragment(6, 9)], russian)):            # начало внутри «о»
            with self.subTest(suspicious=suspicious):
                invalid = result(base)
                invalid["files"][0]["suspicious"] = suspicious
                with self.assertRaises(ValueError):
                    validate_result(base, invalid, {sid: data})

    def test_boundaries_after_multibyte_characters_are_accepted(self):
        base = job()
        sid = base["inputFiles"][0]["submissionId"]
        data = "// \u043a\u043e\nint x;".encode("utf-8")  # «ко» занимает байты 3..6
        self.assertEqual(len(data), 14)
        good = result(base)
        good["files"][0]["suspicious"] = [{"startIndex": 3, "endIndex": 7, "confidence": 0.5}]
        validate_result(base, good, {sid: data})
        self.assertEqual(data[3:7].decode("utf-8"), "\u043a\u043e")
        good["files"][0]["suspicious"] = [{"startIndex": 0, "endIndex": len(data), "confidence": 0.5}]
        validate_result(base, good, {sid: data})


if __name__ == "__main__":
    unittest.main()
