"""Fetch prebuilt model artifacts from the pinned GitHub Release.

Standalone script on purpose: it runs on any Python 3 without installing the
heavy inference dependencies, so consumers can prepare the model artifacts
before the Docker build with a single command:

    python fetch_model.py            # downloads and unpacks artifacts/gigacheck-int2
    python fetch_model.py --force    # re-download even if artifacts already exist
"""

from __future__ import annotations

import argparse
import hashlib
import shutil
import zipfile
from pathlib import Path
from urllib.request import urlopen

REPOSITORY = "ITMO-Red-Team/IntegrityLens"
RELEASE_TAG = "aicode-model-v0.1.0"
ARTIFACT_NAME = "gigacheck-int2.zip"
ARTIFACT_URL = (
    f"https://github.com/{REPOSITORY}/releases/download/{RELEASE_TAG}/{ARTIFACT_NAME}"
)
ARTIFACT_SHA256 = "afd7277e650d08055c6deca54375dabc8423cdc289405b9c08343058a750c28c"
DEFAULT_OUTPUT = Path("artifacts/gigacheck-int2")
REQUIRED_FILES = ("model.safetensors", "quanto_qmap.json", "aicode_model.json")
CHUNK_BYTES = 4 * 1024 * 1024
PROGRESS_STEP_BYTES = 256 * 1024 * 1024


class FetchError(RuntimeError):
    """The artifact cannot be downloaded, verified or unpacked."""


def fetch_model(
    output: Path = DEFAULT_OUTPUT,
    url: str = ARTIFACT_URL,
    sha256: str = ARTIFACT_SHA256,
    force: bool = False,
) -> Path:
    output = output.expanduser().resolve()
    if output.exists() and not output.is_dir():
        raise FetchError(f"output must be a directory: {output}")
    if _is_complete(output) and not force:
        print(f"Artifacts are already in place: {output}")
        return output
    if not force and output.exists() and any(output.iterdir()) and not _is_complete(output):
        raise FetchError(
            f"output directory holds an incomplete fetch, "
            f"remove it or pass --force: {output}"
        )

    staging = output.parent / f".{output.name}.fetch-tmp"
    archive = output.parent / f"{output.name}.zip.part"
    shutil.rmtree(staging, ignore_errors=True)
    output.parent.mkdir(parents=True, exist_ok=True)
    try:
        _download_to(url, sha256, archive)
        model_dir = _extract_archive(archive, staging)
        shutil.rmtree(output, ignore_errors=True)
        model_dir.rename(output)
    finally:
        archive.unlink(missing_ok=True)
        shutil.rmtree(staging, ignore_errors=True)

    if not _is_complete(output):
        raise FetchError(f"fetched artifacts are incomplete: {output}")
    print(f"Model artifacts are ready: {output}")
    return output


def _is_complete(output: Path) -> bool:
    return output.is_dir() and all(
        (output / name).is_file() for name in REQUIRED_FILES
    )


def _download_to(url: str, sha256: str, destination: Path) -> None:
    """Stream the artifact to disk, verifying sha256 while downloading."""
    digest = hashlib.sha256()
    downloaded = 0
    printed = 0
    try:
        with urlopen(url) as response, destination.open("wb") as file:
            while chunk := response.read(CHUNK_BYTES):
                digest.update(chunk)
                file.write(chunk)
                downloaded += len(chunk)
                if downloaded - printed >= PROGRESS_STEP_BYTES:
                    printed = downloaded
                    print(f"  downloaded {downloaded / (1 << 30):.1f} GiB")
    except OSError as exc:
        raise FetchError(f"cannot download {url}: {exc}") from exc
    actual = digest.hexdigest()
    if actual != sha256:
        destination.unlink(missing_ok=True)
        raise FetchError(
            f"checksum mismatch for {url}: expected sha256 {sha256}, got {actual}"
        )


def _extract_archive(archive: Path, staging: Path) -> Path:
    """Unpack the release zip and return the directory with model files.

    The archive may store the files under a single top-level directory or
    flat at the root; both layouts are normalized to a directory holding the
    required model files.
    """
    staging.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(archive) as bundle:
        bundle.extractall(staging)
    entries = list(staging.iterdir())
    model_dir = entries[0] if len(entries) == 1 and entries[0].is_dir() else staging
    if all((model_dir / name).is_file() for name in REQUIRED_FILES):
        return model_dir
    raise FetchError(
        f"unexpected archive layout in {archive}: "
        f"{sorted(entry.name for entry in entries)}"
    )


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(
        prog="fetch-model",
        description="Download prebuilt aicode model artifacts from a GitHub Release.",
    )
    parser.add_argument(
        "--output", type=Path, default=DEFAULT_OUTPUT, help="target directory"
    )
    parser.add_argument("--url", default=ARTIFACT_URL, help="artifact archive URL")
    parser.add_argument("--sha256", default=ARTIFACT_SHA256, help="expected checksum")
    parser.add_argument(
        "--force",
        action="store_true",
        help="re-download and unpack even if artifacts already exist",
    )
    args = parser.parse_args(argv)
    try:
        fetch_model(
            output=args.output, url=args.url, sha256=args.sha256, force=args.force
        )
    except FetchError as exc:
        parser.error(str(exc))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
