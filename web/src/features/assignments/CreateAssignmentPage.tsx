
import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";

import { ApiError } from "../../api/target/client";
import {
  useAssignments,
  useCreateAssignment,
} from "../../api/target/hooks";

function getErrorMessage(error: unknown): string {
  return error instanceof ApiError
    ? error.message
    : "Не удалось создать задание. Попробуйте ещё раз.";
}

export function CreateAssignmentPage() {
  const navigate = useNavigate();
  const assignmentsQuery = useAssignments();
  const createAssignment = useCreateAssignment();

  const [title, setTitle] = useState("");
  const [sourceIds, setSourceIds] = useState<string[]>([]);

  const assignments = assignmentsQuery.data ?? [];

  function toggleSource(id: string) {
    setSourceIds((previous) =>
      previous.includes(id)
        ? previous.filter((item) => item !== id)
        : [...previous, id],
    );
  }

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault();

    const normalizedTitle = title.trim();

    if (!normalizedTitle || normalizedTitle.length > 200) {
      return;
    }

    try {
      const created = await createAssignment.mutateAsync({
        title: normalizedTitle,
        sourceAssignmentIds: sourceIds,
      });

      navigate(`/assignments/${created.id}/created`, {
        replace: true,
      });
    } catch {
      // Ошибка показывается через createAssignment.error.
    }
  }

  return (
    <div className="mx-auto max-w-[760px]">
      <nav className="mb-2 text-sm text-muted">
        <Link
          to="/assignments"
          className="text-accent hover:underline"
        >
          Задания
        </Link>
        {" / "}Новое задание
      </nav>

      <h1 className="mb-5 text-2xl font-semibold">
        Новое задание
      </h1>

      <section className="panel p-6 sm:p-8">
        <form onSubmit={(event) => void handleSubmit(event)}>
          <label className="mb-6 flex flex-col gap-2">
            <span className="font-semibold">Название</span>

            <input
              className="field w-full px-3 py-2.5"
              placeholder="Например, ДЗ-3. Многопоточность"
              maxLength={200}
              required
              value={title}
              onChange={(event) =>
                setTitle(event.target.value)
              }
            />
          </label>

          <fieldset>
            <legend className="font-semibold">
              Задания-источники
            </legend>

            <p className="mb-3 mt-1 text-sm text-muted">
              С работами этих заданий тоже будет сравнение.
            </p>

            {assignmentsQuery.isPending && (
              <p role="status" className="py-4 text-muted">
                Загружаем список заданий...
              </p>
            )}

            {assignmentsQuery.isError && (
              <div role="alert" className="py-4">
                <p className="mb-3 text-[var(--c-high)]">
                  Не удалось загрузить задания-источники.
                </p>

                <button
                  type="button"
                  className="btn"
                  onClick={() =>
                    void assignmentsQuery.refetch()
                  }
                >
                  Повторить
                </button>
              </div>
            )}

            {assignmentsQuery.isSuccess &&
              assignments.length === 0 && (
                <div className="rounded-lg border border-line p-4 text-muted">
                  Пока нет заданий, которые можно
                  использовать как источники.
                </div>
              )}

            {assignmentsQuery.isSuccess &&
              assignments.length > 0 && (
                <div className="overflow-hidden rounded-lg border border-line">
                  {assignments.map((assignment) => (
                    <label
                      key={assignment.id}
                      className="flex cursor-pointer items-center gap-3 border-b border-line px-4 py-3 last:border-b-0 hover:bg-sunk"
                    >
                      <input
                        type="checkbox"
                        checked={sourceIds.includes(
                          assignment.id,
                        )}
                        onChange={() =>
                          toggleSource(assignment.id)
                        }
                        className="h-4 w-4 accent-[var(--c-accent)]"
                      />

                      <span className="min-w-0 flex-1 font-medium">
                        {assignment.title}
                      </span>

                      <span className="whitespace-nowrap text-sm text-muted">
                        {assignment.submissionCount} работ
                      </span>
                    </label>
                  ))}
                </div>
              )}
          </fieldset>

          {createAssignment.isError && (
            <div
              role="alert"
              className="mt-5 rounded-lg bg-[var(--c-high-bg)] p-4 text-[var(--c-high)]"
            >
              {getErrorMessage(createAssignment.error)}
            </div>
          )}

          <div className="mt-8 flex justify-end gap-3">
            <Link to="/assignments" className="btn px-5 py-2.5">
              Отмена
            </Link>

            <button
              type="submit"
              className="btn btn-primary px-5 py-2.5"
              disabled={
                createAssignment.isPending ||
                assignmentsQuery.isPending ||
                assignmentsQuery.isError ||
                title.trim().length === 0
              }
            >
              {createAssignment.isPending
                ? "Сохраняем..."
                : "Сохранить"}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}
