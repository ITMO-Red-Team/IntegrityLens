import { useEffect, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { useBatch, useLabels, usePairs, useSubmissions } from "../../api/hooks";
import type { Method, PairSummary } from "../../api/types";
import { ErrorBox, Kpi, Loading, PageHeader, RiskMeter, RiskPill } from "../../components/ui";
import { DEFAULT_K, DEFAULT_METHOD, DEFAULT_THRESHOLD, METHODS } from "../../lib/constants";
import { pct } from "../../lib/format";
import { pairLabel } from "../../lib/labels";
import { clusters, DECISION_LABEL, queueMetrics, reviewQueue, scorePct, sortPairs, suggestThreshold } from "../../lib/queue";

const clampInt = (v: string | null, def: number, min: number, max: number) => {
  const n = Number.parseInt(v ?? "", 10);
  return Number.isFinite(n) ? Math.min(max, Math.max(min, n)) : def;
};

const decisionCls = { confirmed: "text-high", acceptable: "text-low", false_positive: "text-accent" } as const;

export function useQueueParams() {
  const [params, setParams] = useSearchParams();
  const m = params.get("method");
  const method: Method = METHODS.some((x) => x.value === m) ? (m as Method) : DEFAULT_METHOD;
  const threshold = clampInt(params.get("threshold"), DEFAULT_THRESHOLD, 0, 100);
  const k = clampInt(params.get("k"), DEFAULT_K, 1, 2000);
  const update = (next: { method?: Method; threshold?: number; k?: number }) => {
    const p = new URLSearchParams(params);
    Object.entries(next).forEach(([key, v]) => v !== undefined && p.set(key, String(v)));
    setParams(p, { replace: true });
  };
  return { method, threshold, k, update, search: params.toString() };
}

function ScoreCell({ pair, method, active, threshold }: { pair: PairSummary; method: Method; active: boolean; threshold: number }) {
  const v = scorePct(pair, method);
  if (v === null) return <span className="text-muted" title={pair.scores[method]?.reason?.message ?? "нет оценки"}>—</span>;
  return active ? <RiskMeter risk={v} threshold={threshold} /> : <span className="tabular-nums">{v}%</span>;
}

export function QueuePage() {
  const { batchId = "" } = useParams();
  const navigate = useNavigate();
  const { method, threshold, k, update, search } = useQueueParams();
  const [draft, setDraft] = useState(threshold);
  useEffect(() => setDraft(threshold), [threshold]);

  const batch = useBatch(batchId);
  const done = batch.data?.status === "done";
  const pairsQ = usePairs(batchId, done);
  const subs = useSubmissions(batchId);
  const label = useLabels(batchId);

  if (batch.isLoading) return <Loading what="Загружаю пакет" />;
  if (batch.error) return <ErrorBox error={batch.error} retry={batch.refetch} />;
  if (batch.data?.status === "failed")
    return <div role="alert" className="panel p-6 text-high">Анализ пакета не удался: очередь построить не из чего.</div>;
  if (!done) return <Loading what="Пакет ещё обрабатывается" />;
  if (pairsQ.isLoading) return <Loading what="Загружаю пары" />;
  if (pairsQ.error || !pairsQ.data) return <ErrorBox error={pairsQ.error} retry={pairsQ.refetch} />;

  const pairs = sortPairs(pairsQ.data.pairs, method);
  const queue = reviewQueue(pairs, method, threshold, k);
  const m = queueMetrics(pairs, method, threshold, k);
  const suggestion = suggestThreshold(pairs, method);
  const groups = clusters(subs.data?.submissions.map((s) => s.id) ?? [], pairs, method, threshold);
  const open = (id: string) => navigate(`/batches/${batchId}/pairs/${encodeURIComponent(id)}?${search}`);

  return (
    <>
      <PageHeader step="Шаг 2" title="Очередь ручной проверки">
        <div className="flex flex-wrap gap-3.5 text-xs text-muted">
          <span className="inline-flex items-center gap-1.5"><RiskPill level="high" /> ≥ 70%</span>
          <span className="inline-flex items-center gap-1.5"><RiskPill level="mid" /> ≥ порога</span>
          <span className="inline-flex items-center gap-1.5"><RiskPill level="low" /> ниже порога</span>
        </div>
      </PageHeader>

      <section className="grid grid-cols-2 gap-px overflow-hidden rounded-lg border border-line bg-line md:grid-cols-4" aria-label="Сводка">
        <Kpi label="Пар в пакете" value={pairs.length} hint={`${batch.data?.submissionCount ?? 0} работ`} />
        <Kpi label="К проверке" value={m.queued} hint={`порог ${threshold}%, K = ${k}`} />
        <Kpi label="Точность очереди" value={m.precisionAtK === null ? "—" : pct(m.precisionAtK)} hint="доля подтверждённых среди проверенных пар очереди" />
        <Kpi label="Ложные срабатывания" value={m.falseAlarmRate === null ? "—" : pct(m.falseAlarmRate)} hint={`отзывов: ${m.rated}`} />
      </section>

      <section className="panel">
        <div className="flex flex-wrap items-end gap-5 px-4 py-3.5">
          <label htmlFor="method" className="flex flex-col gap-1 text-xs font-medium text-muted">
            Сортировать по методу
            <select id="method" className="field" value={method} onChange={(e) => update({ method: e.target.value as Method })}>
              {METHODS.map((x) => (
                <option key={x.value} value={x.value}>
                  {x.label}
                </option>
              ))}
            </select>
          </label>
          <label htmlFor="threshold" className="flex flex-col gap-1 text-xs font-medium text-muted">
            <span>
              Порог: <b className="tabular-nums text-ink">{draft}%</b>
            </span>
            <input
              id="threshold"
              type="range"
              min={0}
              max={100}
              value={draft}
              className="w-52 accent-[var(--c-accent)]"
              onChange={(e) => setDraft(+e.target.value)}
              onPointerUp={() => update({ threshold: draft })}
              onKeyUp={() => update({ threshold: draft })}
            />
          </label>
          <label htmlFor="k" className="flex flex-col gap-1 text-xs font-medium text-muted">
            Размер очереди K
            <input
              id="k"
              type="number"
              min={1}
              max={pairs.length}
              defaultValue={k}
              key={k}
              className="field w-24"
              onBlur={(e) => update({ k: clampInt(e.target.value, k, 1, pairs.length) })}
              onKeyDown={(e) => e.key === "Enter" && update({ k: clampInt(e.currentTarget.value, k, 1, pairs.length) })}
            />
          </label>
          {suggestion !== null && suggestion !== threshold && (
            <div className="max-w-md text-xs text-muted">
              По отзывам: порог <b className="text-ink">{suggestion}%</b> убирает известные ложные срабатывания.{" "}
              <button className="text-accent underline" onClick={() => update({ threshold: suggestion })}>
                Применить
              </button>
            </div>
          )}
        </div>

        <div className="flex flex-wrap gap-2 px-4 pb-3.5">
          {groups.length ? (
            groups.map((c, i) => (
              <span key={c.join()} className="rounded-full border border-line bg-sunk px-2.5 py-0.5 text-xs">
                Кластер {i + 1}: {c.map(label).sort().join(", ")}
              </span>
            ))
          ) : (
            <span className="text-muted">Кластеров выше порога нет</span>
          )}
        </div>

        <div className="overflow-x-auto">
          <table className="w-full min-w-[640px] border-collapse text-[13px]">
            <thead>
              <tr>
                <th className="th">#</th>
                <th className="th">Пара</th>
                {METHODS.map((x) => (
                  <th key={x.value} className={`th ${x.value === method ? "text-ink" : ""}`} title={x.hint}>
                    {x.label}
                    {x.value === method ? " ▼" : ""}
                  </th>
                ))}
                <th className="th">Отзыв</th>
              </tr>
            </thead>
            <tbody>
              {pairs.map((p, i) => {
                const inQueue = queue.includes(p);
                const last = inQueue && queue[queue.length - 1] === p;
                return (
                  <tr
                    key={p.id}
                    tabIndex={0}
                    onClick={() => open(p.id)}
                    onKeyDown={(e) => e.key === "Enter" && open(p.id)}
                    className={`cursor-pointer hover:bg-sunk ${inQueue ? "" : "text-muted"} ${last ? "[&>td]:border-b-2 [&>td]:border-b-accent" : ""}`}
                    aria-label={`Пара ${pairLabel(label, p.a, p.b).replace(" ↔ ", " и ")}${inQueue ? ", в очереди" : ""}`}
                  >
                    <td className="td tabular-nums text-muted">{i + 1}</td>
                    <td className="td whitespace-nowrap font-semibold">{pairLabel(label, p.a, p.b)}</td>
                    {METHODS.map((x) => (
                      <td key={x.value} className="td">
                        <ScoreCell pair={p} method={x.value} active={x.value === method} threshold={threshold} />
                      </td>
                    ))}
                    <td className={`td text-xs ${p.decision ? decisionCls[p.decision] : "text-muted"}`}>
                      {p.decision ? DECISION_LABEL[p.decision] : "—"}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}