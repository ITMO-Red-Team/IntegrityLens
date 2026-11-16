//! Greedy String Tiling: те же куски и в том же порядке, что `tiles.py`, но не
//! кубически.
//!
//! `tiles.py` на каждом проходе заново перебирает все пары позиций. Здесь пары
//! позиций перебираются один раз, и только те, где совпал начальный кусок из
//! `min_length` меток: короче порога кусок всё равно не возьмут. Это и есть идея
//! Карпа и Рабина, как у JPlag. Индекс кусков строится один раз на программу,
//! поэтому на паре остаётся слияние двух отсортированных списков.
//!
//! Получается список максимальных совпадений на диагоналях. Покрытие метки только
//! разрезает такое совпадение, новых не создаёт, поэтому проходы работают по этому
//! списку через кучу, а не по всей матрице. Разрезанный кусок пересчитывается
//! лениво, когда дойдёт до вершины кучи.

use std::collections::BinaryHeap;

/// Хеш куска из `size` меток для каждой позиции начала.
pub fn rolling_hashes(labels: &[u32], size: usize) -> Vec<u64> {
    if size == 0 || labels.len() < size {
        return Vec::new();
    }
    const BASE: u64 = 0x9E37_79B9_7F4A_7C15;
    let drop_factor = (1..size).fold(1u64, |power, _| power.wrapping_mul(BASE));
    let mut hash = 0u64;
    let mut hashes = Vec::with_capacity(labels.len() + 1 - size);
    for (end, &label) in labels.iter().enumerate() {
        if end >= size {
            let gone = (labels[end - size] as u64 + 1).wrapping_mul(drop_factor);
            hash = hash.wrapping_sub(gone);
        }
        hash = hash.wrapping_mul(BASE).wrapping_add(label as u64 + 1);
        if end + 1 >= size {
            hashes.push(hash);
        }
    }
    hashes
}

/// Отсортированный индекс кусков из `size` меток: `(хеш, позиция)`.
pub fn gram_index(labels: &[u32], size: usize) -> Vec<(u64, u32)> {
    let mut index: Vec<(u64, u32)> = rolling_hashes(labels, size)
        .into_iter()
        .enumerate()
        .map(|(start, hash)| (hash, start as u32))
        .collect();
    index.sort_unstable();
    index
}

/// Максимальное совпадение на диагонали. Порядок полей задаёт порядок в куче:
/// длинные сначала.
#[derive(Clone, Copy, PartialEq, Eq, PartialOrd, Ord)]
struct Run {
    length: u32,
    start_a: u32,
    start_b: u32,
}

#[derive(Debug, PartialEq, Eq)]
pub struct Tile {
    pub start_a: u32,
    pub start_b: u32,
    pub length: u32,
}

fn diagonal_runs(a: &[u32], b: &[u32], index_a: &[(u64, u32)], index_b: &[(u64, u32)], min_length: usize) -> Vec<Run> {
    let mut runs = Vec::new();
    let (mut x, mut y) = (0, 0);
    while x < index_a.len() && y < index_b.len() {
        let hash = index_a[x].0;
        if hash < index_b[y].0 {
            x += 1;
        } else if hash > index_b[y].0 {
            y += 1;
        } else {
            let x_end = x + index_a[x..].partition_point(|entry| entry.0 == hash);
            let y_end = y + index_b[y..].partition_point(|entry| entry.0 == hash);
            for &(_, i) in &index_a[x..x_end] {
                for &(_, j) in &index_b[y..y_end] {
                    let (i, j) = (i as usize, j as usize);
                    // Совпадение слева: этот кусок — продолжение уже учтённого.
                    if i > 0 && j > 0 && a[i - 1] == b[j - 1] {
                        continue;
                    }
                    // Хеш только отбирает кандидатов, длину считают сами метки.
                    let length = a[i..].iter().zip(&b[j..]).take_while(|(x, y)| x == y).count();
                    if length >= min_length {
                        runs.push(Run { length: length as u32, start_a: i as u32, start_b: j as u32 });
                    }
                }
            }
            x = x_end;
            y = y_end;
        }
    }
    runs
}

fn is_free(run: &Run, used_a: &[bool], used_b: &[bool]) -> bool {
    let (i, j, length) = (run.start_a as usize, run.start_b as usize, run.length as usize);
    !used_a[i..i + length].iter().any(|&used| used) && !used_b[j..j + length].iter().any(|&used| used)
}

/// Непокрытые части куска, которые ещё могут стать тайлом, обратно в кучу.
fn push_remainders(run: &Run, used_a: &[bool], used_b: &[bool], min_length: usize, heap: &mut BinaryHeap<Run>) {
    let (i, j) = (run.start_a as usize, run.start_b as usize);
    let mut start = None;
    for offset in 0..=run.length as usize {
        let free = offset < run.length as usize && !used_a[i + offset] && !used_b[j + offset];
        match (free, start) {
            (true, None) => start = Some(offset),
            (false, Some(from)) => {
                if offset - from >= min_length {
                    heap.push(Run {
                        length: (offset - from) as u32,
                        start_a: (i + from) as u32,
                        start_b: (j + from) as u32,
                    });
                }
                start = None;
            }
            _ => {}
        }
    }
}

pub fn greedy_string_tiling(
    a: &[u32],
    b: &[u32],
    index_a: &[(u64, u32)],
    index_b: &[(u64, u32)],
    min_length: usize,
) -> Vec<Tile> {
    let runs = diagonal_runs(a, b, index_a, index_b, min_length);
    let mut tiles = Vec::new();
    if runs.is_empty() {
        return tiles;
    }
    let mut heap = BinaryHeap::from(runs);
    let mut used_a = vec![false; a.len()];
    let mut used_b = vec![false; b.len()];
    let mut candidates = Vec::new();
    loop {
        // Проход: все непокрытые куски наибольшей длины на его начало.
        candidates.clear();
        let mut longest = 0;
        while let Some(top) = heap.peek().copied() {
            if top.length < longest {
                break;
            }
            heap.pop();
            if is_free(&top, &used_a, &used_b) {
                longest = top.length;
                candidates.push(top);
            } else {
                push_remainders(&top, &used_a, &used_b, min_length, &mut heap);
            }
        }
        if candidates.is_empty() {
            return tiles;
        }
        // Внутри прохода кто раньше по (i, j), тот и забирает метки.
        candidates.sort_unstable_by_key(|run| (run.start_a, run.start_b));
        for run in &candidates {
            if !is_free(run, &used_a, &used_b) {
                push_remainders(run, &used_a, &used_b, min_length, &mut heap);
                continue;
            }
            let (i, j, length) = (run.start_a as usize, run.start_b as usize, run.length as usize);
            used_a[i..i + length].fill(true);
            used_b[j..j + length].fill(true);
            tiles.push(Tile { start_a: run.start_a, start_b: run.start_b, length: run.length });
        }
    }
}

/// `Program.span_of`: наименьшая и наибольшая строка отрезка, 0 — позиции нет.
pub fn span(lines: &[u32], start: u32, length: u32) -> (u32, u32) {
    let (mut first, mut last) = (0, 0);
    for &line in &lines[start as usize..(start + length) as usize] {
        if line != 0 {
            first = if first == 0 { line } else { first.min(line) };
            last = last.max(line);
        }
    }
    (first, last)
}

#[cfg(test)]
mod tests {
    use super::*;

    fn tiles(a: &[u32], b: &[u32], min_length: usize) -> Vec<(u32, u32, u32)> {
        let (index_a, index_b) = (gram_index(a, min_length), gram_index(b, min_length));
        greedy_string_tiling(a, b, &index_a, &index_b, min_length)
            .iter()
            .map(|tile| (tile.start_a, tile.start_b, tile.length))
            .collect()
    }

    // Случаи из tests/test_tiles.py и tools/jplag/how-it-works.md.
    #[test]
    fn swapped_blocks_are_both_found() {
        let a = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
        let b = [7, 8, 9, 10, 99, 1, 2, 3, 4, 5, 6, 98];
        assert_eq!(tiles(&a, &b, 3), vec![(0, 5, 6), (6, 0, 4)]);
        assert_eq!(tiles(&a, &b, 5), vec![(0, 5, 6)]);
        assert_eq!(tiles(&a, &b, 7), vec![]);
    }

    #[test]
    fn inserted_label_cuts_tile() {
        assert_eq!(tiles(&[1, 2, 3, 4, 5, 6], &[1, 2, 3, 99, 4, 5, 6], 3), vec![(0, 0, 3), (3, 4, 3)]);
    }

    #[test]
    fn label_is_covered_once() {
        assert_eq!(tiles(&[1, 2, 3, 1, 2], &[1, 2, 3], 2), vec![(0, 0, 3)]);
    }

    #[test]
    fn hash_collision_is_not_a_match() {
        // Позиции совпали по хешу только если совпали метки: проверка по меткам
        // должна отсеять любой случайно равный хеш, поэтому подменяем индекс.
        let (a, b) = ([1u32, 2, 3], [4u32, 5, 6]);
        assert_eq!(greedy_string_tiling(&a, &b, &[(7, 0)], &[(7, 0)], 3), vec![]);
    }

    #[test]
    fn span_skips_missing_positions() {
        assert_eq!(span(&[0, 5, 0, 3, 9], 0, 4), (3, 5));
        assert_eq!(span(&[0, 0], 0, 2), (0, 0));
    }
}
