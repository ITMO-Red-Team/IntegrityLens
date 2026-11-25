import json
import tempfile
import unittest
from pathlib import Path
from uuid import UUID

from worker.core import v2


class StructsimV2Test(unittest.TestCase):
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
            job = {"contract": v2.CONTRACT, "runId": str(UUID(int=9)), "revision": 2,
                   "method": "structsim", "methodVersion": v2.MOCK_VERSION,
                   "params": {"mockScenario": "partial"}, "submissions": entries}
            (input_dir / "job.json").write_text(json.dumps(job), encoding="utf-8")

            v2.run(input_dir, output_dir)
            result = json.loads((output_dir / "result.json").read_text(encoding="utf-8"))

        self.assertEqual(len(result["pairs"]), 5)
        self.assertNotIn((ids[0], ids[2]), [(p["a"], p["b"]) for p in result["pairs"]])
        failures = [p for p in result["pairs"] if p["value"] is None]
        self.assertEqual({(p["a"], p["b"]): p["reason"]["code"] for p in failures}, {
            (ids[0], ids[1]): "NOT_PARSED_B",
            (ids[1], ids[2]): "NOT_PARSED_A",
            (ids[1], ids[3]): "NOT_PARSED_A",
        })
        self.assertTrue(all(not p["fragments"] for p in failures))
        result["pairs"].pop()
        with self.assertRaisesRegex(ValueError, "coverage"):
            v2.validate_result(job, result)
