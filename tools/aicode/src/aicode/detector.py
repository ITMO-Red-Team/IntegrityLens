"""CPU inference and source-code span mapping for GigaCheck."""

from __future__ import annotations

import json
import os
from bisect import bisect_right
from dataclasses import dataclass
from functools import wraps
from math import ceil
from pathlib import Path
from typing import Any, Iterable


MODEL_ID = "iitolstykh/GigaCheck-Detector-Multi"
MODEL_REVISION = "9fabe1503f99cc8a6f639996f9475c3ec19bc067"
MAX_TOKENS = 900
SUPPORTED_EXTENSIONS = frozenset(
    {
        ".java",
    }
)
IGNORED_DIRECTORIES = frozenset(
    {".git", ".venv", "__pycache__", "build", "dist", "node_modules", "vendor"}
)


class DetectorError(RuntimeError):
    """Input or model output cannot be handled by this detector."""


@dataclass(frozen=True)
class Span:
    start_char: int
    end_char: int
    start_line: int
    end_line: int
    confidence: float


@dataclass(frozen=True)
class FileResult:
    path: str
    characters: int
    flagged_characters: int
    flagged_percent: float
    spans: tuple[Span, ...]


def collect_files(paths: Iterable[Path]) -> list[Path]:
    """Resolve source paths, recursively expanding directories."""
    files: set[Path] = set()
    errors: list[str] = []

    for path in paths:
        path = path.expanduser()
        if not path.exists():
            errors.append(f"path does not exist: {path}")
            continue
        if path.is_file():
            if path.suffix.lower() not in SUPPORTED_EXTENSIONS:
                errors.append(f"unsupported source extension: {path}")
            else:
                files.add(path.resolve())
            continue
        if not path.is_dir():
            errors.append(f"not a file or directory: {path}")
            continue
        walk_errors: list[OSError] = []
        for root, directories, filenames in os.walk(path, onerror=walk_errors.append):
            directories[:] = [
                directory
                for directory in directories
                if directory not in IGNORED_DIRECTORIES
            ]
            for filename in filenames:
                candidate = Path(root) / filename
                if candidate.suffix.lower() in SUPPORTED_EXTENSIONS:
                    files.add(candidate.resolve())
        if walk_errors:
            errors.extend(f"cannot scan directory: {error}" for error in walk_errors)

    if errors:
        raise DetectorError("; ".join(errors))
    if not files:
        raise DetectorError("no supported source files found")
    return sorted(files, key=lambda item: str(item).casefold())


def _token_offsets(tokenizer: Any, text: str) -> list[tuple[int, int]]:
    try:
        encoded = tokenizer(
            text,
            add_special_tokens=False,
            return_offsets_mapping=True,
            truncation=False,
        )
        offsets = encoded["offset_mapping"]
    except (KeyError, TypeError, ValueError) as exc:
        raise DetectorError(
            "tokenizer must support fast-tokenizer character offset mappings"
        ) from exc

    if offsets and isinstance(offsets[0], list) and offsets[0] and isinstance(
        offsets[0][0], (list, tuple)
    ):
        offsets = offsets[0]
    return [(int(start), int(end)) for start, end in offsets]


def text_chunks(
    text: str, tokenizer: Any, max_tokens: int = MAX_TOKENS
) -> list[tuple[int, str]]:
    """Split text into bounded model inputs and retain each chunk's source offset."""
    if max_tokens < 1:
        raise ValueError("max_tokens must be positive")
    if not text:
        return []

    offsets = _token_offsets(tokenizer, text)
    if not offsets:
        return [(0, text)]

    chunks: list[tuple[int, str]] = []
    start_char = 0
    for token_end in range(max_tokens, len(offsets), max_tokens):
        end_char = max(start_char, min(len(text), offsets[token_end][0]))
        if end_char > start_char:
            chunks.append((start_char, text[start_char:end_char]))
            start_char = end_char
    if start_char < len(text):
        chunks.append((start_char, text[start_char:]))
    return chunks


def _merge_ranges(ranges: Iterable[tuple[int, int]]) -> list[tuple[int, int]]:
    merged: list[list[int]] = []
    for start, end in sorted(ranges):
        if end <= start:
            continue
        if merged and start <= merged[-1][1]:
            merged[-1][1] = max(merged[-1][1], end)
        else:
            merged.append([start, end])
    return [(start, end) for start, end in merged]


def _line_number(line_starts: list[int], char_offset: int) -> int:
    return bisect_right(line_starts, char_offset)


def analyze_text(
    path: Path,
    text: str,
    model: Any,
    tokenizer: Any,
    torch: Any,
    threshold: float = 0.8,
    max_tokens: int = MAX_TOKENS,
) -> FileResult:
    if not 0 <= threshold <= 1:
        raise ValueError("threshold must be between 0 and 1")

    spans: list[Span] = []
    for chunk_offset, chunk in text_chunks(text, tokenizer, max_tokens):
        with torch.no_grad():
            output = model([chunk], conf_interval_thresh=threshold)
        interval_batches = getattr(output, "ai_intervals", None)
        if interval_batches is None or len(interval_batches) != 1:
            raise DetectorError(f"model returned no interval predictions for {path}")

        intervals = interval_batches[0]
        if hasattr(intervals, "detach"):
            intervals = intervals.detach().cpu().tolist()
        elif hasattr(intervals, "tolist"):
            intervals = intervals.tolist()
        for interval in intervals:
            if len(interval) != 3:
                raise DetectorError(f"model returned an invalid interval for {path}")
            start, end, confidence = (float(value) for value in interval)
            start = max(0, min(len(chunk), int(start)))
            end = max(start, min(len(chunk), ceil(end)))
            if end <= start:
                continue
            spans.append(
                Span(
                    start_char=chunk_offset + start,
                    end_char=chunk_offset + end,
                    start_line=0,
                    end_line=0,
                    confidence=confidence,
                )
            )

    line_starts = [0]
    line_starts.extend(index + 1 for index, char in enumerate(text) if char == "\n")
    line_spans = tuple(
        Span(
            start_char=span.start_char,
            end_char=span.end_char,
            start_line=_line_number(line_starts, span.start_char),
            end_line=_line_number(line_starts, max(span.start_char, span.end_char - 1)),
            confidence=span.confidence,
        )
        for span in spans
    )
    merged = _merge_ranges((span.start_char, span.end_char) for span in line_spans)
    flagged_characters = sum(end - start for start, end in merged)
    return FileResult(
        path=str(path),
        characters=len(text),
        flagged_characters=flagged_characters,
        flagged_percent=100 * flagged_characters / len(text) if text else 0.0,
        spans=line_spans,
    )


def _patch_attention_output_dimension() -> None:
    """Preserve value dimensions when GigaCheck's torch dispatch drops out_dim."""
    try:
        from gigacheck.model.src.interval_detector.modules import attention
    except ImportError as exc:
        raise DetectorError("GigaCheck attention module is unavailable") from exc

    forward = attention.multi_head_attention_forward
    if getattr(forward, "_aicode_output_dimension_compat", False):
        return

    @wraps(forward)
    def forward_with_output_dimension(*args: Any, **kwargs: Any) -> Any:
        if kwargs.get("out_dim") is None:
            value = kwargs.get("value", args[2] if len(args) > 2 else None)
            if value is not None:
                kwargs["out_dim"] = value.size(-1)
            else:
                kwargs["out_dim"] = kwargs.get(
                    "embed_dim_to_check", args[3] if len(args) > 3 else None
                )
        return forward(*args, **kwargs)

    forward_with_output_dimension._aicode_output_dimension_compat = True
    attention.multi_head_attention_forward = forward_with_output_dimension


def load_model(
    model_id: str = MODEL_ID,
    revision: str = MODEL_REVISION,
    dtype: str = "bfloat16",
    quantization: str = "int2",
    local_files_only: bool = False,
) -> tuple[Any, Any, Any]:
    """Load GigaCheck with CPU-only inference and optional Quanto weight PTQ."""
    try:
        import torch
        from transformers import AutoModel
    except ImportError as exc:
        raise DetectorError(
            "missing inference dependencies; install this tool with `uv sync`"
        ) from exc

    if dtype not in {"bfloat16", "float32"}:
        raise DetectorError(f"unsupported dtype: {dtype}")
    if quantization not in {"none", "int2", "int4"}:
        raise DetectorError(f"unsupported quantization: {quantization}")

    model_path = Path(model_id).expanduser()
    if model_path.is_dir() and (model_path / "quanto_qmap.json").is_file():
        model, tokenizer, torch = _load_exported_model(model_path, torch, AutoModel)
        _patch_attention_output_dimension()
        return model, tokenizer, torch

    model_kwargs = {
        "revision": revision,
        "trust_remote_code": True,
        "torch_dtype": getattr(torch, dtype),
        "low_cpu_mem_usage": True,
        "local_files_only": local_files_only,
    }
    if quantization != "none":
        try:
            from transformers import QuantoConfig
        except ImportError as exc:
            raise DetectorError(
                "Quanto quantization is unavailable; install this tool with `uv sync`"
            ) from exc
        model_kwargs["quantization_config"] = QuantoConfig(weights=quantization)
        model_kwargs["device_map"] = {"": "cpu"}

    model = AutoModel.from_pretrained(model_id, **model_kwargs)
    _patch_attention_output_dimension()
    if quantization == "none":
        model.to("cpu")
    model.eval()
    if getattr(model, "tokenizer", None) is None:
        raise DetectorError("loaded model did not provide its tokenizer")
    model.aicode_quantization = quantization
    return model, model.tokenizer, torch


def _load_exported_model(model_path: Path, torch: Any, auto_model: Any):
    try:
        from accelerate import init_empty_weights
        from optimum.quanto import requantize
        from safetensors.torch import load_file
        from transformers import AutoConfig, AutoTokenizer
    except ImportError as exc:
        raise DetectorError(
            "missing inference dependencies; install this tool with `uv sync`"
        ) from exc

    config = AutoConfig.from_pretrained(
        model_path,
        trust_remote_code=True,
        local_files_only=True,
    )
    with init_empty_weights():
        model = auto_model.from_config(config, trust_remote_code=True)

    with (model_path / "quanto_qmap.json").open(encoding="utf-8") as file:
        quantization_map = json.load(file)
    state_dict = load_file(str(model_path / "model.safetensors"), device="cpu")
    requantize(
        model,
        state_dict=state_dict,
        quantization_map=quantization_map,
        device=torch.device("cpu"),
    )

    tokenizer = AutoTokenizer.from_pretrained(
        model_path,
        local_files_only=True,
    )
    model.tokenizer = tokenizer
    model.eval()
    with (model_path / "aicode_model.json").open(encoding="utf-8") as file:
        metadata = json.load(file)
    model.aicode_quantization = metadata["quantization"]
    return model, tokenizer, torch
