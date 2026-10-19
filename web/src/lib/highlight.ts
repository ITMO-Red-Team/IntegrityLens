import hljs from "highlight.js";
import { highlightAlias } from "./languages";

// Все грамматики highlight.js регистрируются в lib/languages.ts (реестр языков).
// Здесь только функция подсветки одной строки.

/**
 * Подсветка строки кода в один HTML-фрагмент без <pre>/<code>-обёртки.
 * Возвращает `null`, если язык неизвестен или подсветка не удалась, —
 * тогда вызывающий рендерит строку как обычный текст.
 */
export function highlightLine(code: string, language: string | undefined): string | null {
  const alias = highlightAlias(language);
  if (!alias) return null;
  try {
    // highlight.js экранирует HTML внутри кода, что важно, т.к. исходник
    // приходит из пользовательского ZIP и не должен вставляться как разметка.
    return hljs.highlight(code, { language: alias, ignoreIllegals: true }).value;
  } catch {
    return null;
  }
}