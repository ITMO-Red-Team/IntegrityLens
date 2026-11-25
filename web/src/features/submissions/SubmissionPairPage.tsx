
import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";

import { api, ApiError } from "../../api/target/client";
import {
  usePair,
  useSubmission,
} from "../../api/target/hooks";

import type {
  Score,
  PairResult,
} from "../../api/target/types";

type Fragment =
  PairResult["methods"]["structsim"]["fragments"][number];

function errorMessage(error: unknown): string {
  return error instanceof ApiError
    ? error.message
    : "Не удалось загрузить данные. Попробуйте ещё раз.";
}

function scoreText(score: Score): string {
  switch (score.status) {
    case "pending":
      return "В анализе…";

    case "failed":
      return "Ошибка анализа";

    case "not_applicable":
      return "Нет данных";

    case "ok":
      return score.value === null
        ? "Нет данных"
        : score.value.toFixed(2);
  }
}

function belongsToRange(
  line: number,
  range: readonly number[],
): boolean {
  return (
    range.length === 2 &&
    line >= (range[0] ?? Infinity) &&
    line <= (range[1] ?? -Infinity)
  );
}

function CodePane({
  label,
  submissionId,
  fragments,
  selectedFragment,
  side,
}: {
  label: string;
  submissionId: string;
  fragments: Fragment[];
  selectedFragment: number | null;
  side: "a" | "b";
}) {
  const sourceQuery = useQuery({
    queryKey: [
      "target",
      "source",
      submissionId,
    ],
    queryFn: () => api.getSource(submissionId),
    retry: false,
  });

  const lines =
    sourceQuery.data?.split(/\r?\n/) ?? [];

  return (
    <section
      aria-label={`Код работы ${label}`}
      className="panel min-w-0 overflow-hidden"
    >
      <div className="flex items-center justify-between gap-3 border-b border-line bg-sunk p-4">
        <h2 className="font-mono font-semibold">
          {label}
        </h2>

        <span className="text-xs text-muted">
          Java
        </span>
      </div>

      {sourceQuery.isPending && (
        <p role="status" className="p-5 text-muted">
          Загружаем исходный код...
        </p>
      )}

      {sourceQuery.isError && (
        <div role="alert" className="p-5">
          <p className="mb-3 text-red-700">
            {errorMessage(sourceQuery.error)}
          </p>

          <button
            type="button"
            className="btn"
            onClick={() =>
              void sourceQuery.refetch()
            }
          >
            Повторить загрузку
          </button>
        </div>
      )}

      {sourceQuery.isSuccess && (
        <div className="overflow-auto">
          <pre className="min-w-max py-3 font-mono text-xs leading-6">
            <code>
              {lines.map((line, index) => {
                const lineNumber = index + 1;

                const matchingFragments = fragments
                  .map((fragment, fragmentIndex) => ({
                    range: fragment[side],
                    fragmentIndex,
                  }))
                  .filter(({ range }) =>
                    belongsToRange(
                      lineNumber,
                      range,
                    ),
                  );

                const isMatching =
                  matchingFragments.length > 0;

                const isSelected =
                  selectedFragment !== null &&
                  matchingFragments.some(
                    ({ fragmentIndex }) =>
                      fragmentIndex ===
                      selectedFragment,
                  );

                const background = isSelected
                  ? "bg-amber-300"
                  : isMatching
                    ? "bg-amber-100"
                    : "";

                return (
                  <div
                    key={lineNumber}
                    data-line={lineNumber}
                    data-matching={
                      isMatching
                        ? "true"
                        : undefined
                    }
                    data-selected={
                      isSelected
                        ? "true"
                        : undefined
                    }
                    className={`flex min-w-max ${background}`}
                  >
                    <span
                      aria-hidden="true"
                      className="mr-4 inline-block w-12 shrink-0 select-none border-r border-line pr-3 text-right text-muted"
                    >
                      {lineNumber}
                    </span>

                    <span className="whitespace-pre pr-5">
                      {line || " "}
                    </span>
                  </div>
                );
              })}
            </code>
          </pre>
        </div>
      )}
    </section>
  );
}

function FragmentPicker({
  fragments,
  selected,
  onSelect,
}: {
  fragments: Fragment[];
  selected: number | null;
  onSelect: (index: number) => void;
}) {
  if (fragments.length === 0) {
    return (
      <p className="text-sm text-muted">
        Совпадающие фрагменты не найдены.
      </p>
    );
  }

  return (
    <div
      role="group"
      aria-label="Совпадающие фрагменты"
      className="flex flex-wrap gap-2"
    >
      {fragments.map((fragment, index) => {
        const active = selected === index;

        return (
          <button
            key={`${index}-${fragment.a.join("-")}-${fragment.b.join("-")}`}
            type="button"
            aria-pressed={active}
            onClick={() => onSelect(index)}
            className={
              active
                ? "rounded-full border border-amber-500 bg-amber-200 px-3 py-1.5 text-sm font-semibold text-amber-950"
                : "rounded-full border border-amber-200 bg-amber-50 px-3 py-1.5 text-sm text-amber-950 hover:bg-amber-100"
            }
          >
            {index + 1} ·{" "}
            {fragment.a[0]}–{fragment.a[1]}
            {" ↔ "}
            {fragment.b[0]}–{fragment.b[1]}
          </button>
        );
      })}
    </div>
  );
}

export function SubmissionPairPage() {
  const { assignmentId, a, b } =
    useParams<{
      assignmentId: string;
      a: string;
      b: string;
    }>();

  const pairQuery = usePair(
    assignmentId,
    a,
    b,
  );

  // Детали основной работы содержат
  // assignmentId похожих работ.
  const mainSubmissionQuery = useSubmission(
    assignmentId,
    a,
  );

  const [
    selectedFragment,
    setSelectedFragment,
  ] = useState<number | null>(null);

  if (!assignmentId || !a || !b) {
    return (
      <section
        role="alert"
        className="panel p-6"
      >
        Некорректная ссылка на сравнение.
      </section>
    );
  }

  if (a === b) {
    return (
      <section
        role="alert"
        className="panel p-6"
      >
        Для сравнения нужны две разные работы.
      </section>
    );
  }

  if (pairQuery.isPending) {
    return (
      <div
        role="status"
        className="py-10 text-center text-muted"
      >
        Загружаем сравнение...
      </div>
    );
  }

  if (pairQuery.isError) {
    return (
      <section
        role="alert"
        className="panel p-6"
      >
        <h1 className="mb-3 text-xl font-semibold">
          Не удалось сравнить работы
        </h1>

        <p className="mb-4 text-muted">
          {errorMessage(pairQuery.error)}
        </p>

        <button
          type="button"
          className="btn btn-primary"
          onClick={() =>
            void pairQuery.refetch()
          }
        >
          Повторить
        </button>
      </section>
    );
  }

  const pair = pairQuery.data;

  const fragments =
    pair.methods.structsim.fragments;

  const active =
    selectedFragment !== null &&
    selectedFragment >= 0 &&
    selectedFragment < fragments.length
      ? selectedFragment
      : null;

  const similarWork =
    mainSubmissionQuery.data?.similar.find(
      (item) => item.submissionId === b,
    );

  // Нельзя автоматически подставлять
  // текущее задание: работа B может
  // принадлежать заданию-источнику.
  const otherAssignmentId =
    similarWork?.assignmentId;

  return (
    <div className="flex flex-col gap-5">
      <nav className="text-sm text-muted">
        <Link
          to={
            `/assignments/${assignmentId}` +
            `/submissions/${a}`
          }
          className="text-accent hover:underline"
        >
          ← Похожие работы
        </Link>
      </nav>

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold">
            Сравнение двух работ
          </h1>

          <p className="mt-1 text-sm text-muted">
            Сопоставление исходного
            Java-кода методом Structsim
          </p>
        </div>

        <span
          aria-label="Оценка сходства"
          className="rounded-lg bg-sunk px-4 py-2 font-mono text-xl font-semibold"
        >
          {scoreText(pair.score)}
        </span>
      </div>

      {pair.score.status === "failed" && (
        <p
          role="alert"
          className="panel p-4 text-red-700"
        >
          {pair.score.reason?.message ??
            "Не удалось выполнить анализ сходства."}
        </p>
      )}

      {pair.score.status === "pending" && (
        <p
          role="status"
          className="panel p-4 text-muted"
        >
          Анализ сходства ещё выполняется.
        </p>
      )}

      <section className="panel p-4">
        <h2 className="mb-3 font-semibold">
          Совпадающие участки
        </h2>

        <FragmentPicker
          fragments={fragments}
          selected={active}
          onSelect={setSelectedFragment}
        />
      </section>

      <div className="grid items-start gap-4 lg:grid-cols-2">
        <CodePane
          label="Работа A"
          submissionId={a}
          fragments={fragments}
          selectedFragment={active}
          side="a"
        />

        <CodePane
          label="Работа B"
          submissionId={b}
          fragments={fragments}
          selectedFragment={active}
          side="b"
        />
      </div>

      <div className="flex flex-wrap gap-3">
        <Link
          to={
            `/assignments/${assignmentId}` +
            `/submissions/${a}`
          }
          className="btn"
        >
          Вернуться к работе A
        </Link>

        {otherAssignmentId ? (
          <Link
            to={
              `/assignments/${otherAssignmentId}` +
              `/submissions/${b}`
            }
            className="btn btn-primary"
          >
            Открыть работу B как основную
          </Link>
        ) : mainSubmissionQuery.isPending ? (
          <span
            role="status"
            className="text-sm text-muted"
          >
            Определяем задание второй работы...
          </span>
        ) : (
          <span className="text-sm text-muted">
            Переход к работе B недоступен:
            не удалось определить её задание.
          </span>
        )}
      </div>

      <p className="text-sm text-muted">
        Оценка сходства — основание
        для ручной проверки, а не
        доказательство нарушения.
      </p>
    </div>
  );
}
