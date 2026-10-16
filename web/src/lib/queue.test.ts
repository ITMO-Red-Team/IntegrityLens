import { describe, expect, it } from "vitest";
import type { PairSummary } from "../api/types";
import { queueMetrics, reviewQueue, riskLevel, suggestThreshold } from "./queue";

const p = (id: string, risk: number, decision: PairSummary["decision"] = null): PairSummary => ({
  id,
  a: `${id}a`,
  b: `${id}b`,
  risk,
  fragmentCount: 1,
  decision,
  signals: { baseline: 0.1, structure: 0.5, coverage: 0.5, embedding: 0.6 },
});

const pairs = [p("x", 40), p("y", 90, "confirmed"), p("z", 60, "false_positive"), p("w", 75, "confirmed"), p("v", 20)];

describe("очередь", () => {
  it("берёт верх K выше порога по убыванию риска", () => {
    expect(reviewQueue(pairs, 45, 2).map((x) => x.id)).toEqual(["y", "w"]);
    expect(reviewQueue(pairs, 45, 10).map((x) => x.id)).toEqual(["y", "w", "z"]);
  });

  it("считает экономию проверки, precision@K и долю ложных срабатываний", () => {
    const m = queueMetrics(pairs, 45, 10);
    expect(m.queued).toBe(3);
    expect(m.workloadReduction).toBeCloseTo(0.4);
    expect(m.precisionAtK).toBeCloseTo(2 / 3);
    expect(m.falseAlarmRate).toBeCloseTo(1 / 3);
  });

  it("без отзывов метрики качества не выдумываются", () => {
    const m = queueMetrics([p("a", 80), p("b", 50)], 45, 5);
    expect(m.precisionAtK).toBeNull();
    expect(m.falseAlarmRate).toBeNull();
  });

  it("предлагает порог над ложным срабатыванием, не теряя подтверждённых", () => {
    expect(suggestThreshold(pairs)).toBe(61);
    expect(suggestThreshold([p("a", 50, "confirmed"), p("b", 60, "false_positive")])).toBeNull();
    expect(suggestThreshold([p("a", 50, "confirmed")])).toBeNull();
  });

  it("уровень риска зависит от порога", () => {
    expect(riskLevel(80, 45)).toBe("high");
    expect(riskLevel(50, 45)).toBe("mid");
    expect(riskLevel(30, 45)).toBe("low");
  });
});