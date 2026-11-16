import concurrent.futures
import tempfile
import unittest
from pathlib import Path
from unittest import mock
from uuid import uuid4

from worker.__main__ import LeaseLost, RunError, Worker


class QueueTest(unittest.TestCase):
    def setUp(self):
        self.first, self.second = Worker(), Worker()
        self.batch_id, self.run_id = uuid4(), uuid4()
        with self.first.connect() as db:
            db.execute("""insert into batches(id, title, language, archive_name, archive_sha256, submission_count)
                          values (%s, 'queue test', 'java', 'test.zip', %s, 5)""", (self.batch_id, str(uuid4())))
            db.execute("insert into runs(id, batch_id, method) values (%s, %s, %s)",
                       (self.run_id, self.batch_id, self.first.method))
        self.addCleanup(self.cleanup)

    def cleanup(self):
        with self.first.connect() as db:
            db.execute("delete from runs where batch_id = %s", (self.batch_id,))
            db.execute("delete from batches where id = %s", (self.batch_id,))

    def row(self):
        with self.first.connect() as db:
            return db.execute("select * from runs where id = %s", (self.run_id,)).fetchone()

    def test_concurrent_claim_has_one_owner(self):
        with concurrent.futures.ThreadPoolExecutor(max_workers=2) as executor:
            runs = list(executor.map(lambda w: w.claim(), [self.first, self.second]))
        self.assertEqual(sum(r is not None for r in runs), 1)
        self.assertEqual(self.row()["attempts"], 1)

    def test_stale_attempt_cannot_change_new_owner(self):
        old = self.first.claim()
        with self.first.connect() as db:
            db.execute("update runs set heartbeat_at = now() - interval '3 minutes' where id = %s", (self.run_id,))
        current = self.second.claim()
        self.assertEqual(current["attempts"], 2)
        self.first.fail(old, RunError("stale failure", False))
        self.assertEqual(self.row()["status"], "running")
        self.assertEqual(self.row()["locked_by"], self.second.worker_id)
        self.second.fail(current, RunError("retry"))
        last = self.first.claim()
        self.first.fail(last, RunError("last retry"))
        self.assertEqual(self.row()["status"], "failed")
        self.assertIsNone(self.first.claim())

    def test_invalid_input_fails_without_retries(self):
        run = self.first.claim()
        self.first.fail(run, RunError("invalid input", False))
        self.assertEqual(self.row()["status"], "failed")
        self.assertEqual(self.row()["attempts"], 1)

    def test_stopping_worker_returns_run_without_spending_attempt(self):
        run = self.first.claim()
        self.first.stop.set()
        with mock.patch.object(self.first, "execute", side_effect=LeaseLost()):
            self.first.process(run)
        self.assertEqual((self.row()["status"], self.row()["attempts"], self.row()["locked_by"]), ("queued", 0, None))
        self.assertEqual(self.second.claim()["attempts"], 1)

    def test_lost_lease_does_not_touch_new_owner(self):
        old = self.first.claim()
        with self.first.connect() as db:
            db.execute("update runs set heartbeat_at = now() - interval '3 minutes' where id = %s", (self.run_id,))
        self.second.claim()
        self.first.release(old)
        self.assertEqual((self.row()["status"], self.row()["locked_by"]), ("running", self.second.worker_id))

    def test_inconvenient_file_names_do_not_fail_the_batch(self):
        with tempfile.TemporaryDirectory() as folder:
            self.first.s3 = mock.Mock()
            self.first.s3.get_object.side_effect = lambda **_: {"Body": mock.Mock(read=lambda: b"class A {}")}
            for original, expected in (("folder/Main.java", "Main.java"), ("folder/.java", "Main.java"),
                                       ("я" * 126 + ".java", "Main.java"), ("Работа 1.java", "Работа 1.java")):
                entry = self.first.download({"id": uuid4(), "original_name": original, "language": "java",
                                             "storage_key": "key"}, Path(folder))
                self.assertEqual(entry["files"], [expected])

    def test_expired_final_attempt_becomes_failed(self):
        self.first.claim()
        with self.first.connect() as db:
            db.execute("""update runs set attempts = max_attempts, heartbeat_at = now() - interval '3 minutes'
                          where id = %s""", (self.run_id,))
        self.assertIsNone(self.second.claim())
        self.assertEqual(self.row()["status"], "failed")


if __name__ == "__main__":
    unittest.main()
