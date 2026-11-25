
import {
  render,
  screen,
  waitFor,
  within,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import {
  describe,
  expect,
  it,
  vi,
} from "vitest";

import { App } from "../../App";
import { server } from "../../test/setup";

import {
  MOCK_IDS,
  mockSubmissions,
} from "../../mocks/target-fixtures";

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

const items =
  mockSubmissions[MOCK_IDS.success]?.items ?? [];

if (items.length !== 2) {
  throw new Error(
    "Для сортировки нужны две тестовые работы",
  );
}

function firstWorkHref(): string | null {
  const table = screen.getByRole("table");

  return within(table)
    .getByRole("link", {
      name: "Открыть работу A-01",
    })
    .getAttribute("href");
}

describe("Серверная сортировка работ", () => {
  it("переключает сортировку с Structsim на AI через API", async () => {
    authenticateTeacher();

    const requests = vi.fn();

    server.use(
      http.get(
        "*/api/assignments/:assignmentId/submissions",
        ({ request }) => {
          const url = new URL(request.url);

          const sort =
            url.searchParams.get("sort");

          requests(sort);

          return HttpResponse.json({
            page: 1,
            pageSize: 20,
            total: 2,
            items:
              sort === "ai"
                ? [...items].reverse()
                : [...items],
          });
        },
      ),
    );

    const user = userEvent.setup();

    render(
      <App
        initialEntries={[
          `/assignments/${MOCK_IDS.success}`,
        ]}
      />,
    );

    await screen.findByRole("table");

    const select = screen.getByRole(
      "combobox",
      {
        name: "Сортировка работ",
      },
    );

    expect(select).toHaveValue(
      "similarity",
    );

    await waitFor(() => {
      expect(requests).toHaveBeenCalledWith(
        "similarity",
      );
    });

    expect(firstWorkHref()).toBe(
      `/assignments/${MOCK_IDS.success}/submissions/${items[0]?.submissionId}`,
    );

    await user.selectOptions(
      select,
      "ai",
    );

    expect(select).toHaveValue("ai");

    await waitFor(() => {
      expect(requests).toHaveBeenCalledWith(
        "ai",
      );
    });

    const expectedFirstWork = items[1];

    if (!expectedFirstWork) {
      throw new Error(
        "Не найдена тестовая работа",
      );
    }

    // Каждый раз ищем таблицу заново.
    // Не используем DOM-ссылку,
    // полученную до смены сортировки.
    await waitFor(() => {
      expect(firstWorkHref()).toBe(
        `/assignments/${MOCK_IDS.success}/submissions/${expectedFirstWork.submissionId}`,
      );
    });

    await user.selectOptions(
      select,
      "similarity",
    );

    await waitFor(() => {
      expect(firstWorkHref()).toBe(
        `/assignments/${MOCK_IDS.success}/submissions/${items[0]?.submissionId}`,
      );
    });
  });

  it("восстанавливает выбранную сортировку из URL", async () => {
    authenticateTeacher();

    const requests = vi.fn();

    server.use(
      http.get(
        "*/api/assignments/:assignmentId/submissions",
        ({ request }) => {
          const url = new URL(request.url);

          requests(
            url.searchParams.get("sort"),
            url.searchParams.get("page"),
            url.searchParams.get("pageSize"),
          );

          return HttpResponse.json({
            page: 1,
            pageSize: 20,
            total: items.length,
            items,
          });
        },
      ),
    );

    render(
      <App
        initialEntries={[
          `/assignments/${MOCK_IDS.success}?sort=ai`,
        ]}
      />,
    );

    expect(
      await screen.findByRole("table"),
    ).toBeInTheDocument();

    expect(
      screen.getByRole("combobox", {
        name: "Сортировка работ",
      }),
    ).toHaveValue("ai");

    expect(requests).toHaveBeenCalledWith(
      "ai",
      "1",
      "20",
    );
  });
});
