import type { Batch, Feedback, FeedbackInput, NewBatchInput, PairDetail, PairList } from "./types";

const BASE = (import.meta.env.VITE_API_BASE as string | undefined) || "";

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}

function url(path: string, query?: Record<string, string | number | undefined>) {
  const u = new URL(`${BASE}/api${path}`, window.location.origin);
  Object.entries(query ?? {}).forEach(([k, v]) => v !== undefined && u.searchParams.set(k, String(v)));
  return u.toString();
}

async function request<T>(input: string, init?: RequestInit): Promise<T> {
  const res = await fetch(input, init);
  if (!res.ok) {
    let message = `Сервер ответил ${res.status}`;
    try {
      const body = (await res.json()) as { message?: string };
      if (body.message) message = body.message;
    } catch {
      /* тело без JSON */
    }
    throw new ApiError(res.status, message);
  }
  return (await res.json()) as T;
}

export const api = {
  listBatches: () => request<Batch[]>(url("/batches")),

  getBatch: (id: string) => request<Batch>(url(`/batches/${encodeURIComponent(id)}`)),

  createBatch: (input: NewBatchInput) => {
    const fd = new FormData();
    fd.set("language", input.language);
    fd.set("archive", input.archive, input.archive.name);
    return request<Batch>(url("/batches"), { method: "POST", body: fd });
  },

  listPairs: (batchId: string, threshold: number) =>
    request<PairList>(url(`/batches/${encodeURIComponent(batchId)}/pairs`, { threshold })),

  getPair: (pairId: string) => request<PairDetail>(url(`/pairs/${encodeURIComponent(pairId)}`)),

  saveFeedback: (pairId: string, input: FeedbackInput) =>
    request<Feedback>(url(`/pairs/${encodeURIComponent(pairId)}/feedback`), {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(input),
    }),
};