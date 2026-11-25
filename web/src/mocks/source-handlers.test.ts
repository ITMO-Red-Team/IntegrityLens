
import {
  describe,
  expect,
  it,
} from "vitest";

import { api, ApiError } from "../api/target/client";
import {
  MOCK_IDS,
  mockSubmissions,
} from "./target-fixtures";

describe("Target API: исходный Java-код", () => {
  it("возвращает Java-код первой работы", async () => {
    const submission =
      mockSubmissions[MOCK_IDS.success]?.items[0];

    if (!submission) {
      throw new Error("Нет первой тестовой работы");
    }

    const source = await api.getSource(
      submission.submissionId,
    );

    expect(source).toContain(
      "public class BankTransfer",
    );

    expect(source).toContain(
      "synchronized",
    );
  });

  it("возвращает другой код для второй работы", async () => {
    const submissions =
      mockSubmissions[MOCK_IDS.success]?.items;

    if (!submissions || submissions.length < 2) {
      throw new Error("Недостаточно тестовых работ");
    }

    const first = submissions[0];
    const second = submissions[1];

    if (!first || !second) {
      throw new Error("Нет тестовых работ");
    }

    const firstSource = await api.getSource(
      first.submissionId,
    );

    const secondSource = await api.getSource(
      second.submissionId,
    );

    expect(secondSource).toContain(
      "public class MoneyService",
    );

    expect(firstSource).not.toBe(secondSource);
  });

  it("возвращает 404 для неизвестной работы", async () => {
    const unknownId =
      "00000000-0000-4000-8000-999999999999";

    await expect(
      api.getSource(unknownId),
    ).rejects.toBeInstanceOf(ApiError);

    await expect(
      api.getSource(unknownId),
    ).rejects.toMatchObject({
      status: 404,
      code: "NOT_FOUND",
    });
  });
});
