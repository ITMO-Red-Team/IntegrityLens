
import type {
  AssignmentSummary,
  SubmissionList,
  Score,
  SubmissionDetail,
PairResult,
} from "../api/target/types";

export type MockScenario =
  | "success"
  | "empty"
  | "pending"
  | "error";

export const MOCK_IDS = {
  success: "00000000-0000-4000-8000-000000000101",
  empty: "00000000-0000-4000-8000-000000000102",
  pending: "00000000-0000-4000-8000-000000000103",
  error: "00000000-0000-4000-8000-000000000104",
} as const;

export const SCORES = {
  success: {
    status: "ok",
    value: 0.82,
  },
  ai: {
    status: "ok",
    value: 0.18,
  },
  pending: {
    status: "pending",
    value: null,
  },
  failed: {
    status: "failed",
    value: null,
    reason: {
      code: "ANALYSIS_FAILED",
      message: "Ошибка анализа работы",
    },
  },
  notApplicable: {
    status: "not_applicable",
    value: null,
  },
} satisfies Record<string, Score>;

export const mockAssignments: AssignmentSummary[] = [
  {
    id: MOCK_IDS.success,
    title: "Успешная проверка",
    createdAt: "2026-11-20T10:00:00Z",
    submissionCount: 2,
    analysis: { completed: 2, failed: 0 },
  },
  {
    id: MOCK_IDS.empty,
    title: "Пустое задание",
    createdAt: "2026-11-20T11:00:00Z",
    submissionCount: 0,
    analysis: { completed: 0, failed: 0 },
  },
  {
    id: MOCK_IDS.pending,
    title: "Проверка выполняется",
    createdAt: "2026-11-20T12:00:00Z",
    submissionCount: 1,
    analysis: { completed: 0, failed: 0 },
  },
  {
    id: MOCK_IDS.error,
    title: "Ошибка проверки",
    createdAt: "2026-11-20T13:00:00Z",
    submissionCount: 1,
    analysis: { completed: 1, failed: 1 },
  },
];

const createSubmission = (
  studentId: string,
  submissionId: string,
  fullName: string,
  similarity: Score,
  ai: Score,
) => ({
  studentId,
  submissionId,
  fullName,
  submittedAt: "2026-11-20T14:00:00Z",
  similarity,
  ai,
});

export const mockSubmissions: Record<string, SubmissionList> = {
  [MOCK_IDS.success]: {
    page: 1,
    pageSize: 20,
    total: 2,
    items: [
      createSubmission(
        "00000000-0000-4000-8000-000000000201",
        "00000000-0000-4000-8000-000000000301",
        "Иванов Иван",
        SCORES.success,
        SCORES.ai,
      ),
      createSubmission(
        "00000000-0000-4000-8000-000000000202",
        "00000000-0000-4000-8000-000000000302",
        "Петров Пётр",
        SCORES.notApplicable,
        SCORES.ai,
      ),
    ],
  },

  [MOCK_IDS.empty]: {
    page: 1,
    pageSize: 20,
    total: 0,
    items: [],
  },

  [MOCK_IDS.pending]: {
    page: 1,
    pageSize: 20,
    total: 1,
    items: [
      createSubmission(
        "00000000-0000-4000-8000-000000000203",
        "00000000-0000-4000-8000-000000000303",
        "Сидоров Алексей",
        SCORES.success,
        SCORES.pending,
      ),
    ],
  },

  [MOCK_IDS.error]: {
    page: 1,
    pageSize: 20,
    total: 1,
    items: [
      createSubmission(
        "00000000-0000-4000-8000-000000000204",
        "00000000-0000-4000-8000-000000000304",
        "Смирнова Анна",
        SCORES.failed,
        SCORES.ai,
      ),
    ],
  },
};


export const mockSubmissionDetails: Record<
  string,
  SubmissionDetail
> = {};

for (const assignment of mockAssignments) {
  const submissions =
    mockSubmissions[assignment.id]?.items ?? [];

  for (const submission of submissions) {
    mockSubmissionDetails[submission.submissionId] = {
      ...submission,
      suspicious:
        submission.ai.status === "ok"
          ? [
              {
                startIndex: 10,
                endIndex: 45,
                confidence: 0.85,
              },
            ]
          : [],
      similar: submissions
        .filter(
          (other) =>
            other.submissionId !== submission.submissionId,
        )
        .map((other) => ({
          submissionId: other.submissionId,
          assignmentId: assignment.id,
          assignmentTitle: assignment.title,
          score: submission.similarity,
        })),
    };
  }
}

export function createMockPair(
  a: string,
  b: string,
  scenario: MockScenario,
): PairResult {
  const score =
    scenario === "pending"
      ? SCORES.pending
      : scenario === "error"
        ? SCORES.failed
        : scenario === "empty"
          ? SCORES.notApplicable
          : SCORES.success;

  return {
    a,
    b,
    score,
    methods: {
      structsim: {
        score,
        details: {},
        fragments:
          score.status === "ok"
            ? [
                {
                  a: [5, 15],
                  b: [8, 18],
                },
              ]
            : [],
      },
    },
  };
}
