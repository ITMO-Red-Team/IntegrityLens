"""Что дают шаблонные алгоритмы: AUC методов на размеченных наборах со стиранием и без него.

    python -m worker.bench.templates --conplag DIR --irplag DIR --out results/templates_quality.csv

DIR — каталог `versions` ConPlag и `IR-Plag-Dataset`. Запускать в образе метода, разбор итогов — в
worker/bench/README.md. У ConPlag две версии решений: как сданы и с вычищенными вручную шаблонами.
Хорошее стирание поднимает оценку на первой и не трогает вторую, а на коротких учебных работах
IR-Plag почти ничего не находит. Правило и добавки к набору подбирались по train и IR-Plag, поэтому
итог читается по test; `unseen` — пары test, где обоих решений нет в train.
"""
import argparse
import csv
import hashlib
import json
import os
import random
import tempfile
import uuid
from collections import defaultdict
from pathlib import Path

from worker.core import template_filter
from worker.core.contract import CONTRACT
from worker.core.engine import Stopwatch

SPLITS = ("all", "train", "test", "unseen")
FIELDS = ["method", "score", "dataset", "short_tokens", "short_similarity", "similarity", "split", "pairs", "auc", "ap",
          "stripped_lines", "not_parsed", "auc_gain_low", "auc_gain_high"]
BOOTSTRAP_ROUNDS = 1000
CHOSEN = (template_filter.SHORT_TOKENS, template_filter.SHORT_SIMILARITY, template_filter.SIMILARITY)
# Порог для коротких функций, сколько токенов считать короткой, порог для остальных. None — без стирания.
RULES = (None, CHOSEN, *((0, flat, flat) for flat in (.6, .65, .7, .75, .8, .85, .9)), (60, .9, .7), (80, .85, .7))


def _dataset(root, version):
    """Решение — id и хеш файла: копии одного решения в разных парах иногда расходятся."""
    root = Path(root)
    split = {line.strip(): name for name in ("train", "test")
             for line in (root / f"{name}_pairs.csv").read_text().split()}
    files, pairs = defaultdict(dict), []
    with (root / "labels.csv").open() as handle:
        for row in csv.DictReader(handle):
            directory = root / f"version_{version}" / f"{row['sub1']}_{row['sub2']}"
            names = []
            for sub in (row["sub1"], row["sub2"]):
                path = directory / f"{sub}.java"
                name = f"{sub}-{hashlib.sha256(path.read_bytes()).hexdigest()[:8]}"
                files[int(row["problem"])].setdefault(name, path)
                names.append(name)
            pairs.append({"key": (int(row["problem"]), *sorted(names)), "label": int(row["verdict"]),
                          "split": split[f"{row['sub1']}_{row['sub2']}"]})
    train = {name for pair in pairs if pair["split"] == "train" for name in pair["key"][1:]}
    for pair in pairs:
        pair["unseen"] = pair["split"] == "test" and train.isdisjoint(pair["key"][1:])
    return files, pairs


def _irplag(root):
    """Как в research/one: исходное решение задачи против каждого списанного и независимого."""
    files, pairs = {}, []
    for case in sorted(Path(root).glob("case-*")):
        (original,) = case.glob("original/*.java")
        files[case.name] = {"original": original}
        for pattern, label in (("non-plagiarized/*/*.java", 0), ("plagiarized/L*/*/*.java", 1)):
            for path in sorted(case.glob(pattern)):
                name = "-".join(path.relative_to(case).parts[:-1])
                files[case.name][name] = path
                pairs.append({"key": (case.name, *sorted(("original", name))), "label": label, "split": "all",
                              "unseen": False})
    return files, pairs


def _analyzer():
    if os.environ.get("JPLAG_JAR"):
        from worker.core.jplag import analyze
        return "jplag", analyze, lambda pair: {"max": pair["details"]["max"], "avg": pair["details"]["avg"]}
    from worker.core import structsim
    # От порога меняется только часть файлов, а разбор остальных занимает почти всё время.
    parsed, parse = {}, structsim._parse

    def cached(path):
        key = hashlib.sha256(path.read_bytes()).digest()
        if key not in parsed:
            parsed[key] = parse(path)
        return parsed[key]

    structsim._parse = cached
    return "structsim", structsim.analyze, lambda pair: {"combined": pair["value"]}


def _scores(method, analyze, read, files, index):
    """Оценки всех пар каждой задачи через настоящий метод. Задача — один запуск, как в research/one."""
    scores, shares, not_parsed = defaultdict(dict), [], 0
    for problem, sources in files.items():
        with tempfile.TemporaryDirectory(prefix="bench-") as folder:
            root = Path(folder)
            names = {str(uuid.uuid5(uuid.NAMESPACE_OID, name)): name for name in sources}
            job = {"contract": CONTRACT, "runId": str(uuid.uuid4()), "method": method, "params": {}, "submissions": []}
            for sid, name in names.items():
                source = sources[name].read_bytes()
                if index is not None:
                    source, lines = template_filter.strip(source, index)
                    shares.append(sum(last - first + 1 for first, last in lines) / (source.count(b"\n") + 1))
                path = root / "submissions" / sid / "Main.java"
                path.parent.mkdir(parents=True)
                path.write_bytes(source)
                job["submissions"].append({"id": sid, "language": "java", "files": ["Main.java"]})
            (root / "artifacts").mkdir()
            result = analyze(job, root, root / "artifacts", Stopwatch())
        not_parsed += sum(submission["status"] == "failed" for submission in result["submissions"])
        for pair in result["pairs"]:
            if pair["value"] is not None:
                key = (problem, *sorted((names[pair["a"]], names[pair["b"]])))
                for score, value in read(pair).items():
                    scores[score][key] = value
    return scores, sum(shares) / len(shares) if shares else 0.0, not_parsed


def _auc(labels, values):
    order = sorted(range(len(values)), key=values.__getitem__)
    ranks, start = [0.0] * len(values), 0
    while start < len(order):
        end = start
        while end + 1 < len(order) and values[order[end + 1]] == values[order[start]]:
            end += 1
        for position in range(start, end + 1):
            ranks[order[position]] = (start + end) / 2 + 1
        start = end + 1
    positive = sum(labels)
    return (sum(rank for rank, label in zip(ranks, labels) if label) - positive * (positive + 1) / 2) / (
        positive * (len(labels) - positive))


def _ap(labels, values):
    order = sorted(range(len(values)), key=lambda number: -values[number])
    total, hits, previous, start = 0.0, 0, 0.0, 0
    while start < len(order):
        end = start
        while end + 1 < len(order) and values[order[end + 1]] == values[order[start]]:
            end += 1
        hits += sum(labels[order[position]] for position in range(start, end + 1))
        recall = hits / sum(labels)
        total += (recall - previous) * hits / (end + 1)
        previous, start = recall, end + 1
    return total


def _chosen(pairs, split):
    if split == "all":
        return pairs
    if split == "unseen":
        return [pair for pair in pairs if pair["unseen"]]
    return [pair for pair in pairs if pair["split"] == split]


def _gain_interval(pairs, before, after):
    """95% интервал прироста AUC, парный бутстрэп по парам."""
    alive = [pair for pair in pairs if pair["key"] in before and pair["key"] in after]
    rng = random.Random(7)
    gains = []
    for _ in range(BOOTSTRAP_ROUNDS):
        sample = rng.choices(alive, k=len(alive))
        labels = [pair["label"] for pair in sample]
        if 0 < sum(labels) < len(labels):
            gains.append(_auc(labels, [after[pair["key"]] for pair in sample])
                         - _auc(labels, [before[pair["key"]] for pair in sample]))
    gains.sort()
    return gains[int(len(gains) * .025)], gains[int(len(gains) * .975)]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--conplag", required=True)
    parser.add_argument("--irplag", required=True)
    parser.add_argument("--out", required=True)
    args = parser.parse_args()
    method, analyze, read = _analyzer()
    sources = [path.read_bytes() for path in sorted(template_filter.CODE.glob("*.java"))]
    out = Path(args.out)
    is_new = not out.exists()
    with out.open("a", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, FIELDS)
        if is_new:
            writer.writeheader()
        for dataset, (files, pairs) in (("conplag-v1", _dataset(args.conplag, 1)), ("conplag-v2", _dataset(args.conplag, 2)),
                                        ("irplag", _irplag(args.irplag))):
            baseline = None
            for rule in RULES:
                index = None
                if rule is not None:
                    index = template_filter.TemplateIndex(sources, similarity=rule[2], short_similarity=rule[1],
                                                          short_tokens=rule[0])
                scores, stripped, not_parsed = _scores(method, analyze, read, files, index)
                baseline = baseline or scores
                for score, values in scores.items():
                    for split in SPLITS:
                        chosen = [pair for pair in _chosen(pairs, split) if pair["key"] in values]
                        if not chosen:
                            continue
                        labels = [pair["label"] for pair in chosen]
                        numbers = [values[pair["key"]] for pair in chosen]
                        row = {"method": method, "score": score, "dataset": dataset, "split": split, "pairs": len(chosen),
                               "auc": round(_auc(labels, numbers), 4), "ap": round(_ap(labels, numbers), 4),
                               "stripped_lines": round(stripped, 4), "not_parsed": not_parsed}
                        if rule is not None:
                            row.update(short_tokens=rule[0], short_similarity=rule[1], similarity=rule[2])
                        if rule == CHOSEN and split in ("all", "test", "unseen"):
                            low, high = _gain_interval(chosen, baseline[score], values)
                            row.update(auc_gain_low=round(low, 4), auc_gain_high=round(high, 4))
                        writer.writerow(row)
                        handle.flush()
                        print(json.dumps(row), flush=True)


if __name__ == "__main__":
    main()
