
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
import { MOCK_IDS } from "../../mocks/target-fixtures";

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

describe("Создание задания", () => {
  it("создаёт задание с выбранным источником", async () => {
    authenticateTeacher();

    const user = userEvent.setup();

    render(
      <App initialEntries={["/assignments/new"]} />,
    );

    expect(
      await screen.findByRole("heading", {
        name: "Новое задание",
      }),
    ).toBeInTheDocument();

    await user.type(
      screen.getByRole("textbox", {
        name: "Название",
      }),
      "ДЗ-4. Алгоритмы",
    );

    const source = await screen.findByRole(
      "checkbox",
      {
        name: /Успешная проверка/,
      },
    );

    await user.click(source);

    expect(source).toBeChecked();

    await user.click(
      screen.getByRole("button", {
        name: "Сохранить",
      }),
    );

    expect(
      await screen.findByRole("heading", {
        name: "Задание создано",
      }),
    ).toBeInTheDocument();

    expect(
      screen.getByText("ДЗ-4. Алгоритмы"),
    ).toBeInTheDocument();

    const linkInput = screen.getByRole(
      "textbox",
      {
        name: "Ссылка для сдачи",
      },
    ) as HTMLInputElement;

    expect(linkInput.value).toMatch(
      /^https?:\/\/[^/]+\/submit\/mock-[0-9a-f-]+$/,
    );

    await user.click(
      screen.getByRole("link", {
        name: "К списку заданий",
      }),
    );

    const table = await screen.findByRole("table");

    expect(
      within(table).getByRole("link", {
        name: "ДЗ-4. Алгоритмы",
      }),
    ).toBeInTheDocument();

    expect(MOCK_IDS.success).toBeDefined();
  });

  it("не позволяет сохранить пустое название", async () => {
    authenticateTeacher();

    render(
      <App initialEntries={["/assignments/new"]} />,
    );

    await screen.findByRole("heading", {
      name: "Новое задание",
    });

    expect(
      screen.getByRole("button", {
        name: "Сохранить",
      }),
    ).toBeDisabled();
  });

  it("показывает ошибку создания", async () => {
    authenticateTeacher();

    server.use(
      http.post("*/api/assignments", () =>
        HttpResponse.json(
          {
            code: "SERVICE_UNAVAILABLE",
            message: "Сервис временно недоступен",
          },
          { status: 503 },
        ),
      ),
    );

    const user = userEvent.setup();

    render(
      <App initialEntries={["/assignments/new"]} />,
    );

    await screen.findByRole("checkbox", {
      name: /Успешная проверка/,
    });

    await user.type(
      screen.getByRole("textbox", {
        name: "Название",
      }),
      "Тестовое задание",
    );

    await user.click(
      screen.getByRole("button", {
        name: "Сохранить",
      }),
    );

    expect(
      await screen.findByRole("alert"),
    ).toHaveTextContent(
      "Сервис временно недоступен",
    );
  });
});
