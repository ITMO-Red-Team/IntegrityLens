import type { Fragment } from "../api/types";

export type Side = "A" | "B";

/**
 * Подозрительный/схожий участок в одной панели кода.
 *
 * - `group` — цветовая группа: у совпавшей пары участок слева и справа делят
 *   один group, поэтому подсвечиваются одним цветом и реагируют на наведение
 *   вместе. У одиночного участка (например «списано в интернете») свой group.
 * - `fromLine..toLine` — диапазон строк (1-based, включительно).
 * - `fromCol..toCol` — опциональный диапазон символов внутри строки (1-based,
 *   включительно). Если не задан — выделяется вся строка.
 * - `reason` — причина подсветки (показывается в подсказке).
 */
export interface Region {
  id: string;
  group: string;
  side: Side;
  fromLine: number;
  toLine: number;
  fromCol?: number;
  toCol?: number;
  reason?: string;
}

// Цвета градиента (края). Берутся из переменных темы, чтобы работать и в
// тёмной, и в светлой схеме. Заливка задаётся через CSS-переменную --region-bg,
// которую переопределяют state hovered/focused, поэтому компонент ставит её
// в inline-стиле, а не через класс.
const GRADIENT_FROM = "var(--c-f1)"; // цвет А
const GRADIENT_TO = "var(--c-f4)"; // цвет Б

/**
 * Цвет участка — плавный градиент от цвета А к цвету Б по позиции группы
 * среди всех групп (0..count-1). Парные участки одной группы делят позицию,
 * поэтому получают одинаковый цвет. count=1 даёт ровно цвет А.
 * Возвращает CSS-значение фона для использования в inline-стиле.
 */
export function regionColor(groupIndex: number, groupCount: number): string {
  const t = groupCount <= 1 ? 0 : groupIndex / (groupCount - 1);
  const pct = Math.round(t * 100);
  return `color-mix(in srgb, ${GRADIENT_TO} ${pct}%, ${GRADIENT_FROM})`;
}

/**
 * Строит цвет каждой группы по объединённому набору участков обеих панелей.
 * Одиночный участок (только слева или только справа) занимает собственный
 * слот градиента; парные участки одной группы всегда получают одинаковый цвет,
 * т.к. позиция берётся от группы из общего набора, а не от набора конкретной
 * панели. Порядок групп — порядок появления в `regions`.
 */
export function groupColors(regions: Region[]): Map<string, string> {
  const order: string[] = [];
  for (const r of regions) if (!order.includes(r.group)) order.push(r.group);
  return new Map(order.map((g, i) => [g, regionColor(i, order.length)]));
}

/**
 * Строит участки из парных фрагментов API (каждый фрагмент — совпадение
 * диапазона строк A ↔ B). Сейчас бэкенд их не отдаёт, поэтому для реальных
 * данных `fragments` пуст и участков нет.
 */
export function pairedRegions(fragments: Fragment[]): Region[] {
  const regions: Region[] = [];
  fragments.forEach((f, idx) => {
    const group = `pair-${idx}`;
    regions.push({ id: `${group}-A`, group, side: "A", fromLine: f.a[0]!, toLine: f.a[1]! });
    regions.push({ id: `${group}-B`, group, side: "B", fromLine: f.b[0]!, toLine: f.b[1]! });
  });
  return regions;
}

/**
 * Итоговый набор участков для пары. Реальные подозрительные участки приходят
 * с бэкенда в `fragments` (см. core/mock.py): они парные (A ↔ B) и рендерятся
 * напрямую. Если фрагментов нет — добавляются демо-участки, чтобы
 * функциональность оставалась наглядной и в тестах: одиночный с причиной
 * «списано в интернете» и фрагмент от символа до символа. Таким образом, при
 * наличии реальных данных демо-участки не добавляются и не дублируются.
 */
export function buildRegions(fragments: Fragment[], _options: { demo?: boolean } = {}): Region[] {
  const regions = pairedRegions(fragments);
  if (fragments.length === 0) {
    regions.push({
      id: "demo-web-A",
      group: "demo-web",
      side: "A",
      fromLine: 1,
      toLine: 2,
      reason: "Совпадение с фрагментом из интернета",
    });
    regions.push({
      id: "demo-char-B",
      group: "demo-char",
      side: "B",
      fromLine: 4,
      toLine: 4,
      fromCol: 5,
      toCol: 28,
      reason: "Совпадение подстроки (от символа до символа)",
    });
  }
  return regions;
}