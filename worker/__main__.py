import json
import logging
import os
import signal
import socket
import subprocess
import sys
import tempfile
import threading
import time
from pathlib import Path, PurePosixPath
from uuid import uuid4

import boto3
import psycopg
from botocore.config import Config
from psycopg.rows import dict_row
from psycopg.types.json import Jsonb

from worker.core.contract import CONTRACT, validate_result, validate_schema

LOG = logging.getLogger("worker")


class JsonFormatter(logging.Formatter):
    def format(self, record):
        return json.dumps({"timestamp": self.formatTime(record), "level": record.levelname,
                           "message": record.getMessage(), "batchId": getattr(record, "batchId", None),
                           "runId": getattr(record, "runId", None)})


class RunError(Exception):
    def __init__(self, message, retryable=True):
        super().__init__(message)
        self.retryable = retryable


class LeaseLost(Exception):
    pass


class Worker:
    def __init__(self):
        self.method = os.environ["WORKER_METHOD"]
        if self.method not in ("jplag", "structsim"):
            raise ValueError("Unknown WORKER_METHOD")
        self.database = os.environ["DATABASE_URL"]
        self.worker_id = f"{socket.gethostname()}-{uuid4()}"
        self.bucket = os.getenv("S3_BUCKET", "integritylens")
        self.delay = float(os.getenv("MOCK_DELAY_SECONDS", "2"))
        self.scenario = os.getenv("MOCK_SCENARIO", "success")
        if self.scenario not in ("success", "partial", "fail_once", "tool_error", "invalid_input"):
            raise ValueError("Unknown MOCK_SCENARIO")
        self.timeout = float(os.getenv("CORE_TIMEOUT_SECONDS", "600"))
        self.stop = threading.Event()
        self.s3 = boto3.client("s3", endpoint_url=os.environ["S3_ENDPOINT"], region_name="us-east-1",
                               aws_access_key_id=os.environ["S3_ACCESS_KEY"],
                               aws_secret_access_key=os.environ["S3_SECRET_KEY"],
                               config=Config(connect_timeout=5, read_timeout=30, retries={"max_attempts": 2},
                                             s3={"addressing_style": "path"}))

    def connect(self):
        return psycopg.connect(self.database, autocommit=True, row_factory=dict_row, connect_timeout=5)

    def claim(self):
        with self.connect() as db:
            db.execute("""
                update runs set status = 'failed', finished_at = now(),
                    error = 'Worker heartbeat expired after final attempt', locked_by = null
                where method = %s and status = 'running' and attempts >= max_attempts
                    and heartbeat_at < now() - interval '2 minutes'
                """, (self.method,))
            return db.execute("""
                update runs set status = 'running', attempts = attempts + 1, locked_by = %s,
                    started_at = now(), heartbeat_at = now(), finished_at = null, error = null
                where id = (
                    select id from runs where method = %s and attempts < max_attempts
                        and (status = 'queued' or (status = 'running'
                            and heartbeat_at < now() - interval '2 minutes'))
                    order by created_at, id for update skip locked limit 1
                ) returning *
                """, (self.worker_id, self.method)).fetchone()

    def heartbeat(self, run, finished, lost):
        while not finished.wait(15):
            try:
                with self.connect() as db:
                    changed = db.execute("""
                        update runs set heartbeat_at = now()
                        where id = %s and attempts = %s and locked_by = %s and status = 'running'
                        """, (run["id"], run["attempts"], self.worker_id)).rowcount
                if changed != 1:
                    lost.set()
                    return
            except Exception:
                lost.set()
                return

    def execute(self, run, lost):
        with tempfile.TemporaryDirectory(prefix="integritylens-") as folder:
            root = Path(folder)
            input_dir, output_dir = root / "input", root / "output"
            input_dir.mkdir()
            with self.connect() as db:
                submissions = db.execute("select * from submissions where batch_id = %s order by id",
                                         (run["batch_id"],)).fetchall()
            scenario = self.scenario
            if scenario == "fail_once":
                scenario = "tool_error" if run["attempts"] == 1 else "success"
            job = {"contract": CONTRACT, "runId": str(run["id"]), "method": self.method,
                   "params": {**run["params"], "mockScenario": scenario, "delaySeconds": self.delay},
                   "submissions": []}
            for submission in submissions:
                sid = str(submission["id"])
                name = PurePosixPath(submission["original_name"]).name
                destination = input_dir / "submissions" / sid / name
                destination.parent.mkdir(parents=True)
                response = self.s3.get_object(Bucket=self.bucket, Key=submission["storage_key"])
                try:
                    destination.write_bytes(response["Body"].read())
                finally:
                    response["Body"].close()
                job["submissions"].append({"id": sid, "language": submission["language"], "files": [name]})
            validate_schema("job", job)
            (input_dir / "job.json").write_text(json.dumps(job), encoding="utf-8")
            with tempfile.TemporaryFile() as stderr:
                process = subprocess.Popen([sys.executable, "-m", "worker.core", "run", "--input", str(input_dir),
                                            "--output", str(output_dir)], stdout=subprocess.DEVNULL, stderr=stderr)
                try:
                    deadline = time.monotonic() + self.timeout
                    while process.poll() is None:
                        if lost.is_set() or self.stop.is_set():
                            raise LeaseLost()
                        if time.monotonic() >= deadline:
                            raise RunError("Core timeout")
                        time.sleep(0.2)
                    if process.returncode != 0:
                        raise RunError(f"Core exited with code {process.returncode}", process.returncode != 2)
                finally:
                    if process.poll() is None:
                        process.kill()
                    process.wait()
            result = json.loads((output_dir / "result.json").read_text(encoding="utf-8"))
            validate_result(job, result)
            if lost.is_set():
                raise LeaseLost()
            prefix = f"runs/{run['id']}/attempt-{run['attempts']}/"
            for artifact in output_dir.rglob("*"):
                if artifact.is_file():
                    self.s3.upload_file(str(artifact), self.bucket, prefix + artifact.relative_to(output_dir).as_posix())
            with self.connect() as db:
                changed = db.execute("""
                    update runs set status = 'done', result = %s, finished_at = now(),
                        method_version = %s, params = %s, artifacts_prefix = %s, error = null, locked_by = null
                    where id = %s and attempts = %s and locked_by = %s and status = 'running'
                    """, (Jsonb(result), result["methodVersion"], Jsonb(result["params"]), prefix,
                          run["id"], run["attempts"], self.worker_id)).rowcount
            if changed != 1:
                raise LeaseLost()

    def fail(self, run, error):
        retryable = error.retryable if isinstance(error, RunError) else True
        message = str(error) if isinstance(error, RunError) else f"Worker operation failed ({type(error).__name__})"
        with self.connect() as db:
            db.execute("""
                update runs set status = case when %s and attempts < max_attempts then 'queued' else 'failed' end,
                    finished_at = case when %s and attempts < max_attempts then null else now() end,
                    error = %s, locked_by = null, heartbeat_at = null
                where id = %s and attempts = %s and locked_by = %s and status = 'running'
                """, (retryable, retryable, message, run["id"], run["attempts"], self.worker_id))

    def process(self, run):
        fields = {"batchId": str(run["batch_id"]), "runId": str(run["id"])}
        finished, lost = threading.Event(), threading.Event()
        heartbeat = threading.Thread(target=self.heartbeat, args=(run, finished, lost), daemon=True)
        heartbeat.start()
        try:
            LOG.info("Run started", extra=fields)
            self.execute(run, lost)
            LOG.info("Run completed", extra=fields)
        except LeaseLost:
            LOG.warning("Run interrupted or lease lost", extra=fields)
        except Exception as error:
            self.fail(run, error)
            LOG.warning("Run failed: %s", type(error).__name__, extra=fields)
        finally:
            finished.set()
            heartbeat.join(timeout=10)

    def serve(self):
        while not self.stop.is_set():
            try:
                run = self.claim()
                if run:
                    self.process(run)
                else:
                    self.stop.wait(2)
            except Exception as error:
                LOG.error("Worker operation failed: %s", type(error).__name__)
                self.stop.wait(2)


def main():
    handler = logging.StreamHandler()
    handler.setFormatter(JsonFormatter())
    logging.basicConfig(level=logging.INFO, handlers=[handler])
    worker = Worker()
    signal.signal(signal.SIGTERM, lambda *_: worker.stop.set())
    signal.signal(signal.SIGINT, lambda *_: worker.stop.set())
    worker.serve()


if __name__ == "__main__":
    main()
