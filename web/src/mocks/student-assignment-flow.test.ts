
import {
  describe,
  expect,
  it,
} from "vitest";

import { api } from "../api/target/client";

import {
  getSubmissionForm,
  submitJavaFile,
} from "../api/target/students";

import {
  DEMO_STUDENT_ID,
} from "./student-submission-store";

function javaFile(
  name: string,
  text: string,
): File {
  return new File(
    [text],
    name,
    {
      type: "text/plain",
    },
  );
}

async function createTestAssignment() {
  const assignment =
    await api.createAssignment({
      title: "Тест полного цикла",
      sourceAssignmentIds: [],
    });

  const token =
    new URL(
      assignment.submissionUrl,
    ).pathname.split("/").at(-1);

  if (!token) {
    throw new Error(
      "Не удалось получить токен задания",
    );
  }

  return {
    assignment,
    token,
  };
}

describe("Полный цикл студенческой работы", () => {
  it("показывает новую работу преподавателю", async () => {
    const {
      assignment,
      token,
    } = await createTestAssignment();

    const receipt =
      await submitJavaFile(
        token,
        javaFile(
          "Solution.java",
          "public class Solution {}",
        ),
      );

    const updated =
      await api.getAssignment(
        assignment.id,
      );

    expect(
      updated.submissionCount,
    ).toBe(1);

    const list =
      await api.listSubmissions(
        assignment.id,
      );

    expect(list.total).toBe(1);
    expect(list.items).toHaveLength(1);

    expect(
      list.items[0]?.submissionId,
    ).toBe(
      receipt.submissionId,
    );

    expect(
      list.items[0]?.studentId,
    ).toBe(
      DEMO_STUDENT_ID,
    );

    expect(
      list.items[0]?.similarity.status,
    ).toBe("pending");

    expect(
      list.items[0]?.ai.status,
    ).toBe("pending");
  });

  it("разрешает повторную сдачу после сброса преподавателем", async () => {
    const {
      assignment,
      token,
    } = await createTestAssignment();

    const first =
      await submitJavaFile(
        token,
        javaFile(
          "First.java",
          "public class First {}",
        ),
      );

    await api.resetStudentAttempt(
      assignment.id,
      DEMO_STUDENT_ID,
      first.submissionId,
    );

    const reopened =
      await getSubmissionForm(
        token,
      );

    expect(
      reopened.canSubmit,
    ).toBe(true);

    expect(
      reopened.currentSubmission,
    ).toEqual(first);

    const second =
      await submitJavaFile(
        token,
        javaFile(
          "Second.java",
          "public class Second {}",
        ),
      );

    expect(
      second.submissionId,
    ).not.toBe(
      first.submissionId,
    );

    const closed =
      await getSubmissionForm(
        token,
      );

    expect(
      closed.canSubmit,
    ).toBe(false);

    expect(
      closed.currentSubmission,
    ).toEqual(second);

    const list =
      await api.listSubmissions(
        assignment.id,
      );

    expect(
      list.total,
    ).toBe(1);

    expect(
      list.items[0]?.submissionId,
    ).toBe(
      second.submissionId,
    );
  });

  it("не сбрасывает попытку при устаревшем submissionId", async () => {
    const {
      assignment,
      token,
    } = await createTestAssignment();

    await submitJavaFile(
      token,
      javaFile(
        "Solution.java",
        "public class Solution {}",
      ),
    );

    await expect(
      api.resetStudentAttempt(
        assignment.id,
        DEMO_STUDENT_ID,
        "00000000-0000-4000-8000-999999999999",
      ),
    ).rejects.toMatchObject({
      status: 409,
      code: "SUBMISSION_CHANGED",
    });

    const form =
      await getSubmissionForm(
        token,
      );

    expect(
      form.canSubmit,
    ).toBe(false);
  });
});
