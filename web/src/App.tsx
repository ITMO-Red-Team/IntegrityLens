
import {
  QueryClient,
  QueryClientProvider,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import { useState } from "react";
import {
  createBrowserRouter,
  createMemoryRouter,
  Navigate,
  Outlet,
  RouterProvider,
  useNavigate,
  type RouteObject,
} from "react-router-dom";

import { Layout } from "./components/Layout";
import { BatchPage } from "./features/upload/BatchPage";
import { UploadPage } from "./features/upload/UploadPage";
import { QueuePage } from "./features/queue/QueuePage";
import {
  CompareIndex,
  ComparePage,
} from "./features/compare/ComparePage";

import { AuthPage } from "./features/auth/AuthPage";
import { AssignmentsPage } from "./features/assignments/AssignmentsPage";
import { CreateAssignmentPage } from "./features/assignments/CreateAssignmentPage";
import { AssignmentCreatedPage } from "./features/assignments/AssignmentCreatedPage";
import { AssignmentPage } from "./features/assignments/AssignmentPage";

import { SubmissionPage } from "./features/submissions/SubmissionPage";
import { SubmissionPairPage } from "./features/submissions/SubmissionPairPage";
import { StudentSubmissionPage } from "./features/student/StudentSubmissionPage";

import { authApi } from "./api/target/auth";
import { ApiError } from "./api/target/client";

const AUTH_QUERY_KEY = [
  "target",
  "auth",
  "me",
] as const;

const makeQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: {
        retry: 1,
        staleTime: 10_000,
        refetchOnWindowFocus: false,
      },
    },
  });

function ProtectedLayout({
  role,
}: {
  role: "teacher" | "student";
}) {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const {
    data: user,
    isPending,
    error,
  } = useQuery({
    queryKey: AUTH_QUERY_KEY,
    queryFn: authApi.me,
    retry: (count, queryError) =>
      !(
        queryError instanceof ApiError &&
        queryError.status === 401
      ) && count < 1,
  });

  async function logout() {
    try {
      await authApi.logout();

      queryClient.removeQueries({
        queryKey: ["target"],
      });

      navigate("/login", {
        replace: true,
      });
    } catch {
      window.alert(
        "Не удалось выйти. Попробуйте ещё раз.",
      );
    }
  }

  if (isPending) {
    return (
      <div className="p-8 text-center text-muted">
        Проверяем сессию...
      </div>
    );
  }

  if (
    error instanceof ApiError &&
    error.status === 401
  ) {
    return (
      <Navigate
        to="/login"
        replace
      />
    );
  }

  if (error) {
    return (
      <div
        role="alert"
        className="p-8 text-center"
      >
        Не удалось проверить сессию.
        Обновите страницу.
      </div>
    );
  }

  if (!user) {
    return (
      <Navigate
        to="/login"
        replace
      />
    );
  }

  if (user.role !== role) {
    return (
      <Navigate
        to={
          user.role === "teacher"
            ? "/assignments"
            : "/student"
        }
        replace
      />
    );
  }

  return (
    <div className="min-h-screen">
      <header className="border-b border-line bg-surface">
        <div className="mx-auto flex max-w-[1200px] items-center justify-between gap-4 px-4 py-4">
          <strong className="font-display text-xl">
            IntegrityLens
          </strong>

          <div className="flex items-center gap-4">
            <span className="hidden text-sm text-muted sm:inline">
              {user.fullName} ·{" "}
              {user.role === "teacher"
                ? "преподаватель"
                : "студент"}
            </span>

            <button
              type="button"
              className="btn"
              onClick={() =>
                void logout()
              }
            >
              Выйти
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-[1200px] px-4 py-8">
        <Outlet />
      </main>
    </div>
  );
}

function Placeholder({
  title,
}: {
  title: string;
}) {
  return (
    <section className="panel p-8">
      <h1 className="mb-3 text-2xl">
        {title}
      </h1>

      <p className="text-muted">
        Следующим шагом реализуем
        эту страницу по макету Miro.
      </p>
    </section>
  );
}

export const routes: RouteObject[] = [
  {
    path: "/login",
    element: (
      <AuthPage
        key="login"
        mode="login"
      />
    ),
  },
  {
    path: "/register",
    element: (
      <AuthPage
        key="register"
        mode="register"
      />
    ),
  },
  {
    element: (
      <ProtectedLayout role="teacher" />
    ),
    children: [
      {
        path: "/assignments",
        element: <AssignmentsPage />,
      },
      {
        path: "/assignments/new",
        element: <CreateAssignmentPage />,
      },
      {
        path: "/assignments/:assignmentId/created",
        element: <AssignmentCreatedPage />,
      },
      {
        path: "/assignments/:assignmentId",
        element: <AssignmentPage />,
      },
      {
        path: "/assignments/:assignmentId/submissions/:submissionId",
        element: <SubmissionPage />,
      },
      {
        path: "/assignments/:assignmentId/pairs/:a/:b",
        element: <SubmissionPairPage />,
      },
    ],
  },
  {
    element: (
      <ProtectedLayout role="student" />
    ),
    children: [
      {
        path: "/student",
        element: (
          <Placeholder title="Моя работа" />
        ),
      },
      {
        path: "/submit/:token",
        element: <StudentSubmissionPage />,
      },
    ],
  },
  {
    element: <Layout />,
    children: [
      {
        index: true,
        element: (
          <Navigate
            replace
            to="/login"
          />
        ),
      },
      {
        path: "upload",
        element: <UploadPage />,
      },
      {
        path: "batches/:batchId",
        element: <BatchPage />,
      },
      {
        path: "batches/:batchId/queue",
        element: <QueuePage />,
      },
      {
        path: "batches/:batchId/pairs",
        element: <CompareIndex />,
      },
      {
        path: "batches/:batchId/pairs/:pairId",
        element: <ComparePage />,
      },
      {
        path: "*",
        element: (
          <div className="panel p-6">
            Страница не найдена.
          </div>
        ),
      },
    ],
  },
];

export function App({
  initialEntries,
}: {
  initialEntries?: string[];
}) {
  const [queryClient] = useState(
    makeQueryClient,
  );

  const [router] = useState(() =>
    initialEntries
      ? createMemoryRouter(routes, {
          initialEntries,
        })
      : createBrowserRouter(routes),
  );

  return (
    <QueryClientProvider
      client={queryClient}
    >
      <RouterProvider router={router} />
    </QueryClientProvider>
  );
}
