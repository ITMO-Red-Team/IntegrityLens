import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path
from uuid import uuid4

from worker.ai.contract import CONTRACT, InvalidInput, validate_result
from worker.ai.mock import run

SOURCE = "class Main {\n    // комментарий на всякий случай\n    int x = 1;\n}\n".encode("utf-8")


class MockTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)
        self.input = self.root / "input"
        self.input.mkdir()
        self.job = {"contract": CONTRACT, "runId": str(uuid4()), "methodVersion": "aicode-mock/1",
                    "params": {}, "inputFiles": []}
        for i in range(3):
            sid = str(uuid4())
            path = self.input / "submissions" / sid / "Main.java"
            path.parent.mkdir(parents=True)
            path.write_bytes(SOURCE + str(i).encode())
            self.job["inputFiles"].append({"submissionId": sid, "path": f"input/submissions/{sid}/Main.java"})

    def execute(self):
        (self.input / "job.json").write_text(json.dumps(self.job))
        return run(self.job, self.input)

    def sources(self):
        return {f["submissionId"]: (self.root / f["path"]).read_bytes() for f in self.job["inputFiles"]}

    def test_repeatable_and_consistent(self):
        first = self.execute()
        self.assertEqual(first, self.execute())
        self.assertEqual(first["methodVersion"], self.job["methodVersion"])
        validate_result(self.job, first, self.sources())

    def test_fragments_are_byte_ranges_of_the_file(self):
        result = self.execute()
        paths = {f["submissionId"]: self.root / f["path"] for f in self.job["inputFiles"]}
        for file in result["files"]:
            data = paths[file["submissionId"]].read_bytes()
            self.assertTrue(file["suspicious"])
            for fragment in file["suspicious"]:
                data[:fragment["startIndex"]].decode("utf-8")  # граница символа
                data[fragment["startIndex"]:fragment["endIndex"]].decode("utf-8")
            flagged = sum(f["endIndex"] - f["startIndex"] for f in file["suspicious"])
            self.assertEqual(file["score"], flagged / len(data))

    def test_partial_failure_marks_one_file(self):
        self.job["params"] = {"mockScenario": "partial"}
        result = self.execute()
        self.assertEqual(sum(f["status"] == "failed" for f in result["files"]), 1)
        failed = next(f for f in result["files"] if f["status"] == "failed")
        self.assertEqual((failed["score"], failed["suspicious"], failed["reason"]["code"]), (None, [], "NOT_PARSED"))
        validate_result(self.job, result, self.sources())

    def test_scenarios(self):
        self.job["params"] = {"mockScenario": "tool_error"}
        with self.assertRaises(RuntimeError):
            self.execute()
        self.job["params"] = {"mockScenario": "invalid_input"}
        with self.assertRaises(InvalidInput):
            self.execute()

    def test_missing_file_is_invalid_input(self):
        (self.root / self.job["inputFiles"][0]["path"]).unlink()
        with self.assertRaises(InvalidInput):
            self.execute()

    def test_cli_exit_codes(self):
        for scenario, expected in (("tool_error", 1), ("invalid_input", 2), ("success", 0)):
            with self.subTest(scenario=scenario):
                self.job["params"] = {"mockScenario": scenario, "delaySeconds": 0}
                (self.input / "job.json").write_text(json.dumps(self.job))
                process = subprocess.run([sys.executable, "-m", "worker.ai", "run", "--input", str(self.input),
                                          "--output", str(self.root / "output")], capture_output=True, timeout=30)
                self.assertEqual(process.returncode, expected, process.stderr)


if __name__ == "__main__":
    unittest.main()
