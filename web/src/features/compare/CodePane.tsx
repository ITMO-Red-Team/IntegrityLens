import type { SubmissionCode } from "../../api/types";
import { dateTime } from "../../lib/format";

interface Props {
  side: "A" | "B";
  submission: SubmissionCode;
}

export function CodePane({ side, submission }: Props) {
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
      <div className="max-h-[640px] overflow-auto py-1.5 font-mono text-[12.5px] leading-[1.6]" data-testid={`pane-${side}`}>
        {submission.lines.map((line, i) => (
          <div key={i} className="grid grid-cols-[40px_minmax(0,1fr)] whitespace-pre">
            <span className="select-none pr-3 text-right tabular-nums text-muted">{i + 1}</span>
            <span className="pr-3">{line.text || " "}</span>
          </div>
        ))}
      </div>
    </div>
  );
}