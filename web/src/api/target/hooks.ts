
import {
  useQuery,
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";

import { api } from "./client";
import type { CreateAssignmentRequest } from "./types";

export const qk = {
  assignments: ["target", "assignments"] as const,

  assignment: (id: string) =>
    ["target", "assignment", id] as const,

  submissions: (id: string) =>
    ["target", "submissions", id] as const,

  submission: (assignmentId: string, submissionId: string) =>
    ["target", "submission", assignmentId, submissionId] as const,

  pair: (assignmentId: string, a: string, b: string) =>
    ["target", "pair", assignmentId, a, b] as const,
};

export function useAssignments() {
  return useQuery({
    queryKey: qk.assignments,
    queryFn: api.listAssignments,
  });
}

export function useAssignment(id: string | undefined) {
  return useQuery({
    queryKey: qk.assignment(id ?? ""),
    queryFn: () => api.getAssignment(id!),
    enabled: !!id,
  });
}

export function useSubmissions(assignmentId: string | undefined) {
  return useQuery({
    queryKey: qk.submissions(assignmentId ?? ""),
    queryFn: () => api.listSubmissions(assignmentId!),
    enabled: !!assignmentId,
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
      api.getSubmission(assignmentId!, submissionId!),
    enabled: !!assignmentId && !!submissionId,
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
    queryFn: () => api.getPair(assignmentId!, a!, b!),
    enabled: !!assignmentId && !!a && !!b,
  });
}

export function useCreateAssignment() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: (input: CreateAssignmentRequest) =>
      api.createAssignment(input),

    onSuccess: () => {
      qc.invalidateQueries({
        queryKey: qk.assignments,
      });
    },
  });
}

export function useDeleteAssignment() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: api.deleteAssignment,

    onSuccess: () => {
      qc.invalidateQueries({
        queryKey: qk.assignments,
      });
    },
  });
}
