import { describe, expect, it } from "vitest";
import { highlightLine, isLanguageSupported } from "./highlight";

describe("highlight", () => {
  it("распознаёт java и неизвестные языки", () => {
    expect(isLanguageSupported("java")).toBe(true);
    expect(isLanguageSupported("JAVA")).toBe(true);
    expect(isLanguageSupported("pascal")).toBe(false);
    expect(isLanguageSupported(undefined)).toBe(false);
  });

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