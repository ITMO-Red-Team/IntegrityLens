import { useMemo, useRef } from "react";
import { Navigate, useNavigate, useParams, useSearchParams } from "react-router-dom";
import { usePair, usePairs } from "../../api/hooks";
import { ErrorBox, Loading, PageHeader, RiskPill } from "../../components/ui";
import { DEFAULT_THRESHOLD } from "../../lib/constants";
import { pct } from "../../lib/format";
import { riskLevel } from "../../lib/queue";
import { CodePane, FRAG_MARK, type CodePaneHandle } from "./CodePane";
import { FeedbackForm } from "./FeedbackForm";

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
  const paneA = useRef<CodePaneHandle>(null);
  const paneB = useRef<CodePaneHandle>(null);

  const p = pairQ.data;
  const renamed = useMemo(() => new Set(p?.renames.flat() ?? []), [p]);
  const ids = listQ.data?.pairs.map((x) => x.id) ?? [];
  const idx = ids.indexOf(pairId);
  const go = (id: string | undefined) => id && navigate(`/batches/${batchId}/pairs/${encodeURIComponent(id)}${search ? `?${search}` : ""}`);

  if (pairQ.isLoading) return <Loading what="Загружаю пару" />;
  if (pairQ.error || !p) return <ErrorBox error={pairQ.error} retry={pairQ.refetch} />;

  const level = riskLevel(p.risk, threshold);
  const signals = [
    { label: "Структура", value: pct(p.signals.structure), hint: "5-граммы после нормализации имён" },
    { label: "Baseline", value: pct(p.signals.baseline), hint: "сырые токенные 3-граммы" },
    { label: "Покрытие строк", value: pct(p.signals.coverage), hint: "доля строк в совпадающих блоках" },
    { label: "Эмбеддинги", value: pct(p.signals.embedding), hint: "косинус, мок-адаптер" },
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

      <section className="grid grid-cols-1 gap-px overflow-hidden rounded-lg border border-line bg-line sm:grid-cols-2 lg:grid-cols-5" aria-label="Признаки">
        <div className="flex flex-col gap-0.5 bg-surface px-3 py-2.5">
          <span className="eyebrow">Risk score</span>
          <span className="flex items-center gap-2 font-display text-lg font-semibold tabular-nums">
            {p.risk} <RiskPill level={level} />
          </span>
          <span className="text-xs text-muted">взвешенная сумма признаков</span>
        </div>
        {signals.map((s) => (
          <div key={s.label} className="flex flex-col gap-0.5 bg-surface px-3 py-2.5">
            <span className="eyebrow">{s.label}</span>
            <span className="font-display text-lg font-semibold tabular-nums">{s.value}</span>
            <span className="text-xs text-muted">{s.hint}</span>
          </div>
        ))}
      </section>

      <div className="flex flex-wrap gap-3.5 text-xs text-muted">
        <span className="inline-flex items-center gap-1.5"><span className="h-3 w-3 rounded-sm bg-f1" />совпадающий фрагмент</span>
        <span className="inline-flex items-center gap-1.5"><span className="h-3 w-3 rounded-sm border border-line bg-tmpl" />шаблон курса и импорты, исключены</span>
        <span className="inline-flex items-center gap-1.5"><span className="font-medium text-accent underline decoration-dotted">имя</span>переименованный идентификатор</span>
      </div>

      <div className="grid gap-3 lg:grid-cols-2">
        <CodePane ref={paneA} side="A" submission={p.submissionA} ranges={p.fragments.map((f) => f.a)} renamed={renamed} />
        <CodePane ref={paneB} side="B" submission={p.submissionB} ranges={p.fragments.map((f) => f.b)} renamed={renamed} />
      </div>

      <div className="grid gap-4 lg:grid-cols-[minmax(0,1.1fr)_minmax(0,1fr)]">
        <section className="panel flex flex-col gap-3 p-4" aria-labelledby="ev-h">
          <h2 id="ev-h" className="text-lg">
            Основания для ревью
          </h2>
          {p.fragments.length ? (
            <ul className="flex flex-col gap-1.5">
              {p.fragments.map((f) => (
                <li key={f.index}>
                  <button
                    type="button"
                    onClick={() => (paneA.current?.reveal(f.a), paneB.current?.reveal(f.b))}
                    className="grid w-full grid-cols-[12px_1fr_auto] items-center gap-2.5 rounded-md border border-line bg-surface px-2.5 py-2 text-left text-[13px]"
                  >
                    <span className={`h-3 w-3 rounded-sm ${FRAG_MARK[f.index % 4]}`} />
                    <span>
                      Фрагмент {f.index + 1}: строки {f.a[0] + 1}–{f.a[1] + 1} ↔ {f.b[0] + 1}–{f.b[1] + 1}
                    </span>
                    <span className="tabular-nums text-muted">{f.length} стр.</span>
                  </button>
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-muted">Совпадающих блоков от 3 строк нет. Сходство объясняется общими идиомами.</p>
          )}
          <div>
            <div className="eyebrow mb-1.5">Переименования в совпадающих блоках · {p.renames.length}</div>
            {p.renames.length ? (
              <div className="flex flex-wrap gap-1.5 font-mono text-xs">
                {p.renames.map(([x, y]) => (
                  <span key={x} className="rounded border border-line bg-sunk px-1.5 py-0.5">
                    {x} → {y}
                  </span>
                ))}
              </div>
            ) : (
              <span className="text-muted">нет</span>
            )}
          </div>
          <p className="text-[13px] text-muted">{p.explanation}</p>
        </section>

        <FeedbackForm key={p.id} pairId={p.id} initial={p.feedback} onSaved={() => go(ids[idx + 1])} />
      </div>
    </>
  );
}