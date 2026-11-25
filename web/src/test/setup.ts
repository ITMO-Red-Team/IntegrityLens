
import "@testing-library/jest-dom/vitest";

import {
  Blob as NodeBlob,
  File as NodeFile,
} from "node:buffer";

import { cleanup } from "@testing-library/react";
import { setupServer } from "msw/node";
import {
  afterAll,
  afterEach,
  beforeAll,
} from "vitest";

import { resetDb } from "../mocks/db";
import { handlers } from "../mocks/handlers";

import {
  authHandlers,
  resetMockAuth,
} from "../mocks/auth-handlers";

import {
  assignmentWriteHandlers,
  resetMockAssignments,
} from "../mocks/assignment-write-handlers";

import {
  studentHandlers,
  resetMockStudentSubmissions,
} from "../mocks/student-handlers";

import {
  crossAssignmentHandlers,
} from "../mocks/cross-assignment-handlers";

import {
  studentWorkReadHandlers,
} from "../mocks/student-work-read-handlers";

import { targetHandlers } from "../mocks/target-handlers";

const NativeFormData = (
  await new Response("", {
    headers: {
      "content-type":
        "application/x-www-form-urlencoded",
    },
  }).formData()
).constructor as typeof FormData;

Object.assign(globalThis, {
  FormData: NativeFormData,
  File: NodeFile,
  Blob: NodeBlob,
});

const NativeRequest = globalThis.Request;

globalThis.Request = class extends NativeRequest {
  constructor(
    input: RequestInfo | URL,
    init?: RequestInit,
  ) {
    const {
      signal: _signal,
      ...rest
    } = init ?? {};

    super(input, rest);
  }
} as typeof Request;

export const server = setupServer(
  ...authHandlers,
  ...assignmentWriteHandlers,
  ...studentHandlers,
  ...crossAssignmentHandlers,
  ...studentWorkReadHandlers,
  ...targetHandlers,
  ...handlers,
);

beforeAll(() => {
  server.listen({
    onUnhandledRequest: "error",
  });
});

afterEach(() => {
  cleanup();
  server.resetHandlers();
  resetMockAuth();
  resetMockAssignments();
  resetMockStudentSubmissions();
  resetDb();
});

afterAll(() => {
  server.close();
});
