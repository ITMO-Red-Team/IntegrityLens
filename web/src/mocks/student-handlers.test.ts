
import {
  describe,
  expect,
  it,
} from "vitest";

import {
  getSubmissionForm,
  submitJavaFile,
} from "../api/target/students";

import {
  openMockStudentAttempt,
} from "./student-handlers";

import { MOCK_IDS } from "./target-fixtures";

function javaFile(name = "Solution.java") {
  return new File(
    ["public class Solution {}\n"],
    name,
    { type: "text/plain" },
  );
}

describe("Моки студенческой сдачи", () => {
  it("принимает Java-файл и закрывает попытку", async () => {
    const token = MOCK_IDS.empty;

    const before = await getSubmissionForm(token);

    expect(before.canSubmit).toBe(true);
    expect(before.currentSubmission).toBeNull();

    const receipt = await submitJavaFile(
      token,
      javaFile(),
    );

    expect(receipt.submissionId).toBeTruthy();

    const after = await getSubmissionForm(token);

    expect(after.canSubmit).toBe(false);
    expect(after.currentSubmission).toEqual(receipt);
  });

  it("отклоняет повторную сдачу без сброса", async () => {
    const token = MOCK_IDS.empty;

    await submitJavaFile(token, javaFile());

    await expect(
      submitJavaFile(token, javaFile()),
    ).rejects.toMatchObject({
      status: 409,
      code: "SUBMISSION_ALREADY_ACCEPTED",
    });
  });

  it("разрешает повторную сдачу после открытия попытки", async () => {
    const token = MOCK_IDS.empty;

    const first = await submitJavaFile(
      token,
      javaFile(),
    );

    openMockStudentAttempt(token);

    const reopened = await getSubmissionForm(token);

    expect(reopened.canSubmit).toBe(true);
    expect(reopened.currentSubmission).toEqual(first);

    const second = await submitJavaFile(
      token,
      javaFile("Second.java"),
    );

    expect(second.submissionId).not.toBe(
      first.submissionId,
    );

    const closed = await getSubmissionForm(token);

    expect(closed.canSubmit).toBe(false);
    expect(closed.currentSubmission).toEqual(second);
  });
});
