import type { Score } from "../../api/target/types";

export function ComparisonNotice({ score }: { score: Score }) {
  if (!score.comparisons?.skipped) return null;
  return (
    <p className="mt-1 text-xs text-amber-800" role="note">
      Пропущено сравнений: {score.comparisons.skipped}
    </p>
  );
}
