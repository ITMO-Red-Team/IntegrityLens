"""Export a pinned Hugging Face model as a locally loadable quantized checkpoint."""

from __future__ import annotations

import argparse
import json
import shutil
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
    return output


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
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
