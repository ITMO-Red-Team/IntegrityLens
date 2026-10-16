import { strFromU8, unzipSync } from "fflate";
import { delay, http, HttpResponse } from "msw";
import type { Decision, FeedbackInput } from "../api/types";
import type { CorpusItem } from "./corpus";
import * as db from "./db";
import { clusters } from "./engine";

import { MAX_ARCHIVE_BYTES, MAX_ARCHIVE_MB, MAX_WORKS, MIN_WORKS } from "../lib/constants";
const DECISIONS: Decision[] = ["confirmed", "acceptable", "false_positive"];

const latency = () => delay(import.meta.env.MODE === "test" ? 0 : 150);
const error = (status: number, message: string) => HttpResponse.json({ message }, { status });

async function readArchive(archive: File): Promise<CorpusItem[]> {
  const entries = unzipSync(new Uint8Array(await archive.arrayBuffer()));
  return Object.entries(entries)
    .filter(([path]) => /\.java$/i.test(path) && !path.startsWith("__MACOSX/"))
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([, bytes], i) => {
      const n = String(i + 1).padStart(3, "0");
      return {
        id: `S-${n}`,
        fileName: `work_${n}.java`,
        submittedAt: new Date(archive.lastModified || Date.now()).toISOString(),
        code: strFromU8(bytes),
      };
    });
}

export const handlers = [

  http.get("*/api/batches", async () => {
    await latency();
    return HttpResponse.json(db.listBatches());
  }),

  http.post("*/api/batches", async ({ request }) => {
    await latency();
    const fd = await request.formData();
    const archive = fd.get("archive");
    if (!(archive instanceof File)) return error(400, "Прикрепите архив .zip");
        if (archive.size > MAX_ARCHIVE_BYTES) return error(413, `Архив больше ${MAX_ARCHIVE_MB} МБ`);

    let items: CorpusItem[];
    try {
      items = await readArchive(archive);
    } catch {
      return error(400, "Файл не похож на zip-архив");
    }
      if (items.length < MIN_WORKS || items.length > MAX_WORKS)
        return error(400, `В архиве файлов .java: ${items.length}. Нужно от ${MIN_WORKS} до ${MAX_WORKS}`);

    const assignment = archive.name.replace(/\.zip$/i, "");
    const batch = db.createBatch({ course: "", assignment }, items, import.meta.env.MODE === "test" ? 0 : 2000);
    return HttpResponse.json(batch, { status: 202 });
  }),

  http.get("*/api/batches/:batchId", async ({ params }) => {
    await latency();
    const b = db.currentBatch(String(params.batchId));
    return b ? HttpResponse.json(b) : error(404, "Пакет не найден");
  }),

  http.get("*/api/batches/:batchId/pairs", async ({ params, request }) => {
    await latency();
    const id = String(params.batchId);
    const pairs = db.listPairs(id);
    const batch = db.currentBatch(id);
    if (!pairs || !batch) return error(404, "Пакет не найден");
    if (batch.status !== "done") return HttpResponse.json({ pairs: [], clusters: [], total: 0 });
    const threshold = Number(new URL(request.url).searchParams.get("threshold") ?? 45);
    return HttpResponse.json({
      pairs,
      clusters: clusters(batch.submissions.map((s) => s.id), pairs, threshold),
      total: pairs.length,
    });
  }),
  http.get("*/api/pairs/:pairId", async ({ params }) => {
    await latency();
    const p = db.getPair(String(params.pairId));
    return p ? HttpResponse.json(p) : error(404, "Пара не найдена");
  }),
  http.put("*/api/pairs/:pairId/feedback", async ({ params, request }) => {
    await latency();
    const id = String(params.pairId);
    if (!db.hasPair(id)) return error(404, "Пара не найдена");
    const body = (await request.json()) as Partial<FeedbackInput>;
    if (!body.decision || !DECISIONS.includes(body.decision)) return error(400, "Выберите решение");
    return HttpResponse.json(db.saveFeedback(id, { decision: body.decision, comment: (body.comment ?? "").slice(0, 2000) }));
  }),
];