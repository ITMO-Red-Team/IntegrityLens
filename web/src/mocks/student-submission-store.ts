
import type {
  SubmissionReceipt,
} from "../api/target/students";

import type {
  SubmissionRow,
} from "../api/target/types";

export const DEMO_STUDENT_ID =
  "00000000-0000-4000-8000-000000000801";

export type StoredWork = {
  assignmentId: string;
  studentId: string;
  fullName: string;
  fileName: string;
  source: string;
  receipt: SubmissionReceipt;
  attemptOpen: boolean;
};

const works = new Map<string, StoredWork>();

function key(
  assignmentId: string,
  studentId: string,
): string {
  return `${assignmentId}:${studentId}`;
}

export function resetMockWorkStore() {
  works.clear();
}

export function getMockWork(
  assignmentId: string,
  studentId = DEMO_STUDENT_ID,
): StoredWork | undefined {
  return works.get(
    key(assignmentId, studentId),
  );
}

export function getMockWorkBySubmissionId(
  submissionId: string,
): StoredWork | undefined {
  return [...works.values()].find(
    (work) =>
      work.receipt.submissionId === submissionId,
  );
}

export function listMockWorks(
  assignmentId: string,
): StoredWork[] {
  return [...works.values()].filter(
    (work) =>
      work.assignmentId === assignmentId,
  );
}

export function listMockSubmissionRows(
  assignmentId: string,
): SubmissionRow[] {
  return listMockWorks(assignmentId).map(
    (work): SubmissionRow => ({
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
    }),
  );
}

export function saveMockWork(
  assignmentId: string,
  source: string,
  fileName: string,
  studentId = DEMO_STUDENT_ID,
  fullName = "Студент (демо)",
): SubmissionReceipt {
  const receipt: SubmissionReceipt = {
    submissionId: crypto.randomUUID(),
    submittedAt: new Date().toISOString(),
  };

  works.set(
    key(assignmentId, studentId),
    {
      assignmentId,
      studentId,
      fullName,
      fileName,
      source,
      receipt,
      attemptOpen: false,
    },
  );

  return receipt;
}

export function openMockWorkAttempt(
  assignmentId: string,
  studentId: string,
  expectedSubmissionId: string,
): "ok" | "not_found" | "changed" {
  const work = getMockWork(
    assignmentId,
    studentId,
  );

  if (!work) {
    return "not_found";
  }

  if (
    work.receipt.submissionId !==
    expectedSubmissionId
  ) {
    return "changed";
  }

  work.attemptOpen = true;

  return "ok";
}

export function isMockWorkAttemptOpen(
  assignmentId: string,
  studentId = DEMO_STUDENT_ID,
): boolean {
  const work = getMockWork(
    assignmentId,
    studentId,
  );

  return !work || work.attemptOpen;
}
