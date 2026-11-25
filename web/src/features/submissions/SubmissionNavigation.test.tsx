
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

describe("Навигация к работе студента", () => {
  it("открывает работу из списка задания и возвращается обратно", async () => {
    server.use(
      http.get("*/api/auth/me", () =>
        HttpResponse.json({
          id: "00000000-0000-4000-8000-000000000901",
          fullName: "Иванов Игорь Петрович",
          role: "teacher",
        }),
      ),

      http.get(
        "*/api/submissions/:submissionId/source",
        () =>
          new HttpResponse(
            "public class Example {}",
            {
              headers: {
                "Content-Type":
                  "text/plain; charset=utf-8",
              },
            },
          ),
      ),
    );

    const [submission, similarSubmission] =
      mockSubmissions[MOCK_IDS.success]?.items ?? [];

    if (!submission || !similarSubmission) {
      throw new Error(
        "Для теста нужны две работы",
      );
    }

    const user = userEvent.setup();

    render(
      <App
        initialEntries={[
          `/assignments/${MOCK_IDS.success}`,
        ]}
      />,
    );

    const table = await screen.findByRole("table");

    const workLink = within(table).getByRole(
      "link",
      {
        name: `Открыть работу A-${submission.submissionId}`,
      },
    );

    expect(workLink).toHaveAttribute(
      "href",
      `/assignments/${MOCK_IDS.success}/submissions/${submission.submissionId}`,
    );

    const similarWorkLabel = `A-${similarSubmission.submissionId}`;
    expect(within(table).getByRole("link", {
      name: `Открыть работу ${similarWorkLabel}`,
    })).toHaveTextContent(similarWorkLabel);

    await user.click(workLink);

    const similarLink = await screen.findByRole("link", {
      name: /Успешная проверка/,
    });

    expect(similarLink).toHaveTextContent(similarWorkLabel);

    expect(
      screen.getByRole("heading", {
        name: "Исходный Java-код",
      }),
    ).toBeInTheDocument();

    await user.click(
      screen.getByRole("link", {
        name: "Работы задания",
      }),
    );

    expect(
      await screen.findByRole("heading", {
        name: "Успешная проверка",
      }),
    ).toBeInTheDocument();

    expect(
      await screen.findByRole("table"),
    ).toBeInTheDocument();
  });
});
