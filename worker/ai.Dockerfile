# Воркер метода ai: очередь из worker/ и движок из tools/aicode в одном образе.
# Модель скачивается при сборке из релиза репозитория (sha256 проверяется) и лежит
# отдельным stage: код воркера меняется часто, модель — нет.
FROM python:3.11-slim AS model
ARG AICODE_MODEL_URL=https://github.com/ITMO-Red-Team/IntegrityLens/releases/download/aicode-model-v0.1.0/gigacheck-int2.zip
ARG AICODE_MODEL_SHA256=afd7277e650d08055c6deca54375dabc8423cdc289405b9c08343058a750c28c
COPY tools/aicode/fetch_model.py /tmp/fetch_model.py
RUN python /tmp/fetch_model.py --url "$AICODE_MODEL_URL" --sha256 "$AICODE_MODEL_SHA256" \
        --output /opt/aicode-model \
    && rm /tmp/fetch_model.py

# Общая часть: зависимости aicode и очереди. Сборка заканчивается проверкой
# контракта на известном ответе — без модели, поэтому работает везде.
FROM python:3.11-slim AS ai-base

ENV UV_LINK_MODE=copy \
    UV_NO_CACHE=1 \
    PYTHONPATH=/app/src \
    PATH=/app/.venv/bin:${PATH}
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends git g++ \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 app

COPY tools/aicode/pyproject.toml tools/aicode/uv.lock ./
RUN pip install --no-cache-dir uv==0.8.15 \
    && uv sync --frozen --no-dev --no-install-project \
    && uv pip install --python .venv/bin/python --no-deps \
       "git+https://github.com/ai-forever/gigacheck.git@8cdbe6a1ce628b49b4aa0794edf7a425a47a774c"

COPY worker/requirements.txt worker/requirements.txt
RUN uv pip install --python .venv/bin/python -r worker/requirements.txt

COPY tools/aicode/src ./src
COPY worker worker

RUN python -m worker.ai.selfcheck mock

# Модель из релиза и проверка, что она грузится и результат воспроизводится (нужно ~4 ГБ
# памяти на время сборки). Смена версии модели — build-args AICODE_MODEL_URL и AICODE_MODEL_SHA256.
FROM ai-base AS ai
COPY --from=model /opt/aicode-model /opt/aicode-model

ENV AICODE_MODEL_DIR=/opt/aicode-model \
    HF_HUB_OFFLINE=1 \
    TRANSFORMERS_OFFLINE=1

RUN python -m worker.ai.selfcheck real

# torch.utils.cpp_extension всегда берёт file-lock в каталоге сборки quanto, даже когда
# .so уже собран: воркер работает от app, поэтому каталог должен быть доступен ему на запись.
RUN chmod -R a+rwX /app/.venv/lib/python3.11/site-packages/optimum/quanto/library/extensions

# torch и transformers пишут кэш в $HOME; самопроверка выше шла от root, а воркер — от app.
RUN mkdir -p /home/app && chown -R app:app /home/app

USER app
CMD ["python", "-m", "worker"]
