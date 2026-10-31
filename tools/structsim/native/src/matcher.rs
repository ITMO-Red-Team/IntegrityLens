//! Мера 1: порт `difflib.SequenceMatcher(None, a, b, autojunk=False).ratio()`.
//!
//! Алгоритм тот же, что в CPython, и он определяет число, а не только скорость:
//! ищется самый длинный общий кусок, среди равных берётся самый левый в `a`,
//! затем самый левый в `b`, и поиск повторяется слева и справа от него. Другой
//! выбор куска дал бы другую сумму, поэтому заменить его на настоящую LCS нельзя.
//!
//! Откуда скорость при том же алгоритме:
//!
//! - вместо словарей `j2len` два массива с метками строк: чистить их не нужно,
//!   запись из чужой строки просто не совпадает по метке;
//! - индекс вхождений строится один раз на последовательность, а не на сравнение;
//! - вхождения левее `blo` пропускаются двоичным поиском, а не перебором. В
//!   глубине рекурсии, где диапазоны узкие, это основная экономия;
//! - на длинных диапазонах совпадения ищутся по кускам из `GRAM` меток, а не по
//!   одной метке. Меток всего несколько десятков, и `ID` встречается в каждой
//!   третьей позиции, поэтому вхождений одной метки много; куски намного реже.
//!   Куски короче `GRAM` так не видны, но ответ от этого не меняется, пока самый
//!   длинный кусок не короче `GRAM`. А если короче, поиск повторяется по меткам.

use std::collections::HashMap;
use std::hash::{BuildHasherDefault, Hasher};

use crate::tiles::rolling_hashes;

// Подобрано по замерам, см. `docs/native.md`.
const GRAM: usize = 6;
const GRAM_MIN_RANGE: usize = 32;

/// Ключи уже хеши, перемешивать их второй раз незачем.
#[derive(Default)]
struct PassThrough(u64);

impl Hasher for PassThrough {
    fn finish(&self) -> u64 {
        self.0
    }

    fn write(&mut self, _: &[u8]) {
        unreachable!("ключи индекса — только u64");
    }

    fn write_u64(&mut self, value: u64) {
        self.0 = value;
    }
}

/// Индекс кусков из `GRAM` меток: хеш куска с каждой позиции и вхождения по хешу.
struct GramIndex {
    hashes: Vec<u64>,
    postings: Vec<u32>,
    groups: HashMap<u64, (u32, u32), BuildHasherDefault<PassThrough>>,
}

impl GramIndex {
    fn new(labels: &[u32]) -> GramIndex {
        let hashes = rolling_hashes(labels, GRAM);
        let mut sorted: Vec<(u64, u32)> = hashes.iter().enumerate().map(|(at, &hash)| (hash, at as u32)).collect();
        sorted.sort_unstable();
        let mut groups = HashMap::default();
        let mut from = 0;
        while from < sorted.len() {
            let hash = sorted[from].0;
            let to = from + sorted[from..].partition_point(|entry| entry.0 == hash);
            groups.insert(hash, (from as u32, to as u32));
            from = to;
        }
        GramIndex { hashes, postings: sorted.into_iter().map(|entry| entry.1).collect(), groups }
    }

    /// Вхождения куска, начинающиеся в `[blo, bhi)`, по возрастанию.
    #[inline]
    fn occurrences(&self, hash: u64, blo: usize, bhi: usize) -> &[u32] {
        match self.groups.get(&hash) {
            None => &[],
            Some(&(from, to)) => {
                let all = &self.postings[from as usize..to as usize];
                let start = all.partition_point(|&t| (t as usize) < blo);
                let end = start + all[start..].partition_point(|&t| (t as usize) < bhi);
                &all[start..end]
            }
        }
    }
}

/// Последовательность меток с индексом вхождений каждой метки (`b2j` в difflib).
pub struct Seq {
    pub labels: Vec<u32>,
    offsets: Vec<u32>,
    postings: Vec<u32>,
    grams: Option<GramIndex>,
}

impl Seq {
    /// `alphabet` — число различных меток во всём наборе.
    pub fn new(labels: Vec<u32>, alphabet: usize) -> Seq {
        let grams = (labels.len() >= GRAM_MIN_RANGE).then(|| GramIndex::new(&labels));
        let mut offsets = vec![0u32; alphabet + 1];
        for &label in &labels {
            offsets[label as usize + 1] += 1;
        }
        for k in 0..alphabet {
            offsets[k + 1] += offsets[k];
        }
        let mut next = offsets.clone();
        let mut postings = vec![0u32; labels.len()];
        for (position, &label) in labels.iter().enumerate() {
            postings[next[label as usize] as usize] = position as u32;
            next[label as usize] += 1;
        }
        Seq { labels, offsets, postings, grams }
    }

    pub fn len(&self) -> usize {
        self.labels.len()
    }

    pub fn is_empty(&self) -> bool {
        self.labels.is_empty()
    }

    /// Сколько раз встречается метка. По этим числам считается верхняя граница.
    pub fn count(&self, label: usize) -> u32 {
        self.offsets[label + 1] - self.offsets[label]
    }

    /// Вхождения метки в пределах `[blo, bhi)`, по возрастанию.
    #[inline]
    fn occurrences(&self, label: u32, blo: usize, bhi: usize) -> &[u32] {
        let label = label as usize;
        let all = &self.postings[self.offsets[label] as usize..self.offsets[label + 1] as usize];
        let from = if blo == 0 {
            0
        } else {
            all.partition_point(|&j| (j as usize) < blo)
        };
        let to = if bhi == self.labels.len() {
            all.len()
        } else {
            from + all[from..].partition_point(|&j| (j as usize) < bhi)
        };
        &all[from..to]
    }
}

/// Рабочая память одного потока. Выделяется один раз, переживает все сравнения.
pub struct Scratch {
    previous: Vec<u64>,
    current: Vec<u64>,
    row: u32,
    queue: Vec<(u32, u32, u32, u32)>,
}

impl Scratch {
    pub fn new(max_len: usize) -> Scratch {
        Scratch {
            previous: vec![0; max_len + 2],
            current: vec![0; max_len + 2],
            row: 0,
            queue: Vec::new(),
        }
    }

    /// Метка строки — 32 бита. На самых длинных входах она может закончиться.
    fn reserve_rows(&mut self, rows: usize) {
        if (self.row as usize) + rows + 3 >= u32::MAX as usize {
            self.previous.fill(0);
            self.current.fill(0);
            self.row = 0;
        }
        // Пропуск номера: первая строка поиска не должна принять за свою
        // запись последней строки предыдущего поиска.
        self.row += 2;
    }
}

/// Тот же поиск, но клетка таблицы — совпавший кусок из `GRAM` меток, а строка
/// таблицы — его начало в `a`. Порядок обхода клеток такой же, как по меткам,
/// поэтому среди кусков не короче `GRAM` выбор тот же.
///
/// `None` — самый длинный кусок короче `GRAM` или хеши совпали случайно. Случайно
/// совпавший хеш не может незаметно изменить ответ: он может только завысить длину,
/// а завышенный кусок здесь же не пройдёт проверку по самим меткам.
fn find_longest_gram_match(
    a: &[u32],
    a_grams: &GramIndex,
    b: &Seq,
    b_grams: &GramIndex,
    (alo, ahi, blo, bhi): (usize, usize, usize, usize),
    scratch: &mut Scratch,
) -> Option<(usize, usize, usize)> {
    let (mut best_i, mut best_j, mut best) = (alo, blo, 0usize);
    let limit = (ahi - alo).min(bhi - blo);
    scratch.reserve_rows(ahi - alo);
    'rows: for s in alo..=ahi - GRAM {
        let before = scratch.row as u64;
        scratch.row += 1;
        let row = scratch.row as u64;
        for &t in b_grams.occurrences(a_grams.hashes[s], blo, bhi + 1 - GRAM) {
            let t = t as usize;
            let stored = scratch.previous[t];
            let k = if stored >> 32 == before { (stored as u32 as usize) + 1 } else { GRAM };
            scratch.current[t + 1] = (row << 32) | k as u64;
            if k > best {
                best = k;
                best_i = s + GRAM - k;
                best_j = t + GRAM - k;
                if best == limit {
                    break 'rows;
                }
            }
        }
        std::mem::swap(&mut scratch.previous, &mut scratch.current);
    }
    (best > 0 && a[best_i..best_i + best] == b.labels[best_j..best_j + best]).then_some((best_i, best_j, best))
}

/// `find_longest_match` из difflib: `(i, j, k)`, где `k` наибольшее, среди таких
/// `i` наименьшее, среди таких `j` наименьшее.
#[inline]
fn find_longest_match(
    a: &Seq,
    b: &Seq,
    (alo, ahi, blo, bhi): (usize, usize, usize, usize),
    scratch: &mut Scratch,
) -> (usize, usize, usize) {
    if ahi - alo >= GRAM_MIN_RANGE && bhi - blo >= GRAM_MIN_RANGE {
        if let (Some(a_grams), Some(b_grams)) = (&a.grams, &b.grams) {
            let range = (alo, ahi, blo, bhi);
            if let Some(found) = find_longest_gram_match(&a.labels, a_grams, b, b_grams, range, scratch) {
                return found;
            }
        }
    }
    let (mut best_i, mut best_j, mut best) = (alo, blo, 0usize);
    let limit = (ahi - alo).min(bhi - blo);
    scratch.reserve_rows(ahi - alo);
    for (i, &label) in a.labels.iter().enumerate().take(ahi).skip(alo) {
        let before = scratch.row as u64;
        scratch.row += 1;
        let row = scratch.row as u64;
        for &j in b.occurrences(label, blo, bhi) {
            let j = j as usize;
            // previous[j] — длина куска, заканчивавшегося на (i - 1, j - 1).
            let stored = scratch.previous[j];
            let k = if stored >> 32 == before { (stored as u32 as usize) + 1 } else { 1 };
            scratch.current[j + 1] = (row << 32) | k as u64;
            if k > best {
                best = k;
                best_i = i + 1 - k;
                best_j = j + 1 - k;
                // Строгое улучшение дальше невозможно, а равные его не заменят.
                if best == limit {
                    return (best_i, best_j, best);
                }
            }
        }
        std::mem::swap(&mut scratch.previous, &mut scratch.current);
    }
    (best_i, best_j, best)
}

/// Сумма длин совпавших кусков. Порядок обхода на сумму не влияет: части,
/// на которые делит найденный кусок, друг от друга не зависят.
pub fn matching_total(a: &Seq, b: &Seq, scratch: &mut Scratch) -> usize {
    if a.labels == b.labels {
        return a.len();
    }
    let mut total = 0;
    let mut queue = std::mem::take(&mut scratch.queue);
    queue.clear();
    queue.push((0, a.len() as u32, 0, b.len() as u32));
    while let Some((alo, ahi, blo, bhi)) = queue.pop() {
        let range = (alo as usize, ahi as usize, blo as usize, bhi as usize);
        let (i, j, k) = find_longest_match(a, b, range, scratch);
        if k > 0 {
            total += k;
            let (i, j, k) = (i as u32, j as u32, k as u32);
            if alo < i && blo < j {
                queue.push((alo, i, blo, j));
            }
            if i + k < ahi && j + k < bhi {
                queue.push((i + k, ahi, j + k, bhi));
            }
        }
    }
    scratch.queue = queue;
    total
}

/// `compare.order_similarity`: те же пустые случаи и та же формула `ratio()`.
pub fn order_similarity(a: &Seq, b: &Seq, scratch: &mut Scratch) -> f64 {
    if a.is_empty() && b.is_empty() {
        return 1.0;
    }
    if a.is_empty() || b.is_empty() {
        return 0.0;
    }
    ratio(matching_total(a, b, scratch), a.len() + b.len())
}

#[inline]
pub fn ratio(matches: usize, length: usize) -> f64 {
    2.0 * matches as f64 / length as f64
}

#[cfg(test)]
mod tests {
    use super::*;

    fn seq(text: &str) -> Seq {
        Seq::new(text.bytes().map(|b| b as u32).collect(), 256)
    }

    fn similarity(a: &str, b: &str) -> f64 {
        let mut scratch = Scratch::new(a.len().max(b.len()));
        order_similarity(&seq(a), &seq(b), &mut scratch)
    }

    /// Поиск только по меткам, без индекса кусков: эталон для проверки индекса.
    fn plain_total(a: &[u32], b: &[u32], alphabet: usize) -> usize {
        let (mut a, mut b) = (Seq::new(a.to_vec(), alphabet), Seq::new(b.to_vec(), alphabet));
        a.grams = None;
        b.grams = None;
        matching_total(&a, &b, &mut Scratch::new(a.len().max(b.len())))
    }

    // Ожидаемые числа посчитаны difflib: `SequenceMatcher(None, a, b,
    // autojunk=False).ratio()`.
    #[test]
    fn matches_difflib() {
        assert_eq!(similarity("abcd", "bcde"), 0.75);
        assert_eq!(similarity("abxcd", "abcd"), 8.0 / 9.0);
        assert_eq!(similarity("private Thread currentThread;", "private volatile Thread currentThread;"), 2.0 * 29.0 / 67.0);
        assert_eq!(similarity("", ""), 1.0);
        assert_eq!(similarity("a", ""), 0.0);
        assert_eq!(similarity("same", "same"), 1.0);
    }

    // Направление важно: difflib не симметричен, и порт должен это сохранить.
    #[test]
    fn keeps_difflib_asymmetry() {
        let (first, second) = ("tide", "diet");
        assert_eq!(similarity(first, second), 0.25);
        assert_eq!(similarity(second, first), 0.5);
    }

    #[test]
    fn scratch_survives_row_exhaustion() {
        let mut scratch = Scratch::new(4);
        scratch.row = u32::MAX - 4;
        let (a, b) = (Seq::new(vec![1, 2, 9, 4], 10), Seq::new(vec![1, 2, 3, 4], 10));
        assert_eq!(matching_total(&a, &b, &mut scratch), 3);
    }

    // Простой генератор: нужны одинаковые данные на каждом запуске, без зависимостей.
    fn pseudo_random(seed: u64, count: usize, modulo: u64) -> Vec<u32> {
        let mut state = seed;
        (0..count)
            .map(|_| {
                state = state.wrapping_mul(6364136223846793005).wrapping_add(1442695040888963407);
                ((state >> 33) % modulo) as u32
            })
            .collect()
    }

    #[test]
    fn gram_index_gives_the_same_total() {
        for (seed, modulo) in [(1, 2), (2, 3), (3, 5), (4, 40)] {
            let first = pseudo_random(seed, 1500, modulo);
            // Вторая — та же первая с правками: длинные куски должны найтись.
            let mut second = first.clone();
            for (at, noise) in pseudo_random(seed + 100, 40, 1400).into_iter().enumerate() {
                second[noise as usize] = at as u32 % modulo as u32;
            }
            second.rotate_left(300);
            let third = pseudo_random(seed + 200, 900, modulo);
            for (a, b) in [(&first, &second), (&second, &first), (&first, &third), (&third, &second)] {
                let indexed = matching_total(
                    &Seq::new(a.clone(), modulo as usize),
                    &Seq::new(b.clone(), modulo as usize),
                    &mut Scratch::new(a.len().max(b.len())),
                );
                assert_eq!(indexed, plain_total(a, b, modulo as usize));
            }
        }
    }

    #[test]
    fn gram_search_falls_back_when_matches_are_short() {
        // Общие куски не длиннее двух меток: индекс ничего не найдёт.
        let a: Vec<u32> = (0..600).map(|at| (at % 3) as u32).collect();
        let b: Vec<u32> = (0..600).map(|at| (2 - at % 3) as u32).collect();
        let total = matching_total(&Seq::new(a.clone(), 3), &Seq::new(b.clone(), 3), &mut Scratch::new(600));
        assert_eq!(total, plain_total(&a, &b, 3));
    }
}
