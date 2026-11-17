from __future__ import annotations

import json
import re
import sys
from contextlib import nullcontext
from pathlib import Path
from types import ModuleType, SimpleNamespace

import pytest

from aicode.detector import (
    DetectorError,
    _merge_ranges,
    analyze_text,
    collect_files,
    load_model,
    text_chunks,
)
from aicode import export_model as export_model_module


class FakeTokenizer:
    def __call__(self, text, **_kwargs):
        offsets = [(match.start(), match.end()) for match in re.finditer(r"\S+", text)]
        return {"input_ids": list(range(len(offsets))), "offset_mapping": offsets}


class FakeTorch:
    @staticmethod
    def no_grad():
        return nullcontext()


class FakeModel:
    def __init__(self, intervals):
        self.intervals = intervals

    def __call__(self, _texts, conf_interval_thresh):
        assert conf_interval_thresh == 0.8
        return SimpleNamespace(ai_intervals=[self.intervals])


def test_text_chunks_cover_source_and_respect_token_limit():
    text = "one two three four five"
    chunks = text_chunks(text, FakeTokenizer(), max_tokens=2)

    assert "".join(chunk for _, chunk in chunks) == text
    assert [len(FakeTokenizer()(chunk)["input_ids"]) for _, chunk in chunks] == [
        2,
        2,
        1,
    ]
    assert chunks[0][0] == 0
    assert chunks[-1][0] + len(chunks[-1][1]) == len(text)


def test_analyze_text_maps_character_intervals_to_lines_and_unions_overlap():
    text = "first line\nsecond line\n"
    model = FakeModel([[0, 5, 0.91], [3, 8, 0.85]])
    result = analyze_text(
        Path("sample.py"),
        text,
        model,
        FakeTokenizer(),
        FakeTorch(),
    )

    assert result.flagged_characters == 8
    assert result.flagged_percent == pytest.approx(100 * 8 / len(text))
    assert [(span.start_line, span.end_line) for span in result.spans] == [(1, 1), (1, 1)]
    assert [span.confidence for span in result.spans] == [0.91, 0.85]


def test_analyze_text_maps_later_chunks_to_original_lines():
    text = "one\ntwo\nthree\n"
    result = analyze_text(
        Path("sample.py"),
        text,
        FakeModel([[0, 3, 0.9]]),
        FakeTokenizer(),
        FakeTorch(),
        max_tokens=1,
    )

    assert result.flagged_characters == 9
    assert [span.start_line for span in result.spans] == [1, 2, 3]


def test_merge_ranges_handles_overlap_and_touching_ranges():
    assert _merge_ranges([(5, 8), (1, 4), (3, 6), (8, 10)]) == [(1, 10)]


def test_collect_files_recurses_and_ignores_generated_directories(tmp_path):
    source = tmp_path / "src" / "Main.java"
    source.parent.mkdir()
    source.write_text("class Main {}\n", encoding="utf-8")
    ignored = tmp_path / "node_modules" / "dep" / "Other.java"
    ignored.parent.mkdir(parents=True)
    ignored.write_text("class Other {}\n", encoding="utf-8")

    assert collect_files([tmp_path]) == [source.resolve()]


def test_collect_files_reports_unsupported_and_missing_inputs(tmp_path):
    unsupported = tmp_path / "notes.txt"
    unsupported.write_text("text", encoding="utf-8")

    with pytest.raises(DetectorError, match="unsupported source extension"):
        collect_files([unsupported])
    with pytest.raises(DetectorError, match="does not exist"):
        collect_files([tmp_path / "missing.java"])


def test_load_model_uses_quanto_int2_and_cpu_device_map(monkeypatch):
    calls = {}
    attention = _install_fake_gigacheck_attention(monkeypatch)

    class FakeModel:
        tokenizer = object()

        def eval(self):
            return self

    class FakeAutoModel:
        @staticmethod
        def from_pretrained(model_id, **kwargs):
            calls["model_id"] = model_id
            calls["kwargs"] = kwargs
            return FakeModel()

    class FakeQuantoConfig:
        def __init__(self, **kwargs):
            calls["quantization_config"] = kwargs

    fake_torch = ModuleType("torch")
    fake_torch.bfloat16 = object()
    fake_torch.float32 = object()
    fake_transformers = ModuleType("transformers")
    fake_transformers.AutoModel = FakeAutoModel
    fake_transformers.QuantoConfig = FakeQuantoConfig
    monkeypatch.setitem(sys.modules, "torch", fake_torch)
    monkeypatch.setitem(sys.modules, "transformers", fake_transformers)

    model, tokenizer, torch = load_model()

    assert model.tokenizer is tokenizer
    assert torch is fake_torch
    assert calls["quantization_config"] == {"weights": "int2"}
    assert calls["kwargs"]["device_map"] == {"": "cpu"}
    assert calls["kwargs"]["torch_dtype"] is fake_torch.bfloat16
    value = SimpleNamespace(size=lambda _dim: 8)
    assert attention.multi_head_attention_forward(1, 2, value, 16)["out_dim"] == 8


def test_load_model_can_disable_quantization(monkeypatch):
    calls = {}
    _install_fake_gigacheck_attention(monkeypatch)

    class FakeModel:
        tokenizer = object()

        def to(self, device):
            calls["device"] = device
            return self

        def eval(self):
            return self

    class FakeAutoModel:
        @staticmethod
        def from_pretrained(_model_id, **kwargs):
            calls["kwargs"] = kwargs
            return FakeModel()

    fake_torch = ModuleType("torch")
    fake_torch.bfloat16 = object()
    fake_torch.float32 = object()
    fake_transformers = ModuleType("transformers")
    fake_transformers.AutoModel = FakeAutoModel
    monkeypatch.setitem(sys.modules, "torch", fake_torch)
    monkeypatch.setitem(sys.modules, "transformers", fake_transformers)

    load_model(quantization="none")

    assert calls["device"] == "cpu"
    assert "quantization_config" not in calls["kwargs"]


def _install_fake_gigacheck_attention(monkeypatch):
    names = (
        "gigacheck",
        "gigacheck.model",
        "gigacheck.model.src",
        "gigacheck.model.src.interval_detector",
        "gigacheck.model.src.interval_detector.modules",
    )
    packages = {}
    for name in names:
        package = ModuleType(name)
        package.__path__ = []
        packages[name] = package
        monkeypatch.setitem(sys.modules, name, package)
    attention = ModuleType(f"{names[-1]}.attention")
    attention.multi_head_attention_forward = lambda *args, **kwargs: kwargs
    monkeypatch.setitem(sys.modules, attention.__name__, attention)
    packages[names[-1]].attention = attention
    return attention


def test_export_model_saves_quantized_model_and_tokenizer(tmp_path, monkeypatch):
    output = tmp_path / "model"
    source_code = tmp_path / "upstream"
    source_code.mkdir()
    (source_code / "configuration_gigacheck.py").write_text(
        "class GigaCheckConfig: pass\n", encoding="utf-8"
    )
    (source_code / "modeling_gigacheck.py").write_text(
        "class GigaCheckForDetection: pass\n", encoding="utf-8"
    )
    saved = []

    class FakeModel:
        config = SimpleNamespace(
            auto_map={
                "AutoConfig": "configuration_gigacheck.GigaCheckConfig",
                "AutoModel": "modeling_gigacheck.GigaCheckForDetection",
            },
            save_pretrained=lambda path: saved.append(("config", path)),
        )

        def state_dict(self):
            return {"weight": object()}

    class FakeTokenizer:
        def save_pretrained(self, path):
            saved.append(("tokenizer", path))

    monkeypatch.setattr(
        export_model_module,
        "load_model",
        lambda **kwargs: (
            saved.append(("load", kwargs)) or FakeModel(),
            FakeTokenizer(),
            FakeTorch,
        ),
    )
    fake_hub = ModuleType("huggingface_hub")
    fake_hub.hf_hub_download = lambda repo_id, filename, revision, local_files_only: (
        source_code / filename
    )
    monkeypatch.setitem(sys.modules, "huggingface_hub", fake_hub)
    fake_quanto = ModuleType("optimum.quanto")
    fake_quanto.quantization_map = lambda _model: {"layer": {"weights": "qint2"}}
    monkeypatch.setitem(sys.modules, "optimum.quanto", fake_quanto)
    fake_safetensors_torch = ModuleType("safetensors.torch")
    fake_safetensors_torch.save_file = (
        lambda state_dict, path: saved.append(("weights", state_dict, path))
    )
    fake_safetensors = ModuleType("safetensors")
    fake_safetensors.torch = fake_safetensors_torch
    monkeypatch.setitem(sys.modules, "safetensors", fake_safetensors)
    monkeypatch.setitem(sys.modules, "safetensors.torch", fake_safetensors_torch)

    assert export_model_module.export_model(output) == output.resolve()
    assert saved[0][0] == "load"
    assert saved[0][1]["quantization"] == "int2"
    assert saved[0][1]["local_files_only"] is False
    assert saved[1] == ("config", output.resolve())
    assert saved[2] == ("tokenizer", output.resolve())
    assert saved[3][0] == "weights"
    assert saved[3][2] == str(output.resolve() / "model.safetensors")
    assert (output / "quanto_qmap.json").read_text(encoding="utf-8").strip()
    metadata = json.loads((output / "aicode_model.json").read_text(encoding="utf-8"))
    assert metadata["quantization"] == "int2"
    assert (output / "configuration_gigacheck.py").exists()
    assert (output / "modeling_gigacheck.py").exists()


def test_export_model_refuses_nonempty_output_directory(tmp_path):
    output = tmp_path / "existing"
    output.mkdir()
    (output / "keep.txt").write_text("keep", encoding="utf-8")

    with pytest.raises(DetectorError, match="must be empty"):
        export_model_module.export_model(output)


def test_export_model_writes_download_manifest_for_hub_source(tmp_path, monkeypatch):
    output = tmp_path / "model"
    source_code = tmp_path / "upstream"
    source_code.mkdir()
    (source_code / "configuration_gigacheck.py").write_text(
        "class GigaCheckConfig: pass\n", encoding="utf-8"
    )
    (source_code / "modeling_gigacheck.py").write_text(
        "class GigaCheckForDetection: pass\n", encoding="utf-8"
    )
    cache_root = tmp_path / "hf" / "hub"
    model_cache = cache_root / "models--iitolstykh--GigaCheck-Detector-Multi"
    (model_cache / "blobs").mkdir(parents=True)
    (model_cache / "blobs" / "checkpoint").write_bytes(b"x" * 2048)
    xet_dir = tmp_path / "hf" / "xet"
    xet_dir.mkdir()
    (xet_dir / "chunk").write_bytes(b"y" * 512)

    class FakeModel:
        config = SimpleNamespace(
            auto_map={
                "AutoConfig": "configuration_gigacheck.GigaCheckConfig",
                "AutoModel": "modeling_gigacheck.GigaCheckForDetection",
            },
            save_pretrained=lambda _path: None,
        )

        def state_dict(self):
            return {}

    class FakeTokenizer:
        def save_pretrained(self, *_args, **_kwargs):
            pass

    fake_hub = ModuleType("huggingface_hub")
    fake_hub.hf_hub_download = lambda repo_id, filename, revision, local_files_only: (
        source_code / filename
    )
    fake_constants = ModuleType("huggingface_hub.constants")
    fake_constants.HF_HUB_CACHE = str(cache_root)
    fake_constants.HF_HOME = str(tmp_path / "hf")
    fake_hub.constants = fake_constants
    fake_file_download = ModuleType("huggingface_hub.file_download")
    fake_file_download.repo_folder_name = lambda repo_id, repo_type: (
        "models--iitolstykh--GigaCheck-Detector-Multi"
    )
    fake_hub.file_download = fake_file_download
    monkeypatch.setitem(sys.modules, "huggingface_hub", fake_hub)
    monkeypatch.setitem(sys.modules, "huggingface_hub.constants", fake_constants)
    monkeypatch.setitem(sys.modules, "huggingface_hub.file_download", fake_file_download)
    fake_quanto = ModuleType("optimum.quanto")
    fake_quanto.quantization_map = lambda _model: {}
    monkeypatch.setitem(sys.modules, "optimum.quanto", fake_quanto)
    fake_safetensors_torch = ModuleType("safetensors.torch")
    fake_safetensors_torch.save_file = lambda _state, _path: None
    fake_safetensors = ModuleType("safetensors")
    fake_safetensors.torch = fake_safetensors_torch
    monkeypatch.setitem(sys.modules, "safetensors", fake_safetensors)
    monkeypatch.setitem(sys.modules, "safetensors.torch", fake_safetensors_torch)
    monkeypatch.setattr(
        export_model_module,
        "load_model",
        lambda **_kwargs: (FakeModel(), FakeTokenizer(), FakeTorch),
    )

    assert export_model_module.export_model(output) == output.resolve()

    manifest = output.parent / "model.downloads.txt"
    text = manifest.read_text(encoding="utf-8")
    assert "iitolstykh/GigaCheck-Detector-Multi@" in text
    assert str(output.resolve()) in text
    assert str(model_cache) in text
    assert str(model_cache / "blobs" / "checkpoint") in text
    assert "2.0 KB" in text
    assert str(xet_dir) in text
    assert "512 B" in text


def test_export_model_can_load_from_local_checkpoint(tmp_path, monkeypatch):
    source = tmp_path / "source"
    source.mkdir()
    (source / "configuration_gigacheck.py").write_text(
        "class GigaCheckConfig: pass\n", encoding="utf-8"
    )
    (source / "modeling_gigacheck.py").write_text(
        "class GigaCheckForDetection: pass\n", encoding="utf-8"
    )
    output = tmp_path / "output"
    calls = {}

    class FakeModel:
        config = SimpleNamespace(
            auto_map={
                "AutoConfig": "configuration_gigacheck.GigaCheckConfig",
                "AutoModel": "modeling_gigacheck.GigaCheckForDetection",
            },
            save_pretrained=lambda _path: None,
        )

        def save_pretrained(self, *_args, **_kwargs):
            pass

        def state_dict(self):
            return {}

    class FakeTokenizer:
        def save_pretrained(self, *_args, **_kwargs):
            pass

    fake_quanto = ModuleType("optimum.quanto")
    fake_quanto.quantization_map = lambda _model: {}
    monkeypatch.setitem(sys.modules, "optimum.quanto", fake_quanto)
    fake_safetensors_torch = ModuleType("safetensors.torch")
    fake_safetensors_torch.save_file = lambda _state, _path: None
    fake_safetensors = ModuleType("safetensors")
    fake_safetensors.torch = fake_safetensors_torch
    monkeypatch.setitem(sys.modules, "safetensors", fake_safetensors)
    monkeypatch.setitem(sys.modules, "safetensors.torch", fake_safetensors_torch)

    def fake_load_model(**kwargs):
        calls.update(kwargs)
        return FakeModel(), FakeTokenizer(), FakeTorch

    monkeypatch.setattr(export_model_module, "load_model", fake_load_model)

    export_model_module.export_model(
        output,
        model_id=str(source),
        local_files_only=True,
    )

    assert calls["model_id"] == str(source)
    assert calls["local_files_only"] is True
    assert (output / "configuration_gigacheck.py").exists()
    assert (output / "modeling_gigacheck.py").exists()
    assert not (output.parent / f"{output.name}.downloads.txt").exists()
