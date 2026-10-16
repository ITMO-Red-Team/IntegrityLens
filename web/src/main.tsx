import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./styles.css";

function Hello() {
  return (
    <main className="p-8">
      <h1 className="text-2xl font-semibold">IntegrityLens</h1>
      <p className="text-gray-500">Фронтенд запущен.</p>
    </main>
  );
}

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <Hello />
  </StrictMode>,
);