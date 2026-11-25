
import {
  describe,
  expect,
  it,
} from "vitest";

import { ApiError, api } from "../../api/target/client";
import {
  MOCK_IDS,
  mockSubmissions,
} from "../../mocks/target-fixtures";

describe("API управления заданиями", () => {
  it("созданное задание имеет пустой список работ", async () => {
    const created = await api.createAssignment({
      title: "Новое задание",
      sourceAssignmentIds: [],
    });

    const submissions = await api.listSubmissions(
      created.id,
    );

    expect(submissions.total).toBe(0);
    expect(submissions.items).toEqual([]);
  });

  it("изменяет задания-источники", async () => {
    const updated = await api.updateAssignmentSources(
      MOCK_IDS.success,
      [MOCK_IDS.empty],
    );

    expect(updated.sourceAssignmentIds).toEqual([
      MOCK_IDS.empty,
    ]);

    const loaded = await api.getAssignment(
      MOCK_IDS.success,
    );

    expect(loaded.sourceAssignmentIds).toEqual([
      MOCK_IDS.empty,
    ]);
  });

  it("удаляет задание из списка", async () => {
    const created = await api.createAssignment({
      title: "Задание для удаления",
      sourceAssignmentIds: [],
    });

    await api.deleteAssignment(created.id);

    const assignments = await api.listAssignments();

    expect(
      assignments.some(
        (assignment) => assignment.id === created.id,
      ),
    ).toBe(false);

    await expect(
      api.getAssignment(created.id),
    ).rejects.toMatchObject({
      status: 404,
    });
  });

  it("сбрасывает попытку с правильной сдачей", async () => {
    const submission =
      mockSubmissions[MOCK_IDS.success]?.items[0];

    if (!submission) {
      throw new Error("Нет тестовой сдачи");
    }

    await expect(
      api.resetStudentAttempt(
        MOCK_IDS.success,
        submission.studentId,
        submission.submissionId,
      ),
    ).resolves.toBeUndefined();
  });

  it("возвращает 409 при устаревшем ID сдачи", async () => {
    const submission =
      mockSubmissions[MOCK_IDS.success]?.items[0];

    if (!submission) {
      throw new Error("Нет тестовой сдачи");
    }

    const wrongSubmissionId =
      "00000000-0000-4000-8000-000000009999";

    try {
      await api.resetStudentAttempt(
        MOCK_IDS.success,
        submission.studentId,
        wrongSubmissionId,
      );

      throw new Error("Ожидалась ошибка 409");
    } catch (error) {
      expect(error).toBeInstanceOf(ApiError);
      expect(error).toMatchObject({
        status: 409,
        code: "SUBMISSION_CHANGED",
      });
    }
  });
});
