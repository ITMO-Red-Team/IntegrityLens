
import { delay, http, HttpResponse } from "msw";

import {
  mockAssignments,
  mockSubmissions,
} from "./target-fixtures";

const latency = () =>
  delay(import.meta.env.MODE === "test" ? 0 : 150);

const notFound = () =>
  HttpResponse.json(
    {
      code: "NOT_FOUND",
      message: "Объект не найден",
    },
    { status: 404 },
  );

export const targetHandlers = [
  // Список заданий
  http.get("*/api/assignments", async () => {
    await latency();

    return HttpResponse.json(mockAssignments);
  }),

  // Информация о задании
  http.get(
    "*/api/assignments/:assignmentId",
    async ({ params }) => {
      await latency();

      const assignment = mockAssignments.find(
        (item) => item.id === params.assignmentId,
      );

      if (!assignment) return notFound();

      return HttpResponse.json({
        ...assignment,
        submissionUrl:
          `http://localhost:5173/submit/${assignment.id}`,
        sourceAssignmentIds: [],
      });
    },
  ),

  // Список работ студентов
  http.get(
    "*/api/assignments/:assignmentId/submissions",
    async ({ params, request }) => {
      await latency();

      const id = String(params.assignmentId);
      const data = mockSubmissions[id];

      if (!data) return notFound();

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
        return HttpResponse.json(
          {
            code: "VALIDATION_ERROR",
            message: "Некорректная пагинация",
          },
          { status: 422 },
        );
      }

      const start = (page - 1) * pageSize;

      return HttpResponse.json({
        page,
        pageSize,
        total: data.total,
        items: data.items.slice(start, start + pageSize),
      });
    },
  ),
];
