import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { createBrowserRouter, Navigate, RouterProvider } from "react-router-dom";
import { Layout } from "./components/Layout";
import { UploadPage } from "./features/upload/UploadPage";
import { QueuePage } from "./features/queue/QueuePage";
import { ComparePage } from "./features/compare/ComparePage";

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, staleTime: 10_000, refetchOnWindowFocus: false } },
});

const router = createBrowserRouter([
  {
    element: <Layout />,
    children: [
      { index: true, element: <Navigate replace to="/upload" /> },
      { path: "upload", element: <UploadPage /> },
      { path: "batches/:batchId/queue", element: <QueuePage /> },
      { path: "batches/:batchId/pairs", element: <ComparePage /> },
      { path: "batches/:batchId/pairs/:pairId", element: <ComparePage /> },
      { path: "*", element: <div className="panel p-6">Страница не найдена.</div> },
    ],
  },
]);

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  );
}