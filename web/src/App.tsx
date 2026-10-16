import { createBrowserRouter, Navigate, RouterProvider } from "react-router-dom";
import { Layout } from "./components/Layout";
import { UploadPage } from "./features/upload/UploadPage";
import { QueuePage } from "./features/queue/QueuePage";
import { ComparePage } from "./features/compare/ComparePage";

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
  return <RouterProvider router={router} />;
}