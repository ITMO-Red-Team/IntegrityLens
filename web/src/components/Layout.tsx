import { NavLink, Outlet, useParams } from "react-router-dom";
import { useBatch } from "../api/hooks";
import { DEMO_BATCH_ID } from "../lib/constants";

const linkCls = ({ isActive }: { isActive: boolean }) =>
  `rounded-md border px-3 py-1.5 text-[13px] font-medium ${
    isActive ? "border-line bg-sunk text-ink" : "border-transparent text-muted hover:text-ink"
  }`;

function BatchLabel({ batchId }: { batchId: string }) {
  const { data } = useBatch(batchId);
  if (!data) return null;
  return (
    <span className="min-w-0 truncate text-[13px] text-muted">
            {[data.course, data.assignment, `${data.submissions.length} работ`].filter(Boolean).join(" · ")}
    </span>
  );
}

export function Layout() {
  const { batchId = DEMO_BATCH_ID } = useParams();
  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-10 border-b border-line bg-surface">
        <div className="mx-auto max-w-[1320px] px-4">
          <div className="flex flex-wrap items-center gap-4 py-2.5">
            <NavLink to="/" className="flex items-center gap-2 font-display text-lg font-semibold">
              <svg viewBox="0 0 24 24" className="h-[22px] w-[22px]" fill="none" aria-hidden="true">
                <circle cx="10" cy="10" r="6.5" stroke="var(--c-accent)" strokeWidth="2" />
                <path d="M15 15l5.5 5.5" stroke="var(--c-accent)" strokeWidth="2.2" strokeLinecap="round" />
                <path d="M7 9h6M7 12h4" stroke="var(--c-ink)" strokeWidth="1.6" strokeLinecap="round" />
              </svg>
              IntegrityLens
            </NavLink>
            <BatchLabel batchId={batchId} />
            <nav aria-label="Разделы" className="ml-auto flex flex-wrap gap-1">
              <NavLink to="/upload" className={linkCls}>
                Пакет работ
              </NavLink>
              <NavLink to={`/batches/${batchId}/queue`} className={linkCls}>
                Очередь проверки
              </NavLink>
              <NavLink to={`/batches/${batchId}/pairs`} className={linkCls}>
                Сравнение
              </NavLink>
            </nav>
          </div>
          <p className="flex gap-2.5 border-t border-dashed border-line py-2 text-[13px] text-muted">
            <span aria-hidden="true">ⓘ</span>
            <span>
              <b className="font-semibold text-ink">Риск — не вердикт.</b> Система только ранжирует пары для ручной
              проверки и показывает основания. Решение принимает проверяющий. Работы псевдонимизированы.
            </span>
          </p>
        </div>
      </header>
      <main className="mx-auto flex max-w-[1320px] flex-col gap-4 px-4 pt-5 pb-12">
        <Outlet />
      </main>
    </div>
  );
}