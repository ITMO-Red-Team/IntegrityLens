
import {
  describe,
  expect,
  it,
} from "vitest";

import {
  api,
} from "../api/target/client";

import {
  getSubmissionForm,
  submitJavaFile,
} from "../api/target/students";

function tokenFromUrl(url: string): string {
  const parsed = new URL(url);

  const parts = parsed.pathname.split("/");
  const token = parts.at(-1);

  if (!token) {
    throw new Error(
      "В ссылке отсутствует токен сдачи",
    );
  }

  return token;
}

function javaFile(): File {
  return new File(
    [
      "public class StudentSolution {\n",
      "    public static void main(String[] args) {}\n",
      "}\n",
    ],
    "StudentSolution.java",
    {
      type: "text/plain",
    },
  );
}

describe("Сдача по ссылке нового задания", () => {
  it("открывает форму и принимает Java-файл", async () => {
    const created =
      await api.createAssignment({
        title: "Лабораторная работа №1",
        sourceAssignmentIds: [],
      });

    const token = tokenFromUrl(
      created.submissionUrl,
    );

    expect(token).toBe(
      `mock-${created.id}`,
    );

    const form =
      await getSubmissionForm(
        token,
      );

    expect(form.title).toBe(
      "Лабораторная работа №1",
    );

    expect(form.canSubmit).toBe(
      true,
    );

    expect(
      form.currentSubmission,
    ).toBeNull();

    const receipt =
      await submitJavaFile(
        token,
        javaFile(),
      );

    expect(
      receipt.submissionId,
    ).toBeTruthy();

    const after =
      await getSubmissionForm(
        token,
      );

    expect(after.canSubmit).toBe(
      false,
    );

    expect(
      after.currentSubmission,
    ).toEqual(receipt);
  });

  it("закрывает ссылку после удаления задания", async () => {
    const created =
      await api.createAssignment({
        title: "Временное задание",
        sourceAssignmentIds: [],
      });

    const token = tokenFromUrl(
      created.submissionUrl,
    );

    await api.deleteAssignment(
      created.id,
    );

    await expect(
      getSubmissionForm(token),
    ).rejects.toMatchObject({
      status: 404,
    });
  });
});
