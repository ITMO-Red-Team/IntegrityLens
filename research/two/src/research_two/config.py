"""Прибитые версии, источники данных и параметры прогонов.

Всё, что может незаметно поменяться под ногами, собрано здесь: версии
инструментов, контрольная сумма датасета, ожидаемые размеры разметки,
конфигурации прогонов. Стадии берут значения отсюда, а не пишут их по месту.
"""

from __future__ import annotations

from dataclasses import dataclass

# --- Образы ---------------------------------------------------------------

# Baseline. Образ и документация — tools/jplag/.
JPLAG_VERSION = "6.3.0"
JPLAG_IMAGE = f"integritylens/jplag:{JPLAG_VERSION}"

# Шаг 1 structsim. Берётся из самого инструмента, чтобы эксперимент не мог
# разойтись с ним по версии: формат выгрузки дерева между версиями Semgrep
# меняется молча.
from structsim.semgrep import SEMGREP_IMAGE, SEMGREP_VERSION  # noqa: E402

# --- Датасет --------------------------------------------------------------

# ConPlag: плагиат на Codeforces, Java. Slobodkin & Sadovnikov, FRUCT 33,
# CC BY 4.0. Открытая запись Zenodo (6812028 — с ограниченным доступом).
CONPLAG_URL = "https://zenodo.org/records/7332491/files/conplag.zip?download=1"
CONPLAG_MD5 = "47d49c40d0c5318f7cb8e4819eac05ae"
CONPLAG_SIZE_BYTES = 11_684_348

# Версии датасета: имя в эксперименте → каталог внутри архива.
#   as-is          — решения как есть, со своими заготовками участников;
#   template-free  — общий код участника (быстрый ввод, утилиты) удалён
#                    вручную авторами датасета.
DATASET_VERSIONS: dict[str, str] = {
    "as-is": "version_1",
    "template-free": "version_2",
}

# Ожидаемая разметка. Не совпало — скачалось не то, считать метрики нельзя.
EXPECTED_PAIRS = 911
EXPECTED_POSITIVE = 251
EXPECTED_NEGATIVE = 660
EXPECTED_PROBLEMS = 21
EXPECTED_SUBMISSIONS = 975
# Сплит авторов датасета: порог подбирается на train, F1 меряется на test.
EXPECTED_TRAIN = 230
EXPECTED_TEST = 681

# --- JPlag ----------------------------------------------------------------

# Ключи, общие для всех прогонов. Почему именно они — tools/jplag/usage.md:
#   --csv-export   единственный полный источник оценок (архив режет по -n);
#   --cluster-skip кластеризация в эксперименте не нужна, а время тратит;
#   --overwrite    стадии идемпотентны.
JPLAG_COMMON_ARGS: tuple[str, ...] = ("--csv-export", "--cluster-skip", "--overwrite")

JPLAG_DEFAULT_MIN_TOKENS = 9


@dataclass(frozen=True)
class JPlagRun:
    """Одна конфигурация JPlag. `name` идёт в имена файлов и таблицы."""

    name: str
    extra_args: tuple[str, ...] = ()
    # Кривая по -t: не самостоятельная конфигурация, а точка на ней.
    sweep: bool = False

    @property
    def args(self) -> tuple[str, ...]:
        return (*JPLAG_COMMON_ARGS, *self.extra_args)


# Головные конфигурации. `--base-code` сознательно нет: ключ ждёт одну общую
# заготовку на весь набор, а в ConPlag заготовка у каждого участника своя.
# Ответ на вопрос «что, если убрать заготовку» даёт версия template-free —
# там она убрана вручную, точнее любой автоматики.
JPLAG_RUNS: tuple[JPlagRun, ...] = (
    JPlagRun("default"),
    JPlagRun("normalize", ("--normalize",)),
    JPlagRun("match-merging", ("--match-merging",)),
)

# Кривая чувствительности по минимальной длине совпадения. 9 — это default.
JPLAG_MIN_TOKENS_SWEEP: tuple[int, ...] = (3, 5, 7, 11, 13, 15)
JPLAG_SWEEP_RUNS: tuple[JPlagRun, ...] = tuple(
    JPlagRun(f"t{t:02d}", ("-t", str(t)), sweep=True) for t in JPLAG_MIN_TOKENS_SWEEP
)

ALL_JPLAG_RUNS: tuple[JPlagRun, ...] = JPLAG_RUNS + JPLAG_SWEEP_RUNS

# --- structsim ------------------------------------------------------------

STRUCTSIM_LANGUAGE = "java"

# Сколько процессов считают пары. Шаг 3 — чистый Python, упирается в CPU.
STRUCTSIM_WORKERS = 8
# Сколько контейнеров Semgrep разбирают задачи одновременно.
SEMGREP_PARALLEL = 4
JPLAG_PARALLEL = 4

# --- Оценки, которые сравниваются -----------------------------------------

# Имя оценки в таблицах → (инструмент, столбец). Порядок — порядок в отчёте.
STRUCTSIM_SCORES: tuple[str, ...] = ("combined", "contrast_z", "order", "shingle", "blocks")
JPLAG_SCORES: tuple[str, ...] = ("avg", "max")

# Головные оценки: с ними проверяются гипотезы.
HEAD_STRUCTSIM = "structsim:combined"
HEAD_JPLAG = "jplag-default:avg"

# --- Статистика -----------------------------------------------------------

BOOTSTRAP_ITERATIONS = 10_000
BOOTSTRAP_SEED = 20261018
CONFIDENCE_LEVEL = 0.95
