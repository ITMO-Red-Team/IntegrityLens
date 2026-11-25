import hashlib
import importlib
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
from pathlib import Path
from uuid import uuid4

import boto3
import psycopg
from botocore.config import Config
from psycopg.rows import dict_row
from psycopg.types.json import Jsonb

LOG = logging.getLogger("worker")
MOCK_PARAMS = ("mockScenario", "delaySeconds")
LEASE = "interval '2 minutes'"
OWNED = f"""id = %s and attempts = %s and locked_by = %s and status = 'running'
            and heartbeat_at > clock_timestamp() - {LEASE}"""


class RunError(Exception):
    def __init__(self, code, retryable=True):
        super().__init__(code)
        self.code = code
        self.retryable = retryable


class LeaseLost(Exception):
    pass


class Worker:
    def __init__(self):
        self.method = os.environ["WORKER_METHOD"]
        contract_module, self.engine_module = {
            "structsim": ("worker.core.v3", "worker.core.v3"),
            "ai": ("worker.ai.contract", "worker.ai"),
        }[self.method]
        self.contract = importlib.import_module(contract_module)
        self.database = os.getenv("DATABASE_URL", "")
        self.worker_id = f"{socket.gethostname()}-{uuid4()}"
        self.bucket = os.getenv("S3_BUCKET", "integritylens")
        self.engine = os.getenv("CORE_ENGINE", "real")
        if self.engine not in ("real", "mock"):
            raise ValueError("Unknown CORE_ENGINE")
        self.delay = float(os.getenv("MOCK_DELAY_SECONDS", "2"))
        self.scenario = os.getenv("MOCK_SCENARIO", "success")
        if self.scenario not in ("success", "partial", "fail_once", "tool_error", "invalid_input"):
            raise ValueError("Unknown MOCK_SCENARIO")
        version_module = self.contract if self.method == "structsim" else importlib.import_module("worker.ai.version")
        self.version = version_module.MOCK_VERSION if self.engine == "mock" else version_module.method_version()
        configured = os.getenv("WORKER_METHOD_VERSION", self.version)
        if configured != self.version:
            raise ValueError(f"{self.method} method version mismatch: configured {configured}, image {self.version}")
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
            db.execute(f"""
                update analysis_runs set status = 'failed', finished_at = now(), error_code = 'LEASE_EXPIRED',
                    locked_by = null, heartbeat_at = null
                where id in (
                    select id from analysis_runs where method = %s and method_version = %s
                        and status = 'running' and attempts >= max_attempts
                        and heartbeat_at <= clock_timestamp() - {LEASE}
                    for update skip locked
                )
                """, (self.method, self.version))
            return db.execute(f"""
                update analysis_runs set status = 'running', attempts = attempts + 1, locked_by = %s,
                    started_at = now(), heartbeat_at = clock_timestamp(), finished_at = null, error_code = null
                where id = (
                    select id from analysis_runs where method = %s and method_version = %s and attempts < max_attempts
                        and (status = 'queued' or (status = 'running'
                            and heartbeat_at <= clock_timestamp() - {LEASE}))
                    order by created_at, id for update skip locked limit 1
                ) returning *
                """, (self.worker_id, self.method, self.version)).fetchone()

    def owner(self, run):
        return run["id"], run["attempts"], self.worker_id

    def heartbeat(self, run, finished, lost):
        while not finished.wait(15):
            try:
                with self.connect() as db:
                    changed = db.execute(f"""
                        update analysis_runs set heartbeat_at = clock_timestamp() where {OWNED}
                        """, self.owner(run)).rowcount
                if changed != 1:
                    lost.set()
                    return
            except Exception:
                lost.set()
                return

    def params(self, run):
        if any(name in run["params"] for name in MOCK_PARAMS):
            raise RunError("INVALID_JOB", False)
        if self.engine == "real":
            return run["params"]
        scenario = self.scenario
        if scenario == "fail_once":
            scenario = "tool_error" if run["attempts"] == 1 else "success"
        return {**run["params"], "mockScenario": scenario, "delaySeconds": self.delay}

    def download(self, submission, root):
        sid = str(submission["id"])
        path = Path("input", "submissions", sid, "source.java")
        destination = root / path
        destination.parent.mkdir(parents=True)
        response = self.s3.get_object(Bucket=self.bucket, Key=submission["storage_key"])
        with response["Body"] as body:
            data = body.read()
        if hashlib.sha256(data).hexdigest() != submission["sha256"]:
            raise RunError("SOURCE_MISMATCH", False)
        destination.write_bytes(data)
        return path.as_posix(), data

    def core_command(self, input_dir, output_dir):
        command = [sys.executable, "-m", self.engine_module]
        if self.method == "ai":
            command.append("run")
        return [*command, "--input", str(input_dir), "--output", str(output_dir)]

    def run_core(self, input_dir, output_dir, lost):
        process = subprocess.Popen(self.core_command(input_dir, output_dir), stdout=subprocess.DEVNULL,
                                   stderr=subprocess.DEVNULL, start_new_session=True)
        try:
            deadline = time.monotonic() + self.timeout
            while True:
                try:
                    process.wait(timeout=0.1)
                    break
                except subprocess.TimeoutExpired:
                    if lost.is_set() or self.stop.is_set():
                        raise LeaseLost()
                    if time.monotonic() >= deadline:
                        raise RunError("RUN_TIMEOUT", False)
        finally:
            try:
                os.killpg(process.pid, signal.SIGKILL)
            except (ProcessLookupError, PermissionError):
                pass
            process.wait()
        if process.returncode == 2:
            raise RunError("INVALID_JOB", False)
        if process.returncode != 0:
            raise RunError("TOOL_ERROR")

    def execute(self, run, lost):
        with tempfile.TemporaryDirectory(prefix="integritylens-") as folder, self.connect() as db:
            root = Path(folder)
            input_dir, output_dir = root / "input", root / "output"
            input_dir.mkdir()
            job = {"contract": run["contract_version"], "runId": str(run["id"]),
                   "methodVersion": run["method_version"], "params": self.params(run)}
            if self.method == "structsim":
                rows = db.execute("""
                    select s.id, s.storage_key, s.sha256, i.scope
                    from analysis_run_inputs i join submissions s on s.id = i.submission_id
                    where i.run_id = %s order by s.id
                    """, (run["id"],)).fetchall()
                inputs = []
                for row in rows:
                    if lost.is_set() or self.stop.is_set():
                        raise LeaseLost()
                    self.download(row, root)
                    inputs.append({"id": str(row["id"]), "language": "java", "files": ["source.java"],
                                   "scope": row["scope"]})
                job.update(method=self.method, revision=run["revision"], submissions=inputs)
                self.contract.validate_job(job)
            else:
                submission = db.execute("""
                    select id, storage_key, sha256 from submissions where id = %s
                    """, (run["submission_id"],)).fetchone()
                if submission is None:
                    raise LeaseLost()
                path, data = self.download(submission, root)
                sid = str(submission["id"])
                job["inputFiles"] = [{"submissionId": sid, "path": path}]
                self.contract.validate_schema("job", job)

            (input_dir / "job.json").write_text(json.dumps(job), encoding="utf-8")
            self.run_core(input_dir, output_dir, lost)
            result = json.loads((output_dir / "result.json").read_text(encoding="utf-8"))
            try:
                if self.method == "structsim":
                    self.contract.validate_result(job, result)
                else:
                    self.contract.validate_result(job, result, {sid: data})
            except ValueError as error:
                raise RunError("INVALID_RESULT", False) from error
            if lost.is_set() or self.stop.is_set():
                raise LeaseLost()
            self.publish(db, run, result)

    def publish(self, db, run, result):
        with db.transaction():
            if self.method == "structsim":
                assignment = db.execute("""
                    select similarity_revision, deleted_at from assignments where id = %s for share
                    """, (run["assignment_id"],)).fetchone()
                if assignment is None or assignment["deleted_at"] is not None \
                        or assignment["similarity_revision"] != run["revision"]:
                    raise LeaseLost()
            changed = db.execute(f"""
                update analysis_runs set status = 'done', params = %s, finished_at = now(),
                    locked_by = null, heartbeat_at = null, error_code = null
                where {OWNED}
                """, (Jsonb(result["params"]), *self.owner(run))).rowcount
            if changed != 1:
                raise LeaseLost()
            if self.method == "structsim":
                with db.cursor() as cursor:
                    cursor.executemany("""
                        update analysis_run_inputs set error_code = %s where run_id = %s and submission_id = %s
                        """, ((item["reason"]["code"], run["id"], item["id"]) for item in result["failedInputs"]))
                    cursor.executemany("""
                        insert into similarity_results(run_id, a, b, value, reason_code, details, fragments)
                        values (%s, %s, %s, %s, %s, %s, %s)
                        """, ((run["id"], pair["a"], pair["b"], pair["value"], pair.get("reason", {}).get("code"),
                               Jsonb(pair["details"]), Jsonb(pair["fragments"])) for pair in result["pairs"]))
            else:
                file = result["files"][0]
                db.execute("""
                    insert into ai_results(run_id, score, suspicious, reason_code) values (%s, %s, %s, %s)
                    """, (run["id"], file["score"], Jsonb(file["suspicious"]), file.get("reason", {}).get("code")))

    def release(self, run):
        with self.connect() as db:
            db.execute(f"""
                update analysis_runs set status = 'queued', attempts = attempts - 1, started_at = null,
                    locked_by = null, heartbeat_at = null where {OWNED}
                """, self.owner(run))

    def fail(self, run, error):
        with self.connect() as db:
            db.execute(f"""
                update analysis_runs set status = case when %s and attempts < max_attempts then 'queued' else 'failed' end,
                    finished_at = case when %s and attempts < max_attempts then null else now() end,
                    error_code = %s, locked_by = null, heartbeat_at = null where {OWNED}
                """, (error.retryable, error.retryable, error.code, *self.owner(run)))

    def process(self, run):
        finished, lost = threading.Event(), threading.Event()
        heartbeat = threading.Thread(target=self.heartbeat, args=(run, finished, lost), daemon=True)
        heartbeat.start()
        try:
            LOG.info("Run started %s attempt %s", run["id"], run["attempts"])
            self.execute(run, lost)
            LOG.info("Run completed %s", run["id"])
        except LeaseLost:
            if self.stop.is_set():
                self.release(run)
            LOG.info("Run interrupted or lease lost %s", run["id"])
        except Exception as error:
            failure = error if isinstance(error, RunError) else RunError("WORKER_ERROR")
            self.fail(run, failure)
            LOG.warning("Run %s failed: %s (%s)", run["id"], failure.code, type(error).__name__)
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
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
    worker = Worker()
    signal.signal(signal.SIGTERM, lambda *_: worker.stop.set())
    signal.signal(signal.SIGINT, lambda *_: worker.stop.set())
    worker.serve()


if __name__ == "__main__":
    main()
