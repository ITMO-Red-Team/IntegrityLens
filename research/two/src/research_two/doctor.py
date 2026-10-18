"""Проверка окружения эксперимента.

    uv run doctor          — проверить
    uv run doctor --build  — дособрать отсутствующие образы и проверить

Проверяется не «инструмент запустился», а «инструмент выдал ожидаемые числа
на входе, ответ для которого известен заранее»: JPlag на образе без JDK
запускается и молча не разбирает ни одного файла, а Semgrep пропускает
файл, который не смог разобрать, не падая при этом.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import shutil
import sys
import tempfile
import urllib.request
from dataclasses import dataclass
from pathlib import Path

from structsim import compare, semgrep
from structsim.labels import to_program

from research_two import config, containers, paths, tools

OK, FAIL, SKIP = "OK", "FAIL", "SKIP"


@dataclass
class Check:
    name: str
    status: str
    detail: str


def check_dirs() -> Check:
    paths.ensure_dirs()
    return Check("директории", OK, f"results → {paths.RESULTS_DIR}")


def check_docker() -> Check:
    try:
        return Check("Docker", OK, f"демон {containers.daemon_version()}")
    except containers.DockerError as exc:
        return Check("Docker", FAIL, str(exc).splitlines()[0])


def check_images(build: bool) -> list[Check]:
    wanted = {"jplag": config.JPLAG_IMAGE, "semgrep": config.SEMGREP_IMAGE}
    missing = [svc for svc, image in wanted.items() if not containers.image_exists(image)]
    if missing and build:
        print(f"собираю: {', '.join(missing)} …", file=sys.stderr)
        containers.build_images(missing, paths.COMPOSE_FILE)
    return [
        Check(f"образ {svc}", OK, image)
        if containers.image_exists(image)
        else Check(f"образ {svc}", FAIL, f"{image} нет; uv run doctor --build")
        for svc, image in wanted.items()
    ]


def check_jplag() -> Check:
    """JPlag разобрал фикстуры и выдал 1.0 на переименовании и 0.0 на чужом коде."""
    name = "JPlag считает пары"
    if not containers.image_exists(config.JPLAG_IMAGE):
        return Check(name, SKIP, "нет образа")
    out = paths.WORK_DIR / "doctor" / "jplag"
    shutil.rmtree(out, ignore_errors=True)
    try:
        result = tools.run_jplag(paths.JPLAG_FIXTURES, out, config.JPLAG_COMMON_ARGS, 600)
    except (containers.DockerError, FileNotFoundError) as exc:
        return Check(name, FAIL, str(exc).splitlines()[0])
    scores = {
        frozenset((r["submissionName1"], r["submissionName2"])): float(r["averageSimilarity"])
        for r in result.rows
    }
    expected = {
        frozenset(("alice", "bob")): 1.0,
        frozenset(("alice", "carol")): 0.0,
        frozenset(("bob", "carol")): 0.0,
    }
    if result.failed or scores.keys() != expected.keys():
        return Check(name, FAIL, f"пары {sorted(map(sorted, scores))}, не разобраны {result.failed}")
    wrong = {"↔".join(sorted(p)): scores[p] for p, want in expected.items() if abs(scores[p] - want) > 1e-9}
    if wrong:
        return Check(name, FAIL, f"неожиданные оценки {wrong}")
    return Check(name, OK, "alice↔bob = 1.0, с carol = 0.0")


def check_structsim() -> list[Check]:
    """Шаг 1 циклом совпадает с `dump_ast`, и Java ↔ Python дают COMBINED 1.0.

    Второе — заявленное свойство structsim (восемь языков из девяти сходятся
    буква в букву), и Java среди них. Если на этом окружении оно не держится,
    числа на ConPlag считать бессмысленно.
    """
    if not containers.image_exists(config.SEMGREP_IMAGE):
        return [Check("structsim", SKIP, "нет образа Semgrep")]
    langs = paths.STRUCTSIM_TEST_DATA / "langs"
    checks: list[Check] = []
    with tempfile.TemporaryDirectory(dir=paths.WORK_DIR) as tmp:
        out = Path(tmp)
        try:
            status = tools.dump_trees(langs, ["s.java"], out)
            reference = semgrep.dump_ast(langs / "s.java")
        except (containers.DockerError, semgrep.SemgrepError) as exc:
            return [Check("structsim: шаг 1", FAIL, str(exc).splitlines()[0])]
        if not status[0].ok:
            return [Check("structsim: шаг 1", FAIL, status[0].error)]
        looped = json.loads((out / "s.json").read_text(encoding="utf-8"))
        same = looped == reference
        checks.append(
            Check(
                "structsim: шаг 1 циклом",
                OK if same else FAIL,
                f"совпадает с structsim.semgrep.dump_ast, {status[0].ms} мс на файл"
                if same
                else "выгрузка цикла отличается от dump_ast",
            )
        )
        java = to_program(looped, language="java", source="s.java")
        py_tree = json.loads((langs / "s.py.json").read_text(encoding="utf-8"))
        python = to_program(py_tree, language="python", source="s.py")
        score = compare.combined(java, python)
        checks.append(
            Check(
                "structsim: Java ↔ Python",
                OK if abs(score - 1.0) < 1e-9 else FAIL,
                f"COMBINED {score:.3f} на одном алгоритме, {len(java.labels)} меток",
            )
        )
    return checks


def check_dataset() -> Check:
    name = "ConPlag"
    if paths.CONPLAG_ZIP.is_file():
        digest = hashlib.md5(paths.CONPLAG_ZIP.read_bytes()).hexdigest()
        if digest == config.CONPLAG_MD5:
            return Check(name, OK, f"скачан, MD5 {digest}")
        return Check(name, FAIL, f"MD5 {digest}, ожидался {config.CONPLAG_MD5}")
    try:
        request = urllib.request.Request(config.CONPLAG_URL, method="HEAD")
        with urllib.request.urlopen(request, timeout=30) as response:
            return Check(name, OK, f"не скачан; источник отвечает HTTP {response.status}")
    except OSError as exc:
        return Check(name, FAIL, f"источник недоступен: {exc}")


def main() -> int:
    for stream in (sys.stdout, sys.stderr):
        stream.reconfigure(encoding="utf-8", errors="replace")
    parser = argparse.ArgumentParser(description="Проверка окружения эксперимента 2")
    parser.add_argument("--build", action="store_true", help="дособрать образы")
    args = parser.parse_args()

    checks = [check_dirs(), check_docker()]
    if checks[-1].status == OK:
        checks += check_images(args.build)
        checks.append(check_jplag())
        checks += check_structsim()
    checks.append(check_dataset())

    width = max(len(c.name) for c in checks)
    print()
    for c in checks:
        print(f"  [{c.status:^4}] {c.name:<{width}}  {c.detail}")
    print()
    failed = [c for c in checks if c.status == FAIL]
    if failed:
        print(f"Не готово: не прошли {len(failed)} из {len(checks)} проверок.")
        return 1
    print("Окружение готово.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
