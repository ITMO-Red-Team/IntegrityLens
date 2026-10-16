import { useParams } from "react-router-dom";
import { usePairs } from "../../api/hooks";
import { DEFAULT_THRESHOLD } from "../../lib/constants";

export function QueuePage() {
  const { batchId = "" } = useParams();
  const { data, isLoading, error } = usePairs(batchId, DEFAULT_THRESHOLD);

  if (isLoading) return <p className="text-muted">Загрузка…</p>;
  if (error) return <p className="text-high">Ошибка: {(error as Error).message}</p>;

  return (
    <div className="panel flex flex-col gap-2 p-4">
      <h1 className="text-2xl">Очередь ручной проверки</h1>
      <p>Пар в пакете: <b>{data?.total}</b></p>
      <p>Кластеры: {data?.clusters.map((c) => c.join(", ")).join(" | ")}</p>
      <ol className="list-decimal pl-5 font-mono">
        {data?.pairs.slice(0, 5).map((p) => (
          <li key={p.id}>
            {p.a} ↔ {p.b} · риск {p.risk}
          </li>
        ))}
      </ol>
    </div>
  );
}