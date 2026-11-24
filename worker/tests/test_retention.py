import hashlib
import secrets
import time
import unittest
from urllib.error import HTTPError
from urllib.request import Request, urlopen
from uuid import uuid4

from botocore.exceptions import ClientError

from worker.__main__ import Worker


class RetentionTest(unittest.TestCase):
    def test_expiry_clears_current_slot_and_retries_object_deletion(self):
        worker = Worker()
        teacher, student, assignment, submission, run = (uuid4() for _ in range(5))
        key = f"submissions/{submission}/source.java"
        orphan = f"submissions/{uuid4()}/source.java"
        data = b"class Main {}\n"
        worker.s3.put_object(Bucket=worker.bucket, Key=key, Body=data)
        worker.s3.put_object(Bucket=worker.bucket, Key=orphan, Body=data)
        for object_key in (key, orphan):
            self.addCleanup(worker.s3.delete_object, Bucket=worker.bucket, Key=object_key)
        session = secrets.token_urlsafe(32)

        with worker.connect() as db, db.transaction():
            for uid, role in ((teacher, "teacher"), (student, "student")):
                db.execute("""
                    insert into users(id, full_name, email, password_hash, role)
                    values (%s, 'Retention test', %s, 'unused', %s)
                    """, (uid, f"retention-{uid}@example.org", role))
            db.execute("""
                insert into sessions(token_hash, user_id, expires_at)
                values (%s, %s, now() + interval '1 hour')
                """, (hashlib.sha256(session.encode()).digest(), teacher))
            db.execute("""
                insert into assignments(id, teacher_id, title, submission_token)
                values (%s, %s, 'Retention test', %s)
                """, (assignment, teacher, str(assignment)))
            db.execute("insert into submission_slots(assignment_id, student_id) values (%s, %s)", (assignment, student))
            db.execute("""
                insert into submissions(id, assignment_id, student_id, storage_key, size_bytes, sha256, loc,
                    submitted_at, expires_at)
                values (%s, %s, %s, %s, %s, %s, 1, now() - interval '4 years', now() + interval '1 hour')
                """, (submission, assignment, student, key, len(data), hashlib.sha256(data).hexdigest()))
            db.execute("""
                update submission_slots set current_submission_id = %s, can_submit = false
                where assignment_id = %s and student_id = %s
                """, (submission, assignment, student))
            db.execute("""
                insert into analysis_runs(id, method, contract_version, method_version, submission_id, status)
                values (%s, 'ai', 'integritylens.ai/1', 'retention-test', %s, 'done')
                """, (run, submission))
            db.execute("insert into ai_results(run_id, score, suspicious) values (%s, 0.5, '[]')", (run,))
            db.execute("""
                insert into object_deletions(storage_key, queued_at) values (%s, now() - interval '2 minutes')
                """, (orphan,))

        self.addCleanup(self.cleanup_database, worker, teacher, student, assignment, submission, key, orphan)
        source = Request(f"http://api:8080/api/submissions/{submission}/source",
                         headers={"Cookie": f"session={session}"})
        with urlopen(source, timeout=15) as response:
            self.assertEqual(response.read(), data)

        with worker.connect() as db:
            db.execute("update submissions set expires_at = now() - interval '1 minute' where id = %s", (submission,))
            self.wait_until(lambda: db.execute("""
                select current_submission_id is null and can_submit as reopened
                from submission_slots where assignment_id = %s and student_id = %s
                """, (assignment, student)).fetchone()["reopened"], "expired current slot to reopen")
            self.assertIsNone(db.execute("select id from submissions where id = %s", (submission,)).fetchone())
            self.assertIsNone(db.execute("select run_id from ai_results where run_id = %s", (run,)).fetchone())
            self.wait_until(lambda: db.execute("""
                select 1 from object_deletions where storage_key = %s
                """, (key,)).fetchone() is not None, "expired object to enter the deletion queue")
            db.execute("update object_deletions set queued_at = now() - interval '2 minutes' where storage_key = %s",
                       (key,))
            self.wait_until(lambda: not db.execute("""
                select 1 from object_deletions where storage_key in (%s, %s)
                """, (key, orphan)).fetchone(), "expired and orphaned objects to leave deletion queue")

        with self.assertRaises(HTTPError) as error:
            urlopen(source, timeout=15)
        self.assertEqual(error.exception.code, 404)
        for object_key in (key, orphan):
            with self.assertRaises(ClientError) as error:
                worker.s3.head_object(Bucket=worker.bucket, Key=object_key)
            self.assertEqual(error.exception.response["ResponseMetadata"]["HTTPStatusCode"], 404)

    def wait_until(self, condition, label):
        deadline = time.monotonic() + 75
        while not condition():
            if time.monotonic() >= deadline:
                self.fail(f"Timed out waiting for {label}")
            time.sleep(0.5)

    @staticmethod
    def cleanup_database(worker, teacher, student, assignment, submission, key, orphan):
        with worker.connect() as db, db.transaction():
            db.execute("update submission_slots set current_submission_id = null, can_submit = true "
                       "where assignment_id = %s", (assignment,))
            db.execute("delete from submissions where id = %s", (submission,))
            db.execute("delete from submission_slots where assignment_id = %s", (assignment,))
            db.execute("delete from assignments where id = %s", (assignment,))
            db.execute("delete from users where id in (%s, %s)", (teacher, student))
            db.execute("delete from object_deletions where storage_key in (%s, %s)", (key, orphan))
