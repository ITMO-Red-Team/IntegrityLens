import hljs from "highlight.js";
import java from "highlight.js/lib/languages/java";

// Регистрируем грамматику языка, который поддерживает API. Сейчас это java;
// когда метод начнёт принимать другие языки, добавьте грамматику и псевдоним
// в LANGUAGE_ALIASES. Фронт не знает грамматику неизвестного языка — тогда
// код рендерится как обычный текст.
hljs.registerLanguage("java", java);

/** Соответствие API-значений языков грамматикам highlight.js. */
const LANGUAGE_ALIASES: Record<string, string> = {
  java: "java",
};

export function isLanguageSupported(language: string | undefined): boolean {
  if (!language) return false;
  const alias = LANGUAGE_ALIASES[language.toLowerCase()];
  return !!alias && hljs.getLanguage(alias) !== undefined;
}

/**
 * Подсветка строки кода в один HTML-фрагмент без <pre>/<code>-обёртки.
 * Возвращает `null`, если язык неизвестен или подсветка не удалась, —
 * тогда вызывающий рендерит строку как обычный текст.
 */
export function highlightLine(code: string, language: string | undefined): string | null {
  const alias = language ? LANGUAGE_ALIASES[language.toLowerCase()] : undefined;
  if (!alias || hljs.getLanguage(alias) === undefined) return null;
  try {
    // highlight.js экранирует HTML внутри кода, что важно, т.к. исходник
    // приходит из пользовательского ZIP и не должен вставляться как разметка.
    return hljs.highlight(code, { language: alias, ignoreIllegals: true }).value;
  } catch {
    return null;
  }
}