FROM python:3.12.11-slim
WORKDIR /app
COPY worker/requirements.txt worker/requirements.txt
RUN pip install --no-cache-dir -r worker/requirements.txt && useradd --system --uid 10001 app
COPY core core
COPY worker worker
USER app
CMD ["python", "-m", "worker"]
