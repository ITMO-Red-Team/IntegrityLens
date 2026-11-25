
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

import {
  getMockWork,
  listMockSubmissionRows,
  openMockWorkAttempt,
  resetMockWorkStore,
} from "./student-submission-store";

const createdAssignments =
  new Map<string, Assignment>();

const editedAssignments =
  new Map<string, Assignment>();

const deletedAssignments =
  new Set<string>();

const resetAttempts =
  new Set<string>();

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

function originalAssignment(
  id: string,
): Assignment | undefined {
  const original = mockAssignments.find(
    (assignment) =>
      assignment.id === id,
  );

  if (!original) {
    return undefined;
  }

  return {
    ...original,
    submissionUrl:
      `http://localhost:5173/submit/${original.id}`,
    sourceAssignmentIds: [],
  };
}

function getAssignment(
  id: string,
): Assignment | undefined {
  if (
    deletedAssignments.has(id)
  ) {
    return undefined;
  }

  const assignment =
    createdAssignments.get(id) ??
    editedAssignments.get(id) ??
    originalAssignment(id);

  if (!assignment) {
    return undefined;
  }

  if (!createdAssignments.has(id)) {
    return assignment;
  }

  const rows =
    listMockSubmissionRows(id);

  return {
    ...assignment,
    submissionCount: rows.length,
    analysis: {
      completed: 0,
      failed: 0,
    },
  };
}

export function getMockAssignmentByToken(
  token: string,
): Assignment | undefined {
  if (
    token.startsWith("mock-")
  ) {
    const id = token.slice(
      "mock-".length,
    );

    if (
      !createdAssignments.has(id)
    ) {
      return undefined;
    }

    return getAssignment(id);
  }

  if (
    !originalAssignment(token)
  ) {
    return undefined;
  }

  return getAssignment(token);
}

function allAssignments(): AssignmentSummary[] {
  const ids = [
    ...mockAssignments.map(
      (assignment) =>
        assignment.id,
    ),
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
): string {
  return `${assignmentId}:${studentId}`;
}

export function resetMockAssignments() {
  createdAssignments.clear();
  editedAssignments.clear();
  deletedAssignments.clear();
  resetAttempts.clear();
  resetMockWorkStore();
}

export const assignmentWriteHandlers = [
  http.post(
    "*/api/assignments",
    async ({ request }) => {
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
        !Array.isArray(
          sourceAssignmentIds,
        ) ||
        !sourceAssignmentIds.every(
          (id): id is string =>
            typeof id === "string" &&
            isUuid(id),
        ) ||
        new Set(
          sourceAssignmentIds,
        ).size !==
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
          (id) =>
            !getAssignment(id),
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
        createdAt:
          new Date().toISOString(),
        submissionCount: 0,
        analysis: {
          completed: 0,
          failed: 0,
        },
        sourceAssignmentIds,
        submissionUrl:
          `${new URL(request.url).origin}/submit/mock-${id}`,
      };

      createdAssignments.set(
        id,
        assignment,
      );

      return HttpResponse.json(
        assignment,
        { status: 201 },
      );
    },
  ),

  http.get(
    "*/api/assignments",
    ({ request }) => {
      const url = new URL(
        request.url,
      );

      if (
        url.searchParams.get(
          "mockHttpError",
        ) === "1"
      ) {
        return apiError(
          503,
          "SERVICE_UNAVAILABLE",
          "Сервис временно недоступен",
        );
      }

      if (
        url.searchParams.get(
          "mockScenario",
        ) === "empty"
      ) {
        return HttpResponse.json([]);
      }

      return HttpResponse.json(
        allAssignments(),
      );
    },
  ),

  http.get(
    "*/api/assignments/:assignmentId",
    ({ params }) => {
      const assignment =
        getAssignment(
          String(
            params.assignmentId,
          ),
        );

      if (!assignment) {
        return apiError(
          404,
          "NOT_FOUND",
          "Задание не найдено.",
        );
      }

      return HttpResponse.json(
        assignment,
      );
    },
  ),

  http.get(
    "*/api/assignments/:assignmentId/submissions",
    ({ params, request }) => {
      const assignmentId = String(
        params.assignmentId,
      );

      if (
        deletedAssignments.has(
          assignmentId,
        )
      ) {
        return apiError(
          404,
          "NOT_FOUND",
          "Задание не найдено.",
        );
      }

      if (
        !createdAssignments.has(
          assignmentId,
        )
      ) {
        return undefined;
      }

      const url = new URL(
        request.url,
      );

      const page = Number(
        url.searchParams.get(
          "page",
        ) ?? 1,
      );

      const pageSize = Number(
        url.searchParams.get(
          "pageSize",
        ) ?? 20,
      );

      if (
        !Number.isInteger(page) ||
        !Number.isInteger(pageSize) ||
        page < 1 ||
        pageSize < 1 ||
        pageSize > 100
      ) {
        return apiError(
          422,
          "VALIDATION_ERROR",
          "Некорректная пагинация.",
        );
      }

      const sort =
        url.searchParams.get(
          "sort",
        ) ?? "similarity";

      if (
        sort !== "similarity" &&
        sort !== "ai"
      ) {
        return apiError(
          422,
          "VALIDATION_ERROR",
          "Некорректная сортировка.",
        );
      }

      const rows =
        listMockSubmissionRows(
          assignmentId,
        );

      rows.sort(
        (a, b) =>
          a.submissionId.localeCompare(
            b.submissionId,
          ),
      );

      const offset =
        (page - 1) * pageSize;

      const result: SubmissionList = {
        page,
        pageSize,
        total: rows.length,
        items: rows.slice(
          offset,
          offset + pageSize,
        ),
      };

      return HttpResponse.json(
        result,
      );
    },
  ),

  http.put(
    "*/api/assignments/:assignmentId/sources",
    async ({
      params,
      request,
    }) => {
      const id = String(
        params.assignmentId,
      );

      const assignment =
        getAssignment(id);

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

      const sources =
        body.sourceAssignmentIds;

      if (
        !Array.isArray(sources) ||
        !sources.every(
          (source): source is string =>
            typeof source === "string" &&
            isUuid(source),
        ) ||
        new Set(sources).size !==
          sources.length ||
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
          (source) =>
            !getAssignment(
              source,
            ),
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
        sourceAssignmentIds:
          sources,
      };

      if (
        createdAssignments.has(
          id,
        )
      ) {
        createdAssignments.set(
          id,
          updated,
        );
      } else {
        editedAssignments.set(
          id,
          updated,
        );
      }

      return HttpResponse.json(
        updated,
      );
    },
  ),

  http.delete(
    "*/api/assignments/:assignmentId",
    ({ params }) => {
      const id = String(
        params.assignmentId,
      );

      if (
        !getAssignment(id)
      ) {
        return apiError(
          404,
          "NOT_FOUND",
          "Задание не найдено.",
        );
      }

      deletedAssignments.add(
        id,
      );

      return new HttpResponse(
        null,
        { status: 204 },
      );
    },
  ),

  http.post(
    "*/api/assignments/:assignmentId/students/:studentId/reset",
    async ({
      params,
      request,
    }) => {
      const assignmentId =
        String(
          params.assignmentId,
        );

      const studentId =
        String(
          params.studentId,
        );

      if (
        !getAssignment(
          assignmentId,
        )
      ) {
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
        typeof body.expectedSubmissionId !==
          "string" ||
        !isUuid(
          body.expectedSubmissionId,
        )
      ) {
        return apiError(
          422,
          "VALIDATION_ERROR",
          "Некорректный идентификатор сдачи.",
        );
      }

      const expectedId =
        body.expectedSubmissionId;

      // Сначала проверяем работу,
      // поступившую через студенческий мок.
      const mockWork =
        getMockWork(
          assignmentId,
          studentId,
        );

      if (mockWork) {
        const outcome =
          openMockWorkAttempt(
            assignmentId,
            studentId,
            expectedId,
          );

        if (
          outcome === "changed"
        ) {
          return apiError(
            409,
            "SUBMISSION_CHANGED",
            "Текущая работа изменилась. Обновите список.",
          );
        }

        return new HttpResponse(
          null,
          { status: 204 },
        );
      }

      // Для готовых демонстрационных
      // заданий сохраняем прежнюю логику.
      const submission =
        mockSubmissions[
          assignmentId
        ]?.items.find(
          (item) =>
            item.studentId ===
            studentId,
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
        expectedId
      ) {
        return apiError(
          409,
          "SUBMISSION_CHANGED",
          "Текущая работа изменилась. Обновите список.",
        );
      }

      resetAttempts.add(
        resetKey(
          assignmentId,
          studentId,
        ),
      );

      return new HttpResponse(
        null,
        { status: 204 },
      );
    },
  ),
];
