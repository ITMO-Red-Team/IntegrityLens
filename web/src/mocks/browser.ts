
import { setupWorker } from "msw/browser";

import { authHandlers } from "./auth-handlers";
import { targetHandlers } from "./target-handlers";
import { handlers } from "./handlers";

export const worker = setupWorker(
  ...authHandlers,
  ...targetHandlers,
  ...handlers,
);
