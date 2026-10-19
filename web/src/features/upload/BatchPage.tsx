import { Link, useParams } from "react-router-dom";
import { useBatch, useLabels, useSubmissions } from "../../api/hooks";
import type { Run } from "../../api/types";
import { ErrorBox, Loading, OriginalName, PageHeader } from "../../components/ui";
import { methodLabel } from "../../lib/constants";

const RUN_STATUS: Record<Run["status"], { label: string; dot: string }> = {
  queued: { label: "в очереди", dot: "bg-line" },
  running: { label: "выполняется", dot: "bg-accent animate-pulse" },
  done: { label: "готово", dot: "bg-low" },
  failed: { label: "ошибка", dot: "bg-high" },
};

function duration(r: Run) {
  if (!r.startedAt || !r.finishedAt) return "—";
  const s = (new Date(r.finishedAt).getTime() - new Date(r.startedAt).getTime()) / 1000;
  return `${s.toFixed(1)} с`;
}

export function BatchPage() {
  const { batchId = "" } = useParams();
  const batch = useBatch(batchId);
  const subs = useSubmissions(batchId);
  const label = useLabels(batchId);

  if (batch.isLoading) return <Loading what="Загружаю пакет" />;
  if (batch.error || !batch.data) return <ErrorBox error={batch.error} retry={batch.refetch} />;
  const b = batch.data;
  const done = b.status === "done";

  return (
    <>
      <PageHeader step="Пакет" title={b.title}>
        <Link
          to={`/batches/${b.id}/queue`}
          className={`btn btn-primary ${done ? "" : "pointer-events-none opacity-50"}`}
          aria-disabled={!done}
        >
          Открыть очередь проверки →
        </Link>
      </PageHeader>

      {b.status === "failed" && (
        <div role="alert" className="panel border-high p-4 text-high">
          Анализ не удался: все методы завершились с ошибкой.
        </div>
      )}

      <div className="grid gap-4 lg:grid-cols-[minmax(0,1.3fr)_minmax(0,1fr)]">
        <div className="panel overflow-x-auto">
          {subs.isLoading ? (
            <p className="p-4 text-muted">Загружаю работы…</p>
          ) : subs.error ? (
            <ErrorBox error={subs.error} retry={subs.refetch} />
          ) : (
            <table className="w-full border-collapse text-[13px]">
              <thead>
                <tr>
                  <th className="th">Работа</th>
                  <th className="th">Файл</th>
                  <th className="th text-right">Строк</th>
                </tr>
              </thead>
              <tbody>
                {subs.data?.submissions.map((s) => (
                  <tr key={s.id}>
                    <td className="td font-semibold">{label(s.id)}</td>
                    <td className="td">
                      <OriginalName name={s.originalName} />
                    </td>
                    <td className="td text-right tabular-nums">{s.loc}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <div className="panel flex flex-col gap-3 p-4" aria-live="polite">
          <h2 className="text-lg">Анализ {done ? "завершён" : b.status === "failed" ? "не удался" : "идёт"}</h2>
          <ul className="flex flex-col gap-2">
            {b.runs.map((r) => (
              <li key={r.method} className="grid grid-cols-[14px_1fr_auto] items-center gap-2.5 text-[13px]">
                <span className={`h-2.5 w-2.5 rounded-full ${RUN_STATUS[r.status].dot}`} aria-hidden="true" />
                <span>
                  <b>{methodLabel(r.method)}</b> — {RUN_STATUS[r.status].label}
                  {r.status === "done" && r.failed ? `, не разобрано работ: ${r.failed}` : ""}
                  {r.error ? `: ${r.error}` : ""}
                </span>
                <span className="tabular-nums text-muted">{duration(r)}</span>
              </li>
            ))}
          </ul>
          <p className="text-muted">
            Работ: <b className="tabular-nums text-ink">{b.submissionCount}</b> · пар:{" "}
            <b className="tabular-nums text-ink">{b.pairCount}</b>
          </p>
        </div>
      </div>
    </>
  );
}