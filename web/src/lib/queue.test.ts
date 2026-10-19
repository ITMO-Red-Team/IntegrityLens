import { describe, expect, it } from "vitest";
import type { PairSummary } from "../api/types";
import { clusters, queueMetrics, reviewQueue, riskLevel, scorePct, sortPairs, suggestThreshold } from "./queue";

const score = (value: number | null) => ({
  value,
  details: {},
  fragments: [],
  reason: value === null ? { code: "NOT_PARSED", message: "не разобрано" } : null,
});

const p = (id: string, jplag: number | null, structsim: number | null = jplag, decision: PairSummary["decision"] = null): PairSummary => ({
  id,
  a: `${id}a`,
  b: `${id}b`,
  scores: { jplag: score(jplag), structsim: score(structsim) },
  decision,
});

describe("оценки и сортировка", () => {
  it("переводит оценку 0..1 в проценты, null остаётся null", () => {
    expect(scorePct(p("x", 0.456), "jplag")).toBe(46);
    expect(scorePct(p("x", null), "jplag")).toBeNull();
  });

  it("сортирует по выбранному методу, пары без оценки — в конце", () => {
    const pairs = [p("a", 0.2, 0.9), p("b", null, 0.1), p("c", 0.8, 0.3)];
    expect(sortPairs(pairs, "jplag").map((x) => x.id)).toEqual(["c", "a", "b"]);
    expect(sortPairs(pairs, "structsim").map((x) => x.id)).toEqual(["a", "c", "b"]);
  });
});

describe("очередь", () => {
  const pairs = [p("x", 0.4), p("y", 0.9, 0.9, "confirmed"), p("z", 0.6, 0.6, "false_positive"), p("w", 0.75, 0.75, "confirmed"), p("v", null)];

  it("берёт верх K выше порога, пары без оценки не попадают", () => {
    expect(reviewQueue(pairs, "jplag", 45, 2).map((x) => x.id)).toEqual(["y", "w"]);
    expect(reviewQueue(pairs, "jplag", 45, 10).map((x) => x.id)).toEqual(["y", "w", "z"]);
  });

  it("считает точность очереди и долю ложных срабатываний", () => {
    const m = queueMetrics(pairs, "jplag", 45, 10);
    expect(m.queued).toBe(3);
    expect(m.precisionAtK).toBeCloseTo(2 / 3);
    expect(m.falseAlarmRate).toBeCloseTo(1 / 3);
  });

  it("без отзывов метрики качества не выдумываются", () => {
    const m = queueMetrics([p("a", 0.8), p("b", 0.5)], "jplag", 45, 5);
    expect(m.precisionAtK).toBeNull();
    expect(m.falseAlarmRate).toBeNull();
  });

  it("предлагает порог над ложным срабатыванием, не теряя подтверждённых", () => {
    expect(suggestThreshold(pairs, "jplag")).toBe(61);
    expect(suggestThreshold([p("a", 0.5, 0.5, "confirmed"), p("b", 0.6, 0.6, "false_positive")], "jplag")).toBeNull();
  });

  it("собирает кластеры по парам выше порога", () => {
    const ids = ["1", "2", "3", "4"];
    const edges: PairSummary[] = [
      { ...p("e1", 0.9), a: "1", b: "2" },
      { ...p("e2", 0.8), a: "2", b: "3" },
      { ...p("e3", 0.1), a: "3", b: "4" },
    ];
    expect(clusters(ids, edges, "jplag", 45)).toEqual([["1", "2", "3"]]);
  });

  it("уровень риска зависит от порога", () => {
    expect(riskLevel(80, 45)).toBe("high");
    expect(riskLevel(50, 45)).toBe("mid");
    expect(riskLevel(30, 45)).toBe("low");
  });
});