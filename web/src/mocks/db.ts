import type { Batch, Feedback, PairDetail, PairSummary, Stage } from "../api/types";
import { DEMO_BATCH_ID } from "../lib/constants";
import { COURSE_TEMPLATE, DEMO_CORPUS, type CorpusItem } from "./corpus";
import { analyze, explain, prepare, type Prepared } from "./engine";

type StoredPair = Omit<PairDetail, "rank" | "submissionA" | "submissionB" | "feedback" | "decision">;

interface StoredBatch {
  batch: Batch;
  prepared: Map<string, Prepared>;
  pairs: StoredPair[];
  readyAt: number;
}

const STAGE_NAMES = [
  "Распаковка архива и псевдонимизация",
  "Удаление шаблона курса, импортов и комментариев",
  "Baseline: токенные 3-граммы",
  "Структурное сходство (нормализованные 5-граммы)",
  "Поиск фрагментов и risk score",
];

const batches = new Map<string, StoredBatch>();
const feedback = new Map<string, Feedback>();

export const pairId = (batchId: string, a: string, b: string) => `${batchId}:${a}:${b}`;

export function createBatch(
  meta: { id?: string; course: string; assignment: string },
  items: CorpusItem[],
  processingMs = 0,
): Batch {
  const id = meta.id ?? `b-${Date.now().toString(36)}`;
  const prepared = new Map(items.map((s) => [s.id, prepare(s.code, COURSE_TEMPLATE)]));
  const t0 = performance.now();
  const pairs: StoredPair[] = [];
  for (let i = 0; i < items.length; i++)
    for (let j = i + 1; j < items.length; j++) {
      const a = items[i]!;
      const b = items[j]!;
      const r = analyze(a.id, prepared.get(a.id)!, b.id, prepared.get(b.id)!);
      pairs.push({
        id: pairId(id, a.id, b.id),
        batchId: id,
        a: a.id,
        b: b.id,
        risk: r.risk,
        signals: r.signals,
        fragmentCount: r.fragments.length,
        fragments: r.fragments,
        renames: r.renames,
        explanation: explain(r.signals, r.fragments.length),
      });
    }
  pairs.sort((x, y) => y.risk - x.risk);
  const elapsed = performance.now() - t0;
  const stages: Stage[] = STAGE_NAMES.map((name, k) => ({
    name,
    status: "done",
    durationMs: +(elapsed * [0.1, 0.15, 0.2, 0.35, 0.2][k]!).toFixed(2),
  }));
  const batch: Batch = {
    id,
    course: meta.course,
    assignment: meta.assignment,
    language: "java",
    status: "done",
    stages,
    submissions: items.map((s) => ({
      id: s.id,
      fileName: s.fileName,
      submittedAt: s.submittedAt,
      loc: prepared.get(s.id)!.sig.length,
    })),
    pairCount: pairs.length,
    createdAt: new Date().toISOString(),
  };
  batches.set(id, { batch, prepared, pairs, readyAt: Date.now() + processingMs });
  return currentBatch(id)!;
}

export function currentBatch(id: string): Batch | undefined {
  const s = batches.get(id);
  if (!s) return undefined;
  const left = s.readyAt - Date.now();
  if (left <= 0) return s.batch;
  const total = Math.max(1, s.readyAt - new Date(s.batch.createdAt).getTime());
  const doneCount = Math.floor((1 - left / total) * s.batch.stages.length);
  return {
    ...s.batch,
    status: "processing",
    stages: s.batch.stages.map((st, k) =>
      k < doneCount ? st : { name: st.name, status: k === doneCount ? "running" : "pending" },
    ),
  };
}

export const listBatches = () => [...batches.keys()].map((id) => currentBatch(id)!);

export function listPairs(batchId: string): PairSummary[] | undefined {
  const s = batches.get(batchId);
  if (!s) return undefined;
  return s.pairs.map((p) => ({
    id: p.id,
    a: p.a,
    b: p.b,
    risk: p.risk,
    signals: p.signals,
    fragmentCount: p.fragmentCount,
    decision: feedback.get(p.id)?.decision ?? null,
  }));
}

export function getPair(id: string): PairDetail | undefined {
  const batchId = id.split(":")[0]!;
  const s = batches.get(batchId);
  const idx = s?.pairs.findIndex((p) => p.id === id) ?? -1;
  if (!s || idx < 0) return undefined;
  const p = s.pairs[idx]!;
  const sub = (sid: string) => ({
    ...s.batch.submissions.find((x) => x.id === sid)!,
    lines: s.prepared.get(sid)!.lines,
  });
  const fb = feedback.get(id) ?? null;
  return { ...p, rank: idx + 1, submissionA: sub(p.a), submissionB: sub(p.b), feedback: fb, decision: fb?.decision ?? null };
}

export function saveFeedback(id: string, input: Omit<Feedback, "updatedAt">): Feedback {
  const fb: Feedback = { ...input, updatedAt: new Date().toISOString() };
  feedback.set(id, fb);
  return fb;
}

export const hasPair = (id: string) => !!getPair(id);

export function resetDb() {
  batches.clear();
  feedback.clear();
  createBatch({ id: DEMO_BATCH_ID, course: "Алгоритмы, поток 2", assignment: "ДЗ-4. Обход графа в ширину" }, DEMO_CORPUS);
}

resetDb();