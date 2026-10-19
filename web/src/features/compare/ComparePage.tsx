import { useEffect, useState } from "react";
import { Navigate, useNavigate, useParams } from "react-router-dom";
import { useBatch, useLabels, usePair, usePairs } from "../../api/hooks";
import { ErrorBox, Loading, PageHeader } from "../../components/ui";
import { METHODS } from "../../lib/constants";
import { pairLabel } from "../../lib/labels";
import { sortPairs } from "../../lib/queue";
import { buildRegions, groupColors } from "../../lib/regions";
import { useQueueParams } from "../queue/QueuePage";
import { CodePane } from "./CodePane";
import { FeedbackPanel } from "./FeedbackPanel";

export function CompareIndex() {
  const { batchId = "" } = useParams();
  const { method, search } = useQueueParams();
  const batch = useBatch(batchId);
  const done = batch.data?.status === "done";
  const { data, error, refetch } = usePairs(batchId, done);
  if (batch.data && !done) return <Loading what="Пакет ещё обрабатывается" />;
  if (error) return <ErrorBox error={error} retry={refetch} />;
  if (!data) return <Loading what="Ищу пары" />;
  const top = sortPairs(data.pairs, method)[0];
  if (!top) return <div className="panel p-6">В пакете нет пар для сравнения.</div>;
  return <Navigate replace to={`/batches/${batchId}/pairs/${encodeURIComponent(top.id)}${search ? `?${search}` : ""}`} />;
}

export function ComparePage() {
  const { batchId = "", pairId = "" } = useParams();
  const navigate = useNavigate();
  const { method, search } = useQueueParams();
  const pairQ = usePair(pairId);
  const listQ = usePairs(batchId);
  const label = useLabels(batchId);

  const [showRegions, setShowRegions] = useState(true);
  const [hoverGroup, setHoverGroup] = useState<string | null>(null);
  const [focusedGroup, setFocusedGroup] = useState<string | null>(null);

  // При смене пары сбрасываем закреплённый/наведённый регион, иначе в новой
  // паре сохранился бы старый фокус (и код был бы тусклым, если такого региона
  // в ней нет).
  useEffect(() => {
    setHoverGroup(null);
    setFocusedGroup(null);
  }, [pairId]);

  const sorted = sortPairs(listQ.data?.pairs ?? [], method);
  const ids = sorted.map((x) => x.id);
  const idx = ids.indexOf(pairId);
  const go = (id: string | undefined) =>
    id && navigate(`/batches/${batchId}/pairs/${encodeURIComponent(id)}${search ? `?${search}` : ""}`);

  const p = pairQ.data;
  if (pairQ.isLoading) return <Loading what="Загружаю пару" />;
  if (pairQ.error || !p) return <ErrorBox error={pairQ.error} retry={pairQ.refetch} />;

  const fragments = p.scores.jplag?.fragments ?? [];
  const regions = buildRegions(fragments, { demo: true });

  // Градиент считается по объединению групп обеих панелей один раз, чтобы
  // одиночный участок (только слева или только справа) занимал собственный
  // слот и не сдвигал цвета парных участков. Цвет группы одинаков в обеих
  // панелях, т.к. позиция берётся от группы.
  const groupColor = groupColors(regions);

  const regionsA = showRegions ? regions.filter((r) => r.side === "A") : [];
  const regionsB = showRegions ? regions.filter((r) => r.side === "B") : [];

  return (
    <>
      <PageHeader step={`Шаг 3 · пара ${idx >= 0 ? idx + 1 : "…"} из ${sorted.length || "…"}`} title={pairLabel(label, p.a, p.b)}>
        <div className="flex flex-wrap items-center gap-2">
          {regions.length > 0 && (
            <button
              type="button"
              className="btn"
              aria-pressed={showRegions}
              onClick={() => {
                setShowRegions((v) => !v);
                setHoverGroup(null);
                setFocusedGroup(null);
              }}
            >
              {showRegions ? "Скрыть подозрительные участки" : "Показать подозрительные участки"}
            </button>
          )}
          <button className="btn" disabled={idx <= 0} onClick={() => go(ids[idx - 1])} aria-label="Предыдущая пара">
            ←
          </button>
          <select className="field max-w-full" aria-label="Выбрать пару" value={pairId} onChange={(e) => go(e.target.value)}>
            {sorted.map((x) => {
              const v = x.scores[method]?.value;
              return (
                <option key={x.id} value={x.id}>
                  {pairLabel(label, x.a, x.b)} · {v === null || v === undefined ? "—" : `${Math.round(v * 100)}%`}
                </option>
              );
            })}
          </select>
          <button className="btn" disabled={idx < 0 || idx >= ids.length - 1} onClick={() => go(ids[idx + 1])} aria-label="Следующая пара">
            →
          </button>
        </div>
      </PageHeader>

      <section className="grid grid-cols-1 gap-px overflow-hidden rounded-lg border border-line bg-line sm:grid-cols-2" aria-label="Оценки пары">
        {METHODS.map((x) => {
          const s = p.scores[x.value];
          const v = s?.value;
          return (
            <div key={x.value} className="flex flex-col gap-0.5 bg-surface px-3 py-2.5">
              <span className="eyebrow">{x.label}</span>
              <span className="font-display text-lg font-semibold tabular-nums">
                {v === null || v === undefined ? "—" : `${Math.round(v * 100)}%`}
              </span>
              <span className="text-xs text-muted">{v === null || v === undefined ? (s?.reason?.message ?? "нет оценки") : x.hint}</span>
            </div>
          );
        })}
      </section>

      <div className="grid gap-3 lg:grid-cols-2">
        <CodePane
          side="A"
          submissionId={p.a}
          label={label(p.a)}
          info={p.submissions[p.a]}
          regions={regionsA}
          groupColor={groupColor}
          hoverGroup={hoverGroup}
          onHoverGroup={setHoverGroup}
          focusedGroup={focusedGroup}
          onFocusGroup={setFocusedGroup}
        />
        <CodePane
          side="B"
          submissionId={p.b}
          label={label(p.b)}
          info={p.submissions[p.b]}
          regions={regionsB}
          groupColor={groupColor}
          hoverGroup={hoverGroup}
          onHoverGroup={setHoverGroup}
          focusedGroup={focusedGroup}
          onFocusGroup={setFocusedGroup}
        />
      </div>

      <FeedbackPanel key={pairId} pairId={pairId} feedback={p.feedback} />
    </>
  );
}