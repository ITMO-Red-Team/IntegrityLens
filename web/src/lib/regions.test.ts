import { describe, expect, it } from "vitest";
import { buildRegions, groupColors, pairedRegions, regionColor, type Region } from "./regions";
import type { Fragment } from "../api/types";

const frag: Fragment = { a: [2, 3], b: [5, 6] };

describe("pairedRegions", () => {
  it("создаёт пару участков с общей группой", () => {
    const regions = pairedRegions([frag]);
    expect(regions).toHaveLength(2);
    const [a, b] = regions;
    expect(a!.side).toBe("A");
    expect(a!.fromLine).toBe(2);
    expect(a!.toLine).toBe(3);
    expect(b!.side).toBe("B");
    expect(b!.fromLine).toBe(5);
    expect(b!.toLine).toBe(6);
    expect(a!.group).toBe(b!.group);
  });
});

describe("buildRegions", () => {
  it("без демо и без фрагментов не даёт участков", () => {
    expect(buildRegions([])).toEqual([]);
  });

  it("добавляет демо-участки только при demo и наличии фрагментов", () => {
    const regions = buildRegions([frag], { demo: true });
    const single = regions.find((r) => r.id === "demo-web-A");
    const charRange = regions.find((r) => r.id === "demo-char-B");
    expect(single?.reason).toBe("Совпадение с фрагментом из интернета");
    expect(charRange?.fromCol).toBe(5);
    expect(charRange?.toCol).toBe(28);
    // без demo одиночных демо-участков нет
    expect(buildRegions([frag]).some((r) => r.id === "demo-web-A")).toBe(false);
  });
});

describe("groupColors", () => {
  it("парный участок имеет одинаковый цвет в обеих панелях при одиночном в одной из них", () => {
    // pair-0 и pair-1 есть в обеих панелях; demo-web — только в A. По-панельный
    // подсчёт градиента сдвинул бы pair-1 (в A он был бы 50%, в B — 100%).
    // Единый набор групп по объединению даёт одинаковый цвет в обеих панелях.
    const regions = [
      { id: "pair-0-A", group: "pair-0", side: "A", fromLine: 1, toLine: 1 },
      { id: "pair-0-B", group: "pair-0", side: "B", fromLine: 1, toLine: 1 },
      { id: "pair-1-A", group: "pair-1", side: "A", fromLine: 2, toLine: 2 },
      { id: "pair-1-B", group: "pair-1", side: "B", fromLine: 2, toLine: 2 },
      { id: "demo-web-A", group: "demo-web", side: "A", fromLine: 3, toLine: 3, reason: "интернет" },
    ] as Region[];
    const colors = groupColors(regions); // единый градиент по объединению
    // Обе панели используют один и тот же map → pair-1 одного цвета в A и B.
    expect(colors.get("pair-1")).toBe(colors.get("pair-1"));
    // ...и этот цвет отличается от того, что дал бы по-панельный подсчёт в B
    // (в B без одиночного участка pair-1 был бы крайним цветом градиента).
    const onlyB = groupColors(regions.filter((r) => r.side === "B"));
    expect(colors.get("pair-1")).not.toBe(onlyB.get("pair-1"));
  });
});

describe("regionColor", () => {
  it("даёт ровно цвет А при одной группе", () => {
    expect(regionColor(0, 1)).toContain("0%");
  });

  it("интерполирует от А к Б по позиции", () => {
    expect(regionColor(0, 3)).toContain("0%");
    expect(regionColor(1, 3)).toContain("50%");
    expect(regionColor(2, 3)).toContain("100%");
  });

  it("не зацикливается при нуле групп и стабилен", () => {
    expect(regionColor(0, 0)).toBe(regionColor(0, 0));
  });
});