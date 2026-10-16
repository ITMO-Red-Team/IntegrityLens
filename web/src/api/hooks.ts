import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "./client";
import type { FeedbackInput, NewBatchInput } from "./types";

export const qk = {
  batches: ["batches"] as const,
  batch: (id: string) => ["batch", id] as const,
  pairs: (id: string, threshold: number) => ["pairs", id, threshold] as const,
  pair: (id: string) => ["pair", id] as const,
};

export function useBatches() {
  return useQuery({ queryKey: qk.batches, queryFn: api.listBatches });
}

export function useBatch(id: string) {
  return useQuery({
    queryKey: qk.batch(id),
    queryFn: () => api.getBatch(id),
    refetchInterval: (q) => {
      const s = q.state.data?.status;
      return s === "queued" || s === "processing" ? 800 : false;
    },
  });
}

export function usePairs(batchId: string, threshold: number, enabled = true) {
  return useQuery({
    queryKey: qk.pairs(batchId, threshold),
    queryFn: () => api.listPairs(batchId, threshold),
    enabled,
    placeholderData: (prev) => prev,
  });
}

export function usePair(pairId: string) {
  return useQuery({ queryKey: qk.pair(pairId), queryFn: () => api.getPair(pairId) });
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
    mutationFn: (input: FeedbackInput) => api.saveFeedback(pairId, input),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: qk.pair(pairId) });
      qc.invalidateQueries({ queryKey: ["pairs"] });
    },
  });
}