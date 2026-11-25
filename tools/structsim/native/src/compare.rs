//! Шаг 3: четыре меры, `COMBINED` и итог, бит в бит как в `compare.py`.
//!
//! Из `compare.combined` считается только то, что не посчитано раньше. Там мера
//! по функциям вычисляется дважды, хотя в обе стороны даёт одно число, а мера по
//! фрагментам симметрична.

use std::collections::HashMap;

use crate::matcher::{matching_total, order_similarity, ratio, Scratch, Seq};
use crate::tiles::{gram_index, greedy_string_tiling, span, Tile};

pub const SHINGLE_SIZE: usize = 5;
/// На метку куска отводится 12 бит: пять меток должны поместиться в `u64`.
pub const MAX_ALPHABET: usize = 1 << 12;

/// Набор без блоков, общий для всех программ: одинаковый метод у разных авторов
/// сравнивается один раз.
pub struct BlockTable {
    pub blocks: Vec<Seq>,
    alphabet: usize,
}

impl BlockTable {
    pub fn new(blocks: Vec<Seq>, alphabet: usize) -> BlockTable {
        BlockTable { blocks, alphabet }
    }

    /// Сколько меток заведомо не совпадёт: сумма совпавших кусков не может быть
    /// больше пересечения мультимножеств меток.
    fn overlap(&self, x: usize, y: usize) -> usize {
        let (x, y) = (&self.blocks[x], &self.blocks[y]);
        (0..self.alphabet).map(|label| x.count(label).min(y.count(label)) as usize).sum()
    }
}

pub struct Program {
    pub labels: Seq,
    pub lines: Vec<u32>,
    /// Номера непустых блоков в `BlockTable`, с повторами и в порядке программы.
    pub blocks: Vec<u32>,
    shingles: Vec<u64>,
    grams: Vec<(u64, u32)>,
}

impl Program {
    pub fn new(labels: Vec<u32>, lines: Vec<u32>, blocks: Vec<u32>, alphabet: usize, min_tile: usize) -> Program {
        let shingles = shingles(&labels);
        let grams = gram_index(&labels, min_tile);
        Program { labels: Seq::new(labels, alphabet), lines, blocks, shingles, grams }
    }
}

fn shingles(labels: &[u32]) -> Vec<u64> {
    if labels.len() < SHINGLE_SIZE {
        return Vec::new();
    }
    let mut keys: Vec<u64> = labels
        .windows(SHINGLE_SIZE)
        .map(|window| window.iter().fold(0u64, |key, &label| (key << 12) | label as u64))
        .collect();
    keys.sort_unstable();
    keys.dedup();
    keys
}

fn common_shingles(a: &[u64], b: &[u64]) -> usize {
    let (mut x, mut y, mut common) = (0, 0, 0usize);
    while x < a.len() && y < b.len() {
        match a[x].cmp(&b[y]) {
            std::cmp::Ordering::Less => x += 1,
            std::cmp::Ordering::Greater => y += 1,
            std::cmp::Ordering::Equal => {
                common += 1;
                x += 1;
                y += 1;
            }
        }
    }
    common
}

fn shingle_similarity(a: &[u64], b: &[u64], common: usize) -> f64 {
    let union = a.len() + b.len() - common;
    if union == 0 {
        return 0.0;
    }
    common as f64 / union as f64
}

fn containment_similarity(a: &[u64], b: &[u64], common: usize) -> f64 {
    let smaller = a.len().min(b.len());
    if smaller == 0 {
        return 0.0;
    }
    common as f64 / smaller as f64
}

/// Встроенный `sum()` для float в CPython ≥ 3.12: компенсация Ноймайера. Обычное
/// сложение разошлось бы с `compare.py` в последних битах.
struct PythonSum {
    total: f64,
    compensation: f64,
}

impl PythonSum {
    fn new() -> PythonSum {
        PythonSum { total: 0.0, compensation: 0.0 }
    }

    fn add(&mut self, value: f64) {
        let next = self.total + value;
        if self.total.abs() >= value.abs() {
            self.compensation += (self.total - next) + value;
        } else {
            self.compensation += (value - next) + self.total;
        }
        self.total = next;
    }

    fn value(&self) -> f64 {
        if self.compensation != 0.0 && self.compensation.is_finite() {
            self.total + self.compensation
        } else {
            self.total
        }
    }
}

/// Рабочая память потока: `ratio` не симметричен, поэтому ключ упорядочен.
pub struct Worker {
    pub scratch: Scratch,
    memo: HashMap<(u32, u32), u32>,
    candidates: Vec<(f64, u32)>,
}

impl Worker {
    pub fn new(max_len: usize) -> Worker {
        Worker { scratch: Scratch::new(max_len), memo: HashMap::new(), candidates: Vec::new() }
    }
}

/// `_best_match`. Нужен только максимум, поэтому кандидаты идут по убыванию верхней
/// границы и перебор останавливается, как только граница не выше найденного.
fn best_match(x: u32, others: &[u32], table: &BlockTable, worker: &mut Worker) -> f64 {
    let x_len = table.blocks[x as usize].len();
    let mut candidates = std::mem::take(&mut worker.candidates);
    candidates.clear();
    for &y in others {
        if y == x {
            worker.candidates = candidates;
            return 1.0;
        }
        let length = x_len + table.blocks[y as usize].len();
        candidates.push((ratio(table.overlap(x as usize, y as usize), length), y));
    }
    candidates.sort_unstable_by(|left, right| right.0.total_cmp(&left.0).then(left.1.cmp(&right.1)));
    let mut best = 0.0f64;
    for &(bound, y) in &candidates {
        if bound <= best {
            break;
        }
        let total = match worker.memo.get(&(x, y)) {
            Some(&total) => total,
            None => {
                let total = matching_total(&table.blocks[x as usize], &table.blocks[y as usize], &mut worker.scratch) as u32;
                worker.memo.insert((x, y), total);
                total
            }
        };
        best = best.max(ratio(total as usize, x_len + table.blocks[y as usize].len()));
    }
    worker.candidates = candidates;
    best
}

fn blocks_one_way(from: &[u32], to: &[u32], table: &BlockTable, worker: &mut Worker) -> f64 {
    let weight: usize = from.iter().map(|&x| table.blocks[x as usize].len()).sum();
    if weight == 0 {
        return 0.0;
    }
    let mut sum = PythonSum::new();
    for &x in from {
        sum.add(table.blocks[x as usize].len() as f64 * best_match(x, to, table, worker));
    }
    sum.value() / weight as f64
}

fn block_similarity(a: &Program, b: &Program, table: &BlockTable, worker: &mut Worker) -> f64 {
    if a.blocks.is_empty() || b.blocks.is_empty() {
        return 0.0;
    }
    (blocks_one_way(&a.blocks, &b.blocks, table, worker) + blocks_one_way(&b.blocks, &a.blocks, table, worker)) / 2.0
}

pub struct TileLines {
    pub tile: Tile,
    pub lines_a: (u32, u32),
    pub lines_b: (u32, u32),
}

pub struct PairScore {
    pub order: f64,
    pub shingle: f64,
    pub blocks: f64,
    pub combined: f64,
    pub containment: f64,
    pub final_score: f64,
    pub tiles: Vec<TileLines>,
}

pub fn score_pair(a: &Program, b: &Program, table: &BlockTable, min_tile: usize, worker: &mut Worker) -> PairScore {
    let order = order_similarity(&a.labels, &b.labels, &mut worker.scratch);
    let reversed = order_similarity(&b.labels, &a.labels, &mut worker.scratch);
    let common = common_shingles(&a.shingles, &b.shingles);
    let shingle = shingle_similarity(&a.shingles, &b.shingles, common);
    let containment = containment_similarity(&a.shingles, &b.shingles, common);
    let blocks = block_similarity(a, b, table, worker);
    let combined = ((order + shingle + blocks) / 3.0 + (reversed + shingle + blocks) / 3.0) / 2.0;
    let final_score = if containment > combined { containment } else { combined };
    let tiles = if min_tile == 0 {
        Vec::new()
    } else {
        greedy_string_tiling(&a.labels.labels, &b.labels.labels, &a.grams, &b.grams, min_tile)
            .into_iter()
            .map(|tile| TileLines {
                lines_a: span(&a.lines, tile.start_a, tile.length),
                lines_b: span(&b.lines, tile.start_b, tile.length),
                tile,
            })
            .collect()
    };
    PairScore { order, shingle, blocks, combined, containment, final_score, tiles }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn python_sum_matches_cpython() {
        // sum([1e100, 1.0, -1e100, 1.0]) в CPython ≥ 3.12 даёт 2.0, а не 1.0.
        let mut sum = PythonSum::new();
        for value in [1e100, 1.0, -1e100, 1.0] {
            sum.add(value);
        }
        assert_eq!(sum.value(), 2.0);
    }

    #[test]
    fn short_programs_have_no_shingles() {
        assert_eq!(shingles(&[1, 2, 3, 4]), Vec::<u64>::new());
        assert_eq!(shingle_similarity(&[], &[], 0), 0.0);
        assert_eq!(containment_similarity(&[], &[1], 0), 0.0);
    }

    #[test]
    fn shingle_similarity_is_jaccard() {
        let (a, b) = (shingles(&[1, 2, 3, 4, 5, 6]), shingles(&[2, 3, 4, 5, 6, 7]));
        assert_eq!(a.len(), 2);
        assert_eq!(shingle_similarity(&a, &b, common_shingles(&a, &b)), 1.0 / 3.0);
    }

    #[test]
    fn containment_divides_by_smaller_set() {
        let (a, b) = (shingles(&[1, 2, 3, 4, 5, 6]), shingles(&[9, 8, 1, 2, 3, 4, 5, 6, 7, 8, 9]));
        let common = common_shingles(&a, &b);
        assert_eq!(containment_similarity(&a, &b, common), 1.0);
        assert_eq!(containment_similarity(&b, &a, common), 1.0);
        assert!(shingle_similarity(&a, &b, common) < 0.5);
    }
}
