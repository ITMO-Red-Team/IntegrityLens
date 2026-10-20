import { useEffect, useMemo, useRef } from "react";
import { useSource } from "../../api/hooks";
import type { SubmissionInfo } from "../../api/types";
import { OriginalName } from "../../components/ui";
import { highlightLine } from "../../lib/highlight";
import { highlightAlias } from "../../lib/languages";
import { type Region } from "../../lib/regions";

interface Props {
  side: "A" | "B";
  submissionId: string;
  label: string;
  info?: SubmissionInfo;
  /** Участки этой панели (уже отфильтрованы по side). */
  regions: Region[];
  /** Цвет группы (общий для обеих панелей, от объединённого набора групп). */
  groupColor: Map<string, string>;
  /** Группа, на которую сейчас наведён указатель (общая для обеих панелей). */
  hoverGroup: string | null;
  onHoverGroup: (group: string | null) => void;
  /** Группа, закреплённая по клику (общая для обеих панелей). */
  focusedGroup: string | null;
  onFocusGroup: (group: string | null) => void;
}

export function CodePane({
  side,
  submissionId,
  label,
  info,
  regions,
  groupColor,
  hoverGroup,
  onHoverGroup,
  focusedGroup,
  onFocusGroup,
}: Props) {
  const src = useSource(submissionId);
  const lines = useMemo(() => (src.data ?? "").replace(/\r\n/g, "\n").split("\n"), [src.data]);
  const language = info?.language;
  const supported = highlightAlias(language) !== null;
  const scrollRef = useRef<HTMLDivElement>(null);

  // Строка этой панели, к которой надо долистать при фокусе на группе.
  const focusedLine = useMemo(() => {
    if (!focusedGroup) return null;
    const r = regions.find((x) => x.group === focusedGroup);
    return r ? r.fromLine : null;
  }, [focusedGroup, regions]);

  // Автоскролл соседней панели к совпадающему участку.
  useEffect(() => {
    if (focusedLine == null || !scrollRef.current) return;
    const target = scrollRef.current.querySelector(`[data-line="${focusedLine}"]`);
    if (!target) return;
    // jsdom не реализует scrollIntoView — в тестах это просто no-op.
    target.scrollIntoView?.({ block: "center", behavior: "smooth" });
  }, [focusedLine]);

  return (
    <div className="panel flex min-w-0 flex-col">
      <div className="flex flex-wrap items-center justify-between gap-2 border-b border-line px-3 py-2 text-[13px]">
        <b>
          {side} · {label}
        </b>
        <span className="flex items-center gap-2 text-muted">
          {info && <OriginalName name={info.originalName} />}
          {info && <span>· {info.loc} строк</span>}
        </span>
      </div>
      <div
        ref={scrollRef}
        className="max-h-[640px] overflow-auto py-1.5 font-mono text-[12.5px] leading-[1.6]"
        data-testid={`pane-${side}`}
        onMouseLeave={() => onHoverGroup(null)}
        onClick={() => onFocusGroup(null)}
      >
        {src.isLoading && <p className="px-3 text-muted">Загружаю код…</p>}
        {src.error && <p className="px-3 text-high">Не удалось загрузить код: {(src.error as Error).message}</p>}
        {src.data !== undefined &&
          lines.map((text, i) => {
            const lineNo = i + 1;
            const lineRegions = regions.filter((r) => r.fromLine <= lineNo && lineNo <= r.toLine);
            // Приоритет отображения: hover > focus > всё обычное. Пока есть
            // hover, подсвечивается только его блок (даже если есть focus);
            // иначе подсвечивается фокус; иначе ничего не затемняется.
            const activeGroup = hoverGroup ?? focusedGroup;
            const inActive = activeGroup !== null && lineRegions.some((r) => r.group === activeGroup);
            const dimmed = activeGroup !== null && !inActive;
            return (
              <div
                key={i}
                data-line={lineNo}
                className={`grid grid-cols-[40px_minmax(0,1fr)] whitespace-pre transition-opacity ${dimmed ? "opacity-30" : ""}`}
              >
                <span className="select-none pr-3 text-right tabular-nums text-muted">{lineNo}</span>
                <span className="relative pr-3">
                  <span
                    className="relative block"
                    dangerouslySetInnerHTML={{
                      __html: supported ? highlightLine(text, language) ?? escapeHtml(text) : escapeHtml(text),
                    }}
                  />
                  {lineRegions.map((r) => (
                    <RegionOverlay
                      key={r.id}
                      region={r}
                      edge={regionEdge(r, lineNo)}
                      color={groupColor.get(r.group) ?? "transparent"}
                      active={r.group === activeGroup}
                      focused={r.group === focusedGroup}
                      onHover={onHoverGroup}
                      onFocus={onFocusGroup}
                    />
                  ))}
                </span>
              </div>
            );
          })}
      </div>
    </div>
  );
}

/** Позиция строки внутри региона: какая это сторона внешней границы. */
function regionEdge(region: Region, lineNo: number): "single" | "top" | "middle" | "bottom" {
  if (region.fromLine === region.toLine) return "single";
  if (lineNo === region.fromLine) return "top";
  if (lineNo === region.toLine) return "bottom";
  return "middle";
}

/** Наложение участка на строку: вся строка или подстрока от символа до символа. */
function RegionOverlay({
  region,
  edge,
  color,
  active,
  focused,
  onHover,
  onFocus,
}: {
  region: Region;
  edge: "single" | "top" | "middle" | "bottom";
  color: string;
  /** Этот блок — активный для отображения (hover при наличии, иначе focus). */
  active: boolean;
  /** Этот блок — закреплённый по клику (focus). */
  focused: boolean;
  onHover: (g: string | null) => void;
  onFocus: (g: string | null) => void;
}) {
  const posStyle: React.CSSProperties =
    region.fromCol && region.toCol
      ? { left: `${region.fromCol - 1}ch`, width: `${region.toCol - region.fromCol + 1}ch` }
      : { left: 0, right: 0 };
  const style: React.CSSProperties = {
    ...posStyle,
    // Базовая заливка градиентом; активный блок переопределяет --region-bg.
    ["--region-bg" as string]: `color-mix(in srgb, ${color} 34%, transparent)`,
  };
  return (
    <span
      data-testid="region"
      data-group={region.group}
      data-region-id={region.id}
      title={region.reason}
      role="button"
      tabIndex={0}
      onClick={(e) => {
        e.stopPropagation();
        onFocus(focused ? null : region.group);
      }}
      onKeyDown={(e) => {
        if (e.key === "Enter" || e.key === " ") {
          e.preventDefault();
          onFocus(focused ? null : region.group);
        }
      }}
      onMouseEnter={() => onHover(region.group)}
      onMouseLeave={() => onHover(null)}
      className={`region edge-${edge} ${active ? "region-active" : ""}`}
      style={style}
    />
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