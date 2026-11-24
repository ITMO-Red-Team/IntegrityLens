
import { setupWorker } from "msw/browser";
import { handlers } from "./handlers";
import { targetHandlers } from "./target-handlers";

export const worker = setupWorker(
  ...targetHandlers,
  ...handlers,
);
