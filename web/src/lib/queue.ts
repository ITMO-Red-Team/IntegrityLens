import type { Decision, PairSummary } from "../api/types";

export type RiskLevel = "high" | "mid" | "low";
export const HIGH_RISK = 70;

export const riskLevel = (risk: number, threshold: number): RiskLevel =>
  risk >= Math.max(HIGH_RISK, threshold) ? "high" : risk >= threshold ? "mid" : "low";

export const LEVEL_LABEL: Record<RiskLevel, string> = { high: "высокий", mid: "средний", low: "низкий" };

export const DECISION_LABEL: Record<Decision, string> = {
  confirmed: "подтверждено",
  acceptable: "допустимо",
  false_positive: "ложное",
};

export function reviewQueue(pairs: PairSummary[], threshold: number, k: number) {
  return [...pairs].sort((a, b) => b.risk - a.risk).filter((p) => p.risk >= threshold).slice(0, k);
}

export interface QueueMetrics {
  queued: number;
  workloadReduction: number;
  rated: number;
  precisionAtK: number | null;
  falseAlarmRate: number | null;
}

export function queueMetrics(pairs: PairSummary[], threshold: number, k: number): QueueMetrics {
  const q = reviewQueue(pairs, threshold, k);
  const rated = q.filter((p) => p.decision);
  const confirmed = rated.filter((p) => p.decision === "confirmed").length;
  const fp = rated.filter((p) => p.decision === "false_positive").length;
  return {
    queued: q.length,
    workloadReduction: pairs.length ? 1 - q.length / pairs.length : 0,
    rated: pairs.filter((p) => p.decision).length,
    precisionAtK: rated.length ? confirmed / rated.length : null,
    falseAlarmRate: rated.length ? fp / rated.length : null,
  };
}

export function suggestThreshold(pairs: PairSummary[]): number | null {
  const confirmed = pairs.filter((p) => p.decision === "confirmed").map((p) => p.risk);
  const fps = pairs.filter((p) => p.decision === "false_positive").map((p) => p.risk);
  if (!fps.length) return null;
  const candidate = Math.max(...fps) + 1;
  if (confirmed.length && candidate > Math.min(...confirmed)) return null;
  return Math.min(100, candidate);
}

export const EXPORT_COLUMNS = [
  "rank",
  "pair_id",
  "submission_a",
  "submission_b",
  "risk",
  "level",
  "structure",
  "baseline",
  "coverage",
  "embedding",
  "fragments",
  "decision",
] as const;

export function exportRows(pairs: PairSummary[], threshold: number, k: number) {
  return reviewQueue(pairs, threshold, k).map((p, i) => ({
    rank: i + 1,
    pair_id: p.id,
    submission_a: p.a,
    submission_b: p.b,
    risk: p.risk,
    level: riskLevel(p.risk, threshold),
    structure: +p.signals.structure.toFixed(3),
    baseline: +p.signals.baseline.toFixed(3),
    coverage: +p.signals.coverage.toFixed(3),
    embedding: +p.signals.embedding.toFixed(3),
    fragments: p.fragmentCount,
    decision: p.decision ?? "",
  }));
}

const csvCell = (v: unknown) => {
  const s = String(v);
  return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
};

export function toCsv(rows: ReturnType<typeof exportRows>) {
  return [EXPORT_COLUMNS.join(","), ...rows.map((r) => EXPORT_COLUMNS.map((c) => csvCell(r[c])).join(","))].join("\n");
}