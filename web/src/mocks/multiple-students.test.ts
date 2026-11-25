
import {
  describe,
  expect,
  it,
} from "vitest";

import {
  authApi,
} from "../api/target/auth";

import {
  api,
} from "../api/target/client";

import {
  getSubmissionForm,
  submitJavaFile,
} from "../api/target/students";

function javaFile(name: string) {
  return new File(
    [`public class ${name} {}\n`],
    `${name}.java`,
    { type: "text/plain" },
  );
}

async function createStudent(
  fullName: string,
  email: string,
) {
  return authApi.register({
    fullName,
    email,
    password: "password123",
    role: "student",
  });
}

async function login(email: string) {
  return authApi.login({
    email,
    password: "password123",
  });
}

async function createAssignment() {
  const assignment =
    await api.createAssignment({
      title: "Работы двух студентов",
      sourceAssignmentIds: [],
    });

  const token = new URL(
    assignment.submissionUrl,
  ).pathname.split("/").at(-1);

  if (!token) {
    throw new Error(
      "Не удалось получить ссылку сдачи",
    );
  }

  return {
    assignment,
    token,
  };
}

describe("Изоляция попыток разных студентов", () => {
  it("принимает независимые сдачи двух студентов", async () => {
    const firstStudent =
      await createStudent(
        "Иванов Иван",
        "first-student@example.com",
      );

    const secondStudent =
      await createStudent(
        "Петров Пётр",
        "second-student@example.com",
      );

    const {
      assignment,
      token,
    } = await createAssignment();

    await login(
      "first-student@example.com",
    );

    const firstReceipt =
      await submitJavaFile(
        token,
        javaFile("FirstSolution"),
      );

    expect(
      (await getSubmissionForm(token)).canSubmit,
    ).toBe(false);

    await login(
      "second-student@example.com",
    );

    const secondBefore =
      await getSubmissionForm(token);

    expect(secondBefore.canSubmit).toBe(true);
    expect(
      secondBefore.currentSubmission,
    ).toBeNull();

    const secondReceipt =
      await submitJavaFile(
        token,
        javaFile("SecondSolution"),
      );

    expect(
      secondReceipt.submissionId,
    ).not.toBe(
      firstReceipt.submissionId,
    );

    const list =
      await api.listSubmissions(
        assignment.id,
      );

    expect(list.total).toBe(2);

    expect(list.items).toEqual(
      expect.arrayContaining([
        expect.objectContaining({
          studentId:
            firstStudent.id,
          submissionId:
            firstReceipt.submissionId,
          fullName: "Иванов Иван",
        }),
        expect.objectContaining({
          studentId:
            secondStudent.id,
          submissionId:
            secondReceipt.submissionId,
          fullName: "Петров Пётр",
        }),
      ]),
    );

    await login(
      "first-student@example.com",
    );

    const firstAgain =
      await getSubmissionForm(token);

    expect(firstAgain.canSubmit).toBe(false);
    expect(
      firstAgain.currentSubmission,
    ).toEqual(firstReceipt);
  });

  it("сброс попытки одного студента не затрагивает другого", async () => {
    const firstStudent =
      await createStudent(
        "Первый Студент",
        "reset-first@example.com",
      );

    await createStudent(
      "Второй Студент",
      "reset-second@example.com",
    );

    const {
      assignment,
      token,
    } = await createAssignment();

    await login(
      "reset-first@example.com",
    );

    const firstReceipt =
      await submitJavaFile(
        token,
        javaFile("First"),
      );

    await login(
      "reset-second@example.com",
    );

    const secondReceipt =
      await submitJavaFile(
        token,
        javaFile("Second"),
      );

    await api.resetStudentAttempt(
      assignment.id,
      firstStudent.id,
      firstReceipt.submissionId,
    );

    // Второй студент по-прежнему
    // не может отправить работу повторно.
    expect(
      (await getSubmissionForm(token)).canSubmit,
    ).toBe(false);

    await expect(
      submitJavaFile(
        token,
        javaFile("SecondAgain"),
      ),
    ).rejects.toMatchObject({
      status: 409,
      code: "SUBMISSION_ALREADY_ACCEPTED",
    });

    await login(
      "reset-first@example.com",
    );

    const reopened =
      await getSubmissionForm(token);

    expect(reopened.canSubmit).toBe(true);
    expect(
      reopened.currentSubmission,
    ).toEqual(firstReceipt);

    const replacement =
      await submitJavaFile(
        token,
        javaFile("FirstAgain"),
      );

    expect(
      replacement.submissionId,
    ).not.toBe(
      firstReceipt.submissionId,
    );

    const list =
      await api.listSubmissions(
        assignment.id,
      );

    expect(list.total).toBe(2);

    expect(
      list.items.some(
        (item) =>
          item.submissionId ===
          secondReceipt.submissionId,
      ),
    ).toBe(true);

    expect(
      list.items.some(
        (item) =>
          item.submissionId ===
          firstReceipt.submissionId,
      ),
    ).toBe(false);
  });
});
