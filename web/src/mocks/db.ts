import type {
  Batch,
  Decision,
  Feedback,
  Method,
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
const METHODS: Method[] = ["jplag", "structsim"];
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
  createdAt: string;
  submissions: StoredSubmission[];
  scores: Map<string, Record<Method, number>>;
  doneAt: Record<Method, number>;
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

export interface NewItem {
  originalName: string;
  code: string;
}

export function createBatch(title: string, items: NewItem[], processingMs = 0, fixedId?: string): Batch {
  const demo = !!fixedId;
  const id = fixedId ?? uuid(false);
  const sorted = [...items].sort((x, y) => x.originalName.localeCompare(y.originalName));
  const prepared = sorted.map((it) => ({ ...it, id: uuid(demo), p: prepare(it.code, COURSE_TEMPLATE) }));
  const scores = new Map<string, Record<Method, number>>();
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
    }
  const now = Date.now();
  batches.set(id, {
    id,
    title,
    createdAt: new Date(now).toISOString(),
    submissions: prepared.map((s) => ({ id: s.id, originalName: s.originalName, loc: s.code.split("\n").length, code: s.code })),
    scores,
    doneAt: { jplag: now + processingMs * 0.6, structsim: now + processingMs },
    startedAt: now + Math.min(300, processingMs * 0.1),
  });
  return getBatch(id)!;
}

function runStatus(b: StoredBatch, m: Method): Run["status"] {
  const now = Date.now();
  if (now >= b.doneAt[m]) return "done";
  return now >= b.startedAt ? "running" : "queued";
}

export function getBatch(id: string): Batch | undefined {
  const b = batches.get(id);
  if (!b) return undefined;
  const runs: Run[] = METHODS.map((m) => {
    const status = runStatus(b, m);
    const done = status === "done";
    return {
      method: m,
      methodVersion: done ? "mock-web" : null,
      status,
      startedAt: status === "queued" ? null : new Date(b.startedAt).toISOString(),
      finishedAt: done ? new Date(b.doneAt[m]).toISOString() : null,
      parsed: done ? b.submissions.length : null,
      failed: done ? 0 : null,
      error: null,
    };
  });
  const st = runs.map((r) => r.status);
  const status: Batch["status"] = st.every((s) => s === "queued")
    ? "queued"
    : st.some((s) => s === "queued" || s === "running")
      ? "processing"
      : st.some((s) => s === "done")
        ? "done"
        : "failed";
  const n = b.submissions.length;
  return { id: b.id, title: b.title, language: "java", status, createdAt: b.createdAt, submissionCount: n, pairCount: (n * (n - 1)) / 2, runs };
}

export const listBatches = () =>
  [...batches.values()].sort((x, y) => y.createdAt.localeCompare(x.createdAt)).map((b) => getBatch(b.id)!);

function pairScore(b: StoredBatch, m: Method, key: string): PairScore {
  if (runStatus(b, m) !== "done") return { value: null, details: {}, fragments: [], reason: NOT_FINISHED };
  return { value: b.scores.get(key)?.[m] ?? null, details: {}, fragments: [], reason: null };
}

export function listSubmissions(batchId: string): Submission[] | undefined {
  const b = batches.get(batchId);
  if (!b) return undefined;
  return b.submissions.map((s) => {
    const methods: Submission["methods"] = {};
    for (const m of METHODS) {
      if (runStatus(b, m) !== "done") {
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
    return { id: s.id, originalName: s.originalName, language: "java", loc: s.loc, methods };
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
    return { originalName: s.originalName, language: "java", loc: s.loc };
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
  createBatch("hw4-demo", demoItems(), 0, DEMO_BATCH_ID);
}

resetDb();