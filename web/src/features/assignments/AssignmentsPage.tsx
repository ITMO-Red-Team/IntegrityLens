
import { Link } from "react-router-dom";

import { useAssignments } from "../../api/target/hooks";
import type { AssignmentSummary } from "../../api/target/types";
import { ApiError } from "../../api/target/client";

function formatDate(value: string): string {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "—";
  }

  return new Intl.DateTimeFormat("ru-RU", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    timeZone: "UTC",
  }).format(date);
}

function AnalysisStatus({
  assignment,
}: {
  assignment: AssignmentSummary;
}) {
  const count = assignment.submissionCount;
  const { completed, failed } = assignment.analysis;

  if (count === 0) {
    return (
      <span className="rounded-full bg-sunk px-3 py-1 text-xs font-medium text-muted">
        Нет работ
      </span>
    );
  }

  if (completed < count) {
    return (
      <span className="rounded-full bg-[var(--c-mid-bg)] px-3 py-1 text-xs font-semibold text-[var(--c-mid)]">
        {completed} из {count}
        {failed > 0 ? ` · ошибок: ${failed}` : ""}
      </span>
    );
  }

  if (failed > 0) {
    return (
      <span className="rounded-full bg-[var(--c-high-bg)] px-3 py-1 text-xs font-semibold text-[var(--c-high)]">
        Ошибки: {failed} из {count}
      </span>
    );
  }

  return (
    <span className="rounded-full bg-[var(--c-low-bg)] px-3 py-1 text-xs font-semibold text-[var(--c-low)]">
      Готово
    </span>
  );
}

export function AssignmentsPage() {
  const {
    data: assignments,
    isPending,
    isError,
    error,
    refetch,
    isFetching,
  } = useAssignments();

  return (
    <div className="flex flex-col gap-5">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="text-2xl font-semibold">
          Мои задания
        </h1>

        <Link
          to="/assignments/new"
          className="btn btn-primary px-5 py-2.5"
        >
          + Создать задание
        </Link>
      </div>

      {isPending && (
        <div
          role="status"
          className="panel p-8 text-center text-muted"
        >
          Загружаем задания...
        </div>
      )}

      {isError && (
        <section
          role="alert"
          className="panel flex flex-col items-start gap-4 p-6"
        >
          <div>
            <h2 className="mb-2 text-lg">
              Не удалось загрузить задания
            </h2>
            <p className="text-muted">
              {error instanceof ApiError
                ? error.message
                : "Проверьте соединение и попробуйте ещё раз."}
            </p>
          </div>

          <button
            type="button"
            className="btn btn-primary"
            disabled={isFetching}
            onClick={() => void refetch()}
          >
            Повторить
          </button>
        </section>
      )}

      {!isPending &&
        !isError &&
        assignments?.length === 0 && (
          <section className="panel px-6 py-14 text-center">
            <h2 className="mb-2 text-xl">
              У вас пока нет заданий
            </h2>

            <p className="mb-5 text-muted">
              Создайте первое задание, чтобы получить
              ссылку для сдачи работ студентами.
            </p>

            <Link
              to="/assignments/new"
              className="btn btn-primary px-5 py-2.5"
            >
              Создать первое задание
            </Link>
          </section>
        )}

      {!isPending &&
        !isError &&
        assignments &&
        assignments.length > 0 && (
          <section className="panel overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-sunk text-muted">
                  <tr>
                    <th scope="col" className="th px-6 py-3">
                      Название
                    </th>
                    <th scope="col" className="th px-6 py-3">
                      Работ
                    </th>
                    <th scope="col" className="th px-6 py-3">
                      Анализ
                    </th>
                    <th scope="col" className="th px-6 py-3">
                      Создано
                    </th>
                  </tr>
                </thead>

                <tbody>
                  {assignments.map((assignment) => (
                    <tr
                      key={assignment.id}
                      className="border-b border-line last:border-b-0 hover:bg-sunk"
                    >
                      <td className="px-6 py-4 font-semibold">
                        <Link
                          className="text-accent hover:underline"
                          to={`/assignments/${assignment.id}`}
                        >
                          {assignment.title}
                        </Link>
                      </td>

                      <td className="px-6 py-4">
                        {assignment.submissionCount}
                      </td>

                      <td className="px-6 py-4">
                        <AnalysisStatus
                          assignment={assignment}
                        />
                      </td>

                      <td className="whitespace-nowrap px-6 py-4 text-muted">
                        {formatDate(
                          assignment.createdAt,
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        )}
    </div>
  );
}
