
import { http, HttpResponse } from "msw";

import type {
  SubmissionDetail,
} from "../api/target/types";

import {
  getMockAssignmentByToken,
} from "./assignment-write-handlers";

import {
  getMockWorkBySubmissionId,
} from "./student-submission-store";

function notFound() {
  return HttpResponse.json(
    {
      code: "NOT_FOUND",
      message: "Работа не найдена.",
    },
    { status: 404 },
  );
}

function isCreatedAssignment(
  assignmentId: string,
): boolean {
  return getMockAssignmentByToken(
    `mock-${assignmentId}`,
  ) !== undefined;
}

export const studentWorkReadHandlers = [
  http.get(
    "*/api/assignments/:assignmentId/submissions/:submissionId",
    ({ params }) => {
      const assignmentId = String(
        params.assignmentId,
      );

      const submissionId = String(
        params.submissionId,
      );

      // Демонстрационные задания обслуживают
      // существующие targetHandlers.
      if (!isCreatedAssignment(assignmentId)) {
        return undefined;
      }

      const work = getMockWorkBySubmissionId(
        submissionId,
      );

      if (
        !work ||
        work.assignmentId !== assignmentId
      ) {
        return notFound();
      }

      const detail: SubmissionDetail = {
        studentId: work.studentId,
        submissionId:
          work.receipt.submissionId,
        fullName: work.fullName,
        submittedAt:
          work.receipt.submittedAt,
        similarity: {
          status: "pending",
          value: null,
        },
        ai: {
          status: "pending",
          value: null,
        },
        suspicious: [],
        similar: [],
      };

      return HttpResponse.json(detail);
    },
  ),

  http.get(
    "*/api/submissions/:submissionId/source",
    ({ params }) => {
      const submissionId = String(
        params.submissionId,
      );

      const work = getMockWorkBySubmissionId(
        submissionId,
      );

      if (!work) {
        // Передаём запрос существующим обработчикам:
        // новые демонстрационные исходники
        // и старые пакетные проверки.
        return undefined;
      }

      if (
        !getMockAssignmentByToken(
          `mock-${work.assignmentId}`,
        )
      ) {
        return notFound();
      }

      return new HttpResponse(
        work.source,
        {
          status: 200,
          headers: {
            "Content-Type":
              "text/plain; charset=utf-8",
          },
        },
      );
    },
  ),
];
