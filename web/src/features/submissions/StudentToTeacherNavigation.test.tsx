
import {
  render,
  screen,
  within,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import {
  describe,
  expect,
  it,
} from "vitest";

import { App } from "../../App";
import { api } from "../../api/target/client";
import {
  getSubmissionForm,
  submitJavaFile,
} from "../../api/target/students";
import { server } from "../../test/setup";

function authenticateTeacher() {
  server.use(
    http.get("*/api/auth/me", () =>
      HttpResponse.json({
        id: "00000000-0000-4000-8000-000000000901",
        fullName: "Иванов Игорь Петрович",
        role: "teacher",
      }),
    ),
  );
}

describe("Путь от сдачи студента к просмотру преподавателя", () => {
  it("открывает отправленный Java-код и позволяет сбросить попытку", async () => {
    authenticateTeacher();

    const assignment =
      await api.createAssignment({
        title: "Интеграционное задание",
        sourceAssignmentIds: [],
      });

    const token = new URL(
      assignment.submissionUrl,
    ).pathname.split("/").at(-1);

    if (!token) {
      throw new Error(
        "Не удалось получить токен сдачи",
      );
    }

    const source = [
      "public class IntegrationSolution {",
      "    public static void main(String[] args) {",
      '        System.out.println("IntegrityLens");',
      "    }",
      "}",
    ].join("\n");

    const receipt = await submitJavaFile(
      token,
      new File(
        [source],
        "IntegrationSolution.java",
        { type: "text/plain" },
      ),
    );

    const user = userEvent.setup();

    render(
      <App
        initialEntries={[
          `/assignments/${assignment.id}`,
        ]}
      />,
    );

    expect(
      await screen.findByRole("heading", {
        name: "Интеграционное задание",
      }),
    ).toBeInTheDocument();

    const table =
      await screen.findByRole("table");

    const workLink = within(table).getByRole(
      "link",
      { name: "Открыть работу A-01" },
    );

    expect(workLink).toHaveAttribute(
      "href",
      `/assignments/${assignment.id}/submissions/${receipt.submissionId}`,
    );

    expect(
      within(table).getAllByText("В анализе…"),
    ).toHaveLength(2);

    await user.click(workLink);

    expect(
      await screen.findByRole("heading", {
        name: "Исходный Java-код",
      }),
    ).toBeInTheDocument();

    expect(
      await screen.findByText(
        /public class IntegrationSolution/,
      ),
    ).toBeInTheDocument();

    expect(
      screen.getByText(
        /System\.out\.println\("IntegrityLens"\)/,
      ),
    ).toBeInTheDocument();

    expect(
      screen.getByRole("heading", {
        name: "Похожие работы",
      }),
    ).toBeInTheDocument();

    await user.click(
      screen.getByRole("link", {
        name: "Работы задания",
      }),
    );

    expect(
      await screen.findByRole("table"),
    ).toBeInTheDocument();

    await user.click(
      screen.getByRole("button", {
        name: "Сбросить попытку A-01",
      }),
    );

    expect(
      screen.getByRole("alertdialog"),
    ).toHaveTextContent(
      "Разрешить повторную сдачу?",
    );

    await user.click(
      screen.getByRole("button", {
        name: "Подтвердить сброс",
      }),
    );

    expect(
      await screen.findByText(
        /Повторная сдача разрешена/,
      ),
    ).toBeInTheDocument();

    const form = await getSubmissionForm(token);

    expect(form.canSubmit).toBe(true);
    expect(form.currentSubmission).toEqual(
      receipt,
    );

    // Сброс не должен удалять уже принятую работу.
    const submissions =
      await api.listSubmissions(assignment.id);

    expect(submissions.total).toBe(1);

    expect(
      submissions.items[0]?.submissionId,
    ).toBe(receipt.submissionId);
  });
});
