
import { setupWorker } from "msw/browser";

import { authHandlers } from "./auth-handlers";

import {
  assignmentWriteHandlers,
} from "./assignment-write-handlers";

import {
  studentHandlers,
} from "./student-handlers";

import {
  studentWorkReadHandlers,
} from "./student-work-read-handlers";

import { targetHandlers } from "./target-handlers";
import { handlers } from "./handlers";

export const worker = setupWorker(
  ...authHandlers,
  ...assignmentWriteHandlers,
  ...studentHandlers,
  ...studentWorkReadHandlers,
  ...targetHandlers,
  ...handlers,
);
