import { useEffect, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { useBatch, usePairs } from "../../api/hooks";
import { ErrorBox, Kpi, Loading, PageHeader, RiskMeter, RiskPill } from "../../components/ui";
import { DEFAULT_K, DEFAULT_THRESHOLD } from "../../lib/constants";
import { pct } from "../../lib/format";
import { DECISION_LABEL, queueMetrics, reviewQueue, suggestThreshold } from "../../lib/queue";

const clampInt = (v: string | null, def: number, min: number, max: number) => {
  const n = Number.parseInt(v ?? "", 10);
  return Number.isFinite(n) ? Math.min(max, Math.max(min, n)) : def;
};

const decisionCls = { confirmed: "text-high", acceptable: "text-low", false_positive: "text-accent" } as const;

export function QueuePage() {
  const { batchId = "" } = useParams();
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();
  const threshold = clampInt(params.get("threshold"), DEFAULT_THRESHOLD, 0, 100);
  const k = clampInt(params.get("k"), DEFAULT_K, 1, 500);
  const [draft, setDraft] = useState(threshold);
  useEffect(() => setDraft(threshold), [threshold]);

  const batch = useBatch(batchId);
  const pairsQ = usePairs(batchId, threshold, batch.data?.status === "done");

  const update = (next: { threshold?: number; k?: number }) => {
    const p = new URLSearchParams(params);
    if (next.threshold !== undefined) p.set("threshold", String(next.threshold));
    if (next.k !== undefined) p.set("k", String(next.k));
    setParams(p, { replace: true });
  };

  if (batch.isLoading) return <Loading what="Загружаю пакет" />;
  if (batch.error) return <ErrorBox error={batch.error} retry={batch.refetch} />;
  if (batch.data && batch.data.status !== "done") return <Loading what="Пакет ещё обрабатывается" />;
  if (pairsQ.isLoading) return <Loading what="Ранжирую пары" />;
  if (pairsQ.error || !pairsQ.data) return <ErrorBox error={pairsQ.error} retry={pairsQ.refetch} />;

  const { pairs, clusters } = pairsQ.data;
  const queue = reviewQueue(pairs, threshold, k);
  const m = queueMetrics(pairs, threshold, k);
  const suggestion = suggestThreshold(pairs);
  const open = (id: string) => navigate(`/batches/${batchId}/pairs/${encodeURIComponent(id)}?threshold=${threshold}&k=${k}`);

  return (
    <>
      <PageHeader step="Шаг 2" title="Очередь ручной проверки">
        <div className="flex flex-wrap gap-3.5 text-xs text-muted">
          <span className="inline-flex items-center gap-1.5"><RiskPill level="high" /> ≥ 70</span>
          <span className="inline-flex items-center gap-1.5"><RiskPill level="mid" /> ≥ порога</span>
          <span className="inline-flex items-center gap-1.5"><RiskPill level="low" /> ниже порога</span>
        </div>
      </PageHeader>

      <section className="grid grid-cols-2 gap-px overflow-hidden rounded-lg border border-line bg-line md:grid-cols-4" aria-label="Сводка">
        <Kpi label="Пар в пакете" value={pairs.length} hint={`${batch.data?.submissions.length ?? 0} работ`} />
        <Kpi label="К проверке" value={m.queued} hint={`порог ${threshold}, K = ${k}`} />
        <Kpi
          label="Точность очереди"
          value={m.precisionAtK === null ? "—" : pct(m.precisionAtK)}
          hint="доля подтверждённых среди проверенных пар очереди"
        />
        <Kpi label="Ложные срабатывания" value={m.falseAlarmRate === null ? "—" : pct(m.falseAlarmRate)} hint={`отзывов: ${m.rated}`} />
      </section>

      <section className="panel">
        <div className="flex flex-wrap items-end gap-5 px-4 py-3.5">
          <label htmlFor="threshold" className="flex flex-col gap-1 text-xs font-medium text-muted">
            <span>
              Порог риска: <b className="tabular-nums text-ink">{draft}</b>
            </span>
            <input
              id="threshold"
              type="range"
              min={20}
              max={90}
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
          <div className="max-w-md text-xs text-muted">
            {suggestion !== null && suggestion !== threshold ? (
              <span>
                По отзывам: порог <b className="text-ink">{suggestion}</b> убирает известные ложные срабатывания и сохраняет подтверждённые.{" "}
                <button className="text-accent underline" onClick={() => update({ threshold: suggestion })}>
                  Применить
                </button>
              </span>
            ) : (
              "Порог калибруется по отзывам проверяющих: отметьте несколько пар, и здесь появится подсказка."
            )}
          </div>
        </div>

        <div className="flex flex-wrap gap-2 px-4 pb-3.5">
          {clusters.length ? (
            clusters.map((c, i) => (
              <span key={c.join()} className="rounded-full border border-line bg-sunk px-2.5 py-0.5 text-xs">
                Кластер {i + 1}: {c.join(", ")}
              </span>
            ))
          ) : (
            <span className="text-muted">Кластеров выше порога нет</span>
          )}
        </div>

        <div className="overflow-x-auto">
                    <table className="w-full min-w-[580px] border-collapse text-[13px]">
            <thead>
              <tr>
                <th className="th">#</th>
                <th className="th">Пара</th>
                <th className="th">Риск</th>
                <th className="th text-right" title="Код совпадает по устройству, даже если переменные и методы переименованы">
                  Совпадение структуры
                </th>
                <th className="th text-right" title="Код совпадает буквально, как написан. Переименование переменных это значение сильно снижает">
                  Совпадение текста
                </th>
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
                    aria-label={`Пара ${p.a} и ${p.b}, риск ${p.risk}${inQueue ? ", в очереди" : ""}`}
                  >
                    <td className="td tabular-nums text-muted">{i + 1}</td>
                    <td className="td whitespace-nowrap">
                      <b>{p.a}</b> ↔ <b>{p.b}</b>
                    </td>
                    <td className="td">
                      <RiskMeter risk={p.risk} threshold={threshold} />
                    </td>
                    <td className="td text-right tabular-nums">{pct(p.signals.structure)}</td>
                    <td className="td text-right tabular-nums">{pct(p.signals.baseline)}</td>
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