export type Labels = (id: string) => string;

export function makeLabels(submissions: { id: string }[]): Labels {
  const map = new Map(submissions.map((s, i) => [s.id, `S-${String(i + 1).padStart(3, "0")}`]));
  return (id) => map.get(id) ?? id.slice(0, 8);
}

export const pairLabel = (label: Labels, a: string, b: string) => [label(a), label(b)].sort().join(" ↔ ");