import { strFromU8, unzipSync } from "fflate";
import { delay, http, HttpResponse } from "msw";
import type { Decision, FeedbackRequest } from "../api/types";
import { MAX_ARCHIVE_BYTES, MAX_WORKS, MIN_WORKS } from "../lib/constants";
import * as db from "./db";

const DECISIONS: Decision[] = ["confirmed", "acceptable", "false_positive"];
const SKIP = /(^|\/)(__MACOSX\/|\.DS_Store$|Thumbs\.db$)/;
const isTest = import.meta.env.MODE === "test";

const latency = () => delay(isTest ? 0 : 150);
const error = (status: number, code: string, message: string, details: Record<string, unknown> = {}) =>
  HttpResponse.json({ code, message, details }, { status });
const notFound = () => error(404, "NOT_FOUND", "Объект не найден");

export const handlers = [
  http.get("*/api/batches", async () => {
    await latency();
    return HttpResponse.json(db.listBatches());
  }),

  http.post("*/api/batches", async ({ request }) => {
    await latency();
    const fd = await request.formData();
    const archive = fd.get("archive");
    const language = fd.get("language");
    const title = String(fd.get("title") ?? "").trim();
    if (!(archive instanceof File) || !language) return error(400, "VALIDATION_ERROR", "Некорректный запрос");
    if (language !== "java") return error(400, "UNSUPPORTED_LANGUAGE", "В MVP поддерживается только Java");
    if (archive.size > MAX_ARCHIVE_BYTES) return error(413, "ARCHIVE_TOO_LARGE", "Размер архива превышает 10 МиБ");

    let entries: Record<string, Uint8Array>;
    try {
      entries = unzipSync(new Uint8Array(await archive.arrayBuffer()));
    } catch {
      return error(400, "NOT_A_ZIP", "Файл не является корректным ZIP-архивом");
    }
    const files = Object.entries(entries).filter(([path]) => !path.endsWith("/") && !SKIP.test(path));
    const unsupported = files.map(([p]) => p).filter((p) => !p.endsWith(".java"));
    if (unsupported.length)
      return error(400, "UNSUPPORTED_FILES", "Допустимы только файлы .java", { files: unsupported.slice(0, 10) });
    if (files.length < MIN_WORKS || files.length > MAX_WORKS)
      return error(400, "FILE_COUNT_OUT_OF_RANGE", `Требуется от ${MIN_WORKS} до ${MAX_WORKS} работ`, {
        found: files.length,
        min: MIN_WORKS,
        max: MAX_WORKS,
      });

    const items = files.map(([originalName, bytes]) => ({ originalName, code: strFromU8(bytes) }));
    const batch = db.createBatch(title || archive.name, items, isTest ? 0 : 3000);
    return HttpResponse.json(batch, { status: 202 });
  }),

  http.get("*/api/batches/:batchId", async ({ params }) => {
    await latency();
    const b = db.getBatch(String(params.batchId));
    return b ? HttpResponse.json(b) : notFound();
  }),

  http.get("*/api/batches/:batchId/submissions", async ({ params }) => {
    await latency();
    const list = db.listSubmissions(String(params.batchId));
    return list ? HttpResponse.json({ submissions: list, total: list.length }) : notFound();
  }),

  http.get("*/api/submissions/:submissionId/source", async ({ params }) => {
    await latency();
    const text = db.getSource(String(params.submissionId));
    return text === undefined
      ? notFound()
      : new HttpResponse(text, { headers: { "Content-Type": "text/plain;charset=UTF-8" } });
  }),

  http.get("*/api/batches/:batchId/pairs", async ({ params }) => {
    await latency();
    const pairs = db.listPairs(String(params.batchId));
    return pairs ? HttpResponse.json({ pairs, total: pairs.length }) : notFound();
  }),

  http.get("*/api/pairs/:pairId", async ({ params }) => {
    await latency();
    const p = db.getPair(String(params.pairId));
    return p ? HttpResponse.json(p) : notFound();
  }),

  http.put("*/api/pairs/:pairId/feedback", async ({ params, request }) => {
    await latency();
    const id = String(params.pairId);
    if (!db.getPair(id)) return notFound();
    const body = (await request.json()) as Partial<FeedbackRequest>;
    if (!body.decision || !DECISIONS.includes(body.decision)) return error(400, "VALIDATION_ERROR", "Некорректный запрос");
    return HttpResponse.json(db.saveFeedback(id, body.decision, (body.comment ?? "").slice(0, 10000)));
  }),

  http.get("*/api/health", () => HttpResponse.json({ status: "UP" })),
];