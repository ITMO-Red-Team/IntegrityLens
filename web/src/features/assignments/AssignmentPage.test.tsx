
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

function renderAssignment(id: string) {
  render(
    <App initialEntries={[`/assignments/${id}`]} />,
  );
}

describe("Экран «Работы задания»", () => {
  it("показывает статистику и список работ", async () => {
    authenticateTeacher();

    renderAssignment(MOCK_IDS.success);

    expect(
      await screen.findByRole("heading", {
        name: "Успешная проверка",
      }),
    ).toBeInTheDocument();

    const table = await screen.findByRole("table");

    expect(
      within(table).getAllByRole("row"),
    ).toHaveLength(3);

    expect(
      screen.getByText("Проанализировано:"),
    ).toBeInTheDocument();

    expect(
      screen.getByText("0.82"),
    ).toBeInTheDocument();

    expect(
      screen.getAllByText("0.18").length,
    ).toBeGreaterThan(0);

    expect(
      screen.getByRole("checkbox", {
        name: "Показать ФИО",
      }),
    ).not.toBeChecked();
  });

  it("скрывает ФИО до включения переключателя", async () => {
    authenticateTeacher();

    renderAssignment(MOCK_IDS.success);

    await screen.findByRole("table");

    const fullName =
      mockSubmissions[MOCK_IDS.success]?.items[0]
        ?.fullName;

    expect(fullName).toBeDefined();

    if (!fullName) {
      throw new Error(
        "В моках отсутствует ФИО студента",
      );
    }

    expect(
      screen.queryByText(`· ${fullName}`),
    ).not.toBeInTheDocument();

    const user = userEvent.setup();

    const toggle = screen.getByRole("checkbox", {
      name: "Показать ФИО",
    });

    await user.click(toggle);

    expect(toggle).toBeChecked();

    expect(
      screen.getByText(`· ${fullName}`),
    ).toBeInTheDocument();

    await user.click(toggle);

    expect(toggle).not.toBeChecked();

    expect(
      screen.queryByText(`· ${fullName}`),
    ).not.toBeInTheDocument();
  });

  it("показывает пустое задание", async () => {
    authenticateTeacher();

    renderAssignment(MOCK_IDS.empty);

    expect(
      await screen.findByText(
        "Работы ещё не сданы",
      ),
    ).toBeInTheDocument();
  });

  it("показывает независимые состояния анализа", async () => {
    authenticateTeacher();

    renderAssignment(MOCK_IDS.pending);

    expect(
      await screen.findByRole("table"),
    ).toBeInTheDocument();

    expect(
      screen.getByText("В анализе…"),
    ).toBeInTheDocument();

    expect(
      screen.getByText("0.82"),
    ).toBeInTheDocument();
  });

  it("показывает ошибку получения работ", async () => {
    authenticateTeacher();

    server.use(
      http.get(
        "*/api/assignments/:assignmentId/submissions",
        () =>
          HttpResponse.json(
            {
              code: "SERVICE_UNAVAILABLE",
              message: "Работы временно недоступны",
            },
            { status: 503 },
          ),
      ),
    );

    renderAssignment(MOCK_IDS.success);

    const alert = await screen.findByRole(
      "alert",
      {},
      { timeout: 5000 },
    );

    expect(alert).toHaveTextContent(
      "Работы временно недоступны",
    );
  });
});
