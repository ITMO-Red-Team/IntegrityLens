
import {
  render,
  screen,
  within,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import { describe, expect, it } from "vitest";

import { App } from "../../App";
import { api } from "../../api/target/client";
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

describe("Управление заданием", () => {
  it("изменяет источники через интерфейс", async () => {
    authenticateTeacher();

    const user = userEvent.setup();

    render(
      <App
        initialEntries={[
          `/assignments/${MOCK_IDS.success}`,
        ]}
      />,
    );

    await screen.findByRole("heading", {
      name: "Успешная проверка",
    });

    await user.click(
      screen.getByRole("button", {
        name: "Изменить источники",
      }),
    );

    const section = screen.getByRole("region", {
      name: "Редактирование источников",
    });

    const source = within(section).getByRole(
      "checkbox",
      { name: /Пустое задание/ },
    );

    await user.click(source);

    expect(source).toBeChecked();

    await user.click(
      within(section).getByRole("button", {
        name: "Сохранить источники",
      }),
    );

    expect(
      await screen.findByText(
        "Задания-источники обновлены.",
      ),
    ).toBeInTheDocument();

    const updated = await api.getAssignment(
      MOCK_IDS.success,
    );

    expect(updated.sourceAssignmentIds).toEqual([
      MOCK_IDS.empty,
    ]);
  });

  it("удаляет задание только после подтверждения", async () => {
    authenticateTeacher();

    const user = userEvent.setup();

    render(
      <App
        initialEntries={[
          `/assignments/${MOCK_IDS.empty}`,
        ]}
      />,
    );

    await screen.findByRole("heading", {
      name: "Пустое задание",
    });

    await user.click(
      screen.getByRole("button", {
        name: "Удалить задание",
      }),
    );

    const dialog = screen.getByRole("alertdialog", {
      name: "Удалить задание?",
    });

    // До подтверждения задание ещё существует.
    expect(
      await api.getAssignment(MOCK_IDS.empty),
    ).toBeDefined();

    await user.click(
      within(dialog).getByRole("button", {
        name: "Подтвердить удаление",
      }),
    );

    expect(
      await screen.findByRole("heading", {
        name: "Мои задания",
      }),
    ).toBeInTheDocument();

    const table = await screen.findByRole("table");

    expect(
      within(table).queryByRole("link", {
        name: "Пустое задание",
      }),
    ).not.toBeInTheDocument();
  });

  it("разрешает повторную сдачу с подтверждением", async () => {
    authenticateTeacher();

    const user = userEvent.setup();

    render(
      <App
        initialEntries={[
          `/assignments/${MOCK_IDS.success}`,
        ]}
      />,
    );

    await screen.findByRole("table");

    await user.click(
      screen.getByRole("button", {
        name: "Сбросить попытку A-01",
      }),
    );

    const dialog = screen.getByRole("alertdialog", {
      name: "Разрешить повторную сдачу?",
    });

    await user.click(
      within(dialog).getByRole("button", {
        name: "Подтвердить сброс",
      }),
    );

    expect(
      await screen.findByText(
        "Повторная сдача разрешена. Уже принятая работа сохранена.",
      ),
    ).toBeInTheDocument();

    const submissions = await api.listSubmissions(
      MOCK_IDS.success,
    );

    expect(submissions.total).toBe(2);
    expect(
      mockSubmissions[MOCK_IDS.success]?.items,
    ).toHaveLength(2);
  });
});
