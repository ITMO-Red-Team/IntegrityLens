import { useTheme } from "../lib/theme";

/** Красивый переключатель темы: слайдер с солнцем и луной. */
export function ThemeToggle() {
  const [theme, setTheme] = useTheme();
  const dark = theme === "dark";
  const toggle = () => setTheme(dark ? "light" : "dark");

  return (
    <button
      type="button"
      role="switch"
      aria-checked={dark}
      aria-label="Переключить тему"
      title={dark ? "Светлая тема" : "Тёмная тема"}
      onClick={toggle}
      className="theme-toggle"
    >
      <span className="theme-toggle-track" aria-hidden="true">
        <span className={`theme-toggle-thumb ${dark ? "theme-toggle-thumb-dark" : ""}`}>
          {/* Солнце / луна */}
          {dark ? (
            <svg viewBox="0 0 24 24" className="theme-toggle-icon" fill="currentColor">
              <path d="M21 12.8A9 9 0 1 1 11.2 3 7 7 0 0 0 21 12.8z" />
            </svg>
          ) : (
            <svg viewBox="0 0 24 24" className="theme-toggle-icon" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
              <circle cx="12" cy="12" r="4.5" />
              <path d="M12 2v2.5M12 19.5V22M2 12h2.5M19.5 12H22M4.9 4.9l1.8 1.8M17.3 17.3l1.8 1.8M19.1 4.9l-1.8 1.8M6.7 17.3l-1.8 1.8" />
            </svg>
          )}
        </span>
      </span>
    </button>
  );
}