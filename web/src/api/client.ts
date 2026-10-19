import type {
  ApiErrorBody,
  Batch,
  Feedback,
  FeedbackRequest,
  NewBatchInput,
  PairDetail,
  Pairs,
  Submissions,
} from "./types";

const BASE = (import.meta.env.VITE_API_BASE as string | undefined) || "";

export class ApiError extends Error {
  constructor(
    public status: number,
    public code: string,
    message: string,
    public details: Record<string, unknown> = {},
  ) {
    super(message);
  }
}

function url(path: string) {
  return new URL(`${BASE}/api${path}`, window.location.origin).toString();
}

function humanMessage(body: ApiErrorBody): string {
  const files = body.details?.files;
  if (body.code === "UNSUPPORTED_FILES" && Array.isArray(files) && files.length) {
    return `${body.message}: ${files.join(", ")}`;
  }
  return body.message;
}

async function check(res: Response): Promise<Response> {
  if (res.ok) return res;
  let err = new ApiError(res.status, "HTTP_" + res.status, `Сервер ответил ${res.status}`);
  try {
    const body = (await res.json()) as ApiErrorBody;
    if (body?.code) err = new ApiError(res.status, body.code, humanMessage(body), body.details ?? {});
  } catch {
  }
  throw err;
}

async function json<T>(input: string, init?: RequestInit): Promise<T> {
  const res = await check(await fetch(input, init));
  return (await res.json()) as T;
}

const enc = encodeURIComponent;

export const api = {
  listBatches: () => json<Batch[]>(url("/batches")),

  getBatch: (id: string) => json<Batch>(url(`/batches/${enc(id)}`)),

  createBatch: (input: NewBatchInput) => {
    const fd = new FormData();
    fd.set("archive", input.archive, input.archive.name);
    fd.set("language", input.language);
    if (input.title) fd.set("title", input.title);
    return json<Batch>(url("/batches"), { method: "POST", body: fd });
  },

  listSubmissions: (batchId: string) => json<Submissions>(url(`/batches/${enc(batchId)}/submissions`)),

  getSource: async (submissionId: string) => {
    const res = await check(await fetch(url(`/submissions/${enc(submissionId)}/source`)));
    return res.text();
  },

  listPairs: (batchId: string) => json<Pairs>(url(`/batches/${enc(batchId)}/pairs`)),

  getPair: (pairId: string) => json<PairDetail>(url(`/pairs/${enc(pairId)}`)),

  saveFeedback: (pairId: string, input: FeedbackRequest) =>
    json<Feedback>(url(`/pairs/${enc(pairId)}/feedback`), {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(input),
    }),
};