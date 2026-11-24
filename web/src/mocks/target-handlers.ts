
import { delay, http, HttpResponse } from "msw";

import {
  MOCK_IDS,
  mockAssignments,
  mockSubmissions,
  mockSubmissionDetails,
  createMockPair,
  type MockScenario,
} from "./target-fixtures";

const latency = () =>
  delay(import.meta.env.MODE === "test" ? 0 : 150);

const error = (
  status: number,
  code: string,
  message: string,
) =>
  HttpResponse.json(
    { code, message },
    { status },
  );

const notFound = () =>
  error(404, "NOT_FOUND", "Объект не найден");

export const targetHandlers = [
  // Получение текущего пользователя
  http.get("*/api/auth/me", async () => {
    await latency();

    return error(
      401,
      "UNAUTHORIZED",
      "Требуется авторизация",
    );
  }),

  // Получение списка заданий
  http.get("*/api/assignments", async ({ request }) => {
    await latency();

    const url = new URL(request.url);

    if (url.searchParams.get("mockHttpError") === "1") {
      return error(
        503,
        "SERVICE_UNAVAILABLE",
        "Сервис временно недоступен",
      );
    }

    const scenario = url.searchParams.get("mockScenario");

    if (scenario === "empty") {
      return HttpResponse.json([]);
    }

    return HttpResponse.json(mockAssignments);
  }),

  // Получение конкретного задания
  http.get(
    "*/api/assignments/:assignmentId",
    async ({ params }) => {
      await latency();

      const assignmentId = String(params.assignmentId);

      const assignment = mockAssignments.find(
        (item) => item.id === assignmentId,
      );

      if (!assignment) {
        return notFound();
      }

      return HttpResponse.json({
        ...assignment,
        submissionUrl:
          `http://localhost:5173/submit/${assignment.id}`,
        sourceAssignmentIds: [],
      });
    },
  ),

  // Получение списка сдач студентов
  http.get(
    "*/api/assignments/:assignmentId/submissions",
    async ({ params, request }) => {
      await latency();

      const assignmentId = String(params.assignmentId);
      const data = mockSubmissions[assignmentId];

      if (!data) {
        return notFound();
      }

      const url = new URL(request.url);

      const page = Number(
        url.searchParams.get("page") ?? 1,
      );

      const pageSize = Number(
        url.searchParams.get("pageSize") ?? 20,
      );

      if (
        !Number.isInteger(page) ||
        !Number.isInteger(pageSize) ||
        page < 1 ||
        pageSize < 1 ||
        pageSize > 100
      ) {
        return error(
          422,
          "VALIDATION_ERROR",
          "Некорректная пагинация",
        );
      }

      const sort = url.searchParams.get("sort") ?? "similarity";

      if (sort !== "similarity" && sort !== "ai") {
        return error(
          422,
          "VALIDATION_ERROR",
          "Некорректная сортировка",
        );
      }

      const sorted = [...data.items].sort((a, b) => {
        const aValue = a[sort].value;
        const bValue = b[sort].value;

        if (aValue === null && bValue !== null) return 1;
        if (aValue !== null && bValue === null) return -1;

        if (aValue !== null && bValue !== null) {
          const difference = bValue - aValue;
          if (difference !== 0) return difference;
        }

        return a.submissionId.localeCompare(b.submissionId);
      });

      const start = (page - 1) * pageSize;

      return HttpResponse.json({
        page,
        pageSize,
        total: data.total,
        items: sorted.slice(start, start + pageSize),
      });
    },
  ),

  // Получение детальной информации о сдаче
  http.get(
    "*/api/assignments/:assignmentId/submissions/:submissionId",
    async ({ params }) => {
      await latency();

      const assignmentId = String(params.assignmentId);
      const submissionId = String(params.submissionId);

      const belongsToAssignment =
        mockSubmissions[assignmentId]?.items.some(
          (item) => item.submissionId === submissionId,
        );

      if (!belongsToAssignment) {
        return notFound();
      }

      const detail = mockSubmissionDetails[submissionId];

      if (!detail) {
        return notFound();
      }

      return HttpResponse.json(detail);
    },
  ),

  // Получение результата сравнения пары
  http.get(
    "*/api/assignments/:assignmentId/pairs/:a/:b",
    async ({ params, request }) => {
      await latency();

      const assignmentId = String(params.assignmentId);
      const a = String(params.a);
      const b = String(params.b);

      const submissions =
        mockSubmissions[assignmentId]?.items ?? [];

      if (
        a === b ||
        !submissions.some((s) => s.submissionId === a) ||
        !submissions.some((s) => s.submissionId === b)
      ) {
        return notFound();
      }

      const requested = new URL(request.url)
        .searchParams.get("mockScenario");

      const scenario: MockScenario =
        requested === "pending" ||
        requested === "error" ||
        requested === "empty"
          ? requested
          : assignmentId === MOCK_IDS.pending
            ? "pending"
            : assignmentId === MOCK_IDS.error
              ? "error"
              : "success";

      return HttpResponse.json(
        createMockPair(a, b, scenario),
      );
    },
  ),
];
