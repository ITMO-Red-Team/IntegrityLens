import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useMemo } from "react";
import { POLL_MS } from "../lib/constants";
import { makeLabels } from "../lib/labels";
import { api } from "./client";
import type { Batch, BatchStatus, FeedbackRequest, NewBatchInput } from "./types";

export const qk = {
  batches: ["batches"] as const,
  batch: (id: string) => ["batch", id] as const,
  submissions: (id: string) => ["submissions", id] as const,
  pairs: (id: string) => ["pairs", id] as const,
  pair: (id: string) => ["pair", id] as const,
  source: (id: string) => ["source", id] as const,
};

export const isFinished = (s: BatchStatus | undefined) => s === "done" || s === "failed";

export function useBatches() {
  return useQuery({ queryKey: qk.batches, queryFn: api.listBatches });
}

export function useBatch(
  id: string | undefined,
  pollWhile: (batch: Batch) => boolean = (b) => !isFinished(b.status),
) {
  return useQuery({
    queryKey: qk.batch(id ?? ""),
    queryFn: () => api.getBatch(id!),
    enabled: !!id,
    refetchInterval: (q) => (q.state.data && pollWhile(q.state.data) ? POLL_MS : false),
  });
}

export function useSubmissions(batchId: string | undefined) {
  return useQuery({
    queryKey: qk.submissions(batchId ?? ""),
    queryFn: () => api.listSubmissions(batchId!),
    enabled: !!batchId,
  });
}

export function useLabels(batchId: string | undefined) {
  const { data } = useSubmissions(batchId);
  return useMemo(() => makeLabels(data?.submissions ?? []), [data]);
}

// Второй метод сравнения может завершиться после открытия страницы: оценки обновляются, пока он идёт.
export function usePairs(batchId: string | undefined, enabled = true, refresh = false) {
  return useQuery({
    queryKey: qk.pairs(batchId ?? ""),
    queryFn: () => api.listPairs(batchId!),
    enabled: !!batchId && enabled,
    refetchInterval: refresh ? POLL_MS : false,
  });
}

export function usePair(pairId: string, refresh = false) {
  return useQuery({
    queryKey: qk.pair(pairId),
    queryFn: () => api.getPair(pairId),
    refetchInterval: refresh ? POLL_MS : false,
  });
}

export function useSource(submissionId: string | undefined) {
  return useQuery({
    queryKey: qk.source(submissionId ?? ""),
    queryFn: () => api.getSource(submissionId!),
    enabled: !!submissionId,
    staleTime: Infinity,
  });
}

export function useCreateBatch() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: NewBatchInput) => api.createBatch(input),
    onSuccess: () => qc.invalidateQueries({ queryKey: qk.batches }),
  });
}

export function useSaveFeedback(pairId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: FeedbackRequest) => api.saveFeedback(pairId, input),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: qk.pair(pairId) });
      qc.invalidateQueries({ queryKey: ["pairs"] });
    },
  });
}