import os
import unittest
from unittest import mock
from uuid import uuid4

from worker.__main__ import Worker

SOURCE = "class Main {\n    // комментарий\n    int x = 1;\n}\n".encode("utf-8")

AI_ENV = {"WORKER_METHOD": "ai", "CORE_ENGINE": "mock", "MOCK_DELAY_SECONDS": "0",
          "MOCK_SCENARIO": "success"}


class AiQueueTest(unittest.TestCase):
    def setUp(self):
        with mock.patch.dict(os.environ, AI_ENV):
            self.worker = Worker()
        self.batch_id, self.submission_id, self.run_id = uuid4(), uuid4(), uuid4()
        with self.worker.connect() as db:
            db.execute("""insert into batches(id, title, language, archive_name, archive_sha256, submission_count)
                          values (%s, 'ai queue test', 'java', 'test.zip', %s, 5)""",
                       (self.batch_id, str(uuid4())))
            for _ in range(5):
                db.execute("""insert into submissions(id, batch_id, original_name, language, loc, size_bytes,
                                                      sha256, storage_key)
                              values (%s, %s, 'Main.java', 'java', 3, %s, %s, %s)""",
                           (uuid4(), self.batch_id, len(SOURCE), uuid4().hex, f"batches/{uuid4()}/source.java"))
            db.execute("""insert into submissions(id, batch_id, original_name, language, loc, size_bytes,
                                                  sha256, storage_key)
                          values (%s, %s, 'Main.java', 'java', 3, %s, %s, %s)""",
                       (self.submission_id, self.batch_id, len(SOURCE), uuid4().hex, f"batches/{uuid4()}/source.java"))
            db.execute("insert into runs(id, batch_id, method, submission_id) values (%s, %s, 'ai', %s)",
                       (self.run_id, self.batch_id, self.submission_id))
        self.worker.s3 = mock.Mock()
        self.worker.s3.get_object.side_effect = lambda **_: {"Body": mock.Mock(read=lambda: SOURCE)}
        self.addCleanup(self.cleanup)

    def cleanup(self):
        with self.worker.connect() as db:
            db.execute("delete from runs where batch_id = %s", (self.batch_id,))
            db.execute("delete from submissions where batch_id = %s", (self.batch_id,))
            db.execute("delete from batches where id = %s", (self.batch_id,))

    def row(self):
        with self.worker.connect() as db:
            return db.execute("select * from runs where id = %s", (self.run_id,)).fetchone()

    def test_ai_run_processes_single_submission(self):
        run = self.worker.claim()
        self.worker.process(run)
        row = self.row()
        self.assertEqual(row["status"], "done")
        self.assertEqual(row["method_version"], "aicode-mock/1")
        self.assertEqual(row["params"], {"threshold": 0.05, "mockScenario": "success", "delaySeconds": 0.0})
        result = row["result"]  # jsonb уже распарсен psycopg'ом
        self.assertEqual(result["contract"], "integritylens.ai/1")
        self.assertEqual([f["submissionId"] for f in result["files"]], [str(self.submission_id)])
        self.assertEqual(result["files"][0]["status"], "ok")
        for fragment in result["files"][0]["suspicious"]:
            SOURCE[fragment["startIndex"]:fragment["endIndex"]].decode("utf-8")  # границы — байты UTF-8

    def test_invalid_input_fails_without_retries(self):
        self.worker.scenario = "invalid_input"
        run = self.worker.claim()
        self.worker.process(run)
        row = self.row()
        self.assertEqual((row["status"], row["attempts"]), ("failed", 1))


if __name__ == "__main__":
    unittest.main()
