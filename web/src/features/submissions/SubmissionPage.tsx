
import { useQuery } from "@tanstack/react-query";
import { ComparisonNotice } from "./ComparisonNotice";
import { Link, useParams } from "react-router-dom";

import { api, ApiError } from "../../api/target/client";
import { useSubmission } from "../../api/target/hooks";

import type {
  Score,
  SimilarWork,
  SuspiciousRange,
} from "../../api/target/types";

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

function ScoreCard({
  title,
  score,
}: {
  title: string;
  score: Score;
}) {
  return (
    <section className="panel p-5">
      <h2 className="mb-2 text-sm text-muted">
        {title}
      </h2>

      <div className="font-mono text-2xl font-semibold">
        {scoreText(score)}
      </div>
      <ComparisonNotice score={score} />

      {score.status === "failed" &&
        score.reason?.message && (
          <p className="mt-2 text-sm text-red-700">
            {score.reason.message}
          </p>
        )}
    </section>
  );
}

function clamp(
  value: number,
  minimum: number,
  maximum: number,
): number {
  return Math.max(
    minimum,
    Math.min(maximum, value),
  );
}

function SourceCode({
  source,
  suspicious,
}: {
  source: string;
  suspicious: SuspiciousRange[];
}) {
  const bytes = new TextEncoder().encode(source);
  const decoder = new TextDecoder("utf-8");

  const boundaries = new Set<number>([
    0,
    bytes.length,
  ]);

  for (const range of suspicious) {
    const start = clamp(
      range.startIndex,
      0,
      bytes.length,
    );

    const end = clamp(
      range.endIndex,
      0,
      bytes.length,
    );

    if (end > start) {
      boundaries.add(start);
      boundaries.add(end);
    }
  }

  const sorted = [...boundaries].sort(
    (a, b) => a - b,
  );

  const highlighted = sorted.slice(
    0,
    -1,
  ).map((start, index) => {
    const end = sorted[index + 1];

    if (end === undefined) {
      return null;
    }

    const text = decoder.decode(
      bytes.slice(start, end),
    );

    const isSuspicious = suspicious.some(
      (range) =>
        range.startIndex < end &&
        range.endIndex > start,
    );

    return (
      <span
        key={`${start}-${end}`}
        className={
          isSuspicious
            ? "rounded-sm bg-amber-200 text-amber-950"
            : undefined
        }
        data-suspicious={
          isSuspicious ? "true" : undefined
        }
      >
        {text}
      </span>
    );
  });

  return (
    <pre className="overflow-auto p-4 font-mono text-xs leading-6">
      <code>{highlighted}</code>
    </pre>
  );
}

function SimilarWorks({
  items,
  assignmentId,
  submissionId,
}: {
  items: SimilarWork[];
  assignmentId: string;
  submissionId: string;
}) {
  return (
    <section className="panel self-start overflow-hidden">
      <h2 className="border-b border-line p-4 text-lg font-semibold">
        Похожие работы
      </h2>

      {items.length === 0 ? (
        <p className="p-4 text-sm text-muted">
          Похожие работы не найдены.
        </p>
      ) : (
        <div>
          {items.map((item, index) => (
            <Link
              key={`${item.assignmentId}-${item.submissionId}`}
              to={
                `/assignments/${assignmentId}/pairs/` +
                `${submissionId}/${item.submissionId}`
              }
              className="flex items-center gap-3 border-b border-line p-4 last:border-0 hover:bg-sunk"
            >
              <span className="font-mono font-semibold text-accent">
                {`A-${String(index + 1).padStart(2, "0")}`}
              </span>

              <span className="min-w-0 flex-1 text-sm">
                {item.assignmentTitle}
              </span>

              <span className="font-mono font-semibold">
                {scoreText(item.score)}
              </span>
            </Link>
          ))}
        </div>
      )}
    </section>
  );
}

export function SubmissionPage() {
  const { assignmentId, submissionId } =
    useParams<{
      assignmentId: string;
      submissionId: string;
    }>();

  const submissionQuery = useSubmission(
    assignmentId,
    submissionId,
  );

  const sourceQuery = useQuery({
    queryKey: [
      "target",
      "source",
      submissionId ?? "",
    ],
    queryFn: () => api.getSource(submissionId!),
    enabled: !!submissionId,
    retry: false,
  });

  if (!assignmentId || !submissionId) {
    return (
      <div role="alert" className="panel p-6">
        Не указан идентификатор работы.
      </div>
    );
  }

  if (submissionQuery.isPending) {
    return (
      <div role="status" className="py-10 text-center text-muted">
        Загружаем работу...
      </div>
    );
  }

  if (submissionQuery.isError) {
    return (
      <section role="alert" className="panel p-6">
        <h1 className="mb-3 text-xl">
          Не удалось открыть работу
        </h1>

        <p className="mb-4 text-muted">
          {errorMessage(submissionQuery.error)}
        </p>

        <button
          type="button"
          className="btn btn-primary"
          onClick={() =>
            void submissionQuery.refetch()
          }
        >
          Повторить
        </button>
      </section>
    );
  }

  const submission = submissionQuery.data;

  return (
    <div className="flex flex-col gap-5">
      <nav className="text-sm text-muted">
        <Link
          to="/assignments"
          className="text-accent hover:underline"
        >
          Задания
        </Link>

        {" / "}

        <Link
          to={`/assignments/${assignmentId}`}
          className="text-accent hover:underline"
        >
          Работы задания
        </Link>

        {" / Работа"}
      </nav>

      <h1 className="text-2xl font-semibold">
        Работа
      </h1>

      <div className="grid gap-3 sm:grid-cols-2">
        <ScoreCard
          title="Сходство"
          score={submission.similarity}
        />

        <ScoreCard
          title="ИИ-генерация"
          score={submission.ai}
        />
      </div>

      <div className="grid items-start gap-4 lg:grid-cols-[minmax(0,1fr)_360px]">
        <section className="panel min-w-0 overflow-hidden">
          <div className="border-b border-line p-4">
            <h2 className="font-semibold">
              Исходный Java-код
            </h2>
          </div>

          {sourceQuery.isPending && (
            <div
              role="status"
              className="p-6 text-muted"
            >
              Загружаем исходный код...
            </div>
          )}

          {sourceQuery.isError && (
            <div
              role="alert"
              className="p-6"
            >
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
                Повторить загрузку кода
              </button>
            </div>
          )}

          {sourceQuery.isSuccess && (
            <SourceCode
              source={sourceQuery.data}
              suspicious={
                submission.ai.status === "ok"
                  ? submission.suspicious
                  : []
              }
            />
          )}
        </section>

        <SimilarWorks
          items={submission.similar}
          assignmentId={assignmentId}
          submissionId={submissionId}
        />
      </div>
    </div>
  );
}
