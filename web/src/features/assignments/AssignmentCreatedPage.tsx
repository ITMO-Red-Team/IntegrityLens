
import { useState } from "react";
import { Link, useParams } from "react-router-dom";

import { useAssignment } from "../../api/target/hooks";
import { ApiError } from "../../api/target/client";

export function AssignmentCreatedPage() {
  const { assignmentId } = useParams();
  const { data, isPending, error, refetch } =
    useAssignment(assignmentId);

  const [copied, setCopied] = useState(false);
  const [copyError, setCopyError] = useState(false);

  async function copyLink() {
    if (!data?.submissionUrl) return;

    try {
      await navigator.clipboard.writeText(
        data.submissionUrl,
      );

      setCopied(true);
      setCopyError(false);
    } catch {
      setCopied(false);
      setCopyError(true);
    }
  }

  if (isPending) {
    return (
      <div role="status" className="py-10 text-center">
        Загружаем задание...
      </div>
    );
  }

  if (error || !data) {
    return (
      <section role="alert" className="panel mx-auto max-w-[720px] p-8">
        <h1 className="mb-3 text-xl">
          Не удалось открыть задание
        </h1>

        <p className="mb-4 text-muted">
          {error instanceof ApiError
            ? error.message
            : "Проверьте соединение."}
        </p>

        <button
          type="button"
          className="btn"
          onClick={() => void refetch()}
        >
          Повторить
        </button>
      </section>
    );
  }

  return (
    <div className="mx-auto max-w-[720px] py-8">
      <section className="panel p-6 sm:p-10">
        <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-[var(--c-low-bg)] text-2xl text-[var(--c-low)]">
          ✓
        </div>

        <h1 className="text-2xl font-semibold">
          Задание создано
        </h1>

        <p className="mb-7 mt-1 text-muted">
          {data.title}
        </p>

        <label
          htmlFor="submission-url"
          className="mb-2 block font-semibold"
        >
          Ссылка для сдачи
        </label>

        <div className="flex flex-col gap-2 sm:flex-row">
          <input
            id="submission-url"
            className="field min-w-0 flex-1 px-3 py-2.5 font-mono"
            value={data.submissionUrl}
            readOnly
            onFocus={(event) =>
              event.target.select()
            }
          />

          <button
            type="button"
            className="btn btn-primary px-5"
            onClick={() => void copyLink()}
          >
            {copied ? "Скопировано" : "Копировать"}
          </button>
        </div>

        {copyError && (
          <p role="alert" className="mt-2 text-sm text-[var(--c-high)]">
            Не удалось скопировать автоматически.
            Выделите ссылку в поле и скопируйте её вручную.
          </p>
        )}

        {copied && (
          <p role="status" className="mt-2 text-sm text-[var(--c-low)]">
            Ссылка скопирована.
          </p>
        )}

        <div className="mt-10 flex flex-wrap gap-3">
          <Link
            to={`/assignments/${data.id}`}
            className="btn btn-primary px-5 py-2.5"
          >
            Открыть задание
          </Link>

          <Link
            to="/assignments"
            className="btn px-5 py-2.5"
          >
            К списку заданий
          </Link>
        </div>
      </section>
    </div>
  );
}
