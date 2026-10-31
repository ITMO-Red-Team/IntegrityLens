"""Замеры core. Каждый эксперимент дописывает строки в CSV.

    python -m worker.bench.run <эксперимент> --out results.csv [--conplag DIR] [--repeats N]

Запускать в образе метода с лимитом ядер: `docker run --cpus 2 ...`, команды и разбор
итогов — в worker/bench/README.md.
"""
import argparse
import csv
import os
from pathlib import Path

from worker.bench.corpus import conplag, synthetic
from worker.bench.measure import repeated

STRUCTSIM_REFERENCE = {"STRUCTSIM_NATIVE": "off"}
JPLAG_AOT = "-XX:AOTCache=/opt/jplag/aot/jplag.aot"
# Эти замеры про сами методы: синтетика собрана из шаблонных функций и была бы стёрта почти вся.
WHOLE = {"ignoreTemplates": False}


def _method():
    return "jplag" if os.environ.get("JPLAG_JAR") else "structsim"


def _sources(args, count, lines=None):
    if lines is None:
        if not args.conplag:
            raise SystemExit("нужен --conplag: каталог versions/version_1 набора ConPlag")
        return conplag(args.conplag, count)
    return synthetic(count, lines)


def before_after(args, write):
    """Что дала каждая оптимизация: один вход, разные настройки."""
    sources = _sources(args, args.count)
    if _method() == "structsim":
        configurations = [("python, один поток", {**STRUCTSIM_REFERENCE, "CORE_THREADS": "1"}),
                          ("python, разбор параллельно", STRUCTSIM_REFERENCE),
                          ("rust, один поток", {"CORE_THREADS": "1"}),
                          ("rust, разбор параллельно", {})]
    else:
        # На этом входе C1 включается сам, поэтому он возвращается к обоим компиляторам ключом.
        tiered = "-XX:TieredStopAtLevel=4"
        heap = "-XX:+UseSerialGC"
        configurations = [("как есть, с проверкой версии на GitHub", {"JPLAG_JVM_OPTS": f"-Dhttps.proxyHost= {tiered}"}),
                          ("без проверки версии", {"JPLAG_JVM_OPTS": tiered}),
                          ("+ SerialGC", {"JPLAG_JVM_OPTS": f"{tiered} {heap}"}),
                          ("+ AOT-кэш", {"JPLAG_JVM_OPTS": f"{tiered} {JPLAG_AOT} {heap}"}),
                          ("+ только C1", {})]
    for name, env in configurations:
        write(_method(), name, repeated(sources, _method(), args.repeats, env, WHOLE), files=len(sources))


def by_count(args, write):
    """Рост от числа работ."""
    for count in (5, 10, 15, 20, 30, 40, 50):
        write(_method(), "работы", repeated(_sources(args, count), _method(), args.repeats, params=WHOLE), files=count)


def by_size(args, write):
    """Рост от длины программы: на настоящих решениях таких длин нет, поэтому синтетика.

    25 600 строк — около 1 МиБ, это предел API на один файл. У JPlag на таких длинах время
    уходит за минуты, и он останавливается раньше.
    """
    longest = {"jplag": 6400, "structsim": 25600}[_method()]
    for lines in (100, 200, 400, 800, 1600, 3200, 6400, 12800, 25600):
        if lines <= longest:
            sources = synthetic(args.count, lines)
            write(_method(), "длина", repeated(sources, _method(), args.repeats, params=WHOLE), files=args.count, lines=lines)


def single(args, write):
    """Один замер: чтобы сравнивать лимиты ядер, запускается с разным --cpus."""
    write(_method(), "ядра", repeated(_sources(args, args.count), _method(), args.repeats, params=WHOLE), files=args.count)


def templates(args, write):
    """Цена стирания шаблонов: на настоящих решениях и на длинных файлах."""
    sources = _sources(args, args.count)
    write(_method(), "без стирания", repeated(sources, _method(), args.repeats, params=WHOLE), files=len(sources))
    write(_method(), "со стиранием", repeated(sources, _method(), args.repeats), files=len(sources))
    for lines in (800, 6400):
        sources = synthetic(10, lines)
        write(_method(), "со стиранием, длина", repeated(sources, _method(), args.repeats), files=len(sources), lines=lines)


EXPERIMENTS = {"before_after": before_after, "by_count": by_count, "by_size": by_size, "single": single,
               "templates": templates}
FIELDS = ["method", "setup", "files", "lines", "cpus", "wall_s", "cpu_s", "rss_mb", "pairs", "parseMs", "compareMs",
          "templatesMs", "jplagMs", "reportMs", "writeMs"]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("experiment", choices=EXPERIMENTS)
    parser.add_argument("--out", required=True)
    parser.add_argument("--conplag")
    parser.add_argument("--count", type=int, default=50)
    parser.add_argument("--repeats", type=int, default=5)
    parser.add_argument("--cpus", default=os.environ.get("BENCH_CPUS", ""))
    args = parser.parse_args()

    out = Path(args.out)
    is_new = not out.exists()
    with out.open("a", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, FIELDS, extrasaction="ignore")
        if is_new:
            writer.writeheader()

        def write(method, setup, measured, files, lines=""):
            row = {"method": method, "setup": setup, "files": files, "lines": lines, "cpus": args.cpus, **measured}
            writer.writerow(row)
            handle.flush()
            print(row, flush=True)

        EXPERIMENTS[args.experiment](args, write)


if __name__ == "__main__":
    main()
