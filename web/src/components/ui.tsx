import { useState, type ReactNode } from "react";
import { LEVEL_LABEL, riskLevel, type RiskLevel } from "../lib/queue";

const pillCls: Record<RiskLevel, string> = {
  high: "text-high bg-high-bg",
  mid: "text-mid bg-mid-bg",
  low: "text-low bg-low-bg",
};
const barCls: Record<RiskLevel, string> = { high: "bg-high", mid: "bg-mid", low: "bg-low" };

export function RiskPill({ level }: { level: RiskLevel }) {
  return (
    <span className={`whitespace-nowrap rounded px-1.5 py-0.5 text-[11px] font-semibold ${pillCls[level]}`}>
      {LEVEL_LABEL[level]}
    </span>
  );
}

export function RiskMeter({ risk, threshold }: { risk: number; threshold: number }) {
  const level = riskLevel(risk, threshold);
  return (
    <div className="flex items-center gap-2" aria-label={`Риск ${risk} из 100, ${LEVEL_LABEL[level]}`}>
      <span className="w-6 font-semibold tabular-nums">{risk}</span>
      <span className="h-1.5 w-20 overflow-hidden rounded-sm border border-line bg-sunk">
        <i className={`block h-full ${barCls[level]}`} style={{ width: `${risk}%` }} />
      </span>
      <RiskPill level={level} />
    </div>
  );
}

export function PageHeader({ step, title, children }: { step: string; title: string; children?: ReactNode }) {
  return (
    <div className="flex flex-wrap items-end justify-between gap-4">
      <div>
        <div className="eyebrow">{step}</div>
        <h1 className="text-2xl">{title}</h1>
      </div>
      {children}
    </div>
  );
}

export function Loading({ what = "Загрузка" }: { what?: string }) {
  return (
    <div role="status" className="panel p-6 text-muted">
      {what}…
    </div>
  );
}

export function ErrorBox({ error, retry }: { error: unknown; retry?: () => void }) {
  const msg = error instanceof Error ? error.message : "Неизвестная ошибка";
  return (
    <div role="alert" className="panel flex flex-wrap items-center justify-between gap-3 border-high p-4">
      <span>
        <b className="text-high">Не удалось загрузить данные.</b> {msg}
      </span>
      {retry && (
        <button className="btn" onClick={retry}>
          Повторить
        </button>
      )}
    </div>
  );
}

export function Kpi({ label, value, hint }: { label: string; value: ReactNode; hint?: ReactNode }) {
  return (
    <div className="flex flex-col gap-0.5 bg-surface px-3.5 py-3">
      <span className="eyebrow">{label}</span>
      <span className="font-display text-2xl font-semibold tabular-nums">{value}</span>
      {hint && <span className="text-muted">{hint}</span>}
    </div>
  );
}

export function OriginalName({ name }: { name: string }) {
  const [shown, setShown] = useState(false);
  return shown ? (
    <span className="font-mono text-muted">{name}</span>
  ) : (
    <button type="button" className="text-xs text-accent underline" onClick={() => setShown(true)}>
      показать имя файла
    </button>
  );
}