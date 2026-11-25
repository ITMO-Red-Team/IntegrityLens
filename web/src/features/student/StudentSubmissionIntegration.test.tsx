
import {
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {
  http,
  HttpResponse,
} from "msw";
import {
  describe,
  expect,
  it,
  vi,
} from "vitest";

import { App } from "../../App";
import { server } from "../../test/setup";

const TOKEN = "student-integration-token";

const RECEIPT = {
  submissionId:
    "00000000-0000-4000-8000-000000000701",
  submittedAt:
    "2026-11-25T12:30:00Z",
};

function authenticateStudent() {
  server.use(
    http.get("*/api/auth/me", () =>
      HttpResponse.json({
        id: "00000000-0000-4000-8000-000000000801",
        fullName: "Петров Павел Андреевич",
        role: "student",
      }),
    ),
  );
}

function submissionForm(
  canSubmit: boolean,
  currentSubmission:
    | typeof RECEIPT
    | null = null,
) {
  return {
    title: "ДЗ-3. Многопоточность",
    teacherFullName:
      "Иванов Игорь Петрович",
    canSubmit,
    currentSubmission,
  };
}

describe("Интеграция студенческой сдачи", () => {
  it("отправляет Java-файл через multipart/form-data", async () => {
    authenticateStudent();

    const received = vi.fn();

    server.use(
      http.get(
        "*/api/submit/:token",
        () =>
          HttpResponse.json(
            submissionForm(true),
          ),
      ),

      http.post(
        "*/api/submit/:token",
        async ({ request, params }) => {
          const formData =
            await request.formData();

          const uploaded =
            formData.get("file");

          received({
            token: params.token,
            contentType:
              request.headers.get(
                "content-type",
              ),
            fileName:
              uploaded instanceof File
                ? uploaded.name
                : null,
          });

          return HttpResponse.json(
            RECEIPT,
            { status: 201 },
          );
        },
      ),
    );

    const user = userEvent.setup();

    render(
      <App
        initialEntries={[
          `/submit/${TOKEN}`,
        ]}
      />,
    );

    const fileInput =
      await screen.findByLabelText(
        "Java-файл",
      );

    const file = new File(
      [
        "public class Solution {\n",
        "    public static void main(String[] args) {}\n",
        "}\n",
      ],
      "Solution.java",
      {
        type: "text/plain",
      },
    );

    await user.upload(fileInput, file);

    const submitButton =
      screen.getByRole("button", {
        name: "Отправить работу",
      });

    await waitFor(() => {
      expect(
        submitButton,
      ).toBeEnabled();
    });

    await user.click(submitButton);

    expect(
      await screen.findByRole(
        "heading",
        {
          name: "Работа принята",
        },
      ),
    ).toBeInTheDocument();

    expect(
      screen.getByText(
        "Solution.java",
      ),
    ).toBeInTheDocument();

    expect(received).toHaveBeenCalledTimes(
      1,
    );

    expect(received).toHaveBeenCalledWith(
      expect.objectContaining({
        token: TOKEN,
        contentType:
          expect.stringContaining(
            "multipart/form-data",
          ),
        fileName: "Solution.java",
      }),
    );
  });

  it("обрабатывает конфликт повторной сдачи HTTP 409", async () => {
    authenticateStudent();

    let accepted = false;

    server.use(
      http.get(
        "*/api/submit/:token",
        () =>
          HttpResponse.json(
            submissionForm(
              !accepted,
              accepted ? RECEIPT : null,
            ),
          ),
      ),

      http.post(
        "*/api/submit/:token",
        () => {
          accepted = true;

          return HttpResponse.json(
            {
              code:
                "SUBMISSION_ALREADY_ACCEPTED",
              message:
                "Работа уже принята",
            },
            { status: 409 },
          );
        },
      ),
    );

    const user = userEvent.setup();

    render(
      <App
        initialEntries={[
          `/submit/${TOKEN}`,
        ]}
      />,
    );

    const fileInput =
      await screen.findByLabelText(
        "Java-файл",
      );

    const file = new File(
      ["public class Example {}"],
      "Example.java",
      {
        type: "text/plain",
      },
    );

    await user.upload(fileInput, file);

    const submitButton =
      screen.getByRole("button", {
        name: "Отправить работу",
      });

    await waitFor(() => {
      expect(
        submitButton,
      ).toBeEnabled();
    });

    await user.click(submitButton);

    expect(
      await screen.findByRole(
        "heading",
        {
          name: "Работа уже принята",
        },
      ),
    ).toBeInTheDocument();

    expect(
      screen.queryByLabelText(
        "Java-файл",
      ),
    ).not.toBeInTheDocument();
  });

  it("не разрешает преподавателю открыть форму студента", async () => {
    server.use(
      http.get(
        "*/api/auth/me",
        () =>
          HttpResponse.json({
            id: "00000000-0000-4000-8000-000000000901",
            fullName:
              "Иванов Игорь Петрович",
            role: "teacher",
          }),
      ),
    );

    render(
      <App
        initialEntries={[
          `/submit/${TOKEN}`,
        ]}
      />,
    );

    expect(
      await screen.findByRole("alert"),
    ).toHaveTextContent(
      "Сдача работы доступна только студентам.",
    );

    expect(
      screen.queryByLabelText(
        "Java-файл",
      ),
    ).not.toBeInTheDocument();
  });
});
