
import { delay, http, HttpResponse } from "msw";

type Role = "teacher" | "student";

export interface MockUser {
  id: string;
  fullName: string;
  role: Role;
}

interface MockAccount {
  user: MockUser;
  email: string;
  password: string;
}

const accounts = new Map<string, MockAccount>();

let currentUser: MockUser | null = null;
let nextUserId = 1;

const latency = () =>
  delay(import.meta.env.MODE === "test" ? 0 : 150);

function apiError(
  status: number,
  code: string,
  message: string,
) {
  return HttpResponse.json(
    { code, message },
    { status },
  );
}

function isRecord(
  value: unknown,
): value is Record<string, unknown> {
  return (
    value !== null &&
    typeof value === "object" &&
    !Array.isArray(value)
  );
}

function requiredString(
  value: unknown,
): value is string {
  return (
    typeof value === "string" &&
    value.trim().length > 0
  );
}

function validEmail(value: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}

function createUserId(): string {
  const id = nextUserId++;

  return `00000000-0000-4000-8000-${String(id).padStart(12, "0")}`;
}

export function getMockCurrentUser():
  MockUser | null {
  return currentUser;
}

export function resetMockAuth() {
  accounts.clear();
  currentUser = null;
  nextUserId = 1;
}

export const authHandlers = [
  http.post("*/api/auth/register", async ({ request }) => {
    await latency();

    let body: unknown;

    try {
      body = await request.json();
    } catch {
      return apiError(
        400,
        "INVALID_JSON",
        "Некорректный JSON",
      );
    }

    if (!isRecord(body)) {
      return apiError(
        422,
        "VALIDATION_ERROR",
        "Проверьте заполнение полей.",
      );
    }

    const { fullName, email, password, role } = body;

    if (
      !requiredString(fullName) ||
      fullName.length > 200 ||
      !requiredString(email) ||
      email.length > 320 ||
      !validEmail(email) ||
      typeof password !== "string" ||
      password.length < 8 ||
      password.length > 128 ||
      (role !== "teacher" && role !== "student")
    ) {
      return apiError(
        422,
        "VALIDATION_ERROR",
        "Проверьте заполнение полей.",
      );
    }

    const normalizedEmail = email.trim().toLowerCase();

    if (accounts.has(normalizedEmail)) {
      return apiError(
        409,
        "EMAIL_ALREADY_REGISTERED",
        "Этот email уже зарегистрирован.",
      );
    }

    const user: MockUser = {
      id: createUserId(),
      fullName: fullName.trim(),
      role,
    };

    accounts.set(normalizedEmail, {
      user,
      email: normalizedEmail,
      password,
    });

    return HttpResponse.json(user, {
      status: 201,
    });
  }),

  http.post("*/api/auth/login", async ({ request }) => {
    await latency();

    let body: unknown;

    try {
      body = await request.json();
    } catch {
      return apiError(
        400,
        "INVALID_JSON",
        "Некорректный JSON",
      );
    }

    if (!isRecord(body)) {
      return apiError(
        422,
        "VALIDATION_ERROR",
        "Проверьте заполнение полей.",
      );
    }

    const { email, password } = body;

    if (
      !requiredString(email) ||
      typeof password !== "string" ||
      password.length === 0
    ) {
      return apiError(
        422,
        "VALIDATION_ERROR",
        "Проверьте заполнение полей.",
      );
    }

    const account = accounts.get(
      email.trim().toLowerCase(),
    );

    if (!account || account.password !== password) {
      return apiError(
        401,
        "INVALID_CREDENTIALS",
        "Неверный email или пароль",
      );
    }

    currentUser = account.user;

    return HttpResponse.json(currentUser);
  }),

  http.get("*/api/auth/me", async () => {
    await latency();

    if (!currentUser) {
      return apiError(
        401,
        "AUTHENTICATION_REQUIRED",
        "Требуется вход в систему.",
      );
    }

    return HttpResponse.json(currentUser);
  }),

  http.post("*/api/auth/logout", async () => {
    await latency();

    currentUser = null;

    return new HttpResponse(null, {
      status: 204,
    });
  }),
];
