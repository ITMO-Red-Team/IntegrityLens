"""s40: значима ли разница AUC — парный бутстрэп и DeLong.

Сравнения на каждой версии:

| | A | B | гипотеза |
|---|---|---|---|
| 1 | `structsim:combined` | `jplag-default:avg` | H1 |
| 2–4 | `structsim:combined` | `jplag-normalize:avg`, `jplag-match-merging:avg`, `jplag-default:max` | H2 |
| 5 | `structsim:combined` | лучшая оценка JPlag на этой версии | H2, baseline подстроен под набор |
| 6 | `structsim:contrast_z` | `structsim:combined` | H3 |

Для каждого: DeLong (z, p), 95% ДИ разницы парным бутстрэпом по парам
(10 000 повторов) и **кластерным** бутстрэпом по задачам. Второй нужен потому,
что пары одной задачи не независимы: одно решение входит в несколько пар, а
задачи различаются по трудности. Бутстрэп по парам этого не видит и даёт
слишком узкий интервал; кластерный — честнее, но на 21 кластере грубее.
p-значения DeLong поправлены по Холму внутри версии.

Пишет `results/tables/stats.csv` и `results/tables/stats.md`.
"""

from __future__ import annotations

import pandas as pd

from research_two import config, paths, scores, stats


def comparisons(part: pd.DataFrame, columns: list[str]) -> list[tuple[str, str, str]]:
    jplag = [c for c in columns if c.startswith("jplag-")]
    y = part["label"].to_numpy()
    best = max(jplag, key=lambda c: stats.auc(y, part[c].to_numpy(dtype=float)))
    result = [
        ("H1", config.HEAD_STRUCTSIM, config.HEAD_JPLAG),
        ("H2", config.HEAD_STRUCTSIM, "jplag-normalize:avg"),
        ("H2", config.HEAD_STRUCTSIM, "jplag-match-merging:avg"),
        ("H2", config.HEAD_STRUCTSIM, "jplag-default:max"),
    ]
    if best not in {b for _, _, b in result}:
        result.append(("H2*", config.HEAD_STRUCTSIM, best))
    result.append(("H3", "structsim:contrast_z", config.HEAD_STRUCTSIM))
    return result


def run() -> None:
    table = scores.load()
    columns = scores.score_columns(table)
    rows = []
    for version in config.DATASET_VERSIONS:
        part, _ = scores.common(table, version)
        y = part["label"].to_numpy().astype(bool)
        groups = part["problem"].to_numpy()
        version_rows = []
        for i, (hyp, a, b) in enumerate(comparisons(part, columns)):
            sa, sb = part[a].to_numpy(dtype=float), part[b].to_numpy(dtype=float)
            d = stats.delong(y, sa, sb)
            seed = config.BOOTSTRAP_SEED + i
            boot = stats.bootstrap_diff(y, sa, sb, config.BOOTSTRAP_ITERATIONS, seed, config.CONFIDENCE_LEVEL)
            clus = stats.bootstrap_diff(y, sa, sb, config.BOOTSTRAP_ITERATIONS, seed,
                                        config.CONFIDENCE_LEVEL, groups=groups)
            version_rows.append({
                "version": version, "hypothesis": hyp, "a": a, "b": b, "n": len(y),
                "auc_a": d.auc_a, "auc_b": d.auc_b, "diff": d.diff,
                "delong_se": d.se, "delong_z": d.z, "delong_p": d.p,
                "boot_low": boot.low, "boot_high": boot.high, "boot_p_le_0": boot.p_le_zero,
                "cluster_low": clus.low, "cluster_high": clus.high, "cluster_p_le_0": clus.p_le_zero,
            })
            print(f"  {version} {hyp}: {a} − {b} = {d.diff:+.3f}, p = {d.p:.4f}, "
                  f"ДИ [{boot.low:+.3f}; {boot.high:+.3f}], по задачам [{clus.low:+.3f}; {clus.high:+.3f}]",
                  flush=True)
        for row, p in zip(version_rows, stats.holm([r["delong_p"] for r in version_rows])):
            row["delong_p_holm"] = p
        rows += version_rows

    result = pd.DataFrame(rows)
    result.to_csv(paths.TABLES_DIR / "stats.csv", index=False, float_format="%.6g")

    lines = []
    for version, group in result.groupby("version", sort=False):
        lines += [f"### {version}\n",
                  "| | A | B | AUC A | AUC B | A − B | 95% ДИ, по парам | 95% ДИ, по задачам | p DeLong | p Холм |",
                  "|---|---|---|---|---|---|---|---|---|---|"]
        for r in group.itertuples():
            lines.append(
                f"| {r.hypothesis} | `{r.a}` | `{r.b}` | {r.auc_a:.3f} | {r.auc_b:.3f} | {r.diff:+.3f} | "
                f"[{r.boot_low:+.3f}; {r.boot_high:+.3f}] | [{r.cluster_low:+.3f}; {r.cluster_high:+.3f}] | "
                f"{r.delong_p:.2g} | {r.delong_p_holm:.2g} |"
            )
        lines.append("")
    (paths.TABLES_DIR / "stats.md").write_text("\n".join(lines), encoding="utf-8")
