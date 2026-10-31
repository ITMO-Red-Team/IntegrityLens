"""Весь путь через работающий стек: загрузка в API → воркер → результат в API.

    python -m worker.bench.e2e --base http://localhost:18080 --conplag DIR --out e2e.csv --setup новый

Время берётся из самого API: `createdAt` пакета, `startedAt` и `finishedAt` прогонов. В паузах
между загрузками случайная задержка: иначе опрос очереди попадал бы всегда в одну фазу.
"""
import argparse
import csv
import io
import json
import random
import statistics
import time
import urllib.request
import uuid
import zipfile
from datetime import datetime
from pathlib import Path

from worker.bench.corpus import conplag


def request(url, data=None, headers=None):
    with urllib.request.urlopen(urllib.request.Request(url, data=data, headers=headers or {}), timeout=120) as reply:
        return json.load(reply)


def upload(base, sources):
    output = io.BytesIO()
    with zipfile.ZipFile(output, "w", zipfile.ZIP_DEFLATED) as zipped:
        marker = uuid.uuid4()
        for number, source in enumerate(sources):
            zipped.writestr(f"work{number}.java", f"// {marker}\n{source}")
    boundary = "IntegrityLensBench"
    body = (f"--{boundary}\r\nContent-Disposition: form-data; name=\"language\"\r\n\r\njava\r\n"
            f"--{boundary}\r\nContent-Disposition: form-data; name=\"archive\"; filename=\"bench.zip\"\r\n"
            "Content-Type: application/zip\r\n\r\n").encode() + output.getvalue() + f"\r\n--{boundary}--\r\n".encode()
    return request(f"{base}/api/batches", body, {"Content-Type": f"multipart/form-data; boundary={boundary}"})["id"]


def wait_done(base, batch_id):
    while True:
        batch = request(f"{base}/api/batches/{batch_id}")
        if batch["status"] in ("done", "failed"):
            return batch
        time.sleep(0.02)


def moment(text):
    return datetime.fromisoformat(text.replace("Z", "+00:00")).timestamp()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base", default="http://localhost:8080")
    parser.add_argument("--conplag", required=True)
    parser.add_argument("--out", required=True)
    parser.add_argument("--setup", required=True)
    parser.add_argument("--count", type=int, default=50)
    parser.add_argument("--runs", type=int, default=15)
    args = parser.parse_args()
    sources = conplag(args.conplag, args.count)
    samples = {}
    for _ in range(args.runs):
        time.sleep(random.uniform(0, 2))
        batch = wait_done(args.base, upload(args.base, sources))
        created = moment(batch["createdAt"])
        for run in batch["runs"]:
            if run["status"] != "done":
                raise SystemExit(f"прогон не удался: {run}")
            started, finished = moment(run["startedAt"]), moment(run["finishedAt"])
            samples.setdefault(run["method"], []).append((started - created, finished - started))
    out = Path(args.out)
    is_new = not out.exists()
    with out.open("a", newline="", encoding="utf-8") as handle:
        writer = csv.writer(handle)
        if is_new:
            writer.writerow(["setup", "method", "files", "runs", "pickup_median_ms", "pickup_max_ms", "work_median_ms"])
        for method, values in samples.items():
            pickup = [value[0] * 1000 for value in values]
            work = [value[1] * 1000 for value in values]
            row = [args.setup, method, args.count, args.runs, round(statistics.median(pickup)), round(max(pickup)),
                   round(statistics.median(work))]
            writer.writerow(row)
            print(row, flush=True)


if __name__ == "__main__":
    main()
