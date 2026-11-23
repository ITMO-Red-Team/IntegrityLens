"""Версия метода ai: пакет aicode + ревизия модели + режим квантования.

Строка одна и та же в задании и результате: воркер знает её до запуска из метаданных
артефакта в образе, а движок сверяет с фактически загруженной моделью.
"""
import json
import os
from pathlib import Path

MOCK_VERSION = "aicode-mock/1"
DEFAULT_MODEL_DIR = "/opt/aicode-model"


def method_version(model_dir=None):
    model_dir = Path(model_dir or os.environ.get("AICODE_MODEL_DIR", DEFAULT_MODEL_DIR))
    import aicode  # в пакете только __version__, тяжёлых импортов нет

    try:
        metadata = json.loads((model_dir / "aicode_model.json").read_text(encoding="utf-8"))
        revision, quantization = metadata["revision"], metadata["quantization"]
    except (OSError, ValueError, KeyError) as exc:
        raise RuntimeError(f"cannot read model metadata in {model_dir}: {exc}") from exc
    return f"aicode-{aicode.__version__}+gigacheck-{revision[:10]}+{quantization}"
