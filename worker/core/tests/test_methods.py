"""Настоящие методы через весь core. Нужны их инструменты, поэтому в полном объёме тесты
идут в образах воркеров; там, где инструмента нет, соответствующие пропускаются."""
import json
import os
import shutil
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path
from unittest import mock
from uuid import uuid4

from worker.core import run, selfcheck
from worker.core.contract import CONTRACT, InvalidInput, validate_result

KNOWN = Path(__file__).parents[1] / "known"
HAS_JPLAG = Path(os.environ.get("JPLAG_JAR", "/opt/jplag/jplag.jar")).is_file()
try:
    import structsim  # noqa: F401
    HAS_STRUCTSIM = shutil.which(os.environ.get("SEMGREP_CORE", "semgrep-core")) is not None
except ImportError:
    HAS_STRUCTSIM = False


class MethodCase(unittest.TestCase):
    method = None

    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)
        self.input = self.root / "input"
        self.input.mkdir()
        self.job = {"contract": CONTRACT, "runId": str(uuid4()), "method": self.method, "params": {}, "submissions": []}
        self.ids = {}

    def add(self, *names):
        for name in names:
            sid = str(uuid4())
            path = self.input / "submissions" / sid / "Main.java"
            path.parent.mkdir(parents=True)
            shutil.copy(KNOWN / f"{name}.java", path)
            self.job["submissions"].append({"id": sid, "language": "java", "files": ["Main.java"]})
            self.ids.setdefault(name, []).append(sid)

    def execute(self):
        (self.input / "job.json").write_text(json.dumps(self.job))
        output = self.root / f"output-{uuid4()}"
        result = run(self.input, output)
        validate_result(self.job, result)
        self.assertEqual(json.loads((output / "result.json").read_text()), result)
        self.assertTrue((output / "artifacts" / "timings.json").is_file())
        return result

    def pair(self, result, first, second):
        ids = {self.ids[first][0], self.ids[second][-1]}
        return next(p for p in result["pairs"] if {p["a"], p["b"]} == ids)

    def status(self, result, name):
        return next(s for s in result["submissions"] if s["id"] == self.ids[name][0])

    def cli_exit_code(self, **env):
        (self.input / "job.json").write_text(json.dumps(self.job))
        return subprocess.run([sys.executable, "-m", "worker.core", "run", "--input", str(self.input), "--output",
                               str(self.root / "cli")], capture_output=True, env={**os.environ, **env}).returncode

    def common_checks(self):
        self.add("base", "renamed", "other", "partial", "broken")
        first = self.execute()
        self.assertEqual(first, self.execute(), "результат должен воспроизводиться")
        self.assertEqual(self.pair(first, "base", "renamed")["value"], 1.0)
        self.assertEqual(self.status(first, "broken")["status"], "failed")
        self.assertEqual(self.status(first, "broken")["reason"]["code"], "NOT_PARSED")
        self.assertIsNone(self.pair(first, "base", "broken")["value"])
        best = self.status(first, "base")["matches"][0]
        self.assertEqual(best["with"], self.ids["renamed"][0])
        self.assertTrue(best["fragments"])
        return first

    def rejects_unknown_params(self):
        self.add("base", "renamed", "other", "partial", "tiny")
        self.job["params"] = {"nonsense": 1}
        with self.assertRaises(InvalidInput):
            self.execute()
        self.assertEqual(self.cli_exit_code(), 2)


@unittest.skipUnless(HAS_JPLAG, "нет JPlag")
class JplagTest(MethodCase):
    method = "jplag"

    def test_known_answer(self):
        self.assertTrue(selfcheck.check("jplag").startswith("jplag-"))

    def test_scores_fragments_and_reproducibility(self):
        result = self.common_checks()
        pair = self.pair(result, "base", "renamed")
        self.assertEqual(pair["details"]["avg"], 1.0)
        self.assertGreater(pair["details"]["longestMatch"], 9)
        self.assertGreater(self.status(result, "base")["stats"]["tokens"], 9)

    def test_metric_changes_value_only(self):
        self.add("base", "partial", "other", "renamed", "tiny")
        by_default = self.pair(self.execute(), "base", "partial")
        self.assertEqual(by_default["value"], by_default["details"]["max"])
        self.job["params"] = {"metric": "avg"}
        averaged = self.pair(self.execute(), "base", "partial")
        self.assertEqual(averaged["value"], averaged["details"]["avg"])
        self.assertLess(averaged["value"], by_default["value"])

    def test_invalid_params(self):
        self.rejects_unknown_params()
        for params in ({"minTokens": 0}, {"minTokens": "9"}, {"metric": "median"}):
            self.job["params"] = params
            with self.assertRaises(InvalidInput):
                self.execute()

    def test_nothing_valid_is_a_result_not_a_failure(self):
        self.add("broken", "tiny", "broken", "tiny", "broken")
        result = self.execute()
        self.assertTrue(all(s["status"] == "failed" for s in result["submissions"]))
        self.assertTrue(all(p["value"] is None for p in result["pairs"]))

    def test_one_valid_submission_stays_ok_without_score(self):
        self.add("base", "tiny", "broken", "tiny", "broken")
        result = self.execute()
        base = self.status(result, "base")
        self.assertEqual((base["status"], base["score"], base["matches"]), ("ok", None, []))
        self.assertEqual(sum(s["status"] == "failed" for s in result["submissions"]), 4)
        self.assertTrue(all(p["value"] is None for p in result["pairs"]))

    def test_broken_tool_is_retryable(self):
        self.add("base", "renamed", "other", "partial", "tiny")
        self.assertEqual(self.cli_exit_code(JPLAG_JAR="/nonexistent.jar"), 1)


@unittest.skipUnless(HAS_STRUCTSIM, "нет structsim или semgrep-core")
class StructsimTest(MethodCase):
    method = "structsim"

    def test_known_answer(self):
        self.assertTrue(selfcheck.check("structsim").startswith("structsim-"))

    def test_scores_fragments_and_reproducibility(self):
        result = self.common_checks()
        pair = self.pair(result, "base", "renamed")
        self.assertEqual({key: pair["details"][key] for key in ("order", "shingle", "blocks")},
                         {"order": 1.0, "shingle": 1.0, "blocks": 1.0})
        self.assertEqual(self.status(result, "base")["stats"]["labels"], selfcheck.BASE_LABELS)

    def test_contrast_needs_five_parsed_submissions(self):
        self.add("base", "renamed", "other", "partial", "partial")
        self.assertIn("contrastZ", self.pair(self.execute(), "base", "renamed")["details"])
        shutil.copy(KNOWN / "broken.java", self.input / "submissions" / self.ids["partial"][0] / "Main.java")
        self.assertNotIn("contrastZ", self.pair(self.execute(), "base", "renamed")["details"])

    def test_exact_copies_are_scored_too(self):
        self.add("base", "base", "other", "other", "partial")
        result = self.execute()
        self.assertEqual(self.pair(result, "base", "base")["value"], 1.0)
        self.assertEqual(self.pair(result, "other", "other")["value"], 1.0)

    def test_native_and_reference_give_the_same_result(self):
        from structsim import native
        if not native.find_binary():
            self.skipTest("native не собран")
        self.add("base", "renamed", "other", "partial", "tiny")
        fast = self.execute()
        with mock.patch.dict(os.environ, {"STRUCTSIM_NATIVE": "off"}):
            slow = self.execute()
        self.assertEqual(fast, slow)

    def test_min_tile_controls_fragments(self):
        self.add("base", "renamed", "other", "partial", "tiny")
        few = self.pair(self.execute(), "base", "partial")
        self.job["params"] = {"minTile": 1000}
        none = self.pair(self.execute(), "base", "partial")
        self.assertTrue(few["fragments"])
        self.assertEqual(none["fragments"], [])
        self.assertEqual(few["value"], none["value"])

    def test_invalid_params(self):
        self.rejects_unknown_params()
        self.job["params"] = {"minTile": 0}
        with self.assertRaises(InvalidInput):
            self.execute()

    def test_broken_tool_is_retryable(self):
        self.add("base", "renamed", "other", "partial", "tiny")
        self.assertEqual(self.cli_exit_code(SEMGREP_CORE="/nonexistent"), 1)

    def test_killed_tool_is_retryable_not_a_parse_failure(self):
        self.add("base", "renamed", "other", "partial", "tiny")
        killed = self.root / "killed"
        killed.write_text("#!/bin/sh\nkill -9 $$\n")
        killed.chmod(0o755)
        self.assertEqual(self.cli_exit_code(SEMGREP_CORE=str(killed)), 1)


class EngineTest(unittest.TestCase):
    def test_mock_is_chosen_by_scenario_only(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            job = {"contract": CONTRACT, "runId": str(uuid4()), "method": "jplag",
                   "params": {"mockScenario": "success"}, "submissions": []}
            for i in range(5):
                sid = str(uuid4())
                path = root / "submissions" / sid / "Main.java"
                path.parent.mkdir(parents=True)
                path.write_text(f"class Main{i} {{}}")
                job["submissions"].append({"id": sid, "language": "java", "files": ["Main.java"]})
            (root / "job.json").write_text(json.dumps(job))
            self.assertEqual(run(root, root / "output")["methodVersion"], "mock-v1")

    def test_missing_source_is_invalid_input(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            job = {"contract": CONTRACT, "runId": str(uuid4()), "method": "jplag", "params": {},
                   "submissions": [{"id": str(uuid4()), "language": "java", "files": ["Main.java"]} for _ in range(5)]}
            (root / "job.json").write_text(json.dumps(job))
            with self.assertRaises(InvalidInput):
                run(root, root / "output")


if __name__ == "__main__":
    unittest.main()
