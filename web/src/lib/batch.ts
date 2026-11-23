import type { Batch, Run } from "../api/types";

const inProgress = (status: Run["status"]) => status === "queued" || status === "running";

/**
 * Очередь и сравнение открываются, как только готов любой из методов сравнения:
 * AI считается минутами, и его ожидание или ошибка не скрывают их результаты.
 */
export function batchProgress(batch: Batch | undefined) {
  const runs = batch?.runs ?? [];
  const comparison = runs.filter((r) => r.method !== "ai");
  return {
    // Готов хотя бы один метод сравнения.
    ready: comparison.some((r) => r.status === "done"),
    // Какой-то из методов сравнения ещё идёт: его оценки появятся позже.
    comparing: comparison.some((r) => inProgress(r.status)),
    aiPending: runs.some((r) => r.method === "ai" && inProgress(r.status)),
  };
}
