import { useMemo, useState } from "react";
import { useSource } from "../../api/hooks";
import type { SubmissionInfo } from "../../api/types";
import { OriginalName } from "../../components/ui";
import { highlightLine } from "../../lib/highlight";

interface Props {
  side: "A" | "B";
  submissionId: string;
  label: string;
  info?: SubmissionInfo;
}

export function CodePane({ side, submissionId, label, info }: Props) {
  const src = useSource(submissionId);
  const [highlighted, setHighlighted] = useState(true);
  const lines = useMemo(() => (src.data ?? "").replace(/\r\n/g, "\n").split("\n"), [src.data]);
  const language = info?.language;
  const supported = highlightLine("", language) !== null;

  return (
    <div className="panel flex min-w-0 flex-col">
      <div className="flex flex-wrap items-center justify-between gap-2 border-b border-line px-3 py-2 text-[13px]">
        <b>
          {side} · {label}
        </b>
        <span className="flex items-center gap-2 text-muted">
          {info && <OriginalName name={info.originalName} />}
          {info && <span>· {info.loc} строк</span>}
          {/* Кнопка выключения подсветки кода (выключил через false) */}
          {false && supported && (
            <button
              type="button"
              className="btn"
              aria-pressed={highlighted}
              onClick={() => setHighlighted((v) => !v)}
              title="Показать/скрыть подсветку синтаксиса"
            >
              {highlighted ? "Без подсветки" : "С подсветкой"}
            </button>
          )}
        </span>
      </div>
      <div className="max-h-[640px] overflow-auto py-1.5 font-mono text-[12.5px] leading-[1.6]" data-testid={`pane-${side}`}>
        {src.isLoading && <p className="px-3 text-muted">Загружаю код…</p>}
        {src.error && <p className="px-3 text-high">Не удалось загрузить код: {(src.error as Error).message}</p>}
        {src.data !== undefined &&
          lines.map((text, i) => (
            <div key={i} className="grid grid-cols-[40px_minmax(0,1fr)] whitespace-pre">
              <span className="select-none pr-3 text-right tabular-nums text-muted">{i + 1}</span>
              <span
                className="pr-3"
                dangerouslySetInnerHTML={{
                  __html: highlighted && supported ? highlightLine(text, language) ?? escapeHtml(text) : escapeHtml(text),
                }}
              />
            </div>
          ))}
      </div>
    </div>
  );
}

function escapeHtml(value: string): string {
  return value.replace(/[&<>"']/g, (ch) => {
    switch (ch) {
      case "&":
        return "&amp;";
      case "<":
        return "&lt;";
      case ">":
        return "&gt;";
      case '"':
        return "&quot;";
      default:
        return "&#39;";
    }
  });
}
