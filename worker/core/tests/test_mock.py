import copy
import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path
from uuid import uuid4

from worker.core import run
from worker.core.contract import CONTRACT, validate_result
from worker.core.mock import InvalidInput


class MockTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)
        self.input = self.root / "input"
        self.input.mkdir()
        self.job = {"contract": CONTRACT, "runId": str(uuid4()), "method": "jplag", "params": {}, "submissions": []}
        for i in range(5):
            sid = str(uuid4())
            path = self.input / "submissions" / sid / "Main.java"
            path.parent.mkdir(parents=True)
            path.write_text(f"class Main{i} {{}}")
            self.job["submissions"].append({"id": sid, "language": "java", "files": ["Main.java"]})

    def execute(self):
        (self.input / "job.json").write_text(json.dumps(self.job))
        return run(self.input, self.root / "output")

    def test_repeatable_complete_and_consistent(self):
        first = self.execute()
        self.assertEqual(first, self.execute())
        self.assertEqual(len(first["pairs"]), 10)
        self.assertEqual(first["methodVersion"], "mock-v1")
        validate_result(self.job, first)
        self.job["method"] = "structsim"
        second = self.execute()
        self.assertNotEqual([p["value"] for p in first["pairs"]], [p["value"] for p in second["pairs"]])

    def test_scores_depend_on_sources_and_method_not_ids(self):
        first = sorted(p["value"] for p in self.execute()["pairs"])
        for submission in self.job["submissions"]:
            previous = submission["id"]
            submission["id"] = str(uuid4())
            (self.input / "submissions" / previous).rename(self.input / "submissions" / submission["id"])
        self.assertEqual(first, sorted(p["value"] for p in self.execute()["pairs"]))

    def test_partial_failure_preserves_all_pairs(self):
        self.job["params"] = {"mockScenario": "partial"}
        result = self.execute()
        self.assertEqual(sum(s["status"] == "failed" for s in result["submissions"]), 1)
        self.assertEqual(sum(p["value"] is None for p in result["pairs"]), 4)

    def test_rejects_duplicate_missing_and_invalid_results(self):
        result = self.execute()
        duplicate = copy.deepcopy(result)
        duplicate["pairs"][-1] = duplicate["pairs"][0]
        with self.assertRaises(ValueError):
            validate_result(self.job, duplicate)
        result["pairs"][0]["value"] = 2
        with self.assertRaises(Exception):
            validate_result(self.job, result)

    def test_fragments_for_similar_sources(self):
        # Две схожие работы (одинаковые строки с переименованными переменными)
        # должны дать подозрительные фрагменты; несхожие — пустой список.
        base = (
            "public class Main {\n"
            "    static List<Integer> bfs(List<List<Integer>> g, int s) {\n"
            "        boolean[] seen = new boolean[g.size()];\n"
            "        Deque<Integer> q = new ArrayDeque<>();\n"
            "        List<Integer> res = new ArrayList<>();\n"
            "        seen[s] = true;\n"
            "        q.add(s);\n"
            "        while (!q.isEmpty()) {\n"
            "            int cur = q.poll();\n"
            "            res.add(cur);\n"
            "            for (int x : g.get(cur)) {\n"
            "                if (!seen[x]) { seen[x] = true; q.add(x); }\n"
            "            }\n"
            "        }\n"
            "        return res;\n"
            "    }\n"
            "}\n"
        )
        similar = base.replace("seen", "visited").replace("res", "order")
        other = "class Different { void x() { int a = 1; } }"

        # Перезаписываем две работы, остальные — в сторону.
        for i in range(5):
            sid = self.job["submissions"][i]["id"]
            p = self.input / "submissions" / sid / "Main.java"
            p.write_text(similar if i == 0 else (base if i == 1 else other))

        result = self.execute()
        # Пара (0,1) — схожая, у неё есть фрагменты.
        s0 = self.job["submissions"][0]["id"]
        s1 = self.job["submissions"][1]["id"]
        pair = next(p for p in result["pairs"] if {p["a"], p["b"]} == {s0, s1})
        self.assertTrue(pair["fragments"], "схожая пара должна иметь фрагменты")
        for f in pair["fragments"]:
            self.assertEqual(len(f["a"]), 2)
            self.assertEqual(len(f["b"]), 2)
        validate_result(self.job, result)

    def test_invalid_input(self):
        self.job["submissions"][0]["files"] = ["../../Main.java"]
        with self.assertRaises(InvalidInput):
            self.execute()

    def test_cli_exit_codes(self):
        for scenario, expected in (("tool_error", 1), ("invalid_input", 2), ("success", 0)):
            self.job["params"] = {"mockScenario": scenario}
            (self.input / "job.json").write_text(json.dumps(self.job))
            process = subprocess.run([sys.executable, "-m", "worker.core", "run", "--input", str(self.input),
                                      "--output", str(self.root / "output")], capture_output=True, timeout=10)
            self.assertEqual(process.returncode, expected, process.stderr)


if __name__ == "__main__":
    unittest.main()
