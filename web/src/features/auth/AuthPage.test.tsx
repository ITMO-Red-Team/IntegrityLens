
import {
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import {
  describe,
  expect,
  it,
} from "vitest";

import { App } from "../../App";

async function register(
  user: ReturnType<typeof userEvent.setup>,
  role: "teacher" | "student",
  email: string,
) {
  render(
    <App initialEntries={["/register"]} />,
  );

  await user.type(
    screen.getByLabelText("ФИО"),
    "Иванов Иван Иванович",
  );

  await user.type(
    screen.getByLabelText("Email"),
    email,
  );

  await user.type(
    screen.getByLabelText("Пароль"),
    "password123",
  );

  if (role === "teacher") {
    await user.click(
      screen.getByRole("radio", {
        name: "Преподаватель",
      }),
    );
  }

  await user.click(
    screen.getByRole("button", {
      name: "Зарегистрироваться",
    }),
  );

  await screen.findByRole("heading", {
    name: "Вход",
  });

  expect(
    screen.getByRole("status"),
  ).toHaveTextContent("Регистрация успешна");
}

describe("Авторизация по новому API", () => {
  it("показывает форму входа", () => {
    render(
      <App initialEntries={["/login"]} />,
    );

    expect(
      screen.getByRole("heading", {
        name: "Вход",
      }),
    ).toBeInTheDocument();

    expect(
      screen.getByLabelText("Email"),
    ).toBeInTheDocument();

    expect(
      screen.getByLabelText("Пароль"),
    ).toBeInTheDocument();
  });

  it("регистрирует преподавателя", async () => {
    const user = userEvent.setup();

    await register(
      user,
      "teacher",
      "teacher@example.com",
    );
  });

  it("преподаватель может войти и выйти", async () => {
    const user = userEvent.setup();

    await register(
      user,
      "teacher",
      "teacher@example.com",
    );

    await user.clear(
      screen.getByLabelText("Email"),
    );

    await user.type(
      screen.getByLabelText("Email"),
      "teacher@example.com",
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

    expect(
      await screen.findByRole("heading", {
        name: "Мои задания",
      }),
    ).toBeInTheDocument();

    expect(
      screen.getByText(/Иванов Иван Иванович/),
    ).toBeInTheDocument();

    await user.click(
      screen.getByRole("button", {
        name: "Выйти",
      }),
    );

    await screen.findByRole("heading", {
      name: "Вход",
    });

    await waitFor(() => {
      expect(
        screen.queryByRole("button", {
          name: "Выйти",
        }),
      ).not.toBeInTheDocument();
    });
  });

  it("студент попадает на свою страницу", async () => {
    const user = userEvent.setup();

    await register(
      user,
      "student",
      "student@example.com",
    );

    await user.type(
      screen.getByLabelText("Email"),
      "student@example.com",
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

    expect(
      await screen.findByRole("heading", {
        name: "Моя работа",
      }),
    ).toBeInTheDocument();
  });

  it("отклоняет неправильный пароль", async () => {
    const user = userEvent.setup();

    await register(
      user,
      "student",
      "student@example.com",
    );

    await user.type(
      screen.getByLabelText("Email"),
      "student@example.com",
    );

    await user.type(
      screen.getByLabelText("Пароль"),
      "wrongpassword",
    );

    await user.click(
      screen.getByRole("button", {
        name: "Войти",
      }),
    );

    expect(
      await screen.findByRole("alert"),
    ).toHaveTextContent(
      "Неверный email или пароль",
    );
  });

  it("не допускает гостя к заданиям", async () => {
    render(
      <App initialEntries={["/assignments"]} />,
    );

    expect(
      await screen.findByRole("heading", {
        name: "Вход",
      }),
    ).toBeInTheDocument();
  });
});
