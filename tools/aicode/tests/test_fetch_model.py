"""Tests for the standalone fetch_model script (loaded by file path)."""

from __future__ import annotations

import hashlib
import importlib.util
import zipfile
from pathlib import Path

import pytest

_SCRIPT = Path(__file__).resolve().parent.parent / "fetch_model.py"
_spec = importlib.util.spec_from_file_location("fetch_model", _SCRIPT)
fetch_model = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(fetch_model)


def _make_archive(path: Path, root: str | None) -> None:
    files = {
        "model.safetensors": b"weights",
        "quanto_qmap.json": b"{}",
        "aicode_model.json": b"{}",
    }
    with zipfile.ZipFile(path, "w") as bundle:
        for name, data in files.items():
            bundle.writestr(f"{root}/{name}" if root else name, data)


def _sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def test_fetch_model_unpacks_nested_release_archive(tmp_path):
    archive = tmp_path / "bundle.zip"
    _make_archive(archive, root="gigacheck-int2")
    output = tmp_path / "artifacts" / "gigacheck-int2"

    result = fetch_model.fetch_model(
        output=output, url=archive.as_uri(), sha256=_sha256(archive)
    )

    assert (result / "model.safetensors").read_bytes() == b"weights"
    assert (result / "aicode_model.json").is_file()
    assert list(output.parent.glob("*.zip.part")) == []
    assert not (output.parent / ".gigacheck-int2.fetch-tmp").exists()


def test_fetch_model_unpacks_flat_archive(tmp_path):
    archive = tmp_path / "bundle.zip"
    _make_archive(archive, root=None)
    output = tmp_path / "artifacts" / "gigacheck-int2"

    result = fetch_model.fetch_model(
        output=output, url=archive.as_uri(), sha256=_sha256(archive)
    )

    assert (result / "model.safetensors").is_file()
    assert (result / "quanto_qmap.json").is_file()


def test_fetch_model_skips_download_when_artifacts_complete(tmp_path, monkeypatch):
    output = tmp_path / "artifacts" / "gigacheck-int2"
    output.mkdir(parents=True)
    for name in fetch_model.REQUIRED_FILES:
        (output / name).write_bytes(b"present")

    def fail_download(*_args, **_kwargs):
        raise AssertionError("download must be skipped for complete artifacts")

    monkeypatch.setattr(fetch_model, "_download_to", fail_download)

    assert (
        fetch_model.fetch_model(output=output, url="file:///unused", sha256="0" * 64)
        == output.resolve()
    )


def test_fetch_model_rejects_checksum_mismatch(tmp_path):
    archive = tmp_path / "bundle.zip"
    _make_archive(archive, root="gigacheck-int2")
    output = tmp_path / "artifacts" / "gigacheck-int2"

    with pytest.raises(fetch_model.FetchError, match="checksum mismatch"):
        fetch_model.fetch_model(
            output=output, url=archive.as_uri(), sha256="0" * 64
        )

    assert not output.exists()
    assert not (output.parent / "gigacheck-int2.zip.part").exists()


def test_fetch_model_demands_force_for_incomplete_output(tmp_path):
    output = tmp_path / "artifacts" / "gigacheck-int2"
    output.mkdir(parents=True)
    (output / "junk.txt").write_text("junk", encoding="utf-8")

    with pytest.raises(fetch_model.FetchError, match="--force"):
        fetch_model.fetch_model(output=output, url="file:///unused", sha256="0" * 64)


def test_fetch_model_force_replaces_existing_artifacts(tmp_path):
    archive = tmp_path / "bundle.zip"
    _make_archive(archive, root="gigacheck-int2")
    output = tmp_path / "artifacts" / "gigacheck-int2"
    output.mkdir(parents=True)
    (output / "stale.txt").write_text("stale", encoding="utf-8")

    result = fetch_model.fetch_model(
        output=output, url=archive.as_uri(), sha256=_sha256(archive), force=True
    )

    assert (result / "model.safetensors").is_file()
    assert not (result / "stale.txt").exists()
