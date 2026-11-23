import type {
  Batch,
  ComparisonMethod,
  Decision,
  Feedback,
  Fragment,
  PairDetail,
  PairScore,
  PairSummary,
  Reason,
  Run,
  Submission,
} from "../api/types";
import { COURSE_TEMPLATE, DEMO_CORPUS, type CorpusItem } from "./corpus";
import { analyze, prepare } from "./engine";

export const DEMO_BATCH_ID = "00000000-0000-4000-8000-000000000001";
const METHODS: ComparisonMethod[] = ["jplag", "structsim"];
// AI считается дольше сравнения: в демо-режиме он заканчивается последним.
const AI_LONGER = 20;
const NOT_FINISHED: Reason = { code: "RUN_NOT_FINISHED", message: "Анализ ещё не завершён" };

interface StoredSubmission {
  id: string;
  originalName: string;
  loc: number;
  code: string;
}

interface StoredBatch {
  id: string;
  title: string;
  language: string;
  createdAt: string;
  submissions: StoredSubmission[];
  scores: Map<string, Record<ComparisonMethod, number>>;
  fragments: Map<string, Fragment[]>;
  aiScores: Map<string, number>;
  doneAt: Record<ComparisonMethod, number>;
  aiDoneAt: number;
  startedAt: number;
}

const batches = new Map<string, StoredBatch>();
const feedback = new Map<string, Feedback>();
let seq = 0;

function uuid(demo: boolean) {
  if (!demo && typeof crypto !== "undefined" && "randomUUID" in crypto) return crypto.randomUUID();
  seq += 1;
  return `00000000-0000-4000-9000-${seq.toString(16).padStart(12, "0")}`;
}

const round4 = (x: number) => Math.round(x * 10000) / 10000;

// Оценка AI для мока детерминирована работой: 0.05–0.95 из её содержимого.
const aiScore = (code: string) => {
  let h = 0;
  for (let i = 0; i < code.length; i++) h = (h * 31 + code.charCodeAt(i)) >>> 0;
  return round4(((h % 901) + 50) / 1000);
};

export interface NewItem {
  originalName: string;
  code: string;
}

export function createBatch(
  title: string,
  items: NewItem[],
  processingMs = 0,
  fixedId?: string,
  language: string = "java",
  aiProcessingMs = processingMs * AI_LONGER,
): Batch {
  const demo = !!fixedId;
  const id = fixedId ?? uuid(false);
  const sorted = [...items].sort((x, y) => x.originalName.localeCompare(y.originalName));
  const prepared = sorted.map((it) => ({ ...it, id: uuid(demo), p: prepare(it.code, COURSE_TEMPLATE) }));
  const scores = new Map<string, Record<ComparisonMethod, number>>();
  const fragments = new Map<string, Fragment[]>();
  for (let i = 0; i < prepared.length; i++)
    for (let j = i + 1; j < prepared.length; j++) {
      const [x, y] = [prepared[i]!, prepared[j]!].sort((u, v) => u.id.localeCompare(v.id)) as [
        (typeof prepared)[number],
        (typeof prepared)[number],
      ];
      const r = analyze(x.id, x.p, y.id, y.p);
      scores.set(`${x.id}:${y.id}`, {
        jplag: round4(r.risk / 100),
        structsim: round4((r.signals.structure + r.signals.coverage) / 2),
      });
      // Индексы строк движка — 0-based; контракт API — 1-based.
      fragments.set(
        `${x.id}:${y.id}`,
        r.fragments.map((f) => ({ ...f, a: [f.a[0]! + 1, f.a[1]! + 1], b: [f.b[0]! + 1, f.b[1]! + 1] })),
      );
    }
  const now = Date.now();
  batches.set(id, {
    id,
    title,
    language,
    createdAt: new Date(now).toISOString(),
    submissions: prepared.map((s) => ({ id: s.id, originalName: s.originalName, loc: s.code.split("\n").length, code: s.code })),
    scores,
    fragments,
    aiScores: new Map(prepared.map((s) => [s.id, aiScore(s.code)])),
    doneAt: { jplag: now + processingMs * 0.6, structsim: now + processingMs },
    aiDoneAt: now + aiProcessingMs,
    startedAt: now + Math.min(300, processingMs * 0.1),
  });
  return getBatch(id)!;
}

function statusAt(startedAt: number, doneAt: number): Run["status"] {
  const now = Date.now();
  if (now >= doneAt) return "done";
  return now >= startedAt ? "running" : "queued";
}

function summary(method: Run["method"], status: Run["status"], startedAt: number, finishedAt: number, version: string, size: number): Run {
  const done = status === "done";
  return {
    method,
    methodVersion: done ? version : null,
    status,
    startedAt: status === "queued" ? null : new Date(startedAt).toISOString(),
    finishedAt: done ? new Date(finishedAt).toISOString() : null,
    parsed: done ? size : null,
    failed: done ? 0 : null,
    error: null,
  };
}

export function getBatch(id: string): Batch | undefined {
  const b = batches.get(id);
  if (!b) return undefined;
  const size = b.submissions.length;
  const runs: Run[] = [
    ...METHODS.map((m) => summary(m, statusAt(b.startedAt, b.doneAt[m]), b.startedAt, b.doneAt[m], "mock-web", size)),
    summary("ai", statusAt(b.startedAt, b.aiDoneAt), b.startedAt, b.aiDoneAt, "mock-web-ai", size),
  ];
  const st = runs.map((r) => r.status);
  const status: Batch["status"] = st.every((s) => s === "queued")
    ? "queued"
    : st.some((s) => s === "queued" || s === "running")
      ? "processing"
      : st.some((s) => s === "done")
        ? "done"
        : "failed";
  const n = b.submissions.length;
  return { id: b.id, title: b.title, language: b.language, status, createdAt: b.createdAt, submissionCount: n, pairCount: (n * (n - 1)) / 2, runs };
}

export const listBatches = () =>
  [...batches.values()].sort((x, y) => y.createdAt.localeCompare(x.createdAt)).map((b) => getBatch(b.id)!);

function pairScore(b: StoredBatch, m: ComparisonMethod, key: string): PairScore {
  if (statusAt(b.startedAt, b.doneAt[m]) !== "done") return { value: null, details: {}, fragments: [], reason: NOT_FINISHED };
  return {
    value: b.scores.get(key)?.[m] ?? null,
    details: {},
    fragments: m === "jplag" ? b.fragments.get(key) ?? [] : [],
    reason: null,
  };
}

export function listSubmissions(batchId: string): Submission[] | undefined {
  const b = batches.get(batchId);
  if (!b) return undefined;
  return b.submissions.map((s) => {
    const methods: Submission["methods"] = {};
    for (const m of METHODS) {
      if (statusAt(b.startedAt, b.doneAt[m]) !== "done") {
        methods[m] = { status: "pending", score: null, matches: [], stats: {}, reason: NOT_FINISHED };
        continue;
      }
      const matches = [...b.scores.entries()]
        .filter(([k]) => k.split(":").includes(s.id))
        .map(([k, v]) => ({ with: k.split(":").find((x) => x !== s.id)!, score: v[m], fragments: [] }))
        .sort((x, y) => y.score - x.score)
        .slice(0, 3);
      methods[m] = { status: "ok", score: matches[0]?.score ?? null, matches, stats: {}, reason: null };
    }
    methods.ai =
      statusAt(b.startedAt, b.aiDoneAt) === "done"
        ? { status: "ok", score: b.aiScores.get(s.id)!, matches: [], stats: {}, reason: null }
        : { status: "pending", score: null, matches: [], stats: {}, reason: NOT_FINISHED };
    return { id: s.id, originalName: s.originalName, language: b.language, loc: s.loc, methods };
  });
}

export function listPairs(batchId: string): PairSummary[] | undefined {
  const b = batches.get(batchId);
  if (!b) return undefined;
  return [...b.scores.keys()].sort().map((key) => {
    const [a, c] = key.split(":") as [string, string];
    const id = `${batchId}:${key}`;
    return {
      id,
      a,
      b: c,
      scores: { jplag: pairScore(b, "jplag", key), structsim: pairScore(b, "structsim", key) },
      decision: feedback.get(id)?.decision ?? null,
    };
  });
}

export function getPair(id: string): PairDetail | undefined {
  const [batchId, a, c] = id.split(":");
  const b = batchId ? batches.get(batchId) : undefined;
  if (!b || !a || !c || !b.scores.has(`${a}:${c}`)) return undefined;
  const info = (sid: string) => {
    const s = b.submissions.find((x) => x.id === sid)!;
    return { originalName: s.originalName, language: b.language, loc: s.loc };
  };
  const fb = feedback.get(id) ?? null;
  return {
    id,
    batchId: b.id,
    a,
    b: c,
    submissions: { [a]: info(a), [c]: info(c) },
    scores: { jplag: pairScore(b, "jplag", `${a}:${c}`), structsim: pairScore(b, "structsim", `${a}:${c}`) },
    decision: fb?.decision ?? null,
    feedback: fb,
  };
}

export function getSource(submissionId: string): string | undefined {
  for (const b of batches.values()) {
    const s = b.submissions.find((x) => x.id === submissionId);
    if (s) return s.code;
  }
  return undefined;
}

export function saveFeedback(id: string, decision: Decision, comment: string): Feedback {
  const fb: Feedback = { decision, comment, updatedAt: new Date().toISOString() };
  feedback.set(id, fb);
  return fb;
}

const SURNAMES = ["ivanov", "petrov", "sidorova", "kuznetsov", "smirnova", "popov", "volkova", "sokolov"];
export const demoItems = (): NewItem[] =>
  DEMO_CORPUS.map((s: CorpusItem, i) => ({ originalName: `hw4/${SURNAMES[i]}/${s.fileName}`, code: s.code }));

export function demoSubmissionId(corpusId: string): string {
  const i = DEMO_CORPUS.findIndex((s) => s.id === corpusId);
  const name = `hw4/${SURNAMES[i]}/${DEMO_CORPUS[i]!.fileName}`;
  return batches.get(DEMO_BATCH_ID)!.submissions.find((s) => s.originalName === name)!.id;
}

export function resetDb() {
  batches.clear();
  feedback.clear();
  seq = 0;
  createBatch("hw4-demo", demoItems(), 0, DEMO_BATCH_ID, "java");
}

resetDb();