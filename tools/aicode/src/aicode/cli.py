"""Command-line interface for local AI-generated code span detection."""

from __future__ import annotations

import argparse
import json
import os
import sys
from dataclasses import asdict
from pathlib import Path
from typing import Sequence

from aicode.detector import (
    MODEL_ID,
    MODEL_REVISION,
    DetectorError,
    FileResult,
    analyze_text,
    collect_files,
    load_model,
)


def _parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog="aicode",
        description="Local CPU detection of AI-suspected code spans.",
        epilog=(
            "The default int2 Quanto mode reduces model-weight memory for small "
            "RAM servers. The original checkpoint is still a large download. "
            "This is a text detector, not a code-trained model, so results are "
            "heuristic."
        ),
    )
    parser.add_argument("paths", nargs="+", type=Path, help="source files or directories")
    parser.add_argument("--model", default=MODEL_ID, help="Hugging Face model ID or local path")
    parser.add_argument("--revision", default=MODEL_REVISION, help="model revision")
    parser.add_argument(
        "--threshold",
        type=float,
        default=0.8,
        help="minimum interval confidence from 0 to 1 (default: 0.8)",
    )
    parser.add_argument(
        "--dtype",
        choices=("bfloat16", "float32"),
        default="bfloat16",
        help="dtype for unquantized weights and activations (default: bfloat16)",
    )
    parser.add_argument(
        "--quantization",
        choices=("int2", "int4", "none"),
        default=os.environ.get("AICODE_QUANTIZATION", "int2"),
        help="Quanto post-training weight quantization; int2 targets 4 GB RAM (default)",
    )
    parser.add_argument(
        "--local-files-only",
        action="store_true",
        help="do not access Hugging Face; use an already cached/local model",
    )
    parser.add_argument("--json", action="store_true", help="write the full report as JSON")
    parser.add_argument("--output", type=Path, help="write the report to this file")
    return parser


def _read_source(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8-sig")
    except (OSError, UnicodeError) as exc:
        raise DetectorError(f"cannot read {path} as UTF-8: {exc}") from exc


def _report_dict(
    results: Sequence[FileResult], model_id: str, revision: str, quantization: str
) -> dict:
    return {
        "model": model_id,
        "revision": revision,
        "device": "cpu",
        "quantization": quantization,
        "files": [asdict(result) for result in results],
    }


def _render_text(
    results: Sequence[FileResult], model_id: str, revision: str, quantization: str
) -> str:
    lines = [
        f"Model: {model_id}@{revision}",
        "Device: CPU",
        f"Weight quantization: {quantization}",
        "Warning: text detector, not code-trained; results are heuristic.",
        "",
        f"{'FILE':<48} {'FLAGGED':>9} {'SPANS':>7}",
    ]
    for result in results:
        lines.append(
            f"{result.path:<48} {result.flagged_percent:>8.2f}% {len(result.spans):>7}"
        )
        for span in result.spans:
            lines.append(
                f"  lines {span.start_line}-{span.end_line}: "
                f"confidence {span.confidence:.3f}"
            )
    return "\n".join(lines) + "\n"


def main(argv: list[str] | None = None) -> int:
    parser = _parser()
    args = parser.parse_args(argv)
    if not 0 <= args.threshold <= 1:
        parser.error("--threshold must be between 0 and 1")

    try:
        files = collect_files(args.paths)
        if args.output and args.output.resolve() in files:
            raise DetectorError("report output must not overwrite an input source file")
        model, tokenizer, torch = load_model(
            model_id=args.model,
            revision=args.revision,
            dtype=args.dtype,
            quantization=args.quantization,
            local_files_only=args.local_files_only,
        )
        quantization = getattr(model, "aicode_quantization", args.quantization)
        results = [
            analyze_text(
                path=path,
                text=_read_source(path),
                model=model,
                tokenizer=tokenizer,
                torch=torch,
                threshold=args.threshold,
            )
            for path in files
        ]
    except DetectorError as exc:
        print(f"aicode: {exc}", file=sys.stderr)
        return 1

    if args.json:
        rendered = json.dumps(
            _report_dict(results, args.model, args.revision, quantization),
            ensure_ascii=False,
            indent=2,
        )
    else:
        rendered = _render_text(
            results, args.model, args.revision, quantization
        )
    if args.output:
        try:
            args.output.write_text(rendered, encoding="utf-8")
        except OSError as exc:
            print(f"aicode: cannot write {args.output}: {exc}", file=sys.stderr)
            return 1
    else:
        print(rendered, end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
