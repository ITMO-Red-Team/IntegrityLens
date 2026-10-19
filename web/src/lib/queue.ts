import type { Decision, Method, PairSummary } from "../api/types";

export type RiskLevel = "high" | "mid" | "low";
export const HIGH_RISK = 70;

export const riskLevel = (percent: number, threshold: number): RiskLevel =>
  percent >= Math.max(HIGH_RISK, threshold) ? "high" : percent >= threshold ? "mid" : "low";

export const LEVEL_LABEL: Record<RiskLevel, string> = { high: "высокий", mid: "средний", low: "низкий" };

export const DECISION_LABEL: Record<Decision, string> = {
  confirmed: "подтверждено",
  acceptable: "допустимо",
  false_positive: "ложное",
};

export function scorePct(p: PairSummary, method: Method): number | null {
  const v = p.scores[method]?.value;
  return v === null || v === undefined ? null : Math.round(v * 100);
}

export function sortPairs(pairs: PairSummary[], method: Method): PairSummary[] {
  return [...pairs].sort((x, y) => {
    const a = x.scores[method]?.value ?? -1;
    const b = y.scores[method]?.value ?? -1;
    return b - a || x.id.localeCompare(y.id);
  });
}

export function reviewQueue(pairs: PairSummary[], method: Method, threshold: number, k: number) {
  return sortPairs(pairs, method)
    .filter((p) => (scorePct(p, method) ?? -1) >= threshold)
    .slice(0, k);
}

export interface QueueMetrics {
  queued: number;
  rated: number;
  precisionAtK: number | null;
  falseAlarmRate: number | null;
}

export function queueMetrics(pairs: PairSummary[], method: Method, threshold: number, k: number): QueueMetrics {
  const q = reviewQueue(pairs, method, threshold, k);
  const rated = q.filter((p) => p.decision);
  const confirmed = rated.filter((p) => p.decision === "confirmed").length;
  const fp = rated.filter((p) => p.decision === "false_positive").length;
  return {
    queued: q.length,
    rated: pairs.filter((p) => p.decision).length,
    precisionAtK: rated.length ? confirmed / rated.length : null,
    falseAlarmRate: rated.length ? fp / rated.length : null,
  };
}

export function suggestThreshold(pairs: PairSummary[], method: Method): number | null {
  const of = (d: Decision) =>
    pairs.filter((p) => p.decision === d).map((p) => scorePct(p, method)).filter((v): v is number => v !== null);
  const confirmed = of("confirmed");
  const fps = of("false_positive");
  if (!fps.length) return null;
  const candidate = Math.max(...fps) + 1;
  if (confirmed.length && candidate > Math.min(...confirmed)) return null;
  return Math.min(100, candidate);
}

export function clusters(ids: string[], pairs: PairSummary[], method: Method, threshold: number): string[][] {
  const parent = new Map(ids.map((id) => [id, id]));
  const find = (x: string): string => {
    const p = parent.get(x) ?? x;
    if (p === x) return x;
    const r = find(p);
    parent.set(x, r);
    return r;
  };
  pairs
    .filter((p) => (scorePct(p, method) ?? -1) >= threshold)
    .forEach((p) => parent.set(find(p.a), find(p.b)));
  const groups = new Map<string, string[]>();
  ids.forEach((id) => {
    const r = find(id);
    groups.set(r, [...(groups.get(r) ?? []), id]);
  });
  return [...groups.values()].filter((g) => g.length > 1);
}