import time
import unittest
from uuid import uuid4

from worker.__main__ import Worker


class VersionUpgradeTest(unittest.TestCase):
    def test_current_similarity_is_replaced_once_after_upgrade(self):
        worker = Worker()
        teacher, assignment, old_run = (uuid4() for _ in range(3))
        students = [uuid4(), uuid4()]
        submissions = [uuid4(), uuid4()]
        try:
            with worker.connect() as db, db.transaction():
                db.execute("insert into users(id, full_name, email, password_hash, role) "
                           "values (%s, 'Upgrade', %s, 'unused', 'teacher')",
                           (teacher, f"{teacher}@example.org"))
                db.execute("insert into assignments(id, teacher_id, title, submission_token) "
                           "values (%s, %s, 'Upgrade', %s)", (assignment, teacher, str(assignment)))
                for student, submission in zip(students, submissions):
                    db.execute("insert into users(id, full_name, email, password_hash, role) "
                               "values (%s, 'Upgrade', %s, 'unused', 'student')",
                               (student, f"{student}@example.org"))
                    db.execute("insert into submission_slots(assignment_id, student_id) values (%s, %s)",
                               (assignment, student))
                    db.execute("""
                        insert into submissions(id, assignment_id, student_id, storage_key, size_bytes,
                                                sha256, loc, submitted_at, expires_at)
                        values (%s, %s, %s, %s, 1, %s, 1, now(), now() + interval '3 years')
                        """, (submission, assignment, student, str(submission), '0' * 64))
                    db.execute("update submission_slots set current_submission_id=%s, can_submit=false "
                               "where assignment_id=%s and student_id=%s", (submission, assignment, student))
                db.execute("""
                    insert into analysis_runs(id, method, contract_version, method_version, assignment_id, revision)
                    values (%s, 'structsim', 'integritylens.core/2', 'before-upgrade', %s, 0)
                    """, (old_run, assignment))
            deadline = time.monotonic() + 30
            with worker.connect() as db:
                while True:
                    runs = db.execute("select * from analysis_runs where assignment_id=%s order by revision",
                                      (assignment,)).fetchall()
                    if len(runs) == 2:
                        break
                    self.assertLess(time.monotonic(), deadline, 'No replacement run after version upgrade')
                    time.sleep(0.5)
                self.assertEqual(runs[0]['status'], 'cancelled')
                self.assertEqual(runs[1]['method_version'], worker.version)
                self.assertEqual(runs[1]['revision'], 1)
                self.assertEqual(db.execute("select count(*) as n from analysis_run_inputs where run_id=%s",
                                            (runs[1]['id'],)).fetchone()['n'], 2)
                time.sleep(5)
                self.assertEqual(db.execute("select count(*) as n from analysis_runs where assignment_id=%s",
                                            (assignment,)).fetchone()['n'], 2)
        finally:
            with worker.connect() as db, db.transaction():
                db.execute("delete from analysis_runs where assignment_id=%s", (assignment,))
                db.execute("update submission_slots set current_submission_id=null, can_submit=true "
                           "where assignment_id=%s", (assignment,))
                db.execute("delete from submissions where assignment_id=%s", (assignment,))
                db.execute("delete from submission_slots where assignment_id=%s", (assignment,))
                db.execute("delete from assignments where id=%s", (assignment,))
                db.execute("delete from users where id = any(%s)", ([teacher, *students],))
