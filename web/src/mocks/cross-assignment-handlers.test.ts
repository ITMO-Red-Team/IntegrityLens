
import {
  describe,
  expect,
  it,
} from "vitest";

import { api } from "../api/target/client";
import {
  submitJavaFile,
} from "../api/target/students";

import {
  MOCK_IDS,
  mockSubmissions,
} from "./target-fixtures";

const first =
  mockSubmissions[MOCK_IDS.success]
    ?.items[0]?.submissionId;

const source =
  mockSubmissions[MOCK_IDS.pending]
    ?.items[0]?.submissionId;

if (!first || !source) {
  throw new Error(
    "Не найдены демонстрационные работы",
  );
}

describe("Сравнение с заданиями-источниками", () => {
  it("сравнивает работы из двух разных заданий", async () => {
    await api.updateAssignmentSources(
      MOCK_IDS.success,
      [MOCK_IDS.pending],
    );

    const details = await api.getSubmission(
      MOCK_IDS.success,
      first,
    );

    expect(details.similar).toEqual(
      expect.arrayContaining([
        expect.objectContaining({
          submissionId: source,
          assignmentId: MOCK_IDS.pending,
          assignmentTitle:
            "Проверка выполняется",
        }),
      ]),
    );

    const pair = await api.getPair(
      MOCK_IDS.success,
      first,
      source,
    );

    expect(pair.a).toBe(first);
    expect(pair.b).toBe(source);
    expect(pair.score.status).toBe("ok");

    const reversed = await api.getPair(
      MOCK_IDS.success,
      source,
      first,
    );

    expect(reversed.a).toBe(source);
    expect(reversed.b).toBe(first);
  });

  it("запрещает сравнение с невыбранным заданием", async () => {
    await expect(
      api.getPair(
        MOCK_IDS.success,
        first,
        source,
      ),
    ).rejects.toMatchObject({
      status: 404,
      code: "NOT_FOUND",
    });
  });

  it("показывает ожидающий анализ для новой работы", async () => {
    const assignment =
      await api.createAssignment({
        title: "Сравнение с прошлым потоком",
        sourceAssignmentIds: [
          MOCK_IDS.success,
        ],
      });

    const token = new URL(
      assignment.submissionUrl,
    ).pathname.split("/").at(-1);

    if (!token) {
      throw new Error(
        "Отсутствует токен задания",
      );
    }

    const receipt = await submitJavaFile(
      token,
      new File(
        ["public class Solution {}"],
        "Solution.java",
        { type: "text/plain" },
      ),
    );

    const detail = await api.getSubmission(
      assignment.id,
      receipt.submissionId,
    );

    expect(
      detail.similar.some(
        (item) =>
          item.submissionId === first &&
          item.assignmentId ===
            MOCK_IDS.success,
      ),
    ).toBe(true);

    const pair = await api.getPair(
      assignment.id,
      receipt.submissionId,
      first,
    );

    expect(pair.score.status).toBe(
      "pending",
    );

    expect(
      pair.methods.structsim.fragments,
    ).toEqual([]);
  });
});
