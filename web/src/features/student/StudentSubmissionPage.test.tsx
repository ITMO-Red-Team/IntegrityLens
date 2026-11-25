
import {
  render,
  screen,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {
  QueryClient,
  QueryClientProvider,
} from "@tanstack/react-query";
import {
  MemoryRouter,
  Route,
  Routes,
} from "react-router-dom";
import {
  http,
  HttpResponse,
} from "msw";
import {
  describe,
  expect,
  it,
} from "vitest";

import { server } from "../../test/setup";
import { StudentSubmissionPage } from "./StudentSubmissionPage";

const TOKEN = "student-test-token";

function renderPage() {
  const client = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });

  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter
        initialEntries={[`/submit/${TOKEN}`]}
      >
        <Routes>
          <Route
            path="/submit/:token"
            element={<StudentSubmissionPage />}
          />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("Студенческая сдача работы", () => {
  it("показывает доступную форму отправки", async () => {
    server.use(
      http.get("*/api/submit/:token", () =>
        HttpResponse.json({
          title: "ДЗ-3. Многопоточность",
          teacherFullName:
            "Иванов Игорь Петрович",
          canSubmit: true,
          currentSubmission: null,
        }),
      ),
    );

    renderPage();

    expect(
      await screen.findByRole("heading", {
        name: "ДЗ-3. Многопоточность",
      }),
    ).toBeInTheDocument();

    expect(
      screen.getByText(
        /Иванов Игорь Петрович/,
      ),
    ).toBeInTheDocument();

    expect(
      screen.getByLabelText("Java-файл"),
    ).toBeInTheDocument();

    expect(
      screen.getByRole("button", {
        name: "Отправить работу",
      }),
    ).toBeDisabled();
  });

  it("блокирует повторную сдачу без сброса", async () => {
    server.use(
      http.get("*/api/submit/:token", () =>
        HttpResponse.json({
          title: "ДЗ-3. Многопоточность",
          teacherFullName:
            "Иванов Игорь Петрович",
          canSubmit: false,
          currentSubmission: {
            submissionId:
              "00000000-0000-4000-8000-000000000301",
            submittedAt:
              "2026-11-07T18:42:00Z",
          },
        }),
      ),
    );

    renderPage();

    expect(
      await screen.findByRole("heading", {
        name: "Работа уже принята",
      }),
    ).toBeInTheDocument();

    expect(
      screen.queryByLabelText("Java-файл"),
    ).not.toBeInTheDocument();

    expect(
      screen.queryByRole("button", {
        name: "Отправить работу",
      }),
    ).not.toBeInTheDocument();
  });

  it("показывает ошибку неправильного расширения", async () => {
    server.use(
      http.get("*/api/submit/:token", () =>
        HttpResponse.json({
          title: "ДЗ-3. Многопоточность",
          teacherFullName:
            "Иванов Игорь Петрович",
          canSubmit: true,
          currentSubmission: null,
        }),
      ),
    );

    // Разрешаем загрузку файла, не подходящего
    // под HTML-атрибут accept=".java".
    // Это позволяет проверить собственную
    // валидацию приложения.
    const user = userEvent.setup({
      applyAccept: false,
    });

    renderPage();

    const input =
      await screen.findByLabelText("Java-файл");

    const file = new File(
      ["hello"],
      "solution.txt",
      { type: "text/plain" },
    );

    await user.upload(input, file);

    expect(
      await screen.findByRole("alert"),
    ).toHaveTextContent(
      "Допустим только файл с расширением .java.",
    );

    expect(
      screen.getByRole("button", {
        name: "Отправить работу",
      }),
    ).toBeDisabled();
  });
});
