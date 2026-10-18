"""s00: ConPlag — скачать, сверить, распаковать, собрать разметку и наборы.

Что получается:

- `data/conplag.zip` и `data/conplag/` — архив и его `versions/`;
- `results/scores/pairs.csv` — 911 размеченных пар: `pair, sub1, sub2,
  problem, label, split`;
- `results/tables/dataset.csv` — размеры по задачам;
- `work/sets/<версия>/<задача>/<решение>/<решение>.java` — наборы по задачам
  в раскладке JPlag. Оба инструмента запускаются на наборе задачи: JPlag так
  дешевле, а `contrast-z` иначе не определён (фон считается по набору).

Особенность ConPlag: решения лежат по парам (`<sub1>_<sub2>/`), и одно решение,
входящее в несколько пар, местами записано по-разному. Разница — пустые строки
и комментарии (проверено: не больше 8 строк, ни одной строки кода), а их не
видит ни JPlag, ни Semgrep. В набор берётся первый вариант по имени пары;
сколько таких решений — пишется в лог и в `dataset.csv`.
"""

from __future__ import annotations

import csv
import hashlib
import shutil
import urllib.request
import zipfile
from collections import defaultdict
from pathlib import Path, PurePosixPath

from research_two import config, paths


def _download() -> None:
    if paths.CONPLAG_ZIP.is_file():
        return
    print(f"качаю {config.CONPLAG_URL}")
    tmp = paths.CONPLAG_ZIP.with_suffix(".part")
    with urllib.request.urlopen(config.CONPLAG_URL, timeout=120) as response, tmp.open("wb") as out:
        shutil.copyfileobj(response, out)
    tmp.replace(paths.CONPLAG_ZIP)


def _verify_md5() -> None:
    digest = hashlib.md5(paths.CONPLAG_ZIP.read_bytes()).hexdigest()
    if digest != config.CONPLAG_MD5:
        raise SystemExit(f"MD5 архива {digest}, ожидался {config.CONPLAG_MD5}: не та версия")
    print(f"MD5 сходится: {digest}")


def _extract() -> Path:
    """Распаковывает только `versions/`, с проверкой путей внутри архива."""
    root = paths.CONPLAG_DIR / "versions"
    if (root / "labels.csv").is_file():
        return root
    shutil.rmtree(paths.CONPLAG_DIR, ignore_errors=True)
    with zipfile.ZipFile(paths.CONPLAG_ZIP) as archive:
        for member in archive.infolist():
            name = PurePosixPath(member.filename)
            if name.parts[:1] != ("versions",) or member.is_dir():
                continue
            if name.is_absolute() or ".." in name.parts:
                raise SystemExit(f"подозрительный путь в архиве: {member.filename}")
            target = paths.CONPLAG_DIR.joinpath(*name.parts)
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(archive.read(member))
    return root


def _read_pairs(root: Path) -> list[dict[str, str]]:
    with (root / "labels.csv").open(encoding="utf-8", newline="") as handle:
        labels = list(csv.DictReader(handle))
    train = {line.strip() for line in (root / "train_pairs.csv").read_text().splitlines() if line.strip()}
    test = {line.strip() for line in (root / "test_pairs.csv").read_text().splitlines() if line.strip()}

    pairs = []
    for row in labels:
        pair = f"{row['sub1']}_{row['sub2']}"
        split = "train" if pair in train else "test" if pair in test else ""
        pairs.append(
            {
                "pair": pair,
                "sub1": row["sub1"],
                "sub2": row["sub2"],
                "problem": int(row["problem"]),
                "label": int(row["verdict"]),
                "split": split,
            }
        )

    checks = {
        "пар": (len(pairs), config.EXPECTED_PAIRS),
        "плагиат": (sum(p["label"] for p in pairs), config.EXPECTED_POSITIVE),
        "не плагиат": (sum(1 - p["label"] for p in pairs), config.EXPECTED_NEGATIVE),
        "задач": (len({p["problem"] for p in pairs}), config.EXPECTED_PROBLEMS),
        "решений": (len({s for p in pairs for s in (p["sub1"], p["sub2"])}), config.EXPECTED_SUBMISSIONS),
        "train": (sum(p["split"] == "train" for p in pairs), config.EXPECTED_TRAIN),
        "test": (sum(p["split"] == "test" for p in pairs), config.EXPECTED_TEST),
    }
    bad = {k: v for k, v in checks.items() if v[0] != v[1]}
    if bad:
        raise SystemExit(f"разметка не сходится (получено, ожидалось): {bad}")
    print("разметка сходится: " + ", ".join(f"{k} {v[0]}" for k, v in checks.items()))
    return pairs


def _build_sets(root: Path, pairs: list[dict]) -> dict[str, int]:
    """Наборы по задачам; возвращает число решений с разными вариантами."""
    problem_of = {s: p["problem"] for p in pairs for s in (p["sub1"], p["sub2"])}
    conflicts: dict[str, int] = {}
    for version, folder in config.DATASET_VERSIONS.items():
        variants: dict[str, dict[str, Path]] = defaultdict(dict)
        for pair in sorted(p["pair"] for p in pairs):
            for sub in pair.split("_"):
                source = root / folder / pair / f"{sub}.java"
                if not source.is_file():
                    raise SystemExit(f"нет файла {source}")
                digest = hashlib.md5(source.read_bytes()).hexdigest()
                variants[sub].setdefault(digest, source)

        out = paths.SETS_DIR / version
        shutil.rmtree(out, ignore_errors=True)
        for sub, by_digest in variants.items():
            first = next(iter(by_digest.values()))
            target = out / f"{problem_of[sub]:02d}" / sub / f"{sub}.java"
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(first, target)
        conflicts[version] = sum(len(v) > 1 for v in variants.values())
        print(f"{version}: {len(variants)} решений по {len(set(problem_of.values()))} задачам, "
              f"с разными вариантами записи {conflicts[version]}")
    return conflicts


def _write(pairs: list[dict], conflicts: dict[str, int]) -> None:
    paths.ensure_dirs()
    with paths.PAIRS_CSV.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(pairs[0]))
        writer.writeheader()
        writer.writerows(sorted(pairs, key=lambda p: (p["problem"], p["pair"])))

    by_problem: dict[int, list[dict]] = defaultdict(list)
    for p in pairs:
        by_problem[p["problem"]].append(p)
    with (paths.TABLES_DIR / "dataset.csv").open("w", encoding="utf-8", newline="") as handle:
        writer = csv.writer(handle)
        writer.writerow(["problem", "submissions", "set_pairs", "labeled", "positive", "train", "test"])
        for problem in sorted(by_problem):
            group = by_problem[problem]
            n = len({s for p in group for s in (p["sub1"], p["sub2"])})
            writer.writerow([
                problem, n, n * (n - 1) // 2, len(group), sum(p["label"] for p in group),
                sum(p["split"] == "train" for p in group), sum(p["split"] == "test" for p in group),
            ])
    (paths.TABLES_DIR / "dataset_variants.csv").write_text(
        "version,submissions_with_variants\n"
        + "".join(f"{v},{n}\n" for v, n in conflicts.items()),
        encoding="utf-8",
    )


def run() -> None:
    paths.ensure_dirs()
    _download()
    _verify_md5()
    root = _extract()
    pairs = _read_pairs(root)
    conflicts = _build_sets(root, pairs)
    _write(pairs, conflicts)
    print(f"записано: {paths.PAIRS_CSV.relative_to(paths.EXPERIMENT_DIR)}")
