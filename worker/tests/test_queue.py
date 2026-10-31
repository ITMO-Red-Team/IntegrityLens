import concurrent.futures
import time
import unittest
from uuid import uuid4

from worker.__main__ import RunError, Worker


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

    def test_queued_run_notifies_its_method_only(self):
        other = "structsim" if self.first.method == "jplag" else "jplag"
        with self.first.connect() as listener, self.first.connect() as db:
            listener.execute(f"listen runs_{self.first.method}")
            listener.execute(f"listen runs_{other}")
            db.execute("update runs set status = 'failed' where id = %s", (self.run_id,))
            self.assertEqual(list(listener.notifies(timeout=0.2, stop_after=1)), [])
            db.execute("update runs set status = 'queued' where id = %s", (self.run_id,))
            notes = list(listener.notifies(timeout=5, stop_after=1))
        self.assertEqual([(n.channel, n.payload) for n in notes], [(f"runs_{self.first.method}", str(self.run_id))])

    def test_wait_for_work_returns_on_notification(self):
        with self.first.connect() as listener, self.first.connect() as db:
            listener.execute(f"listen runs_{self.first.method}")
            self.first.poll = 30
            db.execute("update runs set status = 'queued' where id = %s", (self.run_id,))
            started = time.monotonic()
            self.first.wait_for_work(listener)
            self.assertLess(time.monotonic() - started, 5)

    def test_expired_final_attempt_becomes_failed(self):
        self.first.claim()
        with self.first.connect() as db:
            db.execute("""update runs set attempts = max_attempts, heartbeat_at = now() - interval '3 minutes'
                          where id = %s""", (self.run_id,))
        self.assertIsNone(self.second.claim())
        self.assertEqual(self.row()["status"], "failed")


if __name__ == "__main__":
    unittest.main()
