
import {
  useQuery,
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";

import {
  api,
  type SubmissionSort,
} from "./client";

import type {
  CreateAssignmentRequest,
} from "./types";

export const qk = {
  assignments: ["target", "assignments"] as const,

  assignment: (id: string) =>
    ["target", "assignment", id] as const,

  submissions: (id: string) =>
    ["target", "submissions", id] as const,

  submissionsPage: (
    id: string,
    sort: SubmissionSort,
    page: number,
    pageSize: number,
  ) =>
    [
      ...qk.submissions(id),
      sort,
      page,
      pageSize,
    ] as const,

  submission: (
    assignmentId: string,
    submissionId: string,
  ) =>
    [
      "target",
      "submission",
      assignmentId,
      submissionId,
    ] as const,

  pair: (
    assignmentId: string,
    a: string,
    b: string,
  ) =>
    ["target", "pair", assignmentId, a, b] as const,
};

export function useAssignments() {
  return useQuery({
    queryKey: qk.assignments,
    queryFn: api.listAssignments,
    refetchInterval: 5_000,
  });
}

export function useAssignment(
  id: string | undefined,
) {
  return useQuery({
    queryKey: qk.assignment(id ?? ""),
    queryFn: () => api.getAssignment(id!),
    enabled: !!id,
    refetchInterval: 3_000,
  });
}

export function useSubmissions(
  assignmentId: string | undefined,
  sort: SubmissionSort = "similarity",
  page = 1,
  pageSize = 20,
) {
  return useQuery({
    queryKey: qk.submissionsPage(
      assignmentId ?? "",
      sort,
      page,
      pageSize,
    ),
    queryFn: () =>
      api.listSubmissions(
        assignmentId!,
        sort,
        page,
        pageSize,
      ),
    enabled: !!assignmentId,
    refetchInterval: 3_000,
  });
}

export function useSubmission(
  assignmentId: string | undefined,
  submissionId: string | undefined,
) {
  return useQuery({
    queryKey: qk.submission(
      assignmentId ?? "",
      submissionId ?? "",
    ),
    queryFn: () =>
      api.getSubmission(
        assignmentId!,
        submissionId!,
      ),
    enabled: !!assignmentId && !!submissionId,
    refetchInterval: 3_000,
  });
}

export function usePair(
  assignmentId: string | undefined,
  a: string | undefined,
  b: string | undefined,
) {
  return useQuery({
    queryKey: qk.pair(
      assignmentId ?? "",
      a ?? "",
      b ?? "",
    ),
    queryFn: () =>
      api.getPair(
        assignmentId!,
        a!,
        b!,
      ),
    enabled: !!assignmentId && !!a && !!b,
    refetchInterval: 3_000,
  });
}

export function useCreateAssignment() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: (input: CreateAssignmentRequest) =>
      api.createAssignment(input),

    onSuccess: (created) => {
      qc.setQueryData(
        qk.assignment(created.id),
        created,
      );

      void qc.invalidateQueries({
        queryKey: qk.assignments,
      });
    },
  });
}

export function useUpdateAssignmentSources() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: ({
      assignmentId,
      sourceAssignmentIds,
    }: {
      assignmentId: string;
      sourceAssignmentIds: string[];
    }) =>
      api.updateAssignmentSources(
        assignmentId,
        sourceAssignmentIds,
      ),

    onSuccess: (updated) => {
      qc.setQueryData(
        qk.assignment(updated.id),
        updated,
      );

      void qc.invalidateQueries({
        queryKey: qk.assignments,
      });

      void qc.invalidateQueries({
        queryKey: qk.submissions(updated.id),
      });
    },
  });
}

export function useDeleteAssignment() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: api.deleteAssignment,

    onSuccess: (_, assignmentId) => {
      qc.removeQueries({
        queryKey: qk.assignment(assignmentId),
      });

      qc.removeQueries({
        queryKey: qk.submissions(assignmentId),
      });

      void qc.invalidateQueries({
        queryKey: qk.assignments,
      });
    },
  });
}

export function useResetStudentAttempt() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: ({
      assignmentId,
      studentId,
      expectedSubmissionId,
    }: {
      assignmentId: string;
      studentId: string;
      expectedSubmissionId: string;
    }) =>
      api.resetStudentAttempt(
        assignmentId,
        studentId,
        expectedSubmissionId,
      ),

    onSuccess: (_, variables) => {
      void qc.invalidateQueries({
        queryKey: qk.submissions(
          variables.assignmentId,
        ),
      });

      void qc.invalidateQueries({
        queryKey: qk.assignment(
          variables.assignmentId,
        ),
      });
    },

    onError: (error, variables) => {
      if (
        error instanceof Error &&
        "status" in error &&
        error.status === 409
      ) {
        void qc.invalidateQueries({
          queryKey: qk.submissions(
            variables.assignmentId,
          ),
        });
      }
    },
  });
}
