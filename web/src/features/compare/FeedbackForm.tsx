import { useEffect, useState, type FormEvent } from "react";
import { useSaveFeedback } from "../../api/hooks";
import type { Decision, Feedback } from "../../api/types";
import { dateTime } from "../../lib/format";

const OPTIONS: { value: Decision; title: string; hint: string }[] = [
  { value: "confirmed", title: "Заимствование подтверждаю", hint: "Нужен разговор со студентами по регламенту курса" },
  { value: "acceptable", title: "Сходство допустимо", hint: "Типовое решение, материал лекции, разрешённая совместная работа" },
  { value: "false_positive", title: "Ложное срабатывание", hint: "Сигнал не подтверждается; учтётся при калибровке порога" },
];

interface Props {
  pairId: string;
  initial?: Feedback | null;
  onSaved?: () => void;
}

export function FeedbackForm({ pairId, initial, onSaved }: Props) {
  const [decision, setDecision] = useState<Decision | null>(initial?.decision ?? null);
  const [comment, setComment] = useState(initial?.comment ?? "");
  const save = useSaveFeedback(pairId);

  useEffect(() => {
    setDecision(initial?.decision ?? null);
    setComment(initial?.comment ?? "");
  }, [pairId, initial?.decision, initial?.comment]);

  const submit = (e: FormEvent) => {
    e.preventDefault();
    if (!decision) return;
    save.mutate({ decision, comment: comment.trim() }, { onSuccess: () => onSaved?.() });
  };

  return (
    <form onSubmit={submit} className="panel flex flex-col gap-3 p-4" aria-labelledby="fb-h">
      <h2 id="fb-h" className="text-lg">
        Решение проверяющего
      </h2>
      <fieldset className="flex flex-col gap-2">
        <legend className="sr-only">Решение</legend>
        {OPTIONS.map((o) => (
          <label
            key={o.value}
            className={`flex cursor-pointer items-start gap-2.5 rounded-md border px-3 py-2.5 ${
              decision === o.value ? "border-accent bg-f2-bg" : "border-line"
            }`}
          >
            <input
              type="radio"
              name="decision"
              value={o.value}
              checked={decision === o.value}
              onChange={() => setDecision(o.value)}
              className="mt-1 accent-[var(--c-accent)]"
            />
            <span>
              <b>{o.title}</b>
              <br />
              <span className="text-muted">{o.hint}</span>
            </span>
          </label>
        ))}
      </fieldset>
      <label htmlFor="fb-comment" className="flex flex-col gap-1 text-xs font-medium text-muted">
        Комментарий (виден только проверяющим)
        <textarea
          id="fb-comment"
          className="field min-h-[70px] font-sans"
          maxLength={2000}
          placeholder="Например: совпадает необязательная сортировка соседей"
          value={comment}
          onChange={(e) => setComment(e.target.value)}
        />
      </label>
      {save.isError && (
        <p role="alert" className="text-high">
          {(save.error as Error).message}
        </p>
      )}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <span className="text-xs text-muted" aria-live="polite">
          {save.isSuccess ? "Отзыв сохранён" : initial ? `Сохранено ${dateTime(initial.updatedAt)}` : "Отзыв не сохранён"}
        </span>
        <button type="submit" className="btn btn-primary" disabled={!decision || save.isPending}>
          {save.isPending ? "Сохраняю…" : "Сохранить и к следующей"}
        </button>
      </div>
    </form>
  );
}