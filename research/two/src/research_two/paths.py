"""Пути эксперимента.

Всё считается от этого файла, чтобы стадии работали из любой рабочей
директории: `research/two/src/research_two/paths.py` → вверх на три уровня.
"""

from pathlib import Path

PKG_DIR = Path(__file__).resolve().parent
EXPERIMENT_DIR = PKG_DIR.parents[1]          # research/two
REPO_DIR = EXPERIMENT_DIR.parents[1]         # корень репозитория

# Инструменты общие и эксперименту не принадлежат.
TOOLS_DIR = REPO_DIR / "tools"
COMPOSE_FILE = TOOLS_DIR / "docker-compose.yml"
STRUCTSIM_TEST_DATA = TOOLS_DIR / "structsim" / "tests" / "data"

# Фикстуры эксперимента: вход, ответ для которого известен заранее.
FIXTURES_DIR = EXPERIMENT_DIR / "fixtures"
JPLAG_FIXTURES = FIXTURES_DIR / "jplag"

# Сырые данные: качаются стадией s00, в git не попадают.
DATA_DIR = EXPERIMENT_DIR / "data"
CONPLAG_ZIP = DATA_DIR / "conplag.zip"
CONPLAG_DIR = DATA_DIR / "conplag"           # распакованный versions/

# Промежуточные выгрузки: большие и воспроизводимые, вне git.
WORK_DIR = EXPERIMENT_DIR / "work"
# Наборы по задачам в раскладке JPlag: <версия>/<задача>/<решение>/<решение>.java
SETS_DIR = WORK_DIR / "sets"
JPLAG_WORK = WORK_DIR / "jplag"              # <версия>/<прогон>/<задача>/results.csv
TREES_DIR = WORK_DIR / "trees"               # <версия>/<задача>/<решение>.json
STRUCTSIM_WORK = WORK_DIR / "structsim"      # <версия>/<задача>.csv — все пары

# Итоги — в git.
RESULTS_DIR = EXPERIMENT_DIR / "results"
SCORES_DIR = RESULTS_DIR / "scores"
TABLES_DIR = RESULTS_DIR / "tables"
FIGURES_DIR = RESULTS_DIR / "figures"
REPORT = RESULTS_DIR / "REPORT.md"

PAIRS_CSV = SCORES_DIR / "pairs.csv"
JPLAG_CSV = SCORES_DIR / "jplag.csv"
STRUCTSIM_CSV = SCORES_DIR / "structsim.csv"


def ensure_dirs() -> None:
    """Создаёт директории, которых нет в git."""
    for d in (DATA_DIR, WORK_DIR, SCORES_DIR, TABLES_DIR, FIGURES_DIR):
        d.mkdir(parents=True, exist_ok=True)
