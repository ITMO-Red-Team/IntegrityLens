"""Поиск общих кусков двух программ — тайлов.

Отвечает на вопрос «какие куски двух программ похожи», а не «насколько
похожи программы целиком»: это вывод для человека, на `COMBINED` он не
влияет.

Алгоритм — Greedy String Tiling, тот же, что у JPlag: искать самый длинный
общий непрерывный кусок из непокрытых меток, забрать его, повторить. Свойства,
которые из этого следуют:

- **порядок кусков неважен.** Если две функции поменяли местами, в список
  попадут обе. Этим тайлы отличаются от того, что считает мера 1: `difflib`
  ищет куски с сохранением порядка и вторую из переставленных функций
  потерял бы;
- **метка покрыта не больше одного раза.** Один и тот же кусок `A` не
  сопоставится двум местам в `B`;
- **кусок непрерывен.** Одна лишняя метка в середине разрезает его на два.
"""

from __future__ import annotations

from structsim.model import Program, Tile

# Минимальная длина тайла. Значение НЕ откалибровано: его надо подбирать на
# размеченных данных.
#
# Почему нельзя взять 9, как `-t` по умолчанию у JPlag: у нас метки гораздо
# мельче. У JPlag решение из 19 строк даёт около 27 меток (≈1.4 на строку), а
# наш шаг 2 на три строки примера выдаёт 20 меток (≈6.7 на строку). То есть
# 9 меток у JPlag — это примерно 6 строк кода, а 9 наших — меньше полутора.
# 10 взято как заведомо больше куска из 5 меток, которым работает мера 2.
MIN_TILE_LENGTH = 10


def _match_length(
    a: tuple[str, ...],
    b: tuple[str, ...],
    i: int,
    j: int,
    used_a: list[bool],
    used_b: list[bool],
) -> int:
    """Сколько меток совпадает подряд, начиная с `i` и `j`, по непокрытым."""
    k = 0
    while (
        i + k < len(a)
        and j + k < len(b)
        and not used_a[i + k]
        and not used_b[j + k]
        and a[i + k] == b[j + k]
    ):
        k += 1
    return k


def greedy_string_tiling(
    a: tuple[str, ...],
    b: tuple[str, ...],
    min_length: int = MIN_TILE_LENGTH,
) -> list[Tile]:
    """Все общие куски длиной не меньше `min_length`, от длинных к коротким.

    За один проход находятся все куски максимальной на этот момент длины, они
    забираются, и проход повторяется для оставшихся меток. Куски,
    перекрывшиеся с уже забранными внутри того же прохода, пропускаются.
    """
    if min_length < 1:
        raise ValueError(f"min_length должен быть положительным, получено {min_length}")

    used_a = [False] * len(a)
    used_b = [False] * len(b)
    tiles: list[Tile] = []

    while True:
        longest = min_length - 1
        candidates: list[tuple[int, int, int]] = []

        for i in range(len(a)):
            if used_a[i]:
                continue
            for j in range(len(b)):
                if used_b[j] or a[i] != b[j]:
                    continue
                k = _match_length(a, b, i, j, used_a, used_b)
                if k > longest:
                    longest = k
                    candidates = [(i, j, k)]
                elif k == longest and k >= min_length:
                    candidates.append((i, j, k))

        if longest < min_length or not candidates:
            return tiles

        for i, j, k in candidates:
            # Внутри прохода куски могли перекрыться: первый уже забрал метки.
            if any(used_a[i : i + k]) or any(used_b[j : j + k]):
                continue
            for offset in range(k):
                used_a[i + offset] = True
                used_b[j + offset] = True
            tiles.append(Tile(start_a=i, start_b=j, length=k))


def tiles_of(
    a: Program, b: Program, min_length: int = MIN_TILE_LENGTH
) -> list[Tile]:
    """Тайлы двух программ, самые длинные первыми."""
    return greedy_string_tiling(a.labels, b.labels, min_length)


def covered(tiles: list[Tile]) -> int:
    """Сколько меток покрыто тайлами в одной программе.

    Тайлы не пересекаются, поэтому покрытие в `A` и в `B` одинаково и равно
    сумме длин.
    """
    return sum(tile.length for tile in tiles)
