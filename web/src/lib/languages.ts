import hljs from "highlight.js";
import java from "highlight.js/lib/languages/java";

// Единая точка описания языков, которые понимает фронт. Каждая запись содержит
// всё, что нужно фиче: значение для API, подпись в UI, допустимые расширения
// файлов и грамматику подсветки. Чтобы добавить язык, достаточно дописать одну
// запись сюда — загрузка, валидация расширений и подсветка подхватят её сами.
hljs.registerLanguage("java", java);

export const LANGUAGES = [
  {
    value: "java",
    label: "Java",
    extensions: [".java"],
    /** Имя грамматики highlight.js (зарегистрирована выше) или null, если подсветки нет. */
    highlight: "java",
  },
] as const;

export type Language = (typeof LANGUAGES)[number]["value"];

export function isSupportedLanguage(value: string | undefined): value is Language {
  return !!value && LANGUAGES.some((l) => l.value === value);
}

export function languageLabel(value: string): string {
  return LANGUAGES.find((l) => l.value === value)?.label ?? value;
}

export function languageExtensions(value: Language): readonly string[] {
  return LANGUAGES.find((l) => l.value === value)?.extensions ?? [];
}

/** Грамматика highlight.js для языка, или null, если подсветка не подключена. */
export function highlightAlias(value: string | undefined): string | null {
  if (!value) return null;
  return LANGUAGES.find((l) => l.value === value)?.highlight ?? null;
}