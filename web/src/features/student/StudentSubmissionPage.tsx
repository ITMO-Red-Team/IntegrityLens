
import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";

import { ApiError } from "../../api/target/client";
import {
  getSubmissionForm,
  submitJavaFile,
  validateJavaFile,
  type SubmissionReceipt,
} from "../../api/target/students";

function formatDate(value: string): string {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("ru-RU", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(date);
}

function errorMessage(error: unknown): string {
  return error instanceof ApiError
    ? error.message
    : "Не удалось выполнить запрос. Попробуйте ещё раз.";
}

export function StudentSubmissionPage() {
  const { token } = useParams<{ token: string }>();
  const queryClient = useQueryClient();

  const [file, setFile] = useState<File | null>(null);
  const [fileError, setFileError] = useState("");
  const [receipt, setReceipt] =
    useState<SubmissionReceipt | null>(null);

  const formQuery = useQuery({
    queryKey: ["target", "student-form", token ?? ""],
    queryFn: () => getSubmissionForm(token!),
    enabled: !!token,
    retry: false,
  });

  const submission = useMutation({
    mutationFn: (selectedFile: File) =>
      submitJavaFile(token!, selectedFile),

    onSuccess: (result) => {
      setReceipt(result);
      void queryClient.invalidateQueries({
        queryKey: ["target", "student-form", token ?? ""],
      });
    },

    onError: (error) => {
      if (
        error instanceof ApiError &&
        error.status === 409
      ) {
        void formQuery.refetch();
      }
    },
  });

  async function selectFile(
    selected: File | null,
  ) {
    setFile(selected);
    setFileError("");
    submission.reset();

    if (!selected) return;

    const problem = await validateJavaFile(selected);
    setFileError(problem ?? "");
  }

  if (!token) {
    return (
      <section role="alert" className="panel p-6">
        Отсутствует ссылка на задание.
      </section>
    );
  }

  if (formQuery.isPending) {
    return (
      <div role="status" className="py-10 text-center text-muted">
        Загружаем задание...
      </div>
    );
  }

  if (formQuery.isError) {
    return (
      <section role="alert" className="panel p-6">
        <h1 className="mb-3 text-xl font-semibold">
          Не удалось открыть задание
        </h1>

        <p className="mb-4 text-muted">
          {errorMessage(formQuery.error)}
        </p>

        <button
          type="button"
          className="btn btn-primary"
          onClick={() => void formQuery.refetch()}
        >
          Повторить
        </button>
      </section>
    );
  }

  const form = formQuery.data;
  const accepted = receipt ?? form.currentSubmission;

  if (receipt) {
    return (
      <section className="panel mx-auto max-w-2xl p-8">
        <div
          className="mb-3 text-3xl text-green-700"
          aria-hidden="true"
        >
          ✓
        </div>

        <h1 className="mb-4 text-2xl font-semibold">
          Работа принята
        </h1>

        <p className="mb-2">
          {form.title}
        </p>

        <p className="mb-5 text-sm text-muted">
          Отправлено: {formatDate(receipt.submittedAt)}
        </p>

        {file && (
          <p className="mb-5 font-mono text-sm">
            {file.name}
          </p>
        )}

        <Link
          to={`/submit/${encodeURIComponent(token)}`}
          className="btn btn-primary"
          onClick={() => {
            setReceipt(null);
            setFile(null);
          }}
        >
          К странице задания
        </Link>
      </section>
    );
  }

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-5">
      <div>
        <h1 className="mb-2 text-2xl font-semibold">
          {form.title}
        </h1>

        <p className="text-sm text-muted">
          Преподаватель: {form.teacherFullName}
        </p>
      </div>

      {!form.canSubmit ? (
        <section className="panel p-6">
          <h2 className="mb-3 text-xl font-semibold">
            Работа уже принята
          </h2>

          <p className="mb-4 text-muted">
            {accepted
              ? `Работа принята ${formatDate(
                  accepted.submittedAt,
                )}.`
              : "Отправка работы сейчас недоступна."}
          </p>

          <p className="text-sm text-muted">
            Повторная сдача возможна после сброса
            попытки преподавателем.
          </p>

          <button
            type="button"
            className="btn mt-5"
            onClick={() => void formQuery.refetch()}
          >
            Проверить доступность повторной сдачи
          </button>
        </section>
      ) : (
        <section className="panel p-6">
          <h2 className="mb-3 text-xl font-semibold">
            Сдача работы
          </h2>

          <p className="mb-5 text-sm text-muted">
            Один файл .java, не более 1 МБ, кодировка UTF-8.
          </p>

          {accepted && (
            <p className="mb-5 rounded-md bg-sunk p-3 text-sm">
              Предыдущая работа принята{" "}
              {formatDate(accepted.submittedAt)}.
              Преподаватель разрешил повторную сдачу.
              Новая работа заменит её как текущую.
            </p>
          )}

          <label className="mb-4 flex flex-col gap-2">
            <span className="font-medium">
              Java-файл
            </span>

            <input
              type="file"
              accept=".java"
              className="field w-full"
              disabled={submission.isPending}
              onChange={(event) => {
                void selectFile(
                  event.target.files?.[0] ?? null,
                );
              }}
            />
          </label>

          {file && (
            <p className="mb-3 font-mono text-sm">
              {file.name} · {file.size} байт
            </p>
          )}

          {fileError && (
            <p role="alert" className="mb-4 text-red-700">
              {fileError}
            </p>
          )}

          {submission.isError && (
            <p role="alert" className="mb-4 text-red-700">
              {errorMessage(submission.error)}
            </p>
          )}

          <button
            type="button"
            className="btn btn-primary"
            disabled={
              !file ||
              !!fileError ||
              submission.isPending
            }
            onClick={() => {
              if (file) submission.mutate(file);
            }}
          >
            {submission.isPending
              ? "Отправляем..."
              : "Отправить работу"}
          </button>

          <p className="mt-4 text-sm text-muted">
            Работу можно отправить один раз.
            Повторную попытку открывает преподаватель.
          </p>
        </section>
      )}
    </div>
  );
}
