
import {
  http,
  HttpResponse,
} from "msw";

import type {
  SubmissionForm,
} from "../api/target/students";

import {
  MAX_JAVA_BYTES,
} from "../api/target/students";

import {
  getMockAssignmentByToken,
} from "./assignment-write-handlers";

import {
  DEMO_STUDENT_ID,
  getMockWork,
  isMockWorkAttemptOpen,
  openMockWorkAttempt,
  resetMockWorkStore,
  saveMockWork,
} from "./student-submission-store";

function apiError(
  status: number,
  code: string,
  message: string,
) {
  return HttpResponse.json(
    { code, message },
    { status },
  );
}

export function resetMockStudentSubmissions() {
  resetMockWorkStore();
}

// Сохраняем вспомогательную функцию,
// используемую существующими тестами.
export function openMockStudentAttempt(
  token: string,
) {
  const assignment =
    getMockAssignmentByToken(
      token,
    );

  if (!assignment) {
    return;
  }

  const work = getMockWork(
    assignment.id,
    DEMO_STUDENT_ID,
  );

  if (!work) {
    return;
  }

  openMockWorkAttempt(
    assignment.id,
    DEMO_STUDENT_ID,
    work.receipt.submissionId,
  );
}

export const studentHandlers = [
  http.get(
    "*/api/submit/:token",
    ({ params }) => {
      const token = String(
        params.token,
      );

      const assignment =
        getMockAssignmentByToken(
          token,
        );

      if (!assignment) {
        return apiError(
          404,
          "NOT_FOUND",
          "Ссылка на задание не найдена.",
        );
      }

      const work =
        getMockWork(
          assignment.id,
          DEMO_STUDENT_ID,
        );

      const form: SubmissionForm = {
        title: assignment.title,
        teacherFullName:
          "Иванов Игорь Петрович",
        currentSubmission:
          work?.receipt ?? null,
        canSubmit:
          isMockWorkAttemptOpen(
            assignment.id,
            DEMO_STUDENT_ID,
          ),
      };

      return HttpResponse.json(
        form,
      );
    },
  ),

  http.post(
    "*/api/submit/:token",
    async ({
      params,
      request,
    }) => {
      const token = String(
        params.token,
      );

      const assignment =
        getMockAssignmentByToken(
          token,
        );

      if (!assignment) {
        return apiError(
          404,
          "NOT_FOUND",
          "Ссылка на задание не найдена.",
        );
      }

      if (
        !isMockWorkAttemptOpen(
          assignment.id,
          DEMO_STUDENT_ID,
        )
      ) {
        return apiError(
          409,
          "SUBMISSION_ALREADY_ACCEPTED",
          "Работа уже принята.",
        );
      }

      let data: FormData;

      try {
        data =
          await request.formData();
      } catch {
        return apiError(
          422,
          "FILE_REQUIRED",
          "Необходимо выбрать Java-файл.",
        );
      }

      const files =
        data.getAll("file");

      if (
        files.length === 0
      ) {
        return apiError(
          422,
          "FILE_REQUIRED",
          "Необходимо выбрать Java-файл.",
        );
      }

      if (
        files.length !== 1
      ) {
        return apiError(
          422,
          "MULTIPLE_FILES",
          "Разрешён только один файл.",
        );
      }

      const file =
        files[0];

      if (
        !file ||
        typeof file !== "object" ||
        !("name" in file) ||
        !("size" in file) ||
        !("arrayBuffer" in file) ||
        typeof file.name !==
          "string" ||
        typeof file.size !==
          "number" ||
        typeof file.arrayBuffer !==
          "function"
      ) {
        return apiError(
          422,
          "FILE_REQUIRED",
          "Необходимо выбрать Java-файл.",
        );
      }

      if (
        !file.name
          .toLowerCase()
          .endsWith(".java")
      ) {
        return apiError(
          422,
          "INVALID_EXTENSION",
          "Допустим только файл .java.",
        );
      }

      if (
        file.size === 0
      ) {
        return apiError(
          422,
          "EMPTY_FILE",
          "Нельзя отправить пустой файл.",
        );
      }

      if (
        file.size >
        MAX_JAVA_BYTES
      ) {
        return apiError(
          413,
          "FILE_TOO_LARGE",
          "Размер файла превышает 1 МБ.",
        );
      }

      let source: string;

      try {
        const bytes =
          new Uint8Array(
            await file.arrayBuffer(),
          );

        source =
          new TextDecoder(
            "utf-8",
            { fatal: true },
          ).decode(bytes);
      } catch {
        return apiError(
          422,
          "INVALID_ENCODING",
          "Файл должен быть в кодировке UTF-8.",
        );
      }

      const receipt =
        saveMockWork(
          assignment.id,
          source,
          file.name,
          DEMO_STUDENT_ID,
        );

      return HttpResponse.json(
        receipt,
        { status: 201 },
      );
    },
  ),
];
