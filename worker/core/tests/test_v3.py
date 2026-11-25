import json
import tempfile
import unittest
from pathlib import Path
from uuid import UUID

from worker.core import v3


class StructsimV3Test(unittest.TestCase):
    def test_scope_and_failed_pair(self):
        with tempfile.TemporaryDirectory() as folder:
            input_dir = Path(folder, "input")
            output_dir = Path(folder, "output")
            ids = [str(UUID(int=n)) for n in range(1, 5)]
            entries = []
            for index, sid in enumerate(ids):
                path = input_dir / "submissions" / sid / "source.java"
                path.parent.mkdir(parents=True)
                path.write_text("class Main {}\n", encoding="utf-8")
                entries.append({"id": sid, "language": "java", "files": [path.name],
                                "scope": "target" if index in (1, 3) else "source"})
            job = {"contract": v3.CONTRACT, "runId": str(UUID(int=9)), "revision": 2,
                   "method": "structsim", "methodVersion": v3.MOCK_VERSION,
                   "params": {"mockScenario": "partial"}, "submissions": entries}
            (input_dir / "job.json").write_text(json.dumps(job), encoding="utf-8")

            v3.run(input_dir, output_dir)
            result = json.loads((output_dir / "result.json").read_text(encoding="utf-8"))

        self.assertEqual(len(result["pairs"]), 5)
        self.assertNotIn((ids[0], ids[2]), [(p["a"], p["b"]) for p in result["pairs"]])
        failures = [p for p in result["pairs"] if p["value"] is None]
        self.assertEqual(len(failures), 3)
        self.assertTrue(all(p["reason"]["code"] == "NOT_PARSED" and not p["fragments"] for p in failures))
        self.assertEqual(result["failedInputs"], [{"id": ids[1], "reason": {"code": "NOT_PARSED"}}])
        import copy
        for bad_ids in ([], [ids[1], ids[1]], [str(UUID(int=99))]):
            invalid = copy.deepcopy(result)
            invalid["failedInputs"] = [{"id": sid, "reason": {"code": "NOT_PARSED"}} for sid in bad_ids]
            with self.assertRaises(ValueError):
                v3.validate_result(job, invalid)
        result["pairs"].pop()
        with self.assertRaisesRegex(ValueError, "coverage"):
            v3.validate_result(job, result)
