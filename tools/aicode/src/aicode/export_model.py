"""Export a pinned Hugging Face model as a locally loadable quantized checkpoint."""

from __future__ import annotations

import argparse
import json
import shutil
from collections.abc import Iterator
from datetime import datetime
from pathlib import Path

from aicode.detector import MODEL_ID, MODEL_REVISION, DetectorError, load_model


def export_model(
    output: Path,
    model_id: str = MODEL_ID,
    revision: str = MODEL_REVISION,
    quantization: str = "int2",
    local_files_only: bool = False,
) -> Path:
    output = output.expanduser().resolve()
    if output.exists() and (not output.is_dir() or any(output.iterdir())):
        raise DetectorError(f"output directory must be empty: {output}")
    output.mkdir(parents=True, exist_ok=True)

    model, tokenizer, _ = load_model(
        model_id=model_id,
        revision=revision,
        quantization=quantization,
        local_files_only=local_files_only,
    )
    from optimum.quanto import quantization_map
    from safetensors.torch import save_file

    model.config.save_pretrained(output)
    tokenizer.save_pretrained(output)
    save_file(model.state_dict(), str(output / "model.safetensors"))
    with (output / "quanto_qmap.json").open("w", encoding="utf-8") as file:
        json.dump(quantization_map(model), file, indent=2)
    with (output / "aicode_model.json").open("w", encoding="utf-8") as file:
        json.dump(
            {"model": model_id, "revision": revision, "quantization": quantization},
            file,
            indent=2,
        )
    _save_remote_code(model, output, model_id, revision)
    _write_download_manifest(output, model_id, revision, quantization)
    return output


def download_manifest_path(output: Path) -> Path:
    """Path of the file listing every path the export downloaded to disk."""
    return output.parent / f"{output.name}.downloads.txt"


def _iter_downloaded_files(directory: Path) -> Iterator[Path]:
    """Yield real files; snapshot symlinks point at blobs and would double-count."""
    if not directory.is_dir():
        return
    for path in sorted(directory.rglob("*")):
        if path.is_file() and not path.is_symlink():
            yield path


def _total_size(paths: Iterator[Path]) -> int:
    total = 0
    for path in paths:
        try:
            total += path.stat().st_size
        except OSError:
            continue
    return total


def _human_size(size: float) -> str:
    for unit in ("B", "KB", "MB", "GB", "TB"):
        if size < 1024 or unit == "TB":
            return f"{size:.1f} {unit}" if unit != "B" else f"{size:.0f} B"
        size /= 1024
    return f"{size:.1f} TB"


def _write_download_manifest(
    output: Path, model_id: str, revision: str, quantization: str
) -> Path | None:
    """Record the full paths of everything this export put on disk.

    Hugging Face caches downloads outside the project directory, where they
    are easy to lose sight of; the manifest makes every path reviewable and
    deletable afterwards. Returns None when the checkpoint came from a local
    directory or the hub library is unavailable: nothing new was downloaded.
    """
    if Path(model_id).expanduser().is_dir():
        return None
    try:
        from huggingface_hub import constants
        from huggingface_hub.file_download import repo_folder_name
    except ImportError:
        return None

    model_cache = Path(constants.HF_HUB_CACHE) / repo_folder_name(
        repo_id=model_id, repo_type="model"
    )
    cache_files = list(_iter_downloaded_files(model_cache))
    xet_dir = Path(constants.HF_HOME) / "xet"
    xet_files = list(_iter_downloaded_files(xet_dir))

    lines = [
        "# aicode: full paths of files placed on disk by this export, one per line.",
        f"# created: {datetime.now().astimezone().isoformat(timespec='seconds')}",
        f"# model: {model_id}@{revision}",
        f"# quantization: {quantization}",
        "#",
        "# Kept: exported artifacts are the Docker build input, do not delete yet.",
        str(output),
        "#",
    ]
    if cache_files:
        lines.append(
            f"# Hugging Face cache ({_human_size(_total_size(cache_files))}): "
            "safe to delete once the exported artifacts are verified."
        )
        lines.append(str(model_cache))
        lines.extend(str(path) for path in cache_files)
    else:
        lines.append(f"# Hugging Face cache is missing, nothing recorded: {model_cache}")
    if xet_files:
        lines.append(
            f"# Shared Xet chunk cache ({_human_size(_total_size(xet_files))}): "
            "other downloads may also use it, delete only if you are sure."
        )
        lines.append(str(xet_dir))
        lines.extend(str(path) for path in xet_files)
    manifest = download_manifest_path(output)
    manifest.write_text("\n".join(lines) + "\n", encoding="utf-8")
    return manifest


def _save_remote_code(model, output: Path, model_id: str, revision: str) -> None:
    auto_map = getattr(model.config, "auto_map", None) or {}
    modules = {
        reference.split(".", 1)[0]
        for value in auto_map.values()
        for reference in (value if isinstance(value, list) else [value])
        if isinstance(reference, str)
    }
    if not modules:
        return

    for module in sorted(modules):
        local_model = Path(model_id).expanduser()
        if local_model.is_dir():
            source = local_model / f"{module}.py"
            if not source.is_file():
                raise DetectorError(f"model source is missing required code: {source}")
        else:
            from huggingface_hub import hf_hub_download

            source = Path(
                hf_hub_download(
                    repo_id=model_id,
                    filename=f"{module}.py",
                    revision=revision,
                    local_files_only=True,
                )
            )
        shutil.copy2(source, output / f"{module}.py")


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(
        prog="aicode-export-model",
        description="Download and export a local, quantized GigaCheck checkpoint.",
    )
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--model", default=MODEL_ID)
    parser.add_argument("--revision", default=MODEL_REVISION)
    parser.add_argument(
        "--quantization",
        choices=("int2", "int4", "none"),
        default="int2",
    )
    parser.add_argument("--local-files-only", action="store_true")
    args = parser.parse_args(argv)

    try:
        output = export_model(
            output=args.output,
            model_id=args.model,
            revision=args.revision,
            quantization=args.quantization,
            local_files_only=args.local_files_only,
        )
    except DetectorError as exc:
        parser.error(str(exc))
    print(f"Exported {args.model}@{args.revision} to {output}")
    manifest = download_manifest_path(output)
    if manifest.is_file():
        print(f"Downloaded file paths: {manifest}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
