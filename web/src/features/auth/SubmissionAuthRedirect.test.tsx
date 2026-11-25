
import {
  render,
  screen,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import {
  describe,
  expect,
  it,
} from "vitest";

import { App } from "../../App";
import { authApi } from "../../api/target/auth";

import {
  MOCK_IDS,
  mockAssignments,
} from "../../mocks/target-fixtures";

const SUBMISSION_PATH =
  `/submit/${MOCK_IDS.empty}`;

async function login(
  user: ReturnType<typeof userEvent.setup>,
  email: string,
) {
  await user.type(
    screen.getByLabelText("Email"),
    email,
  );

  await user.type(
    screen.getByLabelText("Пароль"),
    "password123",
  );

  await user.click(
    screen.getByRole("button", {
      name: "Войти",
    }),
  );
}

describe("Возврат к заданию после авторизации", () => {
  it("возвращает существующего студента к исходной ссылке", async () => {
    await authApi.register({
      fullName: "Студент Тестовый",
      email: "student-redirect@example.com",
      password: "password123",
      role: "student",
    });

    const user = userEvent.setup();

    render(
      <App
        initialEntries={[
          SUBMISSION_PATH,
        ]}
      />,
    );

    expect(
      await screen.findByRole("heading", {
        name: "Вход",
      }),
    ).toBeInTheDocument();

    expect(
      screen.getByText(
        /После входа вы вернётесь/,
      ),
    ).toBeInTheDocument();

    await login(
      user,
      "student-redirect@example.com",
    );

    const title = mockAssignments.find(
      (item) => item.id === MOCK_IDS.empty,
    )?.title;

    expect(title).toBeDefined();

    expect(
      await screen.findByRole("heading", {
        name: title,
      }),
    ).toBeInTheDocument();

    expect(
      screen.getByLabelText("Java-файл"),
    ).toBeInTheDocument();
  });

  it("сохраняет ссылку при регистрации нового студента", async () => {
    const user = userEvent.setup();

    render(
      <App
        initialEntries={[
          SUBMISSION_PATH,
        ]}
      />,
    );

    expect(
      await screen.findByRole("heading", {
        name: "Вход",
      }),
    ).toBeInTheDocument();

    await user.click(
      screen.getByRole("link", {
        name: "Зарегистрироваться",
      }),
    );

    expect(
      await screen.findByRole("heading", {
        name: "Регистрация",
      }),
    ).toBeInTheDocument();

    await user.type(
      screen.getByLabelText("ФИО"),
      "Новый Студент",
    );

    await user.type(
      screen.getByLabelText("Email"),
      "new-student-redirect@example.com",
    );

    await user.type(
      screen.getByLabelText("Пароль"),
      "password123",
    );

    await user.click(
      screen.getByRole("button", {
        name: "Зарегистрироваться",
      }),
    );

    expect(
      await screen.findByRole("heading", {
        name: "Вход",
      }),
    ).toBeInTheDocument();

    expect(
      screen.getByRole("status"),
    ).toHaveTextContent(
      "Регистрация успешна",
    );

    await login(
      user,
      "new-student-redirect@example.com",
    );

    expect(
      await screen.findByLabelText(
        "Java-файл",
      ),
    ).toBeInTheDocument();
  });

  it("не открывает студенческую страницу преподавателю", async () => {
    await authApi.register({
      fullName: "Преподаватель Тестовый",
      email: "teacher-redirect@example.com",
      password: "password123",
      role: "teacher",
    });

    const user = userEvent.setup();

    render(
      <App
        initialEntries={[
          SUBMISSION_PATH,
        ]}
      />,
    );

    expect(
      await screen.findByRole("heading", {
        name: "Вход",
      }),
    ).toBeInTheDocument();

    await login(
      user,
      "teacher-redirect@example.com",
    );

    expect(
      await screen.findByRole("heading", {
        name: "Мои задания",
      }),
    ).toBeInTheDocument();

    expect(
      screen.queryByLabelText(
        "Java-файл",
      ),
    ).not.toBeInTheDocument();
  });
});
