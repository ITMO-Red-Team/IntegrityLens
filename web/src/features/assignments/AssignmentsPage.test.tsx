
import {
  render,
  screen,
  within,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import { describe, expect, it } from "vitest";

import { App } from "../../App";
import { server } from "../../test/setup";
import {
  mockAssignments,
  MOCK_IDS,
} from "../../mocks/target-fixtures";

const teacher = {
  id: "00000000-0000-4000-8000-000000000901",
  fullName: "Иванов Игорь Петрович",
  role: "teacher",
};

function authenticateTeacher() {
  server.use(
    http.get("*/api/auth/me", () =>
      HttpResponse.json(teacher),
    ),
  );
}

describe("Экран «Мои задания»", () => {
  it("показывает задания из нового API", async () => {
    authenticateTeacher();

    render(
      <App initialEntries={["/assignments"]} />,
    );

    expect(
      await screen.findByRole("heading", {
        name: "Мои задания",
      }),
    ).toBeInTheDocument();

    const table = await screen.findByRole("table");
    const rows = within(table).getAllByRole("row");

    expect(rows).toHaveLength(5);

    expect(
      screen.getByText("Успешная проверка"),
    ).toBeInTheDocument();

    expect(
      screen.getByText("Пустое задание"),
    ).toBeInTheDocument();

    expect(
      screen.getByText("Проверка выполняется"),
    ).toBeInTheDocument();

    expect(
      screen.getByText("Ошибка проверки"),
    ).toBeInTheDocument();

    expect(
      screen.getByText("0 из 1"),
    ).toBeInTheDocument();

    expect(
      screen.getByText("Ошибки: 1 из 1"),
    ).toBeInTheDocument();

    expect(mockAssignments).toHaveLength(4);
  });

  it("показывает пустое состояние", async () => {
    authenticateTeacher();

    server.use(
      http.get("*/api/assignments", () =>
        HttpResponse.json([]),
      ),
    );

    render(
      <App initialEntries={["/assignments"]} />,
    );

    expect(
      await screen.findByText(
        "У вас пока нет заданий",
      ),
    ).toBeInTheDocument();

    expect(
      screen.getByRole("link", {
        name: "Создать первое задание",
      }),
    ).toHaveAttribute(
      "href",
      "/assignments/new",
    );
  });

  it("показывает HTTP-ошибку и позволяет повторить запрос", async () => {
    authenticateTeacher();

    let attempts = 0;

    server.use(
      http.get("*/api/assignments", () => {
        attempts += 1;

        // Первые два запроса завершаются ошибкой:
        // исходный запрос и автоматический retry.
        if (attempts <= 2) {
          return HttpResponse.json(
            {
              code: "SERVICE_UNAVAILABLE",
              message: "Сервис временно недоступен",
            },
            { status: 503 },
          );
        }

        // После нажатия «Повторить» запрос успешен.
        return HttpResponse.json(mockAssignments);
      }),
    );

    const user = userEvent.setup();

    render(
      <App initialEntries={["/assignments"]} />,
    );

    // React Query выполняет автоматический retry
    // с задержкой. Даём ему время завершиться.
    const alert = await screen.findByRole(
      "alert",
      {},
      { timeout: 5000 },
    );

    expect(alert).toHaveTextContent(
      "Сервис временно недоступен",
    );

    expect(attempts).toBe(2);

    await user.click(
      screen.getByRole("button", {
        name: "Повторить",
      }),
    );

    expect(
      await screen.findByRole("table"),
    ).toBeInTheDocument();

    expect(attempts).toBe(3);
  });

  it("ведёт к странице выбранного задания", async () => {
    authenticateTeacher();

    const user = userEvent.setup();

    render(
      <App initialEntries={["/assignments"]} />,
    );

    const link = await screen.findByRole("link", {
      name: "Успешная проверка",
    });

    expect(link).toHaveAttribute(
      "href",
      `/assignments/${MOCK_IDS.success}`,
    );

    await user.click(link);

    expect(
      await screen.findByRole("heading", {
        name: "Задание",
      }),
    ).toBeInTheDocument();
  });
});
