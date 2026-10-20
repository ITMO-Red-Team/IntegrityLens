import { useCallback, useEffect, useState } from "react";

export type Theme = "light" | "dark";

const STORAGE_KEY = "integritylens-theme";

const hasWindow = () => typeof window !== "undefined";
const hasStorage = () => {
  try {
    return hasWindow() && typeof window.localStorage !== "undefined";
  } catch {
    return false;
  }
};

/** Тема по системной настройке (в тестах/SSR — светлая). */
function systemTheme(): Theme {
  if (hasWindow() && window.matchMedia) {
    return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
  }
  return "light";
}

/**
 * Применяет тему к <html>. Атрибут data-theme всегда задаётся явно ("light"
 * или "dark") — это важно: если его снять для светлой темы, то при тёмной
 * системе сработает правило @media (prefers-color-scheme: dark) для
 * :root:not([data-theme]) и страница останется тёмной. Явный атрибут
 * переопределяет системную настройку и для светлой, и для тёмной.
 */
function applyTheme(theme: Theme) {
  if (!hasWindow()) return;
  document.documentElement.setAttribute("data-theme", theme);
}

export function useTheme(): [Theme, (t: Theme) => void] {
  const [theme, setTheme] = useState<Theme>(() => {
    if (hasStorage()) {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved === "light" || saved === "dark") return saved;
    }
    return systemTheme();
  });

  useEffect(() => {
    applyTheme(theme);
    if (hasStorage()) localStorage.setItem(STORAGE_KEY, theme);
  }, [theme]);

  return [theme, useCallback((t: Theme) => setTheme(t), [])];
}