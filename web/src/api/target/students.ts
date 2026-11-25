
import type { components } from "./schema";
import {
  apiRequest,
  ApiError,
} from "./client";

export type SubmissionForm =
  components["schemas"]["SubmissionForm"];

export type SubmissionReceipt =
  components["schemas"]["SubmissionReceipt"];

export const MAX_JAVA_BYTES = 1_000_000;

const BASE =
  (import.meta.env.VITE_API_BASE as string | undefined) || "";

function submissionUrl(token: string): string {
  return new URL(
    `${BASE}/api/submit/${encodeURIComponent(token)}`,
    window.location.origin,
  ).toString();
}

export async function getSubmissionForm(
  token: string,
): Promise<SubmissionForm> {
  return apiRequest<SubmissionForm>(
    `/submit/${encodeURIComponent(token)}`,
  );
}

export async function validateJavaFile(
  file: File,
): Promise<string | null> {
  if (!file.name.toLowerCase().endsWith(".java")) {
    return "Допустим только файл с расширением .java.";
  }

  if (file.size === 0) {
    return "Файл пуст. Выберите непустой Java-файл.";
  }

  if (file.size > MAX_JAVA_BYTES) {
    return "Размер файла превышает 1 МБ.";
  }

  try {
    const bytes = new Uint8Array(await file.arrayBuffer());

    new TextDecoder("utf-8", {
      fatal: true,
    }).decode(bytes);
  } catch {
    return "Файл должен быть в кодировке UTF-8.";
  }

  return null;
}

export async function submitJavaFile(
  token: string,
  file: File,
): Promise<SubmissionReceipt> {
  const validationError = await validateJavaFile(file);

  if (validationError) {
    throw new ApiError(
      422,
      "FILE_VALIDATION_ERROR",
      validationError,
    );
  }

  const data = new FormData();
  data.append("file", file);

  const response = await fetch(
    submissionUrl(token),
    {
      method: "POST",
      credentials: "same-origin",
      body: data,
    },
  );

  if (!response.ok) {
    let code = `HTTP_${response.status}`;
    let message =
      `Сервер вернул ошибку ${response.status}`;

    try {
      const body = (await response.json()) as {
        code?: string;
        message?: string;
      };

      if (typeof body.code === "string") {
        code = body.code;
      }

      if (typeof body.message === "string") {
        message = body.message;
      }
    } catch {
    }

    throw new ApiError(
      response.status,
      code,
      message,
    );
  }

  return (await response.json()) as SubmissionReceipt;
}
