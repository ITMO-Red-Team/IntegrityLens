import os
import tempfile
import threading
import time
import unittest
from pathlib import Path
from unittest import mock

ENV = {"WORKER_METHOD": "ai", "CORE_ENGINE": "mock", "MOCK_SCENARIO": "success",
       "WORKER_METHOD_VERSION": "aicode-mock/1", "DATABASE_URL": "postgresql://unused",
       "S3_ENDPOINT": "http://unused", "S3_ACCESS_KEY": "unused", "S3_SECRET_KEY": "unused"}


class CoreProcessTest(unittest.TestCase):
    def setUp(self):
        patcher = mock.patch.dict(os.environ, ENV)
        patcher.start()
        self.addCleanup(patcher.stop)
        from worker.__main__ import Worker
        self.worker = Worker()
        self.lost = threading.Event()
        folder = tempfile.TemporaryDirectory()
        self.addCleanup(folder.cleanup)
        self.pid_file = Path(folder.name) / "child.pid"

    def fake_core(self, script):
        self.worker.core_command = lambda *_: ["sh", "-c", script]

    def child_is_gone(self):
        child = int(self.pid_file.read_text())
        for _ in range(100):
            try:
                os.kill(child, 0)
            except ProcessLookupError:
                return True
            # Без init зомби некому забрать, но процесс уже убит: это тоже результат.
            status = Path(f"/proc/{child}/stat")
            if status.exists() and status.read_text().rsplit(")", 1)[1].split()[0] == "Z":
                return True
            time.sleep(0.05)
        return False

    def test_timeout_kills_children_of_core(self):
        from worker.__main__ import RunError
        self.worker.timeout = 0.5
        self.fake_core(f"sleep 60 & echo $! > {self.pid_file}; wait")
        with self.assertRaises(RunError) as caught:
            self.worker.run_core("in", "out", self.lost)
        self.assertFalse(caught.exception.retryable)
        self.assertTrue(self.child_is_gone())

    def test_lost_lease_kills_children_of_core(self):
        from worker.__main__ import LeaseLost
        self.fake_core(f"sleep 60 & echo $! > {self.pid_file}; wait")
        threading.Timer(0.3, self.lost.set).start()
        with self.assertRaises(LeaseLost):
            self.worker.run_core("in", "out", self.lost)
        self.assertTrue(self.child_is_gone())

    def test_exit_codes_decide_retry(self):
        from worker.__main__ import RunError
        for exit_status, code, retryable in ((1, "TOOL_ERROR", True), (2, "INVALID_JOB", False)):
            self.fake_core(f"echo private-source >&2; exit {exit_status}")
            with self.assertRaises(RunError) as caught:
                self.worker.run_core("in", "out", self.lost)
            self.assertEqual(caught.exception.code, code)
            self.assertEqual(caught.exception.retryable, retryable)

    def test_mock_params_from_the_queue_are_rejected(self):
        from worker.__main__ import RunError
        self.worker.engine = "real"
        for name in ("mockScenario", "delaySeconds"):
            with self.assertRaises(RunError) as caught:
                self.worker.params({"params": {name: "success"}, "attempts": 1})
            self.assertFalse(caught.exception.retryable)
        self.assertEqual(self.worker.params({"params": {"threshold": 0.05}, "attempts": 1}), {"threshold": 0.05})
        self.worker.engine = "mock"
        self.assertEqual(self.worker.params({"params": {}, "attempts": 1})["mockScenario"], "success")

    def test_success_returns_quickly(self):
        self.fake_core("exit 0")
        started = time.monotonic()
        self.worker.run_core("in", "out", self.lost)
        self.assertLess(time.monotonic() - started, 0.2)


if __name__ == "__main__":
    unittest.main()
