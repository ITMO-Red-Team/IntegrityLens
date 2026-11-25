//! Все пары набора: программы на входе, оценки и тайлы на выходе.
//!
//! Формат — числа через пробел, см. `docs/native.md`. Он текстовый, чтобы вход
//! можно было посмотреть глазами, а JSON не нужен, чтобы не тянуть зависимости:
//! разбирать тут нечего, кроме чисел.

mod compare;
mod matcher;
mod tiles;

use std::collections::HashMap;
use std::io::{self, Read, Write};
use std::process::ExitCode;
use std::sync::atomic::{AtomicUsize, Ordering};
use std::time::Instant;

use compare::{score_pair, BlockTable, PairScore, Program, Worker, MAX_ALPHABET};
use matcher::Seq;

const PROTOCOL: &str = "structsim-native/3";
const SCOPED_PROTOCOL: &str = "structsim-native/4";

struct Numbers<'a> {
    tokens: std::str::SplitAsciiWhitespace<'a>,
}

impl Numbers<'_> {
    fn next(&mut self) -> Result<usize, String> {
        let token = self.tokens.next().ok_or("вход закончился раньше времени")?;
        token.parse().map_err(|_| format!("ожидалось число, пришло {token:?}"))
    }

    fn take(&mut self, count: usize) -> Result<Vec<u32>, String> {
        (0..count).map(|_| self.next().map(|value| value as u32)).collect()
    }
}

/// Программа в том виде, как она пришла: блоки ещё без общей таблицы.
struct RawProgram {
    labels: Vec<u32>,
    lines: Vec<u32>,
    blocks: Vec<Vec<u32>>,
}

struct Input {
    programs: Vec<RawProgram>,
    targets: Vec<bool>,
    alphabet: usize,
    min_tile: usize,
    threads: usize,
}

fn parse(text: &str) -> Result<Input, String> {
    let mut tokens = text.split_ascii_whitespace();
    let header = tokens.next().ok_or("пустой вход")?;
    if header != PROTOCOL && header != SCOPED_PROTOCOL {
        return Err(format!("неподдерживаемый формат {header:?}"));
    }
    let mut numbers = Numbers { tokens };
    let count = numbers.next()?;
    let alphabet = numbers.next()?;
    let min_tile = numbers.next()?;
    let threads = numbers.next()?;
    if alphabet > MAX_ALPHABET {
        return Err(format!("меток {alphabet}, поддерживается до {MAX_ALPHABET}"));
    }
    let targets = if header == SCOPED_PROTOCOL {
        (0..count)
            .map(|_| match numbers.next()? {
                0 => Ok(false),
                1 => Ok(true),
                _ => Err("допустимые флаги целевой работы: 0 и 1".to_string()),
            })
            .collect::<Result<Vec<_>, _>>()?
    } else {
        vec![true; count]
    };
    let mut programs = Vec::with_capacity(count);
    for _ in 0..count {
        let length = numbers.next()?;
        let labels = numbers.take(length)?;
        let lines = numbers.take(length)?;
        let block_count = numbers.next()?;
        let mut blocks = Vec::with_capacity(block_count);
        for _ in 0..block_count {
            let size = numbers.next()?;
            blocks.push(numbers.take(size)?);
        }
        if labels.iter().chain(blocks.iter().flatten()).any(|&label| label as usize >= alphabet) {
            return Err("номер метки вне алфавита".to_string());
        }
        programs.push(RawProgram { labels, lines, blocks });
    }
    Ok(Input { programs, targets, alphabet, min_tile, threads })
}

/// Параллельный `map` с раздачей по одному: цена задач различается на порядки,
/// поэтому делить их поровну заранее нельзя.
fn parallel_map<T: Sync, R: Send, S>(
    items: &[T],
    threads: usize,
    make_state: impl Fn() -> S + Sync,
    work: impl Fn(&T, &mut S) -> R + Sync,
) -> Vec<R> {
    let next = AtomicUsize::new(0);
    let run = || {
        let mut state = make_state();
        let mut done = Vec::new();
        loop {
            let index = next.fetch_add(1, Ordering::Relaxed);
            let Some(item) = items.get(index) else { return done };
            done.push((index, work(item, &mut state)));
        }
    };
    let mut results: Vec<(usize, R)> = if threads <= 1 {
        run()
    } else {
        std::thread::scope(|scope| {
            let handles: Vec<_> = (0..threads).map(|_| scope.spawn(run)).collect();
            handles.into_iter().flat_map(|handle| handle.join().expect("поток упал")).collect()
        })
    };
    results.sort_unstable_by_key(|(index, _)| *index);
    results.into_iter().map(|(_, result)| result).collect()
}

fn build(input: Input, threads: usize) -> (Vec<Program>, BlockTable) {
    let Input { programs: raw, alphabet, min_tile, .. } = input;
    let mut ids: HashMap<Vec<u32>, u32> = HashMap::new();
    let mut unique = Vec::new();
    let mut prepared = Vec::with_capacity(raw.len());
    for RawProgram { labels, lines, blocks } in raw {
        let blocks: Vec<u32> = blocks
            .into_iter()
            .filter(|block| !block.is_empty())
            .map(|block| {
                *ids.entry(block).or_insert_with_key(|block| {
                    unique.push(block.clone());
                    unique.len() as u32 - 1
                })
            })
            .collect();
        prepared.push((labels, lines, blocks));
    }
    let unique = parallel_map(&unique, threads, || (), |block, _| Seq::new(block.clone(), alphabet));
    let programs = parallel_map(
        &prepared,
        threads,
        || (),
        |(labels, lines, blocks), _| Program::new(labels.clone(), lines.clone(), blocks.clone(), alphabet, min_tile),
    );
    (programs, BlockTable::new(unique, alphabet))
}

fn write_pair(out: &mut impl Write, a: usize, b: usize, score: &PairScore) -> io::Result<()> {
    write!(
        out,
        "{a} {b} {} {} {} {} {} {} {}",
        score.order,
        score.shingle,
        score.blocks,
        score.combined,
        score.containment,
        score.final_score,
        score.tiles.len()
    )?;
    for item in &score.tiles {
        write!(
            out,
            " {} {} {} {} {} {} {}",
            item.tile.start_a, item.tile.start_b, item.tile.length, item.lines_a.0, item.lines_a.1, item.lines_b.0, item.lines_b.1
        )?;
    }
    writeln!(out)
}

fn run() -> Result<(), String> {
    let started = Instant::now();
    let mut text = String::new();
    io::stdin().read_to_string(&mut text).map_err(|error| format!("чтение входа: {error}"))?;
    let mut input = parse(&text).map_err(|error| format!("разбор входа: {error}"))?;
    drop(text);

    let threads = match input.threads {
        0 => std::thread::available_parallelism().map_or(1, |count| count.get()),
        count => count,
    };
    let min_tile = input.min_tile;
    let targets = std::mem::take(&mut input.targets);
    let parsed = started.elapsed();
    let (programs, table) = build(input, threads);
    let indexed = started.elapsed();

    let pairs: Vec<(usize, usize)> = (0..programs.len())
        .flat_map(|a| (a + 1..programs.len()).map(move |b| (a, b)))
        .filter(|&(a, b)| targets[a] || targets[b])
        .collect();
    let max_len = programs
        .iter()
        .map(|program| program.labels.len())
        .chain(table.blocks.iter().map(Seq::len))
        .max()
        .unwrap_or(0);
    let scores = parallel_map(
        &pairs,
        threads,
        || Worker::new(max_len),
        |&(a, b), worker| score_pair(&programs[a], &programs[b], &table, min_tile, worker),
    );
    let scored = started.elapsed();

    let mut out = io::BufWriter::new(io::stdout().lock());
    for (&(a, b), score) in pairs.iter().zip(&scores) {
        write_pair(&mut out, a, b, score).map_err(|error| format!("запись результата: {error}"))?;
    }
    out.flush().map_err(|error| format!("запись результата: {error}"))?;

    if std::env::var_os("STRUCTSIM_NATIVE_TIMINGS").is_some() {
        eprintln!(
            "threads={threads} parse_ms={} index_ms={} pairs_ms={} total_ms={}",
            parsed.as_millis(),
            (indexed - parsed).as_millis(),
            (scored - indexed).as_millis(),
            started.elapsed().as_millis()
        );
    }
    Ok(())
}

fn main() -> ExitCode {
    if std::env::args().nth(1).as_deref() == Some("--version") {
        println!("{} {PROTOCOL}", env!("CARGO_PKG_VERSION"));
        return ExitCode::SUCCESS;
    }
    match run() {
        Ok(()) => ExitCode::SUCCESS,
        Err(error) => {
            eprintln!("structsim-native: {error}");
            ExitCode::FAILURE
        }
    }
}
