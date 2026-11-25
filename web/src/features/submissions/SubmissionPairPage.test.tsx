
import {
  render,
  screen,
  waitFor,
  within,
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
import { http, HttpResponse } from "msw";
import {
  describe,
  expect,
  it,
} from "vitest";

import { SubmissionPairPage } from "./SubmissionPairPage";
import { server } from "../../test/setup";

import {
  MOCK_IDS,
  mockSubmissionDetails,
  mockSubmissions,
} from "../../mocks/target-fixtures";

const submissions =
  mockSubmissions[MOCK_IDS.success]?.items ?? [];

const first = submissions[0]?.submissionId;
const second = submissions[1]?.submissionId;

if (!first || !second) {
  throw new Error("Нужны две тестовые работы");
}

function renderPage(
  assignmentId = MOCK_IDS.success,
  a = first,
  b = second,
) {
  const client = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter
        initialEntries={[
          `/assignments/${assignmentId}/pairs/${a}/${b}`,
        ]}
      >
        <Routes>
          <Route
            path="/assignments/:assignmentId/pairs/:a/:b"
            element={<SubmissionPairPage />}
          />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("Сравнение двух работ", () => {
  it("показывает два исходника рядом", async () => {
    renderPage();

    expect(
      await screen.findByRole("heading", {
        name: "Сравнение двух работ",
      }),
    ).toBeInTheDocument();

    const paneA = screen.getByRole("region", {
      name: "Код работы Работа A",
    });

    const paneB = screen.getByRole("region", {
      name: "Код работы Работа B",
    });

    expect(
      await within(paneA).findByText(
        /public class BankTransfer/,
      ),
    ).toBeInTheDocument();

    expect(
      await within(paneB).findByText(
        /public class MoneyService/,
      ),
    ).toBeInTheDocument();

    expect(
      screen.getByLabelText("Оценка сходства"),
    ).toHaveTextContent("0.82");
  });

  it("выделяет выбранный совпадающий фрагмент", async () => {
    const user = userEvent.setup();

    renderPage();

    const group = await screen.findByRole(
      "group",
      {
        name: "Совпадающие фрагменты",
      },
    );

    const fragmentButtons =
      within(group).getAllByRole("button");

    expect(
      fragmentButtons.length,
    ).toBeGreaterThan(0);

    const firstButton = fragmentButtons[0];

    if (!firstButton) {
      throw new Error(
        "Нет совпадающего фрагмента",
      );
    }

    await user.click(firstButton);

    expect(firstButton).toHaveAttribute(
      "aria-pressed",
      "true",
    );

    const paneA = screen.getByRole("region", {
      name: "Код работы Работа A",
    });

    const paneB = screen.getByRole("region", {
      name: "Код работы Работа B",
    });

    expect(
      paneA.querySelectorAll(
        '[data-selected="true"]',
      ).length,
    ).toBeGreaterThan(0);

    expect(
      paneB.querySelectorAll(
        '[data-selected="true"]',
      ).length,
    ).toBeGreaterThan(0);
  });

  it("показывает ошибку загрузки сравнения", async () => {
    server.use(
      http.get(
        "*/api/assignments/:assignmentId/pairs/:a/:b",
        () =>
          HttpResponse.json(
            {
              code: "SERVICE_UNAVAILABLE",
              message:
                "Сравнение временно недоступно",
            },
            { status: 503 },
          ),
      ),
    );

    renderPage();

    expect(
      await screen.findByRole("alert"),
    ).toHaveTextContent(
      "Сравнение временно недоступно",
    );

    expect(
      screen.getByRole("button", {
        name: "Повторить",
      }),
    ).toBeInTheDocument();
  });

  it("отклоняет сравнение работы с самой собой", () => {
    renderPage(
      MOCK_IDS.success,
      first,
      first,
    );

    expect(
      screen.getByRole("alert"),
    ).toHaveTextContent(
      "Для сравнения нужны две разные работы.",
    );
  });

  it("использует собственное задание похожей работы", async () => {
    const original =
      mockSubmissionDetails[first];

    if (!original) {
      throw new Error(
        "Нет данных основной работы",
      );
    }

    // Работа B относится к другому заданию.
    // Это отдельный тестовый сценарий:
    // не изменяем общие фикстуры.
    server.use(
      http.get(
        "*/api/assignments/:assignmentId/submissions/:submissionId",
        ({ params }) => {
          if (
            String(params.assignmentId) !==
              MOCK_IDS.success ||
            String(params.submissionId) !== first
          ) {
            return undefined;
          }

          return HttpResponse.json({
            ...original,
            similar: [
              {
                submissionId: second,
                assignmentId: MOCK_IDS.empty,
                assignmentTitle:
                  "Задание-источник",
                score: {
                  status: "ok",
                  value: 0.82,
                },
              },
            ],
          });
        },
      ),
    );

    renderPage();

    const otherWorkLink =
      await screen.findByRole("link", {
        name: "Открыть работу B как основную",
      });

    expect(otherWorkLink).toHaveAttribute(
      "href",
      `/assignments/${MOCK_IDS.empty}/submissions/${second}`,
    );

    expect(otherWorkLink).not.toHaveAttribute(
      "href",
      `/assignments/${MOCK_IDS.success}/submissions/${second}`,
    );
  });

  it("не создаёт неправильную ссылку при отсутствии контекста", async () => {
    server.use(
      http.get(
        "*/api/assignments/:assignmentId/submissions/:submissionId",
        () =>
          HttpResponse.json(
            {
              code: "NOT_FOUND",
              message: "Работа не найдена",
            },
            { status: 404 },
          ),
      ),
    );

    renderPage();

    await waitFor(() => {
      expect(
        screen.getByText(
          /Переход к работе B недоступен/,
        ),
      ).toBeInTheDocument();
    });

    expect(
      screen.queryByRole("link", {
        name: "Открыть работу B как основную",
      }),
    ).not.toBeInTheDocument();
  });
});
