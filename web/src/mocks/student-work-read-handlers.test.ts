
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
  DEMO_STUDENT_ID,
} from "./student-submission-store";

function javaFile(
  source: string,
  name = "Solution.java",
): File {
  return new File(
    [source],
    name,
    { type: "text/plain" },
  );
}

async function createAssignment() {
  const assignment = await api.createAssignment({
    title: "Просмотр новой работы",
    sourceAssignmentIds: [],
  });

  const token = new URL(
    assignment.submissionUrl,
  ).pathname.split("/").at(-1);

  if (!token) {
    throw new Error(
      "Не найден токен сдачи",
    );
  }

  return {
    assignment,
    token,
  };
}

describe("Чтение студенческих работ", () => {
  it("возвращает детали и исходный Java-код", async () => {
    const {
      assignment,
      token,
    } = await createAssignment();

    const source = [
      "public class StudentSolution {",
      "    public static void main(String[] args) {",
      '        System.out.println("Привет!");',
      "    }",
      "}",
    ].join("\n");

    const receipt = await submitJavaFile(
      token,
      javaFile(source),
    );

    const detail = await api.getSubmission(
      assignment.id,
      receipt.submissionId,
    );

    expect(detail.studentId).toBe(
      DEMO_STUDENT_ID,
    );

    expect(detail.submissionId).toBe(
      receipt.submissionId,
    );

    expect(detail.similarity.status).toBe(
      "pending",
    );

    expect(detail.ai.status).toBe(
      "pending",
    );

    expect(detail.suspicious).toEqual([]);
    expect(detail.similar).toEqual([]);

    expect(
      await api.getSource(receipt.submissionId),
    ).toBe(source);
  });

  it("не открывает работу через чужое задание", async () => {
    const first = await createAssignment();
    const second = await createAssignment();

    const receipt = await submitJavaFile(
      first.token,
      javaFile("public class Example {}"),
    );

    await expect(
      api.getSubmission(
        second.assignment.id,
        receipt.submissionId,
      ),
    ).rejects.toMatchObject({
      status: 404,
      code: "NOT_FOUND",
    });
  });

  it("отклоняет просмотр заменённой сдачи", async () => {
    const {
      assignment,
      token,
    } = await createAssignment();

    const first = await submitJavaFile(
      token,
      javaFile("public class First {}"),
    );

    await api.resetStudentAttempt(
      assignment.id,
      DEMO_STUDENT_ID,
      first.submissionId,
    );

    const second = await submitJavaFile(
      token,
      javaFile("public class Second {}"),
    );

    expect(
      await api.getSource(second.submissionId),
    ).toBe("public class Second {}");

    await expect(
      api.getSubmission(
        assignment.id,
        first.submissionId,
      ),
    ).rejects.toMatchObject({
      status: 404,
    });

    await expect(
      api.getSource(first.submissionId),
    ).rejects.toMatchObject({
      status: 404,
    });
  });
});
