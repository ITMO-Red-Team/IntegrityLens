import json
import tempfile
import unittest
from pathlib import Path
from uuid import uuid4

from worker.ai.contract import CONTRACT, InvalidInput
from worker.ai.engine import DEFAULT_THRESHOLD, input_paths, load_job, merged_params, run


def write_job(directory, params=None):
    input_dir = Path(directory) / "input"
    input_dir.mkdir(parents=True, exist_ok=True)
    job = {"contract": CONTRACT, "runId": str(uuid4()), "methodVersion": "aicode-mock/1",
           "params": params or {}, "inputFiles": []}
    for _ in range(2):
        sid = str(uuid4())
        path = input_dir / "submissions" / sid / "Main.java"
        path.parent.mkdir(parents=True)
        path.write_text("class Main {}")
        job["inputFiles"].append({"submissionId": sid, "path": f"input/submissions/{sid}/Main.java"})
    (input_dir / "job.json").write_text(json.dumps(job), encoding="utf-8")
    return input_dir, job


class LoadJobTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)

    def test_loads_and_checks_uniqueness(self):
        input_dir, job = write_job(self.directory.name)
        self.assertEqual(load_job(input_dir), job)
        job["inputFiles"][1]["submissionId"] = job["inputFiles"][0]["submissionId"]
        (input_dir / "job.json").write_text(json.dumps(job), encoding="utf-8")
        with self.assertRaises(InvalidInput):
            load_job(input_dir)

    def test_rejects_broken_job(self):
        input_dir = Path(self.directory.name) / "input"
        input_dir.mkdir()
        (input_dir / "job.json").write_text("{", encoding="utf-8")
        with self.assertRaises(InvalidInput):
            load_job(input_dir)

    def test_missing_file_is_invalid_input(self):
        input_dir, job = write_job(self.directory.name)
        (Path(self.directory.name) / job["inputFiles"][0]["path"]).unlink()
        with self.assertRaises(InvalidInput):
            input_paths(job, input_dir)


class MergedParamsTest(unittest.TestCase):
    def test_defaults(self):
        self.assertEqual(merged_params({"params": {}}), {"threshold": DEFAULT_THRESHOLD})

    def test_override_and_normalization(self):
        self.assertEqual(merged_params({"params": {"threshold": 0}}), {"threshold": 0.0})
        self.assertEqual(merged_params({"params": {"threshold": 1}}), {"threshold": 1.0})

    def test_rejects_unknown(self):
        with self.assertRaises(InvalidInput):
            merged_params({"params": {"maxTokens": 900}})

    def test_rejects_bad_threshold(self):
        for threshold in ("0.5", True, None, -0.1, 1.1):
            with self.subTest(threshold=threshold):
                with self.assertRaises(InvalidInput):
                    merged_params({"params": {"threshold": threshold}})


class RunTest(unittest.TestCase):
    def test_mock_dispatch_writes_result(self):
        with tempfile.TemporaryDirectory() as directory:
            input_dir, job = write_job(directory, {"mockScenario": "success", "delaySeconds": 0})
            result = run(input_dir, Path(directory) / "output")
            self.assertEqual((result["runId"], result["methodVersion"]), (job["runId"], job["methodVersion"]))
            self.assertEqual(len(result["files"]), 2)
            written = json.loads((Path(directory) / "output" / "result.json").read_text(encoding="utf-8"))
            self.assertEqual(written, result)


if __name__ == "__main__":
    unittest.main()
