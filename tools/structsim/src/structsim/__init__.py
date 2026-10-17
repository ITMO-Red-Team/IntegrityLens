"""Структурное сходство программ.

Четыре шага:

1. **общее дерево** — файл разбирается Semgrep в синтаксическое дерево,
   одинаковое по устройству для разных языков — *не реализован*;
2. **очистка** — дерево превращается в последовательность меток, из которой
   пропало всё, что зависит от автора и языка — *не реализован*;
3. **сравнение пары** — три меры и `COMBINED` (`compare.py`);
4. **сравнение с фоном** — `contrast-z` для набора решений одной задачи
   (`contrast.py`).

Что откуда вызывается и какие данные идут между шагами — `docs/pipeline.md`.

Оценка говорит о сходстве **реализации**, а не алгоритма: один алгоритм,
записанный по-разному, может получить низкую оценку, а разные алгоритмы с
похожим кодом — заметную.
"""

from structsim.compare import combined, explain
from structsim.contrast import contrast_z, pair_key
from structsim.model import Block, Program, Tile
from structsim.tiles import tiles_of

__all__ = [
    "Block",
    "Program",
    "Tile",
    "combined",
    "contrast_z",
    "explain",
    "pair_key",
    "tiles_of",
]
