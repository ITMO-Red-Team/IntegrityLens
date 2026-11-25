
import { http, HttpResponse } from "msw";

import type {
  Assignment,
  PairResult,
  Score,
  SimilarWork,
  SubmissionDetail,
  SubmissionRow,
} from "../api/target/types";

import {
  getMockAssignmentByToken,
} from "./assignment-write-handlers";

import {
  createMockPair,
  mockSubmissionDetails,
  mockSubmissions,
} from "./target-fixtures";

import {
  getMockWorkBySubmissionId,
  listMockSubmissionRows,
} from "./student-submission-store";

type Candidate = {
  assignment: Assignment;
  row: SubmissionRow;
};

const pendingScore: Score = {
  status: "pending",
  value: null,
};

function notFound() {
  return HttpResponse.json(
    {
      code: "NOT_FOUND",
      message: "Работа не найдена.",
    },
    { status: 404 },
  );
}

function getAssignment(
  id: string,
): Assignment | undefined {
  return (
    getMockAssignmentByToken(`mock-${id}`) ??
    getMockAssignmentByToken(id)
  );
}

function rowsFor(
  assignmentId: string,
): SubmissionRow[] {
  const saved = listMockSubmissionRows(
    assignmentId,
  );

  const fixture =
    mockSubmissions[assignmentId]?.items ?? [];

  return [...fixture, ...saved];
}

function findCurrent(
  assignmentId: string,
  submissionId: string,
): Candidate | undefined {
  const assignment = getAssignment(
    assignmentId,
  );

  if (!assignment) {
    return undefined;
  }

  const row = rowsFor(
    assignmentId,
  ).find(
    (item) =>
      item.submissionId === submissionId,
  );

  return row
    ? { assignment, row }
    : undefined;
}

function findSourceCandidate(
  assignment: Assignment,
  submissionId: string,
): Candidate | undefined {
  for (const sourceId of assignment.sourceAssignmentIds) {
    const candidate = findCurrent(
      sourceId,
      submissionId,
    );

    if (candidate) {
      return candidate;
    }
  }

  return undefined;
}

function sourceCandidates(
  assignment: Assignment,
): Candidate[] {
  return assignment.sourceAssignmentIds.flatMap(
    (sourceId) => {
      const source = getAssignment(
        sourceId,
      );

      if (!source) {
        return [];
      }

      return rowsFor(sourceId).map(
        (row) => ({
          assignment: source,
          row,
        }),
      );
    },
  );
}

function pairScore(
  a: Candidate,
  b: Candidate,
): Score {
  // Для работ, загруженных студентом,
  // полноценный Structsim в MSW отсутствует.
  const hasUploadedWork =
    !!getMockWorkBySubmissionId(
      a.row.submissionId,
    ) ||
    !!getMockWorkBySubmissionId(
      b.row.submissionId,
    );

  if (hasUploadedWork) {
    return pendingScore;
  }

  if (
    a.row.similarity.status !== "ok" ||
    b.row.similarity.status !== "ok"
  ) {
    return pendingScore;
  }

  return {
    status: "ok",
    value: 0.82,
  };
}

function comparison(
  a: Candidate,
  b: Candidate,
): PairResult {
  const score = pairScore(a, b);

  if (score.status === "ok") {
    return createMockPair(
      a.row.submissionId,
      b.row.submissionId,
      "success",
    );
  }

  return {
    a: a.row.submissionId,
    b: b.row.submissionId,
    score,
    methods: {
      structsim: {
        score,
        details: {},
        fragments: [],
      },
    },
  };
}

function detailFor(
  candidate: Candidate,
): SubmissionDetail {
  const fixture =
    mockSubmissionDetails[
      candidate.row.submissionId
    ];

  if (fixture) {
    return {
      ...fixture,
      similar: [...fixture.similar],
    };
  }

  return {
    ...candidate.row,
    suspicious: [],
    similar: [],
  };
}

export const crossAssignmentHandlers = [
  http.get(
    "*/api/assignments/:assignmentId/submissions/:submissionId",
    ({ params }) => {
      const assignmentId = String(
        params.assignmentId,
      );

      const submissionId = String(
        params.submissionId,
      );

      const main = findCurrent(
        assignmentId,
        submissionId,
      );

      if (!main) {
        return undefined;
      }

      if (
        main.assignment.sourceAssignmentIds.length === 0
      ) {
        return undefined;
      }

      const detail = detailFor(main);

      const sourceSimilar: SimilarWork[] =
        sourceCandidates(
          main.assignment,
        )
          .filter(
            (candidate) =>
              candidate.row.submissionId !==
              submissionId,
          )
          .map((candidate) => ({
            submissionId:
              candidate.row.submissionId,
            assignmentId:
              candidate.assignment.id,
            assignmentTitle:
              candidate.assignment.title,
            score: pairScore(
              main,
              candidate,
            ),
          }));

      const seen = new Set(
        detail.similar.map(
          (item) => item.submissionId,
        ),
      );

      for (const item of sourceSimilar) {
        if (!seen.has(item.submissionId)) {
          detail.similar.push(item);
          seen.add(item.submissionId);
        }
      }

      return HttpResponse.json(
        detail,
      );
    },
  ),

  http.get(
    "*/api/assignments/:assignmentId/pairs/:a/:b",
    ({ params }) => {
      const assignmentId = String(
        params.assignmentId,
      );

      const aId = String(params.a);
      const bId = String(params.b);

      const assignment = getAssignment(
        assignmentId,
      );

      if (!assignment) {
        return undefined;
      }

      if (aId === bId) {
        return notFound();
      }

      const mainA = findCurrent(
        assignmentId,
        aId,
      );

      const mainB = findCurrent(
        assignmentId,
        bId,
      );

      // Внутри одного демонстрационного
      // задания работает старый обработчик.
      if (mainA && mainB) {
        return undefined;
      }

      if (!mainA && !mainB) {
        return notFound();
      }

      const a =
        mainA ??
        findSourceCandidate(
          assignment,
          aId,
        );

      const b =
        mainB ??
        findSourceCandidate(
          assignment,
          bId,
        );

      if (!a || !b) {
        return notFound();
      }

      return HttpResponse.json(
        comparison(a, b),
      );
    },
  ),
];
