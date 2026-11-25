
import {
  render,
  screen,
  waitFor,
  within,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {
  QueryClient,
  QueryClientProvider,
} from "@tanstack/react-query";
import {
  MemoryRouter,
  Route,
  Routes,
} from "react-router-dom";
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";

import { SubmissionPairPage } from "./SubmissionPairPage";

import {
  MOCK_IDS,
  mockSubmissions,
} from "../../mocks/target-fixtures";

const submissions =
  mockSubmissions[MOCK_IDS.success]?.items ?? [];

const first = submissions[0]?.submissionId;
const second = submissions[1]?.submissionId;

if (!first || !second) {
  throw new Error("Нужны две тестовые работы");
}

const scrollSpy = vi.fn();

let originalScrollIntoView:
  PropertyDescriptor | undefined;

function renderPage() {
  const client = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter
        initialEntries={[
          `/assignments/${MOCK_IDS.success}/pairs/${first}/${second}`,
        ]}
      >
        <Routes>
          <Route
            path="/assignments/:assignmentId/pairs/:a/:b"
            element={<SubmissionPairPage />}
          />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

async function findPane(label: string) {
  return screen.findByRole("region", {
    name: `Код работы ${label}`,
  });
}

async function waitForCode() {
  const paneA = await findPane("Работа A");
  const paneB = await findPane("Работа B");

  await within(paneA).findByText(
    /public class BankTransfer/,
  );

  await within(paneB).findByText(
    /public class MoneyService/,
  );

  return {
    paneA,
    paneB,
  };
}

describe("Синхронная прокрутка сравнения", () => {
  beforeEach(() => {
    scrollSpy.mockClear();

    originalScrollIntoView =
      Object.getOwnPropertyDescriptor(
        HTMLElement.prototype,
        "scrollIntoView",
      );

    Object.defineProperty(
      HTMLElement.prototype,
      "scrollIntoView",
      {
        configurable: true,
        writable: true,
        value: scrollSpy,
      },
    );
  });

  afterEach(() => {
    if (originalScrollIntoView) {
      Object.defineProperty(
        HTMLElement.prototype,
        "scrollIntoView",
        originalScrollIntoView,
      );
    } else {
      Reflect.deleteProperty(
        HTMLElement.prototype,
        "scrollIntoView",
      );
    }

    vi.clearAllMocks();
  });

  it("при выборе строки A выделяет и прокручивает связанную строку B", async () => {
    const user = userEvent.setup();

    renderPage();

    const {
      paneA,
      paneB,
    } = await waitForCode();

    const matchedLineA =
      within(paneA).getByRole("button", {
        name:
          "Работа A, строка 5, показать совпадение",
      });

    await user.click(matchedLineA);

    await waitFor(() => {
      expect(
        paneB.querySelector(
          '[data-line="8"][data-selected="true"]',
        ),
      ).not.toBeNull();

      expect(scrollSpy).toHaveBeenCalled();
    });

    expect(
      screen.getByText(
        /Связанная область работы B/,
      ),
    ).toBeInTheDocument();
  });

  it("при выборе строки B выделяет и прокручивает связанную строку A", async () => {
    const user = userEvent.setup();

    renderPage();

    const {
      paneA,
      paneB,
    } = await waitForCode();

    const matchedLineB =
      within(paneB).getByRole("button", {
        name:
          "Работа B, строка 8, показать совпадение",
      });

    await user.click(matchedLineB);

    await waitFor(() => {
      expect(
        paneA.querySelector(
          '[data-line="5"][data-selected="true"]',
        ),
      ).not.toBeNull();

      expect(scrollSpy).toHaveBeenCalled();
    });

    expect(
      screen.getByText(
        /Связанная область работы A/,
      ),
    ).toBeInTheDocument();
  });

  it("кнопка выбора фрагмента выделяет обе панели", async () => {
    const user = userEvent.setup();

    renderPage();

    const {
      paneA,
      paneB,
    } = await waitForCode();

    const group = screen.getByRole("group", {
      name: "Совпадающие фрагменты",
    });

    const button =
      within(group).getAllByRole("button")[0];

    if (!button) {
      throw new Error(
        "Не найден совпадающий фрагмент",
      );
    }

    await user.click(button);

    await waitFor(() => {
      expect(
        paneA.querySelectorAll(
          '[data-selected="true"]',
        ).length,
      ).toBeGreaterThan(0);

      expect(
        paneB.querySelectorAll(
          '[data-selected="true"]',
        ).length,
      ).toBeGreaterThan(0);

      expect(scrollSpy).toHaveBeenCalled();
    });

    expect(button).toHaveAttribute(
      "aria-pressed",
      "true",
    );
  });
});
