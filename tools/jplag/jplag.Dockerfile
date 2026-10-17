# Baseline: JPlag — токенный детектор на Greedy String Tiling.
# Версия и контрольная сумма прибиты: результаты должны быть воспроизводимы,
# а релизные артефакты GitHub теоретически заменяемы.
# Именно JDK, а не JRE: модуль языка Java у JPlag разбирает исходники
# настоящим компилятором (`ToolProvider.getSystemJavaCompiler()` в
# `JavacAdapter`). В JRE компилятора нет, вызов возвращает null, и JPlag
# сообщает «found 0 valid submissions», ничего не объясняя.
FROM eclipse-temurin:25-jdk

ARG JPLAG_VERSION=6.3.0
ARG JPLAG_SHA256=sha256:5f2c21e8b88ed77134effcb3a5a3ab13d188f6a3e16d401387f7479e92db9aa2

ADD --checksum=${JPLAG_SHA256} \
    https://github.com/jplag/JPlag/releases/download/v${JPLAG_VERSION}/jplag-${JPLAG_VERSION}-jar-with-dependencies.jar \
    /opt/jplag/jplag.jar

WORKDIR /work

# Режим RUN, иначе JPlag пытается поднять встроенный просмотрщик отчёта.
ENTRYPOINT ["java", "-jar", "/opt/jplag/jplag.jar", "--mode", "RUN"]
CMD ["--help"]
