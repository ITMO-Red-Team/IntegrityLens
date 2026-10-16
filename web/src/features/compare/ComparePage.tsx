import { Navigate, useNavigate, useParams, useSearchParams } from "react-router-dom";
import { usePair, usePairs } from "../../api/hooks";
import { ErrorBox, Loading, PageHeader, RiskPill } from "../../components/ui";
import { DEFAULT_THRESHOLD } from "../../lib/constants";
import { pct } from "../../lib/format";
import { riskLevel } from "../../lib/queue";
import { CodePane } from "./CodePane";

function useThreshold() {
  const [params] = useSearchParams();
  const t = Number.parseInt(params.get("threshold") ?? "", 10);
  return { threshold: Number.isFinite(t) ? t : DEFAULT_THRESHOLD, search: params.toString() };
}

export function CompareIndex() {
  const { batchId = "" } = useParams();
  const { threshold, search } = useThreshold();
  const { data, error, refetch } = usePairs(batchId, threshold);
  if (error) return <ErrorBox error={error} retry={refetch} />;
  if (!data) return <Loading what="Ищу пары" />;
  const top = data.pairs[0];
  if (!top) return <div className="panel p-6">В пакете нет пар для сравнения.</div>;
  return <Navigate replace to={`/batches/${batchId}/pairs/${encodeURIComponent(top.id)}${search ? `?${search}` : ""}`} />;
}

export function ComparePage() {
  const { batchId = "", pairId = "" } = useParams();
  const navigate = useNavigate();
  const { threshold, search } = useThreshold();
  const pairQ = usePair(pairId);
  const listQ = usePairs(batchId, threshold);

  const ids = listQ.data?.pairs.map((x) => x.id) ?? [];
  const idx = ids.indexOf(pairId);
  const go = (id: string | undefined) => id && navigate(`/batches/${batchId}/pairs/${encodeURIComponent(id)}${search ? `?${search}` : ""}`);

  const p = pairQ.data;
  if (pairQ.isLoading) return <Loading what="Загружаю пару" />;
  if (pairQ.error || !p) return <ErrorBox error={pairQ.error} retry={pairQ.refetch} />;

  const metrics = [
    { label: "Совпадение структуры", value: pct(p.signals.structure), hint: "совпадает устройство кода, даже при переименовании" },
    { label: "Совпадение текста", value: pct(p.signals.baseline), hint: "код совпадает буквально, как написан" },
  ];

  return (
    <>
      <PageHeader step={`Шаг 3 · пара ${p.rank} из ${listQ.data?.total ?? "…"}`} title={`${p.a} ↔ ${p.b}`}>
        <div className="flex flex-wrap items-center gap-2">
          <button className="btn" disabled={idx <= 0} onClick={() => go(ids[idx - 1])} aria-label="Предыдущая пара">
            ←
          </button>
          <select className="field max-w-full" aria-label="Выбрать пару" value={pairId} onChange={(e) => go(e.target.value)}>
            {listQ.data?.pairs.map((x) => (
              <option key={x.id} value={x.id}>
                {x.a} ↔ {x.b} · {x.risk}
              </option>
            ))}
          </select>
          <button className="btn" disabled={idx < 0 || idx >= ids.length - 1} onClick={() => go(ids[idx + 1])} aria-label="Следующая пара">
            →
          </button>
        </div>
      </PageHeader>

      <section className="grid grid-cols-1 gap-px overflow-hidden rounded-lg border border-line bg-line sm:grid-cols-3" aria-label="Метрики пары">
        <div className="flex flex-col gap-0.5 bg-surface px-3 py-2.5">
          <span className="eyebrow">Риск</span>
          <span className="flex items-center gap-2 font-display text-lg font-semibold tabular-nums">
            {p.risk} <RiskPill level={riskLevel(p.risk, threshold)} />
          </span>
          <span className="text-xs text-muted">из 100</span>
        </div>
        {metrics.map((m) => (
          <div key={m.label} className="flex flex-col gap-0.5 bg-surface px-3 py-2.5">
            <span className="eyebrow">{m.label}</span>
            <span className="font-display text-lg font-semibold tabular-nums">{m.value}</span>
            <span className="text-xs text-muted">{m.hint}</span>
          </div>
        ))}
      </section>

      <div className="grid gap-3 lg:grid-cols-2">
        <CodePane side="A" submission={p.submissionA} />
        <CodePane side="B" submission={p.submissionB} />
      </div>
    </>
  );
}