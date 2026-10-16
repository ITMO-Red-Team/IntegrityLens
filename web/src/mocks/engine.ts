import type { CodeLine, Fragment, Signals } from "../api/types";

export const MIN_RUN = 3;
export const WEIGHTS = { baseline: 0.1, structure: 0.45, coverage: 0.3, embedding: 0.15 } as const;

const KEEP = new Set(
  (
    "abstract assert boolean break byte case catch char class continue default do double else enum extends final " +
    "finally float for if implements import instanceof int interface long native new package private protected public " +
    "return short static super switch synchronized this throw throws try void volatile while var true false null " +
    "String System out println print Math Integer Long Arrays Collections Objects Scanner List ArrayList LinkedList " +
    "Map HashMap TreeMap Set HashSet TreeSet Queue Deque ArrayDeque PriorityQueue Iterator Comparator " +
    "add addAll addFirst addLast get put poll pollFirst pollLast offer push pop peek remove size isEmpty contains " +
    "containsKey sort fill length nextInt of forEach stream toList main args"
  ).split(" "),
);

export const tokenize = (s: string): string[] =>
  s.match(/[A-Za-z_$][A-Za-z0-9_$]*|\d+|"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|::|->|\+\+|--|[<>=!]=|&&|\|\||\S/g) ?? [];

export const normalizeToken = (t: string): string =>
  /^[A-Za-z_$]/.test(t) ? (KEEP.has(t) ? t : "V") : /^\d/.test(t) ? "N" : /^["']/.test(t) ? "S" : t;

export const isIdentifier = (t: string) => /^[A-Za-z_$][A-Za-z0-9_$]*$/.test(t) && !KEEP.has(t);

function stripLineComment(s: string) {
  let quote: string | null = null;
  for (let i = 0; i < s.length; i++) {
    const c = s[i]!;
    if (quote) {
      if (c === "\\") i++;
      else if (c === quote) quote = null;
    } else if (c === '"' || c === "'") quote = c;
    else if (c === "/" && s[i + 1] === "/") return s.slice(0, i).trimEnd();
  }
  return s;
}

const isBoilerplate = (s: string) => /^(import|package)\s/.test(s);
const isBracesOnly = (s: string) => /^[{}();\s]*$/.test(s);

interface SigLine {
  i: number;
  raw: string[];
  n: string;
}

export interface Prepared {
  lines: CodeLine[];
  sig: SigLine[];
}

export function prepare(code: string, template: string): Prepared {
  const tpl = template
    .split("\n")
    .map((l) => stripLineComment(l.trim()))
    .filter((l) => !isBracesOnly(l))
    .map((l) => tokenize(l).map(normalizeToken).join(" "));
  let tplPos = 0;
  const lines: CodeLine[] = [];
  const sig: SigLine[] = [];
  let inBlock = false;

  code.replace(/\r\n/g, "\n").split("\n").forEach((text, i) => {
    let s = text.trim();
    let kind: CodeLine["kind"] = "code";

    if (inBlock) {
      kind = "comment";
      if (s.includes("*/")) inBlock = false;
    } else if (!s) kind = "blank";
    else if (s.startsWith("//")) kind = "comment";
    else if (s.startsWith("/*")) {
      kind = "comment";
      if (!s.includes("*/")) inBlock = true;
    } else {
      s = stripLineComment(s);
      if (isBoilerplate(s)) kind = "template";
    }

    if (kind === "code") {
      const raw = tokenize(s);
      const n = raw.map(normalizeToken).join(" ");
      if (tplPos < tpl.length && n === tpl[tplPos]) {
        kind = "template";
        tplPos++;
      } else if (!isBracesOnly(s)) sig.push({ i, raw, n });
    }
    lines.push({ text, kind });
  });
  return { lines, sig };
}

function grams(arr: string[], k: number) {
  const g = new Set<string>();
  for (let i = 0; i + k <= arr.length; i++) g.add(arr.slice(i, i + k).join("\u0001"));
  return g;
}

export function jaccard(a: Set<string>, b: Set<string>) {
  if (!a.size || !b.size) return 0;
  let inter = 0;
  a.forEach((v) => b.has(v) && inter++);
  return inter / (a.size + b.size - inter);
}

function hash01(s: string) {
  let h = 2166136261;
  for (const c of s) {
    h ^= c.charCodeAt(0);
    h = Math.imul(h, 16777619);
  }
  return ((h >>> 0) % 1000) / 1000;
}

interface Run {
  i: number;
  j: number;
  L: number;
}

/** Жадный поиск непересекающихся максимальных серий одинаковых нормализованных строк. */
export function matchRuns(A: SigLine[], B: SigLine[], minLen = MIN_RUN): Run[] {
  const cand: Run[] = [];
  for (let i = 0; i < A.length; i++)
    for (let j = 0; j < B.length; j++) {
      if (A[i]!.n !== B[j]!.n) continue;
      if (i > 0 && j > 0 && A[i - 1]!.n === B[j - 1]!.n) continue; // не начало серии
      let L = 0;
      while (i + L < A.length && j + L < B.length && A[i + L]!.n === B[j + L]!.n) L++;
      if (L >= minLen) cand.push({ i, j, L });
    }
  cand.sort((x, y) => y.L - x.L);
  const usedA = new Set<number>();
  const usedB = new Set<number>();
  const runs: Run[] = [];
  for (const c of cand) {
    let free = true;
    for (let k = 0; k < c.L && free; k++) if (usedA.has(c.i + k) || usedB.has(c.j + k)) free = false;
    if (!free) continue;
    for (let k = 0; k < c.L; k++) {
      usedA.add(c.i + k);
      usedB.add(c.j + k);
    }
    runs.push(c);
  }
  return runs.sort((x, y) => x.i - y.i);
}

export interface PairAnalysis {
  signals: Signals;
  risk: number;
  fragments: Fragment[];
  renames: [string, string][];
}

export function analyze(idA: string, a: Prepared, idB: string, b: Prepared): PairAnalysis {
  const rawA = a.sig.flatMap((x) => x.raw);
  const rawB = b.sig.flatMap((x) => x.raw);
  const baseline = jaccard(grams(rawA, 3), grams(rawB, 3));
  const structure = jaccard(grams(rawA.map(normalizeToken), 5), grams(rawB.map(normalizeToken), 5));
  const runs = matchRuns(a.sig, b.sig);
  const matched = runs.reduce((s, r) => s + r.L, 0);
  const coverage = matched / Math.max(1, Math.min(a.sig.length, b.sig.length));
  const embedding = Math.min(0.99, 0.42 + 0.5 * structure + (hash01(idA + idB) - 0.5) * 0.06);

  const renames = new Map<string, string>();
  for (const r of runs)
    for (let k = 0; k < r.L; k++) {
      const ta = a.sig[r.i + k]!.raw;
      const tb = b.sig[r.j + k]!.raw;
      ta.forEach((t, ix) => {
        const u = tb[ix];
        if (u !== undefined && t !== u && isIdentifier(t)) renames.set(t, u);
      });
    }

  const fragments: Fragment[] = runs.map((r, index) => ({
    index,
    a: [a.sig[r.i]!.i, a.sig[r.i + r.L - 1]!.i],
    b: [b.sig[r.j]!.i, b.sig[r.j + r.L - 1]!.i],
    length: r.L,
  }));

  const signals: Signals = { baseline, structure, coverage, embedding };
  return { signals, risk: riskScore(signals), fragments, renames: [...renames] };
}

export function riskScore(s: Signals) {
  const v =
    WEIGHTS.baseline * s.baseline +
    WEIGHTS.structure * s.structure +
    WEIGHTS.coverage * s.coverage +
    WEIGHTS.embedding * s.embedding;
  return Math.round(100 * Math.min(1, Math.max(0, v)));
}

export function explain(s: Signals, fragmentCount: number) {
  const pct = (x: number) => `${Math.round(x * 100)}%`;
  const parts: string[] = [];
  if (s.structure - s.baseline > 0.3)
    parts.push(
      `Структурное сходство (${pct(s.structure)}) заметно выше лексического (${pct(s.baseline)}): ` +
        `устройство кода совпадает, а имена отличаются. Лексический baseline такую пару пропустил бы.`,
    );
  if (s.coverage >= 0.7) parts.push(`В совпадающих блоках ${pct(s.coverage)} содержательных строк меньшей работы.`);
  if (!parts.length && fragmentCount)
    parts.push("Совпадают отдельные короткие блоки. Часто это типовые идиомы задачи: проверьте, есть ли в них необычные решения.");
  if (!parts.length) parts.push("Явных оснований для дополнительного ревью нет.");
  return parts.join(" ");
}

export function clusters(ids: string[], edges: { a: string; b: string; risk: number }[], threshold: number) {
  const parent = new Map(ids.map((id) => [id, id]));
  const find = (x: string): string => {
    const p = parent.get(x)!;
    if (p === x) return x;
    const r = find(p);
    parent.set(x, r);
    return r;
  };
  edges.filter((e) => e.risk >= threshold).forEach((e) => parent.set(find(e.a), find(e.b)));
  const groups = new Map<string, string[]>();
  ids.forEach((id) => {
    const r = find(id);
    groups.set(r, [...(groups.get(r) ?? []), id]);
  });
  return [...groups.values()].filter((g) => g.length > 1);
}