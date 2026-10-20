import { describe, expect, it } from "vitest";
import { highlightLine } from "./highlight";
import { highlightAlias, isSupportedLanguage, languageExtensions, languageLabel } from "./languages";

describe("languages registry", () => {
  it("распознаёт java и неизвестные языки", () => {
    expect(isSupportedLanguage("java")).toBe(true);
    expect(isSupportedLanguage("pascal")).toBe(false);
    expect(isSupportedLanguage(undefined)).toBe(false);
  });

  it("подсвечивает java, а неизвестные языки нет", () => {
    expect(highlightAlias("java")).toBe("java");
    expect(highlightAlias("pascal")).toBeNull();
    expect(highlightAlias(undefined)).toBeNull();
  });

  it("возвращает подпись и расширения", () => {
    expect(languageLabel("java")).toBe("Java");
    expect(languageLabel("pascal")).toBe("pascal");
    expect(languageExtensions("java")).toEqual([".java"]);
  });
});

describe("highlight", () => {
  it("подсвечивает ключевое слово в HTML", () => {
    const html = highlightLine("public static void main(String[] args) {}", "java");
    expect(html).toBeTruthy();
    expect(html).toMatch(/<span class="hljs-keyword">public<\/span>/);
  });

  it("экранирует HTML-спецсимволы из исходника", () => {
    const html = highlightLine('System.out.println("<script>")', "java");
    expect(html).not.toContain("<script>");
    expect(html).toContain("&lt;");
  });

  it("не подсвечивает неизвестный язык", () => {
    expect(highlightLine("println('x')", "pascal")).toBeNull();
    expect(highlightLine("println('x')", undefined)).toBeNull();
  });
});