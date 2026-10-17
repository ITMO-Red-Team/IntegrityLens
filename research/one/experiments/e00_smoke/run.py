"""e00 — проверка инструментов на входе с заранее известным ответом.

    uv run python experiments/e00_smoke/run.py

«Запустился» и «сработал» — разные вещи (tools/README.md, правило 3). Оба
инструмента здесь запускаются не так, как задумано в tools/ (не в Docker), и
до основных экспериментов надо убедиться, что локальная замена считает то же.
Третий инструмент, Dolos, в tools/ нет; для него проверяется только, что
локальная установка той версии и на известном входе даёт ожидаемое.
"""

from __future__ import annotations

import json
import os
import subprocess
import sys
from pathlib import Path

from structsim.labels import to_program

from research_one import dolos, jplag, ssim
from research_one.paths import TOOLS, results_of, work_of

HERE = Path(__file__).parent
NAME = "e00_smoke"
FIXTURES = TOOLS / "structsim" / "tests" / "data"
RELAX_SEQUENCE = (
    "FUNC P4 IF ID IDX ID + ID < ID IDX ID ; ID IDX ID = ID IDX ID + ID END END"
)


def check(name: str, ok: bool, detail: str, checks: list[dict]) -> None:
    checks.append({"check": name, "ok": bool(ok), "detail": detail})
    print(f"[{'OK' if ok else 'FAIL'}] {name}: {detail}")


def main() -> int:
    checks: list[dict] = []
    out = results_of(NAME)

    # --- structsim: локальный semgrep-core против выгрузок из контейнера ----
    version = ssim.check_version()
    check("semgrep-core версия", True, version, checks)

    fixtures = sorted(FIXTURES.rglob("*.json"))
    same = []
    for tree_path in fixtures:
        source = tree_path.with_suffix("")          # relax.cpp.json → relax.cpp
        if not source.exists():
            continue
        docker_labels = to_program(json.loads(tree_path.read_text())).labels
        local_labels = to_program(ssim.dump_ast(source)).labels
        same.append((source.relative_to(FIXTURES).as_posix(), docker_labels == local_labels, len(local_labels)))
    check(
        "метки: локально == Docker, все выгрузки tests/data",
        all(ok for _, ok, _ in same) and len(same) > 0,
        ", ".join(f"{n}:{'=' if ok else '≠'}{k}" for n, ok, k in same),
        checks,
    )

    relax = " ".join(to_program(ssim.dump_ast(FIXTURES / "relax.cpp")).labels)
    check("relax.cpp даёт последовательность из docs/step2.md", relax == RELAX_SEQUENCE, relax, checks)

    # Быстрые тесты самого structsim (без Docker), из окружения исследования.
    env = {**os.environ, "PYTHONDONTWRITEBYTECODE": "1"}
    tests = subprocess.run(
        [sys.executable, "-m", "pytest", "-q", "-p", "no:cacheprovider", str(TOOLS / "structsim" / "tests")],
        capture_output=True, text=True, env=env,
    )
    summary = tests.stdout.strip().splitlines()[-1] if tests.stdout.strip() else tests.stderr[-300:]
    check("pytest tools/structsim (без docker)", tests.returncode == 0, summary, checks)

    # --- все три инструмента на alice / bob / carol -------------------------
    inputs = HERE / "input"
    files = {d.name: next(d.glob("*.java")) for d in sorted(inputs.iterdir()) if d.is_dir()}

    programs, skipped = ssim.to_programs(files)
    scores = ssim.explain_pairs(programs, [("alice", "bob"), ("alice", "carol"), ("bob", "carol")])
    ab = scores[frozenset(("alice", "bob"))]["combined"]
    ac = scores[frozenset(("alice", "carol"))]["combined"]
    check("structsim: разобраны все 3", not skipped and len(programs) == 3, f"пропущено {skipped}", checks)
    check("structsim: alice↔bob = 1.0 (переименование)", abs(ab - 1.0) < 1e-9, f"COMBINED {ab:.3f}", checks)
    check("structsim: alice↔carol заметно ниже", ac < 0.7, f"COMBINED {ac:.3f}", checks)

    run = jplag.run(inputs, work_of(NAME) / "jplag")
    by_pair = run.by_pair()
    jab = by_pair[frozenset(("alice", "bob"))]
    jac = by_pair[frozenset(("alice", "carol"))]
    check("JPlag: разобраны все 3", not run.failed and len(run.pairs) == 3, f"failed={run.failed}, пар {len(run.pairs)}", checks)
    check("JPlag: alice↔bob = 1.0 (переименование)", jab.average == 1.0, f"avg {jab.average:.3f}, max {jab.maximum:.3f}", checks)
    check("JPlag: alice↔carol заметно ниже", jac.average < 0.5, f"avg {jac.average:.3f}, max {jac.maximum:.3f}", checks)

    check(f"Dolos версия {dolos.DOLOS_VERSION}", True, dolos.version(), checks)
    drun = dolos.run(inputs, work_of(NAME) / "dolos")
    d_pairs = drun.by_pair()
    dab = d_pairs[frozenset(("alice", "bob"))]
    dac = d_pairs[frozenset(("alice", "carol"))]
    check(
        "Dolos: разобраны все 3",
        not drun.failed and not drun.empty and len(drun.pairs) == 3,
        f"с ошибкой разбора {drun.failed}, без отпечатков {drun.empty}, пар {len(drun.pairs)}",
        checks,
    )
    check("Dolos: alice↔bob = 1.0 (переименование)", dab.similarity == 1.0,
          f"similarity {dab.similarity:.3f}, max {dab.maximum:.3f}", checks)
    check("Dolos: alice↔carol заметно ниже", dac.similarity < 0.5,
          f"similarity {dac.similarity:.3f}, max {dac.maximum:.3f}", checks)

    pairs_table = [
        {
            "pair": f"{a}–{b}",
            "jplag_avg": by_pair[frozenset((a, b))].average,
            "jplag_max": by_pair[frozenset((a, b))].maximum,
            "structsim_combined": scores[frozenset((a, b))]["combined"],
            "dolos_similarity": d_pairs[frozenset((a, b))].similarity,
            "dolos_max": d_pairs[frozenset((a, b))].maximum,
        }
        for a, b in [("alice", "bob"), ("alice", "carol"), ("bob", "carol")]
    ]
    (out / "checks.json").write_text(
        json.dumps({"checks": checks, "pairs": pairs_table}, ensure_ascii=False, indent=2) + "\n"
    )
    failed = [c for c in checks if not c["ok"]]
    print(f"\n{len(checks) - len(failed)}/{len(checks)} проверок прошло → {out / 'checks.json'}")
    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
