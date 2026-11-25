
import { http, HttpResponse } from "msw";

import type {
  Assignment,
  AssignmentSummary,
  SubmissionList,
} from "../api/target/types";

import {
  mockAssignments,
  mockSubmissions,
} from "./target-fixtures";

const createdAssignments = new Map<string, Assignment>();
const editedAssignments = new Map<string, Assignment>();
const deletedAssignments = new Set<string>();
const resetAttempts = new Set<string>();

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

function originalAssignment(id: string): Assignment | undefined {
  const original = mockAssignments.find(
    (assignment) => assignment.id === id,
  );

  if (!original) return undefined;

  return {
    ...original,
    submissionUrl: `http://localhost:5173/submit/${original.id}`,
    sourceAssignmentIds: [],
  };
}

function getAssignment(id: string): Assignment | undefined {
  if (deletedAssignments.has(id)) {
    return undefined;
  }

  return (
    createdAssignments.get(id) ??
    editedAssignments.get(id) ??
    originalAssignment(id)
  );
}

function allAssignments(): AssignmentSummary[] {
  const ids = [
    ...mockAssignments.map((assignment) => assignment.id),
    ...createdAssignments.keys(),
  ];

  return ids
    .map((id) => getAssignment(id))
    .filter(
      (assignment): assignment is Assignment =>
        assignment !== undefined,
    );
}

function resetKey(
  assignmentId: string,
  studentId: string,
) {
  return `${assignmentId}:${studentId}`;
}

export function resetMockAssignments() {
  createdAssignments.clear();
  editedAssignments.clear();
  deletedAssignments.clear();
  resetAttempts.clear();
}

export const assignmentWriteHandlers = [
  http.post("*/api/assignments", async ({ request }) => {
    let body: unknown;

    try {
      body = await request.json();
    } catch {
      return apiError(
        422,
        "VALIDATION_ERROR",
        "Некорректные данные задания.",
      );
    }

    if (!isRecord(body)) {
      return apiError(
        422,
        "VALIDATION_ERROR",
        "Проверьте заполнение полей.",
      );
    }

    const {
      title,
      sourceAssignmentIds = [],
    } = body;

    if (
      typeof title !== "string" ||
      title.trim().length < 1 ||
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

    if (
      sourceAssignmentIds.some(
        (id) => !getAssignment(id),
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

  http.get("*/api/assignments", ({ request }) => {
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
      const assignment = getAssignment(
        String(params.assignmentId),
      );

      if (!assignment) {
        return apiError(
          404,
          "NOT_FOUND",
          "Задание не найдено.",
        );
      }

      return HttpResponse.json(assignment);
    },
  ),

  // Для нового задания сдач ещё нет.
  // Для старых заданий продолжает работать
  // обработчик из target-handlers.ts.
  http.get(
    "*/api/assignments/:assignmentId/submissions",
    ({ params }) => {
      const assignmentId = String(params.assignmentId);

      if (deletedAssignments.has(assignmentId)) {
        return apiError(
          404,
          "NOT_FOUND",
          "Задание не найдено.",
        );
      }

      if (!createdAssignments.has(assignmentId)) {
        return undefined;
      }

      const result: SubmissionList = {
        page: 1,
        pageSize: 20,
        total: 0,
        items: [],
      };

      return HttpResponse.json(result);
    },
  ),

  http.put(
    "*/api/assignments/:assignmentId/sources",
    async ({ params, request }) => {
      const id = String(params.assignmentId);
      const assignment = getAssignment(id);

      if (!assignment) {
        return apiError(
          404,
          "NOT_FOUND",
          "Задание не найдено.",
        );
      }

      let body: unknown;

      try {
        body = await request.json();
      } catch {
        return apiError(
          422,
          "VALIDATION_ERROR",
          "Некорректный список источников.",
        );
      }

      if (!isRecord(body)) {
        return apiError(
          422,
          "VALIDATION_ERROR",
          "Некорректный список источников.",
        );
      }

      const sources = body.sourceAssignmentIds;

      if (
        !Array.isArray(sources) ||
        !sources.every(
          (source): source is string =>
            typeof source === "string" &&
            isUuid(source),
        ) ||
        new Set(sources).size !== sources.length ||
        sources.includes(id)
      ) {
        return apiError(
          422,
          "VALIDATION_ERROR",
          "Некорректный список источников.",
        );
      }

      if (
        sources.some(
          (source) => !getAssignment(source),
        )
      ) {
        return apiError(
          404,
          "NOT_FOUND",
          "Задание-источник не найдено.",
        );
      }

      const updated: Assignment = {
        ...assignment,
        sourceAssignmentIds: sources,
      };

      if (createdAssignments.has(id)) {
        createdAssignments.set(id, updated);
      } else {
        editedAssignments.set(id, updated);
      }

      return HttpResponse.json(updated);
    },
  ),

  http.delete(
    "*/api/assignments/:assignmentId",
    ({ params }) => {
      const id = String(params.assignmentId);

      if (!getAssignment(id)) {
        return apiError(
          404,
          "NOT_FOUND",
          "Задание не найдено.",
        );
      }

      deletedAssignments.add(id);

      return new HttpResponse(null, {
        status: 204,
      });
    },
  ),

  http.post(
    "*/api/assignments/:assignmentId/students/:studentId/reset",
    async ({ params, request }) => {
      const assignmentId = String(params.assignmentId);
      const studentId = String(params.studentId);

      if (!getAssignment(assignmentId)) {
        return apiError(
          404,
          "NOT_FOUND",
          "Задание не найдено.",
        );
      }

      let body: unknown;

      try {
        body = await request.json();
      } catch {
        return apiError(
          422,
          "VALIDATION_ERROR",
          "Некорректные данные запроса.",
        );
      }

      if (
        !isRecord(body) ||
        typeof body.expectedSubmissionId !== "string" ||
        !isUuid(body.expectedSubmissionId)
      ) {
        return apiError(
          422,
          "VALIDATION_ERROR",
          "Некорректный идентификатор сдачи.",
        );
      }

      const submission =
        mockSubmissions[assignmentId]?.items.find(
          (item) => item.studentId === studentId,
        );

      if (!submission) {
        return apiError(
          404,
          "NOT_FOUND",
          "Сдача студента не найдена.",
        );
      }

      if (
        submission.submissionId !==
        body.expectedSubmissionId
      ) {
        return apiError(
          409,
          "SUBMISSION_CHANGED",
          "Текущая работа изменилась. Обновите список.",
        );
      }

      resetAttempts.add(
        resetKey(assignmentId, studentId),
      );

      return new HttpResponse(null, {
        status: 204,
      });
    },
  ),
];
