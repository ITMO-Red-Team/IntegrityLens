"""Два набора на Java с разметкой «плагиат / не плагиат».

**IR-Plag** (Karnalim et al., 2019): 7 учебных задач, на каждую — исходное
решение, 15 независимых и 40–54 списанных с него на шести уровнях маскировки
по Faidhi & Robinson (L1 — только комментарии и оформление, … L6 — изменена
логика управления). Протокол — как у авторов: исходное решение сравнивается с
каждым остальным, списанные — положительный класс, независимые —
отрицательный. 355 + 105 = 460 пар.

**ConPlag** (Slobodkin & Sadovnikov, 2022): 911 размеченных пар настоящих
решений с соревнований Codeforces, 21 задача, 251 плагиат. Две версии: `1` —
как сдано, `2` — с вручную вычищенным шаблонным кодом. Авторы дают разбиение
на train/test, его и используем для выбора порога.

Решения в обоих наборах раскладываются так, как этого ждёт JPlag: одна
поддиректория — одно решение, имя поддиректории — имя решения.
"""

from __future__ import annotations

import csv
import hashlib
import shutil
from collections import defaultdict
from dataclasses import dataclass
from pathlib import Path

from research_one.paths import DATA

IRPLAG = DATA / "irplag" / "IR-Plag-Dataset"
CONPLAG = DATA / "conplag" / "versions"


@dataclass(frozen=True)
class LabeledPair:
    group: str          # задача: case-01 / problem-07 — пары сравниваются только внутри
    a: str
    b: str
    label: int          # 1 — плагиат
    level: str          # IR-Plag: L1..L6 или non; ConPlag: plag / non
    split: str = ""     # ConPlag: train / test


@dataclass
class Dataset:
    name: str
    files: dict[str, dict[str, Path]]   # задача → {имя решения: файл}
    pairs: list[LabeledPair]

    def lay_out(self, root: Path) -> dict[str, Path]:
        """Раскладка для JPlag: root/<задача>/<решение>/<файл>. Копии, не ссылки."""
        dirs = {}
        for group, files in self.files.items():
            group_dir = root / group
            if group_dir.exists():
                shutil.rmtree(group_dir)
            for name, source in files.items():
                target = group_dir / name / source.name
                target.parent.mkdir(parents=True)
                shutil.copyfile(source, target)
            dirs[group] = group_dir
        return dirs


def irplag() -> Dataset:
    files: dict[str, dict[str, Path]] = {}
    pairs: list[LabeledPair] = []
    for case in sorted(IRPLAG.glob("case-*")):
        group = case.name
        (original,) = case.glob("original/*.java")
        members = {"orig": original}
        for path in sorted(case.glob("non-plagiarized/*/*.java")):
            name = f"non-{path.parent.name}"
            members[name] = path
            pairs.append(LabeledPair(group, "orig", name, 0, "non"))
        for path in sorted(case.glob("plagiarized/L*/*/*.java")):
            level = path.parent.parent.name
            name = f"{level}-{path.parent.name}"
            members[name] = path
            pairs.append(LabeledPair(group, "orig", name, 1, level))
        files[group] = members
    return Dataset("irplag", files, pairs)


def conplag(version: int) -> Dataset:
    """Пары ConPlag. Решение называется `<id>-<sha8>`.

    Одно и то же решение лежит копией в директории каждой своей пары, и у
    10 (версия 1) и 13 (версия 2) решений копии расходятся. Поэтому имя
    решения включает хеш содержимого: каждая пара сравнивается ровно по тем
    файлам, что лежат в её директории.
    """
    root = CONPLAG / f"version_{version}"
    split = {}
    for name in ("train", "test"):
        for line in (CONPLAG / f"{name}_pairs.csv").read_text().split():
            split[line.strip()] = name

    files: dict[str, dict[str, Path]] = defaultdict(dict)
    pairs: list[LabeledPair] = []
    with (CONPLAG / "labels.csv").open() as handle:
        for row in csv.DictReader(handle):
            pair_dir = root / f"{row['sub1']}_{row['sub2']}"
            group = f"problem-{int(row['problem']):02d}"
            names = []
            for sub in (row["sub1"], row["sub2"]):
                path = pair_dir / f"{sub}.java"
                digest = hashlib.sha256(path.read_bytes()).hexdigest()[:8]
                name = f"{sub}-{digest}"
                files[group].setdefault(name, path)
                names.append(name)
            label = int(row["verdict"])
            pairs.append(
                LabeledPair(
                    group, names[0], names[1], label,
                    "plag" if label else "non",
                    split[f"{row['sub1']}_{row['sub2']}"],
                )
            )
    return Dataset(f"conplag-v{version}", dict(sorted(files.items())), pairs)
