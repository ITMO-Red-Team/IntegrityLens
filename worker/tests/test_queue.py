import unittest
from concurrent.futures import ThreadPoolExecutor
from uuid import uuid4

from worker.__main__ import LeaseLost, RunError, Worker


class QueueTest(unittest.TestCase):
    def setUp(self):
        self.worker = Worker()
        self.other = Worker()
        self.teacher = uuid4()
        self.assignment = uuid4()
        with self.worker.connect() as db:
            db.execute("""
                insert into users(id, full_name, email, password_hash, role)
                values (%s, 'Queue test', %s, 'unused', 'teacher')
                """, (self.teacher, f"queue-{self.teacher}@example.org"))
            db.execute("""
                insert into assignments(id, teacher_id, title, submission_token)
                values (%s, %s, 'Queue test', %s)
                """, (self.assignment, self.teacher, str(self.assignment)))
        self.addCleanup(self.cleanup_rows)

    def cleanup_rows(self):
        with self.worker.connect() as db:
            db.execute("delete from analysis_runs where assignment_id = %s", (self.assignment,))
            db.execute("delete from assignments where id = %s", (self.assignment,))
            db.execute("delete from users where id = %s", (self.teacher,))

    def enqueue(self, max_attempts=3):
        run_id = uuid4()
        with self.worker.connect() as db:
            db.execute("""
                insert into analysis_runs(id, method, contract_version, method_version, assignment_id, revision, max_attempts)
                values (%s, 'structsim', 'integritylens.core/2', %s, %s, 0, %s)
                """, (run_id, self.worker.version, self.assignment, max_attempts))
        return run_id

    def expire_lease(self, run_id):
        with self.worker.connect() as db:
            db.execute("update analysis_runs set heartbeat_at = now() - interval '3 minutes' where id = %s", (run_id,))

    def state(self, run_id):
        with self.worker.connect() as db:
            return db.execute("select * from analysis_runs where id = %s", (run_id,)).fetchone()

    def test_one_owner_and_expired_attempt_cannot_publish(self):
        run_id = self.enqueue()
        with ThreadPoolExecutor(2) as pool:
            outcomes = list(pool.map(lambda worker: worker.claim(), (self.worker, self.other)))
        first, second = (self.worker, self.other) if outcomes[0] else (self.other, self.worker)
        claim = next(row for row in outcomes if row)
        self.assertEqual(sum(row is not None for row in outcomes), 1)
        self.assertEqual(claim["id"], run_id)
        self.assertEqual(claim["attempts"], 1)

        self.expire_lease(run_id)
        with first.connect() as db, self.assertRaises(LeaseLost):
            first.publish(db, claim, {"pairs": [], "params": {}})
        replacement = second.claim()
        self.assertEqual(replacement["attempts"], 2)
        with first.connect() as db, self.assertRaises(LeaseLost):
            first.publish(db, claim, {"pairs": [], "params": {}})
        first.fail(claim, RunError("OUTDATED_ATTEMPT", False))
        self.assertEqual(self.state(run_id)["locked_by"], second.worker_id)
        second.fail(replacement, RunError("TOOL_ERROR"))
        self.assertEqual(self.state(run_id)["status"], "queued")

    def test_stale_revision_cannot_publish(self):
        run_id = self.enqueue()
        run = self.worker.claim()
        with self.worker.connect() as db:
            db.execute("update assignments set similarity_revision = 1 where id = %s", (self.assignment,))
            with self.assertRaises(LeaseLost):
                self.worker.publish(db, run, {"pairs": [], "params": {}})
        self.assertEqual(self.state(run_id)["status"], "running")

    def test_last_expired_attempt_fails_and_is_not_reclaimed(self):
        run_id = self.enqueue(max_attempts=1)
        self.worker.claim()
        self.expire_lease(run_id)
        self.assertIsNone(self.other.claim())
        row = self.state(run_id)
        self.assertEqual((row["status"], row["error_code"], row["locked_by"]), ("failed", "LEASE_EXPIRED", None))

    def test_released_attempt_can_be_claimed_again(self):
        run_id = self.enqueue()
        run = self.worker.claim()
        self.worker.release(run)
        self.assertEqual(self.state(run_id)["attempts"], 0)
        self.assertEqual(self.other.claim()["id"], run_id)
