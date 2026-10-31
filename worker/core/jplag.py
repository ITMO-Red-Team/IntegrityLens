"""JPlag: запуск CLI и перевод отчёта в контракт core.

Оценки и куски берутся из `comparisons/` архива `.jplag`: там есть и обе метрики, и
строки. Архив режет пары по `-n` (tools/jplag/pitfalls.md, ловушка 1), поэтому `-n -1`
и проверка, что пришли все пары.
"""
import codecs
import json
import os
import re
import shlex
import subprocess
import tempfile
import zipfile
from pathlib import Path

from .contract import InvalidInput
from .engine import merged_params, positive_int, sources_of
from .result import assemble

VERSION = "6.3.0"
DEFAULT_PARAMS = {"minTokens": 9, "metric": "max"}
METRICS = {"avg": "AVG", "max": "MAX"}
# CLI синхронно и без таймаута спрашивает у api.github.com, нет ли новой версии: это
# 150 мс на хорошей сети и зависание на плохой. Отключить можно только прокси в никуда.
NO_VERSION_CHECK = ["-Dhttps.proxyHost=127.0.0.1", "-Dhttps.proxyPort=9"]
# На типичном наборе JVM не успевает окупить второй компилятор и тратит на него ядра.
# С нескольких мегабайт исходников он уже нужен: см. замеры в worker/bench/README.md.
C1_ONLY = ["-XX:TieredStopAtLevel=1"]
C1_MAX_BYTES = 1024 * 1024
ASCII = bytes(range(128))
FAILURES = {
    "CANNOT_PARSE": "Файл не удалось разобрать",
    "TOO_SMALL": "Слишком мало токенов для сравнения",
    "NOTHING_TO_PARSE": "В работе нечего разбирать",
}
UUID = r"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
NOT_ENOUGH = re.compile(r"Not enough valid submissions! \(found (\d+) valid")
LOGGED_FAILURES = (
    (re.compile(rf"Failed to parse submission ({UUID})"), "CANNOT_PARSE"),
    (re.compile(rf"Submission ({UUID}) contains \d+ tokens"), "TOO_SMALL"),
    (re.compile(rf'Nothing to parse for submission "({UUID})"'), "NOTHING_TO_PARSE"),
)


def _command(submissions, report, min_tokens, size):
    java = os.environ.get("JPLAG_JAVA", "java")
    jar = os.environ.get("JPLAG_JAR", "/opt/jplag/jplag.jar")
    options = shlex.split(os.environ.get("JPLAG_JVM_OPTS", ""))
    compiler = C1_ONLY if size <= C1_MAX_BYTES else []
    return [java, *NO_VERSION_CHECK, *compiler, *options, "-jar", jar, "--mode", "RUN", str(submissions),
            "-l", "java", "-r", str(report), "--cluster-skip", "--overwrite", "--encoding", "UTF-8",
            "-n", "-1", "-t", str(min_tokens), "--log-level=WARN"]


def _with_room(source):
    """Исходник, на котором JPlag не упадёт: tools/jplag/pitfalls.md, ловушки 6 и 7.

    Конец объявления JPlag считает как конец минус длина напечатанного инициализатора, а
    javac печатает не-ASCII символ шестью знаками. У начала файла смещение уходит ниже нуля,
    и работа считается неразобранной. Комментарий в первой строке сдвигает смещения, а строки
    и токены от него не меняются. Метка порядка байтов javac недопустима, её нужно убрать.
    """
    if source.startswith(codecs.BOM_UTF8):
        source = source[len(codecs.BOM_UTF8):]
    room = 6 * len(source.translate(None, ASCII)) + 2 * len(source) + 4096
    return b"/*" + b" " * room + b"*/" + source


def _copy_with_room(sources, root):
    for sid, path in sources.items():
        copy = root / sid / path.name
        copy.parent.mkdir()
        copy.write_bytes(_with_room(path.read_bytes()))


def _failures_from_log(log, ids, valid):
    """Причины, если запуск оборвался раньше отчёта: валидных работ меньше двух."""
    failed = {}
    for pattern, state in LOGGED_FAILURES:
        for sid in pattern.findall(log):
            failed[sid] = FAILURES[state]
    if len(failed) != len(ids) - valid:
        # Журнал разошёлся со счётчиком: кто разобрался, неизвестно, значит никто.
        return {sid: failed.get(sid, FAILURES["CANNOT_PARSE"]) for sid in ids}
    return failed


def _lines(start, end):
    # Конец бывает раньше начала, JPlag сам об этом предупреждает: см. _with_room.
    return [start["line"], max(start["line"], end["line"])]


def _pair(comparison, metric):
    first, second = comparison["firstSubmissionId"], comparison["secondSubmissionId"]
    fragments = []
    for match in comparison["matches"]:
        in_first = _lines(match["startInFirst"], match["endInFirst"])
        in_second = _lines(match["startInSecond"], match["endInSecond"])
        fragments.append({"a": in_first, "b": in_second} if first < second else {"a": in_second, "b": in_first})
    similarities = comparison["similarities"]
    return (min(first, second), max(first, second)), {
        "value": similarities[metric],
        "details": {"avg": similarities["AVG"], "max": similarities["MAX"],
                    "longestMatch": int(similarities["LONGEST_MATCH"])},
        "fragments": sorted(fragments, key=lambda f: (f["a"], f["b"])),
    }


def _read_report(path, ids, metric):
    with zipfile.ZipFile(path) as archive:
        info = json.loads(archive.read("runInformation.json"))
        index = json.loads(archive.read("submissionFileIndex.json"))["fileIndexes"]
        scored = dict(_pair(json.loads(archive.read(name)), metric) for name in archive.namelist()
                      if name.startswith("comparisons/"))
    failed = {item["submissionId"]: FAILURES.get(item["submissionState"], FAILURES["CANNOT_PARSE"])
              for item in info["failedSubmissions"]}
    valid = len(ids) - len(failed)
    if len(scored) != valid * (valid - 1) // 2:
        raise RuntimeError(f"JPlag report has {len(scored)} comparisons for {valid} submissions")
    stats = {sid: {"tokens": sum(entry["tokenCount"] for entry in files.values())} for sid, files in index.items()}
    version = "{major}.{minor}.{patch}".format(**info["version"])
    return version, scored, failed, stats


def analyze(job, input_dir, artifacts, watch):
    params = merged_params(job, DEFAULT_PARAMS)
    min_tokens = positive_int(params, "minTokens")
    if not isinstance(params["metric"], str) or params["metric"] not in METRICS:
        raise InvalidInput()
    sources = sources_of(job, input_dir)
    ids = list(sources)
    report = artifacts / "report"
    size = sum(path.stat().st_size for path in sources.values())
    with tempfile.TemporaryDirectory(prefix="jplag-") as folder:
        _copy_with_room(sources, Path(folder))
        process = subprocess.run(_command(folder, report, min_tokens, size), stdin=subprocess.DEVNULL,
                                 capture_output=True, text=True, errors="replace")
    watch.lap("jplag")
    log = process.stdout + process.stderr
    too_few = NOT_ENOUGH.search(log)
    if process.returncode == 0:
        version, scored, failed, stats = _read_report(report.with_suffix(".jplag"), ids, METRICS[params["metric"]])
    elif too_few:
        version, scored, stats = VERSION, {}, {}
        failed = _failures_from_log(log, ids, int(too_few.group(1)))
    else:
        raise RuntimeError(f"JPlag exited with code {process.returncode}: {log[-500:]}")
    result = assemble(job, f"jplag-{version}", params, scored, failed, stats)
    watch.lap("report")
    return result
