
import { useState, type FormEvent } from "react";
import {
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";
import {
  Link,
  useLocation,
  useNavigate,
} from "react-router-dom";

import {
  authApi,
  type UserRole,
} from "../../api/target/auth";
import { ApiError } from "../../api/target/client";

type AuthMode = "login" | "register";

function errorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    return error.message;
  }

  return "Не удалось связаться с сервером. Попробуйте ещё раз.";
}

export function AuthPage({ mode }: { mode: AuthMode }) {
  const navigate = useNavigate();
  const location = useLocation();
  const queryClient = useQueryClient();

  const isRegister = mode === "register";

  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState<UserRole>("student");

  const mutation = useMutation({
    mutationFn: async () => {
      if (isRegister) {
        return authApi.register({
          fullName: fullName.trim(),
          email: email.trim(),
          password,
          role,
        });
      }

      return authApi.login({
        email: email.trim(),
        password,
      });
    },

    onSuccess: (user) => {
      if (isRegister) {
        navigate("/login", {
          replace: true,
          state: {
            message:
              "Регистрация успешна. Теперь войдите в аккаунт.",
          },
        });
        return;
      }

      queryClient.setQueryData(
        ["target", "auth", "me"],
        user,
      );

      navigate(
        user.role === "teacher"
          ? "/assignments"
          : "/student",
        { replace: true },
      );
    },
  });

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    mutation.mutate();
  }

  const successMessage =
    (location.state as { message?: string } | null)
      ?.message;

  return (
    <div className="flex min-h-screen items-center justify-center px-4 py-12">
      <div className="w-full max-w-[440px]">
        <div className="mb-8 text-center">
          <div className="mb-2 font-display text-3xl font-semibold text-ink">
            IntegrityLens
          </div>

          <p className="text-sm text-muted">
            Система проверки учебных работ
          </p>
        </div>

        <section className="panel p-7">
          <h1 className="mb-6 text-2xl font-semibold">
            {isRegister ? "Регистрация" : "Вход"}
          </h1>

          {successMessage && (
            <div
              role="status"
              className="mb-5 rounded-md border border-line bg-sunk p-3 text-sm"
            >
              {successMessage}
            </div>
          )}

          {mutation.isError && (
            <div
              role="alert"
              className="mb-5 rounded-md border border-line bg-[var(--c-high-bg)] p-3 text-sm text-[var(--c-high)]"
            >
              {errorMessage(mutation.error)}
            </div>
          )}

          <form
            onSubmit={handleSubmit}
            className="flex flex-col gap-4"
          >
            {isRegister && (
              <label className="flex flex-col gap-1.5">
                <span className="font-medium">
                  ФИО
                </span>

                <input
                  className="field w-full"
                  autoComplete="name"
                  required
                  maxLength={200}
                  value={fullName}
                  onChange={(event) =>
                    setFullName(event.target.value)
                  }
                  placeholder="Иванов Иван Иванович"
                />
              </label>
            )}

            <label className="flex flex-col gap-1.5">
              <span className="font-medium">
                Email
              </span>

              <input
                className="field w-full"
                type="email"
                autoComplete="email"
                required
                maxLength={320}
                value={email}
                onChange={(event) =>
                  setEmail(event.target.value)
                }
                placeholder="name@example.com"
              />
            </label>

            <label className="flex flex-col gap-1.5">
              <span className="font-medium">
                Пароль
              </span>

              <input
                className="field w-full"
                type="password"
                autoComplete={
                  isRegister
                    ? "new-password"
                    : "current-password"
                }
                required
                minLength={8}
                maxLength={128}
                value={password}
                onChange={(event) =>
                  setPassword(event.target.value)
                }
                placeholder="Не менее 8 символов"
              />
            </label>

            {isRegister && (
              <fieldset className="flex flex-col gap-2">
                <legend className="mb-2 font-medium">
                  Роль
                </legend>

                <label className="flex items-center gap-2">
                  <input
                    type="radio"
                    name="role"
                    value="student"
                    checked={role === "student"}
                    onChange={() =>
                      setRole("student")
                    }
                  />
                  Студент
                </label>

                <label className="flex items-center gap-2">
                  <input
                    type="radio"
                    name="role"
                    value="teacher"
                    checked={role === "teacher"}
                    onChange={() =>
                      setRole("teacher")
                    }
                  />
                  Преподаватель
                </label>
              </fieldset>
            )}

            <button
              className="btn btn-primary mt-2 w-full py-2.5"
              type="submit"
              disabled={mutation.isPending}
            >
              {mutation.isPending
                ? "Подождите..."
                : isRegister
                  ? "Зарегистрироваться"
                  : "Войти"}
            </button>
          </form>

          <div className="mt-6 text-center text-sm text-muted">
            {isRegister ? (
              <>
                Уже есть учётная запись?{" "}
                <Link
                  to="/login"
                  className="font-medium text-accent underline"
                >
                  Войти
                </Link>
              </>
            ) : (
              <>
                Нет учётной записи?{" "}
                <Link
                  to="/register"
                  className="font-medium text-accent underline"
                >
                  Зарегистрироваться
                </Link>
              </>
            )}
          </div>
        </section>
      </div>
    </div>
  );
}
