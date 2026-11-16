"""Что входит в датасет: задания, размеры, ручные решения по разметке.

Отбор детерминирован: зависит только от содержимого исходного набора и от
значений в этом файле. Меняешь что-то здесь — пересобирай (`build.py`).
"""

from __future__ import annotations

from dataclasses import dataclass

# Границы размера работы в строках (требование 6).
MIN_LINES, MAX_LINES = 30, 300

# Чем написаны работы в `ai/`: каждая — отдельным запуском с чистым контекстом.
AI_MODEL = "claude-opus-5-5"


@dataclass(frozen=True)
class Assignment:
    id: str
    title: str
    split: str            # dev — подбор порогов и весов, test — итоговая оценка
    problem: int          # номер задачи в ConPlag
    size: int             # работ в задании
    max_positive: int     # потолок списанных работ; кейсы берутся целиком
    seed: int
    # Задание-источник «прошлого потока»: у стольких кейсов-пар одна работа
    # уходит в него, и в нём всего столько работ. 0 — источника нет.
    past_cases: int = 0
    past_size: int = 0


@dataclass(frozen=True)
class Review:
    """Ручная проверка пары, у которой в ConPlag нет метки."""

    problem: int
    a: str
    b: str
    plagiarism: bool
    note: str


ASSIGNMENTS = (
    Assignment(id="a01", title="Parsa's Humongous Tree", split="dev", problem=1, size=50, max_positive=15, seed=2801),
    Assignment(id="a02", title="Menorah", split="test", problem=4, size=50, max_positive=14, seed=2804),
    Assignment(id="a03", title="Air Conditioners", split="dev", problem=8, size=50, max_positive=12, seed=2808),
    Assignment(id="a04", title="Phoenix and Towers", split="test", problem=9, size=50, max_positive=14, seed=2809),
    Assignment(id="a05", title="Interesting Story", split="dev", problem=15, size=50, max_positive=14, seed=2815, past_cases=3, past_size=25),
    Assignment(id="a06", title="Minimum Grid Path", split="dev", problem=18, size=50, max_positive=13, seed=2818),
    Assignment(id="a07", title="Armchairs", split="test", problem=19, size=50, max_positive=14, seed=2819, past_cases=3, past_size=25),
    Assignment(id="a08", title="Monsters And Spells", split="test", problem=21, size=35, max_positive=8, seed=2821),
)

# Пары без метки ConPlag, просмотренные вручную. Обязательны все, которые
# JPlag 6.3.0 оценил >= 0.8 на сырой или очищенной от шаблонов версии
# (`jplag.py review`). Работы, совпадающие с точностью до пробелов, сюда не
# входят: они объединяются в кейс автоматически.
REVIEWS = (
    Review(
        1, "1a31f5cb", "bf85ab7b", False,
        "сходство даёт общий шаблон быстрого ввода; решения устроены по-разному",
    ),
    Review(
        1, "1a31f5cb", "51019113", False,
        "одна каноничная ДП по дереву; разные структуры данных, индексация, имена",
    ),
    Review(
        4, "76ad805a", "90dc2b20", True,
        "та же структура и те же редкие детали (заглушка 1e7, пара s1/s2, вид условия); "
        "переименованы счётчики, заменён способ ввода",
    ),
    Review(
        9, "829d2024", "8f31b279", False,
        "каноничная жадность с очередью; свой класс пары, элемент меняется на месте",
    ),
    Review(
        9, "3b5cec19", "829d2024", False,
        "то же: 829d2024 написана иначе, чем размеченная пара 3b5cec19–8f31b279",
    ),
    Review(
        19, "d9f6c257", "f6ca6fc8", True,
        "неоднозначно: одна и та же необычная запись ДП с отдельной диагональю, "
        "имена разные; обе короче 30 строк и в датасет не входят, "
        "отнесены к списанным, чтобы не считаться честными",
    ),
    Review(
        21, "624529e3", "80cc284c", True,
        "один алгоритм строка в строку с теми же именами (length, curr, ans); "
        "отличия в оформлении и записи присваиваний",
    ),
)
