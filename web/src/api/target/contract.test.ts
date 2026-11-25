
import {
  describe,
  expect,
  it,
  vi,
} from "vitest";

import {
  http,
  HttpResponse,
} from "msw";

import { server } from "../../test/setup";

import { api } from "./client";
import { authApi } from "./auth";

import {
  submitJavaFile,
} from "./students";

const ASSIGNMENT_ID =
  "00000000-0000-4000-8000-000000000101";

const STUDENT_ID =
  "00000000-0000-4000-8000-000000000201";

const SUBMISSION_ID =
  "00000000-0000-4000-8000-000000000301";

const OTHER_ID =
  "00000000-0000-4000-8000-000000000302";

describe("HTTP-контракты нового API", () => {
  it("отправляет регистрацию и вход по OpenAPI", async () => {
    const requests = vi.fn();

    server.use(
      http.post(
        "*/api/auth/register",
        async ({ request }) => {
          requests(
            "register",
            await request.json(),
          );

          return HttpResponse.json(
            {
              id: STUDENT_ID,
              fullName: "Тестовый Студент",
              role: "student",
            },
            { status: 201 },
          );
        },
      ),

      http.post(
        "*/api/auth/login",
        async ({ request }) => {
          requests(
            "login",
            await request.json(),
          );

          return HttpResponse.json({
            id: STUDENT_ID,
            fullName: "Тестовый Студент",
            role: "student",
          });
        },
      ),
    );

    await authApi.register({
      fullName: "Тестовый Студент",
      email: "student@example.com",
      password: "password123",
      role: "student",
    });

    await authApi.login({
      email: "student@example.com",
      password: "password123",
    });

    expect(requests).toHaveBeenCalledWith(
      "register",
      {
        fullName: "Тестовый Студент",
        email: "student@example.com",
        password: "password123",
        role: "student",
      },
    );

    expect(requests).toHaveBeenCalledWith(
      "login",
      {
        email: "student@example.com",
        password: "password123",
      },
    );
  });

  it("передаёт сортировку и пагинацию на сервер", async () => {
    const requests = vi.fn();

    server.use(
      http.get(
        "*/api/assignments/:assignmentId/submissions",
        ({ request, params }) => {
          const url = new URL(request.url);

          requests({
            assignmentId:
              params.assignmentId,
            sort: url.searchParams.get(
              "sort",
            ),
            page: url.searchParams.get(
              "page",
            ),
            pageSize: url.searchParams.get(
              "pageSize",
            ),
          });

          return HttpResponse.json({
            page: 2,
            pageSize: 10,
            total: 0,
            items: [],
          });
        },
      ),
    );

    const result = await api.listSubmissions(
      ASSIGNMENT_ID,
      "ai",
      2,
      10,
    );

    expect(requests).toHaveBeenCalledWith({
      assignmentId: ASSIGNMENT_ID,
      sort: "ai",
      page: "2",
      pageSize: "10",
    });

    expect(result.page).toBe(2);
    expect(result.pageSize).toBe(10);
  });

  it("передаёт expectedSubmissionId при сбросе попытки", async () => {
    const requests = vi.fn();

    server.use(
      http.post(
        "*/api/assignments/:assignmentId/students/:studentId/reset",
        async ({ request, params }) => {
          requests({
            assignmentId:
              params.assignmentId,
            studentId: params.studentId,
            body: await request.json(),
          });

          return new HttpResponse(null, {
            status: 204,
          });
        },
      ),
    );

    await api.resetStudentAttempt(
      ASSIGNMENT_ID,
      STUDENT_ID,
      SUBMISSION_ID,
    );

    expect(requests).toHaveBeenCalledWith({
      assignmentId: ASSIGNMENT_ID,
      studentId: STUDENT_ID,
      body: {
        expectedSubmissionId:
          SUBMISSION_ID,
      },
    });
  });

  it("сохраняет порядок UUID в запросе сравнения", async () => {
    const requests = vi.fn();

    server.use(
      http.get(
        "*/api/assignments/:assignmentId/pairs/:a/:b",
        ({ params }) => {
          requests({
            assignmentId:
              params.assignmentId,
            a: params.a,
            b: params.b,
          });

          const pending = {
            status: "pending",
            value: null,
          };

          return HttpResponse.json({
            a: params.a,
            b: params.b,
            score: pending,
            methods: {
              structsim: {
                score: pending,
                fragments: [],
                details: {},
              },
            },
          });
        },
      ),
    );

    const result = await api.getPair(
      ASSIGNMENT_ID,
      OTHER_ID,
      SUBMISSION_ID,
    );

    expect(requests).toHaveBeenCalledWith({
      assignmentId: ASSIGNMENT_ID,
      a: OTHER_ID,
      b: SUBMISSION_ID,
    });

    expect(result.a).toBe(OTHER_ID);
    expect(result.b).toBe(SUBMISSION_ID);
  });

  it("отправляет ровно один Java-файл как multipart", async () => {
    const requests = vi.fn();

    server.use(
      http.post(
        "*/api/submit/:token",
        async ({ request, params }) => {
          const contentType =
            request.headers.get(
              "content-type",
            );

          const form =
            await request.formData();

          const files =
            form.getAll("file");

          const file = files[0];

          requests({
            token: params.token,
            contentType,
            count: files.length,
            name:
              file instanceof File
                ? file.name
                : null,
          });

          return HttpResponse.json(
            {
              submissionId: SUBMISSION_ID,
              submittedAt:
                "2026-11-20T14:00:00Z",
            },
            { status: 201 },
          );
        },
      ),
    );

    const receipt = await submitJavaFile(
      "test-token",
      new File(
        ["public class Solution {}\n"],
        "Solution.java",
        { type: "text/plain" },
      ),
    );

    expect(receipt.submissionId).toBe(
      SUBMISSION_ID,
    );

    expect(requests).toHaveBeenCalledOnce();

    const request = requests.mock.calls[0]?.[0];

    expect(request).toMatchObject({
      token: "test-token",
      count: 1,
      name: "Solution.java",
    });

    expect(
      request.contentType,
    ).toMatch(/^multipart\/form-data; boundary=/);
  });

  it("сохраняет код и HTTP-статус конфликта", async () => {
    server.use(
      http.post(
        "*/api/assignments/:assignmentId/students/:studentId/reset",
        () =>
          HttpResponse.json(
            {
              code: "SUBMISSION_CHANGED",
              message:
                "Текущая работа изменилась. Обновите список.",
            },
            { status: 409 },
          ),
      ),
    );

    await expect(
      api.resetStudentAttempt(
        ASSIGNMENT_ID,
        STUDENT_ID,
        SUBMISSION_ID,
      ),
    ).rejects.toMatchObject({
      status: 409,
      code: "SUBMISSION_CHANGED",
    });
  });
});
