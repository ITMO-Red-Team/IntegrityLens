FROM python:3.11-slim AS builder

ARG AICODE_QUANTIZATION=int2
ENV UV_LINK_MODE=copy \
    UV_NO_CACHE=1 \
    PYTHONPATH=/app/src \
    PATH=/app/.venv/bin:${PATH}
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends git g++ \
    && rm -rf /var/lib/apt/lists/*

COPY pyproject.toml uv.lock ./
RUN pip install --no-cache-dir uv==0.8.15 \
    && uv sync --frozen --no-dev --no-install-project \
    && uv pip install --python .venv/bin/python --no-deps \
       "git+https://github.com/ai-forever/gigacheck.git@8cdbe6a1ce628b49b4aa0794edf7a425a47a774c"

COPY src ./src
RUN --mount=type=cache,target=/root/.cache/huggingface,sharing=locked \
    model_dir="$(HF_HOME=/root/.cache/huggingface .venv/bin/python -c \
      "from huggingface_hub import snapshot_download; print(snapshot_download(repo_id='iitolstykh/GigaCheck-Detector-Multi', revision='9fabe1503f99cc8a6f639996f9475c3ec19bc067'))")" \
    && HF_HOME=/root/.cache/huggingface HF_HUB_OFFLINE=1 TRANSFORMERS_OFFLINE=1 \
    .venv/bin/python -m aicode.export_model \
        --model "$model_dir" \
        --output /opt/aicode-model \
        --quantization "${AICODE_QUANTIZATION}" \
        --local-files-only

RUN HF_HOME=/root/.cache/huggingface HF_HUB_OFFLINE=1 TRANSFORMERS_OFFLINE=1 \
    .venv/bin/python -c \
      "from pathlib import Path; import torch; from transformers import AutoModel; from aicode.detector import _load_exported_model; _load_exported_model(Path('/opt/aicode-model'), torch, AutoModel)"

RUN .venv/bin/python -c \
      "import torch; import optimum.quanto; torch.ops.quanto.unpack(torch.zeros((8,), dtype=torch.uint8), 2)"

FROM python:3.11-slim
ARG AICODE_QUANTIZATION=int2
ENV PATH="/app/.venv/bin:${PATH}" \
    PYTHONPATH=/app/src \
    AICODE_QUANTIZATION=${AICODE_QUANTIZATION} \
    HF_HUB_OFFLINE=1 \
    TRANSFORMERS_OFFLINE=1
WORKDIR /work

COPY --from=builder /app/.venv /app/.venv
COPY --from=builder /app/src /app/src
COPY --from=builder /opt/aicode-model /opt/aicode-model

ENTRYPOINT ["python", "-m", "aicode.cli", "--model", "/opt/aicode-model", "--local-files-only"]
CMD ["--help"]
