import { describe, expect, it } from "vitest";
import { COURSE_TEMPLATE, DEMO_CORPUS, GROUND_TRUTH } from "./corpus";
import { analyze, clusters, normalizeToken, prepare, tokenize } from "./engine";

const prep = (id: string) => prepare(DEMO_CORPUS.find((s) => s.id === id)!.code, COURSE_TEMPLATE);
const pair = (a: string, b: string) => analyze(a, prep(a), b, prep(b));
const isTrue = (a: string, b: string) => GROUND_TRUTH.some(([x, y]) => (x === a && y === b) || (x === b && y === a));

function rankAll() {
  const res: { a: string; b: string; risk: number }[] = [];
  for (let i = 0; i < DEMO_CORPUS.length; i++)
    for (let j = i + 1; j < DEMO_CORPUS.length; j++) {
      const a = DEMO_CORPUS[i]!.id;
      const b = DEMO_CORPUS[j]!.id;
      res.push({ a, b, risk: pair(a, b).risk });
    }
  return res.sort((x, y) => y.risk - x.risk);
}

describe("нормализация Java-кода", () => {
  it("заменяет имена на V, но сохраняет ключевые слова и стандартную библиотеку", () => {
    expect(tokenize("for (int nb : graph.get(node)) {").map(normalizeToken).join(" ")).toBe("for ( int V : V . get ( V ) ) {");
  });

  it("исключает импорты, шаблон курса, комментарии и Javadoc", () => {
    const p = prep("S-019");
    const kinds = (k: string) => p.lines.filter((l) => l.kind === k).map((l) => l.text.trim());
    expect(kinds("template")).toContain("import java.util.*;");
    expect(kinds("template")).toContain("public static void main(String[] args) {");
    expect(kinds("comment")).toContain("// список смежности");
    expect(kinds("comment")).toContain("* Обход в ширину.");
    expect(p.sig.some((l) => l.raw.includes("ребро"))).toBe(false); // хвостовой // комментарий отрезан
  });

  it("исключает только сам шаблон, а похожие строки студента оставляет", () => {
    const p = prep("S-019");
    expect(p.sig.some((l) => l.raw.join("") === "intp=in.nextInt();")).toBe(true);
  });
});

describe("устойчивость к переименованию и перефразированию", () => {
  it("структурный признак видит копию с переименованием, а baseline — нет", () => {
    const r = pair("S-014", "S-027");
    expect(r.signals.baseline).toBeLessThan(0.3);
    expect(r.signals.structure).toBeGreaterThan(0.9);
    expect(r.renames).toContainEqual(["visited", "seen"]);
  });

  it("комментарии и Javadoc не мешают найти копию", () => {
    expect(pair("S-008", "S-019").risk).toBeGreaterThanOrEqual(70);
  });

  it("находит заимствованный метод при перестановке методов", () => {
    expect(pair("S-014", "S-033").fragments.some((f) => f.length >= 5)).toBe(true);
  });
});

describe("ранжирование на размеченном корпусе", () => {
  it("precision@4 и recall@4 равны 1", () => {
    const top = rankAll().slice(0, 4);
    const tp = top.filter((p) => isTrue(p.a, p.b)).length;
    expect(tp / 4).toBe(1);
    expect(tp / GROUND_TRUTH.length).toBe(1);
  });

  it("false accusation rate = 0: независимые работы ниже порога 45", () => {
    expect(rankAll().filter((p) => p.risk >= 45 && !isTrue(p.a, p.b))).toEqual([]);
  });

  it("собирает кластеры по парам выше порога", () => {
    const c = clusters(DEMO_CORPUS.map((s) => s.id), rankAll(), 45).map((g) => [...g].sort());
    expect(c).toContainEqual(["S-008", "S-019"]);
    expect(c).toContainEqual(["S-014", "S-027", "S-033"]);
  });
});

describe("latency пакетной проверки", () => {
  it("40 работ (780 пар) обрабатываются быстрее 2 секунд", () => {
    const many = Array.from({ length: 40 }, (_, i) => prepare(DEMO_CORPUS[i % DEMO_CORPUS.length]!.code, COURSE_TEMPLATE));
    const t0 = performance.now();
    for (let i = 0; i < many.length; i++) for (let j = i + 1; j < many.length; j++) analyze(`${i}`, many[i]!, `${j}`, many[j]!);
    expect(performance.now() - t0).toBeLessThan(2000);
  });
});