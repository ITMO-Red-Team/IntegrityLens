
import type {
  ApiErrorBody,
  User,
  Assignment,
  AssignmentSummary,
  CreateAssignmentRequest,
  SubmissionList,
  SubmissionDetail,
  PairResult,
} from "./types";

const BASE =
  (import.meta.env.VITE_API_BASE as string | undefined) || "";

export class ApiError extends Error {
  constructor(
    public status: number,
    public code: string,
    message: string,
    public details: Record<string, unknown> = {},
  ) {
    super(message);
    this.name = "ApiError";
  }
}

function apiUrl(path: string): string {
  return new URL(
    `${BASE}/api${path}`,
    window.location.origin,
  ).toString();
}

async function checkResponse(
  response: Response,
): Promise<Response> {
  if (response.ok) {
    return response;
  }

  let error = new ApiError(
    response.status,
    `HTTP_${response.status}`,
    `Сервер вернул ошибку ${response.status}`,
  );

  try {
    const body = (await response.json()) as ApiErrorBody;

    if (
      body &&
      typeof body.code === "string" &&
      typeof body.message === "string"
    ) {
      error = new ApiError(
        response.status,
        body.code,
        body.message,
        body.details ?? {},
      );
    }
  } catch {
    // Для HTTP-ошибки без JSON используем общий текст.
  }

  throw error;
}

export async function apiRequest<T>(
  path: string,
  options?: RequestInit,
): Promise<T> {
  const response = await fetch(apiUrl(path), {
    credentials: "same-origin",
    ...options,
  });

  await checkResponse(response);

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export async function apiTextRequest(
  path: string,
  options?: RequestInit,
): Promise<string> {
  const response = await fetch(apiUrl(path), {
    credentials: "same-origin",
    ...options,
  });

  await checkResponse(response);

  return response.text();
}

export async function apiVoidRequest(
  path: string,
  options?: RequestInit,
): Promise<void> {
  const response = await fetch(apiUrl(path), {
    credentials: "same-origin",
    ...options,
  });

  await checkResponse(response);
}

const enc = encodeURIComponent;

export const api = {
  getCurrentUser: () =>
    apiRequest<User>("/auth/me"),

  logout: () =>
    apiVoidRequest("/auth/logout", {
      method: "POST",
    }),

  listAssignments: () =>
    apiRequest<AssignmentSummary[]>("/assignments"),

  getAssignment: (assignmentId: string) =>
    apiRequest<Assignment>(
      `/assignments/${enc(assignmentId)}`,
    ),

  createAssignment: (input: CreateAssignmentRequest) =>
    apiRequest<Assignment>("/assignments", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(input),
    }),

  updateAssignmentSources: (
    assignmentId: string,
    sourceAssignmentIds: string[],
  ) =>
    apiRequest<Assignment>(
      `/assignments/${enc(assignmentId)}/sources`,
      {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          sourceAssignmentIds,
        }),
      },
    ),

  deleteAssignment: (assignmentId: string) =>
    apiVoidRequest(
      `/assignments/${enc(assignmentId)}`,
      { method: "DELETE" },
    ),

  resetStudentAttempt: (
    assignmentId: string,
    studentId: string,
    expectedSubmissionId: string,
  ) =>
    apiVoidRequest(
      `/assignments/${enc(assignmentId)}/students/${enc(studentId)}/reset`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          expectedSubmissionId,
        }),
      },
    ),

  listSubmissions: (assignmentId: string) =>
    apiRequest<SubmissionList>(
      `/assignments/${enc(assignmentId)}/submissions`,
    ),

  getSubmission: (
    assignmentId: string,
    submissionId: string,
  ) =>
    apiRequest<SubmissionDetail>(
      `/assignments/${enc(assignmentId)}/submissions/${enc(submissionId)}`,
    ),

  getPair: (
    assignmentId: string,
    a: string,
    b: string,
  ) =>
    apiRequest<PairResult>(
      `/assignments/${enc(assignmentId)}/pairs/${enc(a)}/${enc(b)}`,
    ),

  getSource: (submissionId: string) =>
    apiTextRequest(
      `/submissions/${enc(submissionId)}/source`,
    ),
};
