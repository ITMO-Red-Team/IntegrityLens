
import { describe, expect, it } from "vitest";

import {
  MOCK_IDS,
  mockAssignments,
} from "./target-fixtures";

import type {
  SubmissionList,
  SubmissionDetail,
  PairResult,
} from "../api/target/types";

const BASE = "http://localhost/api";

async function getJson<T>(path: string) {
  const response = await fetch(`${BASE}${path}`);

  return {
    status: response.status,
    body: (await response.json()) as T,
  };
}

describe("Target API: assignments", () => {
  it("returns assignments", async () => {
    const { status, body } = await getJson<
      typeof mockAssignments
    >("/assignments");

    expect(status).toBe(200);
    expect(body).toHaveLength(4);
    expect(body[0]?.id).toBe(MOCK_IDS.success);
  });

  it("returns empty assignments", async () => {
    const { status, body } = await getJson<
      typeof mockAssignments
    >("/assignments?mockScenario=empty");

    expect(status).toBe(200);
    expect(body).toEqual([]);
  });

  it("returns HTTP 503", async () => {
    const { status, body } = await getJson<{
      code: string;
      message: string;
    }>("/assignments?mockHttpError=1");

    expect(status).toBe(503);
    expect(body.code).toBe("SERVICE_UNAVAILABLE");
  });

  it("returns 404 for unknown assignment", async () => {
    const { status } = await getJson<unknown>(
      "/assignments/00000000-0000-4000-8000-999999999999",
    );

    expect(status).toBe(404);
  });
});

describe("Target API: submissions", () => {
  it("returns successful analysis", async () => {
    const { status, body } = await getJson<SubmissionList>(
      `/assignments/${MOCK_IDS.success}/submissions`,
    );

    expect(status).toBe(200);
    expect(body.total).toBe(2);
    expect(body.items[0]?.similarity.status).toBe("ok");
    expect(body.items[0]?.ai.status).toBe("ok");
  });

  it("returns empty submissions", async () => {
    const { body } = await getJson<SubmissionList>(
      `/assignments/${MOCK_IDS.empty}/submissions`,
    );

    expect(body.total).toBe(0);
    expect(body.items).toEqual([]);
  });

  it("returns pending analysis", async () => {
    const { body } = await getJson<SubmissionList>(
      `/assignments/${MOCK_IDS.pending}/submissions`,
    );

    expect(body.items[0]?.ai.status).toBe("pending");
    expect(body.items[0]?.ai.value).toBeNull();
  });

  it("returns failed analysis", async () => {
    const { body } = await getJson<SubmissionList>(
      `/assignments/${MOCK_IDS.error}/submissions`,
    );

    const score = body.items[0]?.similarity;

    expect(score?.status).toBe("failed");
    expect(score?.value).toBeNull();
    expect(score?.reason?.code).toBe("ANALYSIS_FAILED");
  });

  it("supports pagination", async () => {
    const { body } = await getJson<SubmissionList>(
      `/assignments/${MOCK_IDS.success}/submissions?page=1&pageSize=1`,
    );

    expect(body.page).toBe(1);
    expect(body.pageSize).toBe(1);
    expect(body.total).toBe(2);
    expect(body.items).toHaveLength(1);
  });

  it("rejects invalid pagination", async () => {
    const { status } = await getJson<unknown>(
      `/assignments/${MOCK_IDS.success}/submissions?page=0`,
    );

    expect(status).toBe(422);
  });

  it("rejects invalid sorting", async () => {
    const { status } = await getJson<unknown>(
      `/assignments/${MOCK_IDS.success}/submissions?sort=wrong`,
    );

    expect(status).toBe(422);
  });
});

describe("Target API: submission details", () => {
  it("returns submission details", async () => {
    const list = await getJson<SubmissionList>(
      `/assignments/${MOCK_IDS.success}/submissions`,
    );

    const id = list.body.items[0]!.submissionId;

    const { status, body } = await getJson<SubmissionDetail>(
      `/assignments/${MOCK_IDS.success}/submissions/${id}`,
    );

    expect(status).toBe(200);
    expect(body.submissionId).toBe(id);
    expect(body.suspicious).toBeDefined();
    expect(body.similar).toBeDefined();
  });

  it("has no suspicious ranges when AI is pending", async () => {
    const list = await getJson<SubmissionList>(
      `/assignments/${MOCK_IDS.pending}/submissions`,
    );

    const id = list.body.items[0]!.submissionId;

    const { body } = await getJson<SubmissionDetail>(
      `/assignments/${MOCK_IDS.pending}/submissions/${id}`,
    );

    expect(body.ai.status).toBe("pending");
    expect(body.suspicious).toEqual([]);
  });
});

describe("Target API: pairs", () => {
  it("returns successful pair comparison", async () => {
    const list = await getJson<SubmissionList>(
      `/assignments/${MOCK_IDS.success}/submissions`,
    );

    const [a, b] = list.body.items.map(
      (item) => item.submissionId,
    );

    const { status, body } = await getJson<PairResult>(
      `/assignments/${MOCK_IDS.success}/pairs/${a}/${b}`,
    );

    expect(status).toBe(200);
    expect(body.score.status).toBe("ok");
    expect(body.methods.structsim.fragments.length).toBeGreaterThan(0);
  });

  it("returns pending pair without fragments", async () => {
    const list = await getJson<SubmissionList>(
      `/assignments/${MOCK_IDS.success}/submissions`,
    );

    const [a, b] = list.body.items.map(
      (item) => item.submissionId,
    );

    const { body } = await getJson<PairResult>(
      `/assignments/${MOCK_IDS.success}/pairs/${a}/${b}?mockScenario=pending`,
    );

    expect(body.score.status).toBe("pending");
    expect(body.methods.structsim.fragments).toEqual([]);
  });

  it("returns failed pair without fragments", async () => {
    const list = await getJson<SubmissionList>(
      `/assignments/${MOCK_IDS.success}/submissions`,
    );

    const [a, b] = list.body.items.map(
      (item) => item.submissionId,
    );

    const { body } = await getJson<PairResult>(
      `/assignments/${MOCK_IDS.success}/pairs/${a}/${b}?mockScenario=error`,
    );

    expect(body.score.status).toBe("failed");
    expect(body.methods.structsim.fragments).toEqual([]);
  });
});
