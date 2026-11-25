
import { apiRequest } from "./client";
import type { User } from "./types";

export type UserRole = "teacher" | "student";

export interface RegisterInput {
  fullName: string;
  email: string;
  password: string;
  role: UserRole;
}

export interface LoginInput {
  email: string;
  password: string;
}

export const authApi = {
  register: (input: RegisterInput): Promise<User> =>
    apiRequest<User>("/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(input),
    }),

  login: (input: LoginInput): Promise<User> =>
    apiRequest<User>("/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(input),
    }),

  me: (): Promise<User> =>
    apiRequest<User>("/auth/me"),

  logout: (): Promise<void> =>
    apiRequest<void>("/auth/logout", {
      method: "POST",
    }),
};
