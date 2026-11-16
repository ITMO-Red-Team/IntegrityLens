# Общая часть: очередь, core и контракт. Тяжёлые инструменты у методов разные, поэтому
# образов два, jplag и structsim, и ни один не тащит чужое. Сборка заканчивается
# проверкой на известном ответе: «запустился» и «сработал» — разные вещи.
FROM python:3.12.11-slim AS base
WORKDIR /app
COPY worker/requirements.txt worker/requirements.txt
RUN pip install --no-cache-dir -r worker/requirements.txt && useradd --system --uid 10001 app
COPY worker worker
USER app
CMD ["python", "-m", "worker"]

# Своя JVM вместо JDK на 600 МБ. Модуль jdk.compiler обязателен: JPlag разбирает Java
# настоящим компилятором и без него молча не разбирает ни одного файла.
FROM eclipse-temurin:25-jdk AS jplag-runtime
ARG JPLAG_VERSION=6.3.0
ARG JPLAG_SHA256=sha256:5f2c21e8b88ed77134effcb3a5a3ab13d188f6a3e16d401387f7479e92db9aa2
ADD --checksum=${JPLAG_SHA256} \
    https://github.com/jplag/JPlag/releases/download/v${JPLAG_VERSION}/jplag-${JPLAG_VERSION}-jar-with-dependencies.jar \
    /opt/jplag/jplag.jar
RUN jlink --add-modules java.base,java.compiler,java.logging,java.management,java.naming,java.sql,java.xml,jdk.charsets,jdk.compiler,jdk.unsupported,jdk.zipfs \
    --no-header-files --no-man-pages --strip-debug --output /opt/jplag/jre \
    && chmod a+r /opt/jplag/jplag.jar \
    && mkdir /tmp/aot

FROM base AS jplag
COPY --from=jplag-runtime /opt/jplag /opt/jplag
COPY --chown=app --from=jplag-runtime /tmp/aot /opt/jplag/aot
ENV JPLAG_JAVA=/opt/jplag/jre/bin/java JPLAG_JAR=/opt/jplag/jplag.jar
# AOT-кэш JVM записывается на настоящем запуске и обязан совпасть с рабочими ключами.
RUN JPLAG_JVM_OPTS="-XX:AOTCacheOutput=/opt/jplag/aot/jplag.aot -XX:+UseSerialGC" python -m worker.core.selfcheck jplag
ENV JPLAG_JVM_OPTS="-XX:AOTCache=/opt/jplag/aot/jplag.aot -XX:+UseSerialGC"

# Только бинарник: колесо с PyPI в разы меньше официального образа на 1.6 ГБ.
FROM python:3.12.11-slim AS semgrep
COPY worker/semgrep-requirements.txt /tmp/requirements.txt
RUN pip download --quiet --no-deps --only-binary :all: --require-hashes -r /tmp/requirements.txt -d /tmp/wheel \
    && python -m zipfile -e /tmp/wheel/*.whl /tmp/unpacked \
    && mv /tmp/unpacked/semgrep-*.data/purelib/semgrep/bin /opt/semgrep \
    && rm /opt/semgrep/__init__.py \
    && chmod +x /opt/semgrep/semgrep-core

FROM rust:1.95.0-slim-bookworm AS structsim-native
WORKDIR /native
COPY tools/structsim/native .
RUN cargo build --release --locked

FROM base AS structsim
COPY --from=semgrep /opt/semgrep /opt/semgrep
COPY --from=structsim-native /native/target/release/structsim-native /usr/local/bin/structsim-native
COPY tools/structsim/src/structsim structsim
ENV SEMGREP_CORE=/opt/semgrep/semgrep-core
RUN python -m worker.core.selfcheck structsim
