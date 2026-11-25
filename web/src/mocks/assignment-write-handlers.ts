
import { http, HttpResponse } from "msw";

import type {
  Assignment,
  AssignmentSummary,
} from "../api/target/types";

import { mockAssignments } from "./target-fixtures";

const createdAssignments = new Map<string, Assignment>();

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

function isUuid(value: string): boolean {
  return /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(
    value,
  );
}

function allAssignments(): AssignmentSummary[] {
  return [
    ...mockAssignments,
    ...createdAssignments.values(),
  ];
}

export function resetMockAssignments() {
  createdAssignments.clear();
}

export const assignmentWriteHandlers = [
  // Создание задания
  http.post("*/api/assignments", async ({ request }) => {
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

    const { title, sourceAssignmentIds = [] } = body;

    if (
      typeof title !== "string" ||
      title.trim().length === 0 ||
      title.trim().length > 200 ||
      !Array.isArray(sourceAssignmentIds) ||
      !sourceAssignmentIds.every(
        (id): id is string =>
          typeof id === "string" && isUuid(id),
      ) ||
      new Set(sourceAssignmentIds).size !==
        sourceAssignmentIds.length
    ) {
      return apiError(
        422,
        "VALIDATION_ERROR",
        "Проверьте заполнение полей.",
      );
    }

    const existing = allAssignments();

    if (
      sourceAssignmentIds.some(
        (id) => !existing.some(
          (assignment) => assignment.id === id,
        ),
      )
    ) {
      return apiError(
        404,
        "NOT_FOUND",
        "Задание-источник не найдено.",
      );
    }

    const id = crypto.randomUUID();

    const assignment: Assignment = {
      id,
      title: title.trim(),
      createdAt: new Date().toISOString(),
      submissionCount: 0,
      analysis: {
        completed: 0,
        failed: 0,
      },
      sourceAssignmentIds,
      submissionUrl:
        `${new URL(request.url).origin}/submit/mock-${id}`,
    };

    createdAssignments.set(id, assignment);

    return HttpResponse.json(assignment, {
      status: 201,
    });
  }),

  // Новые задания добавляются к существующим фикстурам.
  // Пока ничего не создано, запрос обрабатывает
  // прежний targetHandlers из задачи #41.
  http.get("*/api/assignments", ({ request }) => {
    if (createdAssignments.size === 0) {
      return undefined;
    }

    const url = new URL(request.url);

    if (url.searchParams.get("mockHttpError") === "1") {
      return apiError(
        503,
        "SERVICE_UNAVAILABLE",
        "Сервис временно недоступен",
      );
    }

    if (url.searchParams.get("mockScenario") === "empty") {
      return HttpResponse.json([]);
    }

    return HttpResponse.json(allAssignments());
  }),

  http.get(
    "*/api/assignments/:assignmentId",
    ({ params }) => {
      const assignment = createdAssignments.get(
        String(params.assignmentId),
      );

      if (!assignment) {
        return undefined;
      }

      return HttpResponse.json(assignment);
    },
  ),
];
