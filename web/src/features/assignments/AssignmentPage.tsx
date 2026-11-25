
import { useState } from "react";
import {
  Link,
  useNavigate,
  useParams,
  useSearchParams,
} from "react-router-dom";

import {
  useAssignment,
  useAssignments,
  useDeleteAssignment,
  useResetStudentAttempt,
  useSubmissions,
  useUpdateAssignmentSources,
} from "../../api/target/hooks";

import type {
  Score,
  SubmissionRow,
} from "../../api/target/types";

import {
  ApiError,
  type SubmissionSort,
} from "../../api/target/client";
import { anonymousSubmissionLabel } from "../../lib/labels";

const PAGE_SIZE = 20;

function errorMessage(error: unknown): string {
  return error instanceof ApiError
    ? error.message
    : "Не удалось выполнить действие. Попробуйте ещё раз.";
}

function formatDate(value: string): string {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "—";
  }

  return new Intl.DateTimeFormat("ru-RU", {
    day: "2-digit",
    month: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

function ScoreCell({
  score,
}: {
  score: Score;
}) {
  if (score.status === "pending") {
    return (
      <span className="text-muted">
        В анализе…
      </span>
    );
  }

  if (score.status === "failed") {
    return (
      <span
        className="text-red-700"
        title={
          score.reason?.message ??
          "Ошибка анализа"
        }
      >
        Ошибка
      </span>
    );
  }

  if (score.status === "not_applicable") {
    return (
      <span className="text-muted">
        Нет данных
      </span>
    );
  }

  if (score.value === null) {
    return (
      <span className="text-muted">
        —
      </span>
    );
  }

  const color =
    score.value >= 0.75
      ? "var(--c-high)"
      : score.value >= 0.4
        ? "var(--c-mid)"
        : "var(--c-low)";

  return (
    <span
      className="font-mono font-semibold"
      style={{ color }}
    >
      {score.value.toFixed(2)}
    </span>
  );
}

type PendingReset = {
  studentId: string;
  submissionId: string;
  label: string;
};

function readPage(value: string | null): number {
  if (!value || !/^[1-9]\d*$/.test(value)) {
    return 1;
  }

  const number = Number(value);

  return Number.isSafeInteger(number)
    ? number
    : 1;
}

function readSort(
  value: string | null,
): SubmissionSort {
  return value === "ai"
    ? "ai"
    : "similarity";
}

export function AssignmentPage() {
  const { assignmentId } = useParams();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] =
    useSearchParams();

  const sort = readSort(
    searchParams.get("sort"),
  );

  const page = readPage(
    searchParams.get("page"),
  );

  const assignmentQuery =
    useAssignment(assignmentId);

  const submissionsQuery =
    useSubmissions(
      assignmentId,
      sort,
      page,
      PAGE_SIZE,
    );

  const assignmentsQuery =
    useAssignments();

  const updateSources =
    useUpdateAssignmentSources();

  const deleteAssignment =
    useDeleteAssignment();

  const resetAttempt =
    useResetStudentAttempt();

  const [showNames, setShowNames] =
    useState(false);

  const [copied, setCopied] =
    useState(false);

  const [copyError, setCopyError] =
    useState(false);

  const [
    editingSources,
    setEditingSources,
  ] = useState(false);

  const [
    selectedSources,
    setSelectedSources,
  ] = useState<string[]>([]);

  const [
    confirmDelete,
    setConfirmDelete,
  ] = useState(false);

  const [
    pendingReset,
    setPendingReset,
  ] = useState<PendingReset | null>(null);

  const [notice, setNotice] =
    useState("");

  const assignment = assignmentQuery.data;
  const submissions = submissionsQuery.data;

  const availableSources = (
    assignmentsQuery.data ?? []
  ).filter(
    (item) => item.id !== assignmentId,
  );

  function changeSort(value: SubmissionSort) {
    setSearchParams(
      (previous) => {
        const next = new URLSearchParams(
          previous,
        );

        if (value === "similarity") {
          next.delete("sort");
        } else {
          next.set("sort", value);
        }

        next.delete("page");

        return next;
      },
      { replace: true },
    );
  }

  function changePage(value: number) {
    if (value < 1) {
      return;
    }

    setSearchParams(
      (previous) => {
        const next = new URLSearchParams(
          previous,
        );

        if (value === 1) {
          next.delete("page");
        } else {
          next.set("page", String(value));
        }

        return next;
      },
    );
  }

  async function copyLink() {
    if (!assignment?.submissionUrl) {
      return;
    }

    try {
      await navigator.clipboard.writeText(
        assignment.submissionUrl,
      );

      setCopied(true);
      setCopyError(false);
    } catch {
      setCopied(false);
      setCopyError(true);
    }
  }

  function beginEditingSources() {
    if (!assignment) {
      return;
    }

    setSelectedSources([
      ...assignment.sourceAssignmentIds,
    ]);

    updateSources.reset();
    setEditingSources(true);
    setNotice("");
  }

  function toggleSource(id: string) {
    setSelectedSources((previous) =>
      previous.includes(id)
        ? previous.filter(
            (item) => item !== id,
          )
        : [...previous, id],
    );
  }

  async function saveSources() {
    if (!assignmentId) {
      return;
    }

    try {
      await updateSources.mutateAsync({
        assignmentId,
        sourceAssignmentIds:
          selectedSources,
      });

      setEditingSources(false);
      setNotice(
        "Задания-источники обновлены.",
      );
    } catch {
      // Ошибка отображается ниже.
    }
  }

  async function removeAssignment() {
    if (!assignmentId) {
      return;
    }

    try {
      await deleteAssignment.mutateAsync(
        assignmentId,
      );

      navigate("/assignments", {
        replace: true,
      });
    } catch {
      // Ошибка отображается ниже.
    }
  }

  async function confirmResetAttempt() {
    if (!assignmentId || !pendingReset) {
      return;
    }

    try {
      await resetAttempt.mutateAsync({
        assignmentId,
        studentId:
          pendingReset.studentId,
        expectedSubmissionId:
          pendingReset.submissionId,
      });

      setPendingReset(null);
      setNotice(
        "Повторная сдача разрешена. Уже принятая работа сохранена.",
      );
    } catch {
      // Ошибка отображается в подтверждении.
    }
  }

  if (assignmentQuery.isPending) {
    return (
      <div
        role="status"
        className="py-10 text-center text-muted"
      >
        Загружаем задание...
      </div>
    );
  }

  if (
    assignmentQuery.isError ||
    !assignment
  ) {
    return (
      <section
        role="alert"
        className="panel p-8"
      >
        <h1 className="mb-3 text-xl">
          Не удалось открыть задание
        </h1>

        <p className="mb-4 text-muted">
          {errorMessage(
            assignmentQuery.error,
          )}
        </p>

        <button
          type="button"
          className="btn btn-primary"
          onClick={() =>
            void assignmentQuery.refetch()
          }
        >
          Повторить
        </button>
      </section>
    );
  }

  const total = assignment.submissionCount;
  const completed =
    assignment.analysis.completed;
  const failed =
    assignment.analysis.failed;

  const processing = Math.max(
    total - completed,
    0,
  );

  const pageCount = submissions
    ? Math.ceil(
        submissions.total / PAGE_SIZE,
      )
    : 0;

  return (
    <div className="flex flex-col gap-5">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <nav className="mb-1 text-sm text-muted">
            <Link
              to="/assignments"
              className="text-accent hover:underline"
            >
              Задания
            </Link>
            {" / "}
            {assignment.title}
          </nav>

          <h1 className="text-2xl font-semibold">
            {assignment.title}
          </h1>
        </div>

        <div className="flex flex-wrap gap-2">
          <button
            type="button"
            className="btn px-4 py-2"
            onClick={() =>
              void copyLink()
            }
          >
            {copied
              ? "Ссылка скопирована"
              : "Копировать ссылку"}
          </button>

          <button
            type="button"
            className="btn px-4 py-2"
            onClick={beginEditingSources}
          >
            Изменить источники
          </button>

          <button
            type="button"
            className="btn border border-red-200 px-4 py-2 text-red-700"
            onClick={() => {
              deleteAssignment.reset();
              setConfirmDelete(true);
            }}
          >
            Удалить задание
          </button>
        </div>
      </div>

      {copyError && (
        <div
          role="alert"
          className="panel p-4 text-red-700"
        >
          Не удалось скопировать ссылку.
          Попробуйте ещё раз.
        </div>
      )}

      {notice && (
        <div role="status" className="panel p-4">
          {notice}
        </div>
      )}

      {editingSources && (
        <section
          aria-label="Редактирование источников"
          className="panel p-6"
        >
          <h2 className="mb-2 text-xl font-semibold">
            Изменить задания-источники
          </h2>

          <p className="mb-4 text-sm text-muted">
            С работами выбранных заданий
            будет выполняться сравнение.
            Изменение источников запускает
            пересчёт сходства, но не анализа ИИ.
          </p>

          {assignmentsQuery.isPending && (
            <p role="status">
              Загружаем задания...
            </p>
          )}

          {assignmentsQuery.isError && (
            <div role="alert">
              <p className="mb-3">
                Не удалось загрузить
                задания-источники.
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

          {assignmentsQuery.isSuccess && (
            <div className="overflow-hidden rounded-lg border border-line">
              {availableSources.length === 0 ? (
                <p className="p-4 text-muted">
                  Других заданий пока нет.
                </p>
              ) : (
                availableSources.map(
                  (source) => (
                    <label
                      key={source.id}
                      className="flex cursor-pointer items-center gap-3 border-b border-line p-4 last:border-0"
                    >
                      <input
                        type="checkbox"
                        checked={selectedSources.includes(
                          source.id,
                        )}
                        onChange={() =>
                          toggleSource(source.id)
                        }
                      />

                      <span className="min-w-0 flex-1">
                        {source.title}
                      </span>

                      <span className="text-sm text-muted">
                        {source.submissionCount} работ
                      </span>
                    </label>
                  ),
                )
              )}
            </div>
          )}

          {updateSources.isError && (
            <p
              role="alert"
              className="mt-4 text-red-700"
            >
              {errorMessage(
                updateSources.error,
              )}
            </p>
          )}

          <div className="mt-5 flex justify-end gap-3">
            <button
              type="button"
              className="btn"
              onClick={() =>
                setEditingSources(false)
              }
            >
              Отмена
            </button>

            <button
              type="button"
              className="btn btn-primary"
              disabled={
                updateSources.isPending ||
                !assignmentsQuery.isSuccess
              }
              onClick={() =>
                void saveSources()
              }
            >
              {updateSources.isPending
                ? "Сохраняем..."
                : "Сохранить источники"}
            </button>
          </div>
        </section>
      )}

      {confirmDelete && (
        <section
          role="alertdialog"
          aria-labelledby="delete-title"
          aria-describedby="delete-description"
          className="panel border border-red-200 p-6"
        >
          <h2
            id="delete-title"
            className="mb-2 text-xl font-semibold"
          >
            Удалить задание?
          </h2>

          <p
            id="delete-description"
            className="mb-5 text-muted"
          >
            Задание «{assignment.title}»
            исчезнет из активного списка,
            а ссылка для сдачи перестанет
            работать. Это действие нельзя
            отменить через интерфейс.
          </p>

          {deleteAssignment.isError && (
            <p
              role="alert"
              className="mb-4 text-red-700"
            >
              {errorMessage(
                deleteAssignment.error,
              )}
            </p>
          )}

          <div className="flex flex-wrap gap-3">
            <button
              type="button"
              className="btn"
              disabled={deleteAssignment.isPending}
              onClick={() =>
                setConfirmDelete(false)
              }
            >
              Отмена
            </button>

            <button
              type="button"
              className="btn border border-red-200 bg-red-700 px-5 py-2 text-white"
              disabled={deleteAssignment.isPending}
              onClick={() =>
                void removeAssignment()
              }
            >
              {deleteAssignment.isPending
                ? "Удаляем..."
                : "Подтвердить удаление"}
            </button>
          </div>
        </section>
      )}

      {pendingReset && (
        <section
          role="alertdialog"
          aria-labelledby="reset-title"
          aria-describedby="reset-description"
          className="panel p-6"
        >
          <h2
            id="reset-title"
            className="mb-2 text-xl font-semibold"
          >
            Разрешить повторную сдачу?
          </h2>

          <p
            id="reset-description"
            className="mb-5 text-muted"
          >
            Для работы {pendingReset.label}
            откроется новая попытка сдачи.
            Текущая работа не удаляется.
          </p>

          {resetAttempt.isError && (
            <p
              role="alert"
              className="mb-4 text-red-700"
            >
              {errorMessage(
                resetAttempt.error,
              )}
            </p>
          )}

          <div className="flex flex-wrap gap-3">
            <button
              type="button"
              className="btn"
              disabled={resetAttempt.isPending}
              onClick={() =>
                setPendingReset(null)
              }
            >
              Отмена
            </button>

            <button
              type="button"
              className="btn btn-primary"
              disabled={resetAttempt.isPending}
              onClick={() =>
                void confirmResetAttempt()
              }
            >
              {resetAttempt.isPending
                ? "Сбрасываем..."
                : "Подтвердить сброс"}
            </button>
          </div>
        </section>
      )}

      <section className="panel flex flex-wrap items-center justify-between gap-4 p-4">
        <div className="flex flex-wrap gap-x-6 gap-y-2 text-sm">
          <span>
            Работ: <strong>{total}</strong>
          </span>
          <span>
            Проанализировано:{" "}
            <strong>{completed}</strong>
          </span>
          <span>
            В анализе:{" "}
            <strong>{processing}</strong>
          </span>
          <span>
            Ошибки:{" "}
            <strong className="text-red-700">
              {failed}
            </strong>
          </span>
        </div>

        <label className="flex cursor-pointer items-center gap-2 font-medium">
          <input
            type="checkbox"
            checked={showNames}
            onChange={(event) =>
              setShowNames(event.target.checked)
            }
            className="h-4 w-4"
          />
          Показать ФИО
        </label>
      </section>

      <section className="panel overflow-hidden">
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-line p-4">
          <label
            htmlFor="submission-sort"
            className="font-medium"
          >
            Сортировка работ
          </label>

          <select
            id="submission-sort"
            aria-label="Сортировка работ"
            className="field w-full max-w-[260px]"
            value={sort}
            onChange={(event) =>
              changeSort(
                event.target.value as SubmissionSort,
              )
            }
          >
            <option value="similarity">
              По сходству (Structsim)
            </option>
            <option value="ai">
              По AI-оценке
            </option>
          </select>
        </div>

        {submissionsQuery.isPending && (
          <div
            role="status"
            className="p-8 text-center text-muted"
          >
            Загружаем работы...
          </div>
        )}

        {submissionsQuery.isError && (
          <div
            role="alert"
            className="p-8"
          >
            <h2 className="mb-2 text-lg">
              Не удалось загрузить работы
            </h2>

            <p className="mb-4 text-muted">
              {errorMessage(
                submissionsQuery.error,
              )}
            </p>

            <button
              type="button"
              className="btn btn-primary"
              onClick={() =>
                void submissionsQuery.refetch()
              }
            >
              Повторить
            </button>
          </div>
        )}

        {submissionsQuery.isSuccess &&
          submissions?.total === 0 && (
            <div className="p-10 text-center">
              <h2 className="mb-2 text-lg">
                Работы ещё не сданы
              </h2>

              <p className="text-muted">
                Передайте студентам ссылку
                на задание.
              </p>
            </div>
          )}

        {submissionsQuery.isSuccess &&
          submissions &&
          submissions.total > 0 &&
          submissions.items.length === 0 && (
            <div className="p-10 text-center">
              <h2 className="mb-2 text-lg">
                На этой странице работ нет
              </h2>

              <button
                type="button"
                className="btn btn-primary mt-3"
                onClick={() => changePage(1)}
              >
                К первой странице
              </button>
            </div>
          )}

        {submissionsQuery.isSuccess &&
          submissions &&
          submissions.items.length > 0 && (
            <>
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                  <thead className="bg-sunk text-muted">
                    <tr>
                      <th scope="col" className="th">
                        #
                      </th>
                      <th scope="col" className="th">
                        Работа
                      </th>
                      <th scope="col" className="th">
                        Сходство
                      </th>
                      <th scope="col" className="th">
                        ИИ-генерация
                      </th>
                      <th scope="col" className="th">
                        Сдано
                      </th>
                      <th scope="col" className="th">
                        Действие
                      </th>
                    </tr>
                  </thead>

                  <tbody>
                    {submissions.items.map(
                      (
                        item: SubmissionRow,
                        index: number,
                      ) => {
                        const number =
                          (page - 1) * PAGE_SIZE +
                          index +
                          1;

                        const label = anonymousSubmissionLabel(
                          item.submissionId,
                        );

                        return (
                          <tr
                            key={item.submissionId}
                            className="border-b border-line last:border-0 hover:bg-sunk"
                          >
                            <td className="td text-muted">
                              {number}
                            </td>

                            <td className="td">
                              <Link
                                to={
                                  `/assignments/${assignmentId}` +
                                  `/submissions/${item.submissionId}`
                                }
                                className="wrap-anywhere font-mono font-semibold text-accent hover:underline"
                                aria-label={`Открыть работу ${label}`}
                              >
                                {label}
                              </Link>

                              {showNames && (
                                <span className="ml-2 text-muted">
                                  · {item.fullName}
                                </span>
                              )}
                            </td>

                            <td className="td">
                              <ScoreCell
                                score={item.similarity}
                              />
                            </td>

                            <td className="td">
                              <ScoreCell
                                score={item.ai}
                              />
                            </td>

                            <td className="td whitespace-nowrap text-muted">
                              {formatDate(
                                item.submittedAt,
                              )}
                            </td>

                            <td className="td">
                              <button
                                type="button"
                                className="text-accent hover:underline"
                                aria-label={`Сбросить попытку ${label}`}
                                onClick={() => {
                                  resetAttempt.reset();
                                  setNotice("");
                                  setPendingReset({
                                    studentId:
                                      item.studentId,
                                    submissionId:
                                      item.submissionId,
                                    label,
                                  });
                                }}
                              >
                                Сбросить попытку
                              </button>
                            </td>
                          </tr>
                        );
                      },
                    )}
                  </tbody>
                </table>
              </div>

              <div className="flex flex-wrap items-center justify-between gap-3 border-t border-line px-4 py-3 text-xs text-muted">
                <span>
                  Показано{" "}
                  {submissions.items.length} из{" "}
                  {submissions.total}
                </span>

                <nav
                  aria-label="Страницы работ"
                  className="flex items-center gap-3"
                >
                  <button
                    type="button"
                    className="btn"
                    disabled={page <= 1}
                    onClick={() =>
                      changePage(page - 1)
                    }
                  >
                    Назад
                  </button>

                  <span>
                    Страница {page} из{" "}
                    {Math.max(pageCount, 1)}
                  </span>

                  <button
                    type="button"
                    className="btn"
                    disabled={page >= pageCount}
                    onClick={() =>
                      changePage(page + 1)
                    }
                  >
                    Далее
                  </button>
                </nav>
              </div>
            </>
          )}
      </section>
    </div>
  );
}
