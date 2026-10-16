import { Link, useParams } from "react-router-dom";
import { useBatch } from "../../api/hooks";
import type { Stage } from "../../api/types";
import { ErrorBox, Loading, PageHeader } from "../../components/ui";
import { dateTime } from "../../lib/format";

const dot: Record<Stage["status"], string> = {
  pending: "bg-line",
  running: "bg-accent animate-pulse",
  done: "bg-low",
  failed: "bg-high",
};

export function BatchPage() {
  const { batchId = "" } = useParams();
  const { data, error, refetch, isLoading } = useBatch(batchId);
  if (isLoading) return <Loading what="Загружаю пакет" />;
  if (error || !data) return <ErrorBox error={error} retry={refetch} />;
  const done = data.status === "done";

  return (
    <>
      <PageHeader step="Пакет" title={data.assignment}>
        <Link to={`/batches/${data.id}/queue`} className={`btn btn-primary ${done ? "" : "pointer-events-none opacity-50"}`} aria-disabled={!done}>
          Открыть очередь проверки →
        </Link>
      </PageHeader>
      <div className="grid gap-4 lg:grid-cols-[minmax(0,1.3fr)_minmax(0,1fr)]">
        <div className="panel overflow-x-auto">
          <table className="w-full border-collapse text-[13px]">
            <thead>
              <tr>
                <th className="th">ID</th>
                <th className="th">Файл</th>
                <th className="th">Сдано</th>
                <th className="th text-right">Строк кода</th>
              </tr>
            </thead>
            <tbody>
              {data.submissions.map((s) => (
                <tr key={s.id}>
                  <td className="td font-semibold">{s.id}</td>
                  <td className="td font-mono text-muted">{s.fileName}</td>
                  <td className="td tabular-nums">{dateTime(s.submittedAt)}</td>
                  <td className="td text-right tabular-nums">{s.loc}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="panel flex flex-col gap-3 p-4" aria-live="polite">
          <h2 className="text-lg">Обработка {done ? "завершена" : "идёт"}</h2>
          <ul className="flex flex-col gap-2">
            {data.stages.map((s) => (
              <li key={s.name} className="grid grid-cols-[14px_1fr_auto] items-center gap-2.5 text-[13px]">
                <span className={`h-2.5 w-2.5 rounded-full ${dot[s.status]}`} aria-hidden="true" />
                <span>{s.name}</span>
                <span className="tabular-nums text-muted">{s.durationMs !== undefined ? `${s.durationMs.toFixed(1)} мс` : "—"}</span>
              </li>
            ))}
          </ul>
          <p className="text-muted">
            Пар сравнено: <b className="tabular-nums text-ink">{done ? data.pairCount : "…"}</b>
          </p>
        </div>
      </div>
    </>
  );
}