import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useState } from "react";
import { createBrowserRouter, createMemoryRouter, Navigate, RouterProvider, type RouteObject } from "react-router-dom";
import { Layout } from "./components/Layout";
import { BatchPage } from "./features/upload/BatchPage";
import { UploadPage } from "./features/upload/UploadPage";
import { QueuePage } from "./features/queue/QueuePage";
import { CompareIndex, ComparePage } from "./features/compare/ComparePage";

const makeQueryClient = () =>
  new QueryClient({ defaultOptions: { queries: { retry: 1, staleTime: 10_000, refetchOnWindowFocus: false } } });

export const routes: RouteObject[] = [
  {
    element: <Layout />,
    children: [
      { index: true, element: <Navigate replace to="/upload" /> },
      { path: "upload", element: <UploadPage /> },
      { path: "batches/:batchId", element: <BatchPage /> },
      { path: "batches/:batchId/queue", element: <QueuePage /> },
      { path: "batches/:batchId/pairs", element: <CompareIndex /> },
      { path: "batches/:batchId/pairs/:pairId", element: <ComparePage /> },
      { path: "*", element: <div className="panel p-6">Страница не найдена.</div> },
    ],
  },
];

export function App({ initialEntries }: { initialEntries?: string[] }) {
  const [queryClient] = useState(makeQueryClient);
  const [router] = useState(() =>
    initialEntries ? createMemoryRouter(routes, { initialEntries }) : createBrowserRouter(routes),
  );
  return (
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  );
}