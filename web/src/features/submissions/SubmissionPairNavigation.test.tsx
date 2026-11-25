
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

describe("Навигация к сравнению работ", () => {
  it("открывает сравнение из похожих работ и возвращается обратно", async () => {
    authenticateTeacher();

    const submissions =
      mockSubmissions[MOCK_IDS.success]?.items ?? [];

    const first = submissions[0]?.submissionId;
    const second = submissions[1]?.submissionId;

    if (!first || !second) {
      throw new Error(
        "Для теста нужны две работы",
      );
    }

    const user = userEvent.setup();

    render(
      <App
        initialEntries={[
          `/assignments/${MOCK_IDS.success}/submissions/${first}`,
        ]}
      />,
    );

    const similarHeading =
      await screen.findByRole("heading", {
        name: "Похожие работы",
      });

    const similarSection =
      similarHeading.closest("section");

    if (!similarSection) {
      throw new Error(
        "Не найдена секция похожих работ",
      );
    }

    const comparisonLink =
      within(similarSection).getByRole("link");

    expect(comparisonLink).toHaveAttribute(
      "href",
      `/assignments/${MOCK_IDS.success}/pairs/${first}/${second}`,
    );

    await user.click(comparisonLink);

    expect(
      await screen.findByRole("heading", {
        name: "Сравнение двух работ",
      }),
    ).toBeInTheDocument();

    expect(
      screen.getByLabelText("Оценка сходства"),
    ).toHaveTextContent("0.82");

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

    const fragmentGroup = screen.getByRole(
      "group",
      {
        name: "Совпадающие фрагменты",
      },
    );

    const fragmentButton =
      within(fragmentGroup).getAllByRole("button")[0];

    if (!fragmentButton) {
      throw new Error(
        "Нет фрагмента для сравнения",
      );
    }

    await user.click(fragmentButton);

    expect(fragmentButton).toHaveAttribute(
      "aria-pressed",
      "true",
    );

    await user.click(
      screen.getByRole("link", {
        name: /Похожие работы/,
      }),
    );

    expect(
      await screen.findByRole("heading", {
        name: "Похожие работы",
      }),
    ).toBeInTheDocument();

    expect(
      screen.getByRole("heading", {
        name: "Исходный Java-код",
      }),
    ).toBeInTheDocument();
  });
});
