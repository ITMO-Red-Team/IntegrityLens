import { useEffect, useRef, useState } from "react";
import { useSaveFeedback } from "../../api/hooks";
import type { Decision, Feedback } from "../../api/types";

const DECISIONS: { value: Decision; label: string; hint: string }[] = [
  { value: "confirmed", label: "Подтверждено", hint: "Совпадение реальное" },
  { value: "acceptable", label: "Допустимо", hint: "Некритичное совпадение" },
  { value: "false_positive", label: "Ложное", hint: "Ложное срабатывание" },
];

const MAX_COMMENT = 10000;

/** Черновик отзыва, кэшируемый между переходами по парам (не теряется). */
interface Draft {
  decision: Decision | null;
  comment: string;
}

const draftCache = new Map<string, Draft>();

interface Props {
  pairId: string;
  /** Уже сохранённый на бэкенде отзыв (может быть null). */
  feedback: Feedback | null;
}

/** Панель отзыва по паре: вердикт + обязательный комментарий. */
export function FeedbackPanel({ pairId, feedback }: Props) {
  const save = useSaveFeedback(pairId);
  const cached = draftCache.get(pairId);
  const [draft, setDraft] = useState<Draft>(
    () => cached ?? { decision: feedback?.decision ?? null, comment: feedback?.comment ?? "" },
  );
  const [savedAt, setSavedAt] = useState<string | null>(feedback?.updatedAt ?? null);

  // Если кэша ещё не было, а данные пары подгрузились — берём их.
  const initialized = useRef(!!cached);
  useEffect(() => {
    if (!initialized.current) {
      setDraft({ decision: feedback?.decision ?? null, comment: feedback?.comment ?? "" });
      initialized.current = true;
    }
  }, [feedback]);

  // Синхронизируем черновик в кэш на каждый рендер, чтобы не терять при переходе.
  useEffect(() => {
    draftCache.set(pairId, draft);
  }, [pairId, draft]);

  const backendComment = feedback?.comment?.trim() ?? "";
  const pendingChanges =
    draft.comment.trim() !== backendComment || draft.decision !== (feedback?.decision ?? null);

  const valid = !!draft.decision && draft.comment.trim() !== "";

  // Предупреждение при попытке уйти/закрыть с несохранёнными правками.
  useEffect(() => {
    if (!pendingChanges) return;
    const handler = (e: BeforeUnloadEvent) => {
      e.preventDefault();
      e.returnValue = "";
    };
    window.addEventListener("beforeunload", handler);
    return () => window.removeEventListener("beforeunload", handler);
  }, [pendingChanges]);

  const onSubmit = () => {
    if (!draft.decision || draft.comment.trim() === "") return;
    save.mutate(
      { decision: draft.decision, comment: draft.comment.trim() },
      {
        onSuccess: (res) => {
          setDraft({ decision: res.decision, comment: res.comment ?? "" });
          setSavedAt(res.updatedAt);
        },
      },
    );
  };

  return (
    <div className="panel p-4" aria-label="Отзыв по паре">
      <div className="flex items-center justify-between gap-2">
        <span className="eyebrow">Отзыв</span>
        {pendingChanges ? (
          <span className="text-xs font-medium text-mid" role="status">
            ⚠ не сохранено
          </span>
        ) : savedAt ? (
          <span className="text-xs text-low" role="status">
            ✓ сохранено
          </span>
        ) : null}
      </div>

      <div className="mt-2 flex flex-wrap gap-1.5">
        {DECISIONS.map((d) => {
          const active = draft.decision === d.value;
          return (
            <button
              key={d.value}
              type="button"
              aria-pressed={active}
              title={d.hint}
              onClick={() => setDraft((prev) => ({ ...prev, decision: d.value }))}
              className={`btn ${active ? "btn-primary" : ""}`}
            >
              {d.label}
            </button>
          );
        })}
      </div>

      <textarea
        className="field mt-2 w-full resize-y"
        rows={4}
        value={draft.comment}
        placeholder="Комментарий (обязателен)"
        aria-label="Комментарий"
        onChange={(e) => setDraft((prev) => ({ ...prev, comment: e.target.value.slice(0, MAX_COMMENT) }))}
      />
      <p className="mt-0.5 text-right text-[11px] text-muted">
        {draft.comment.length}/{MAX_COMMENT}
      </p>

      {save.isError && (
        <p className="text-sm text-high" role="alert">
          Не удалось сохранить: {(save.error as Error).message}
        </p>
      )}

      <div className="mt-2 flex items-center gap-3">
        <button className="btn btn-primary" disabled={!valid || save.isPending} onClick={onSubmit}>
          {save.isPending ? "Сохраняю…" : "Сохранить отзыв"}
        </button>
        {savedAt && <span className="text-xs text-muted">сохранено {new Date(savedAt).toLocaleString()}</span>}
      </div>
    </div>
  );
}