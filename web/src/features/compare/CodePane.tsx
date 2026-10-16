import { forwardRef, useImperativeHandle, useRef } from "react";
import type { SubmissionCode } from "../../api/types";
import { dateTime } from "../../lib/format";

export const FRAG_BG = ["bg-f1-bg", "bg-f2-bg", "bg-f3-bg", "bg-f4-bg"] as const;
export const FRAG_MARK = ["bg-f1", "bg-f2", "bg-f3", "bg-f4"] as const;

interface Props {
  side: "A" | "B";
  submission: SubmissionCode;
  ranges: [number, number][];
  renamed: Set<string>;
}

export interface CodePaneHandle {
  reveal: (range: [number, number]) => void;
}

const IDENT = /([A-Za-z_][A-Za-z0-9_]*)/g;

function fragmentOf(i: number, ranges: [number, number][]) {
  return ranges.findIndex(([a, b]) => i >= a && i <= b);
}

export const CodePane = forwardRef<CodePaneHandle, Props>(function CodePane({ side, submission, ranges, renamed }, ref) {
  const box = useRef<HTMLDivElement>(null);

  useImperativeHandle(ref, () => ({
    reveal: ([a, b]) => {
      const root = box.current;
      const first = root?.querySelector<HTMLElement>(`[data-line="${a}"]`);
      if (!root || !first) return;
      root.scrollTo?.({ top: first.offsetTop - 24, behavior: "smooth" });
      for (let i = a; i <= b; i++) {
        const el = root.querySelector<HTMLElement>(`[data-line="${i}"]`);
        el?.classList.add("outline-2", "-outline-offset-2", "outline-accent", "outline");
        setTimeout(() => el?.classList.remove("outline-2", "-outline-offset-2", "outline-accent", "outline"), 1200);
      }
    },
  }));

  return (
    <div className="panel flex min-w-0 flex-col">
      <div className="flex justify-between gap-2 border-b border-line px-3 py-2 text-[13px]">
        <b>
          {side} · {submission.id}
        </b>
        <span className="truncate text-muted">
          {submission.fileName} · сдано {dateTime(submission.submittedAt)}
        </span>
      </div>
      <div ref={box} className="relative max-h-[520px] overflow-auto py-1.5 font-mono text-[12.5px] leading-[1.6]" data-testid={`pane-${side}`}>
        {submission.lines.map((line, i) => {
          const f = fragmentOf(i, ranges);
          const bg = line.kind === "template" ? "bg-tmpl" : f >= 0 ? FRAG_BG[f % 4] : "";
          const textCls = line.kind === "template" || line.kind === "comment" ? "text-muted" : "";
          return (
            <div key={i} data-line={i} data-fragment={f >= 0 ? f : undefined} className={`grid grid-cols-[40px_6px_minmax(0,1fr)] whitespace-pre ${bg}`}>
              <span className="select-none pr-2 text-right tabular-nums text-muted">{i + 1}</span>
              <span className={f >= 0 ? FRAG_MARK[f % 4] : ""} />
              <span className={`pr-3 ${textCls}`}>
                {f >= 0
                  ? line.text.split(IDENT).map((part, k) =>
                      renamed.has(part) ? (
                        <span key={k} className="font-medium text-accent underline decoration-dotted underline-offset-[3px]" title="Переименованный идентификатор">
                          {part}
                        </span>
                      ) : (
                        part
                      ),
                    )
                  : line.text || " "}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
});