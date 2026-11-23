import { describe, expect, it } from "vitest";
import type { Batch, Run } from "../api/types";
import { batchProgress } from "./batch";

const run = (method: Run["method"], status: Run["status"]): Run => ({
  method,
  status,
  methodVersion: null,
  startedAt: null,
  finishedAt: null,
  parsed: null,
  failed: null,
  error: null,
});

const batch = (...runs: Run[]): Batch => ({
  id: "b",
  title: "t",
  language: "java",
  status: "processing",
  createdAt: "",
  submissionCount: 5,
  pairCount: 10,
  runs,
});

describe("что уже можно смотреть в пакете", () => {
  it("сравнение готово, AI ещё идёт: результаты доступны", () => {
    const p = batchProgress(batch(run("jplag", "done"), run("structsim", "done"), run("ai", "running")));
    expect(p).toEqual({ ready: true, comparing: false, aiPending: true });
  });

  it("готов первый из методов сравнения: результаты доступны, второй догоняет", () => {
    const p = batchProgress(batch(run("jplag", "done"), run("structsim", "running"), run("ai", "queued")));
    expect(p).toEqual({ ready: true, comparing: true, aiPending: true });
  });

  it("ничего не готово: результаты закрыты", () => {
    const p = batchProgress(batch(run("jplag", "running"), run("structsim", "queued"), run("ai", "running")));
    expect(p).toEqual({ ready: false, comparing: true, aiPending: true });
  });

  it("готов только AI: очередь пар открывать не из чего", () => {
    const p = batchProgress(batch(run("jplag", "running"), run("structsim", "queued"), run("ai", "done")));
    expect(p).toEqual({ ready: false, comparing: true, aiPending: false });
  });

  it("ошибка одного метода сравнения не скрывает результат другого", () => {
    const p = batchProgress(batch(run("jplag", "failed"), run("structsim", "done"), run("ai", "failed")));
    expect(p).toEqual({ ready: true, comparing: false, aiPending: false });
  });

  it("сравнение не удалось, даже когда AI готов: результата нет", () => {
    const p = batchProgress(batch(run("jplag", "failed"), run("structsim", "failed"), run("ai", "done")));
    expect(p).toEqual({ ready: false, comparing: false, aiPending: false });
  });

  it("без прогона AI результат сравнения открывается", () => {
    const p = batchProgress(batch(run("jplag", "done"), run("structsim", "done")));
    expect(p).toEqual({ ready: true, comparing: false, aiPending: false });
  });

  it("пакет ещё не загружен", () => {
    expect(batchProgress(undefined)).toEqual({ ready: false, comparing: false, aiPending: false });
  });
});
