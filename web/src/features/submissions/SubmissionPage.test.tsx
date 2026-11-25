
import {
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import {
  QueryClient,
  QueryClientProvider,
} from "@tanstack/react-query";
import {
  MemoryRouter,
  Route,
  Routes,
} from "react-router-dom";
import { http, HttpResponse } from "msw";
import {
  describe,
  expect,
  it,
} from "vitest";

import { SubmissionPage } from "./SubmissionPage";
import { server } from "../../test/setup";
import {
  MOCK_IDS,
  mockSubmissions,
} from "../../mocks/target-fixtures";

const submissionId =
  mockSubmissions[MOCK_IDS.success]
    ?.items[0]?.submissionId;

if (!submissionId) {
  throw new Error(
    "В фикстурах отсутствует тестовая работа",
  );
}

function renderPage(
  assignmentId = MOCK_IDS.success,
  workId = submissionId,
) {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter
        initialEntries={[
          `/assignments/${assignmentId}/submissions/${workId}`,
        ]}
      >
        <Routes>
          <Route
            path="/assignments/:assignmentId/submissions/:submissionId"
            element={<SubmissionPage />}
          />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("Детальный просмотр работы", () => {
  it("показывает оценки, исходный код и похожие работы", async () => {
    const javaSource = "public class Example {}";

    server.use(
      http.get(
        "*/api/submissions/:submissionId/source",
        () =>
          new HttpResponse(javaSource, {
            headers: {
              "Content-Type":
                "text/plain; charset=utf-8",
            },
          }),
      ),
    );

    const { container } = renderPage();

    // Ждём, пока исходник полностью загрузится.
    await waitFor(() => {
      const code = container.querySelector("pre code");

      expect(code).not.toBeNull();
      expect(code).toHaveTextContent(javaSource);
    });

    expect(
      screen.getByText("Сходство"),
    ).toBeInTheDocument();

    expect(
      screen.getByText("ИИ-генерация"),
    ).toBeInTheDocument();

    expect(
      screen.getByRole("heading", {
        name: "Похожие работы",
      }),
    ).toBeInTheDocument();

    // Исходный код содержит подсвеченные
    // подозрительные участки.
    const highlighted = container.querySelectorAll(
      '[data-suspicious="true"]',
    );

    expect(highlighted.length).toBeGreaterThan(0);
  });

  it("подсвечивает байтовые диапазоны UTF-8", async () => {
    const javaSource =
      "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    server.use(
      http.get(
        "*/api/submissions/:submissionId/source",
        () =>
          new HttpResponse(javaSource, {
            headers: {
              "Content-Type":
                "text/plain; charset=utf-8",
            },
          }),
      ),
    );

    const { container } = renderPage();

    await waitFor(() => {
      const code = container.querySelector("pre code");

      expect(code).not.toBeNull();
      expect(code).toHaveTextContent(javaSource);
    });

    const highlights = container.querySelectorAll(
      '[data-suspicious="true"]',
    );

    expect(highlights.length).toBeGreaterThan(0);

    const highlightedText = Array.from(highlights)
      .map((element) => element.textContent ?? "")
      .join("");

    expect(highlightedText.length).toBeGreaterThan(0);
  });

  it("не скрывает результаты при ошибке загрузки кода", async () => {
    server.use(
      http.get(
        "*/api/submissions/:submissionId/source",
        () =>
          HttpResponse.json(
            {
              code: "SERVICE_UNAVAILABLE",
              message: "Исходник временно недоступен",
            },
            { status: 503 },
          ),
      ),
    );

    renderPage();

    expect(
      await screen.findByRole("alert"),
    ).toHaveTextContent(
      "Исходник временно недоступен",
    );

    expect(
      screen.getByRole("heading", {
        name: "Похожие работы",
      }),
    ).toBeInTheDocument();

    expect(
      screen.getByText("Сходство"),
    ).toBeInTheDocument();

    expect(
      screen.getByText("ИИ-генерация"),
    ).toBeInTheDocument();
  });
});
