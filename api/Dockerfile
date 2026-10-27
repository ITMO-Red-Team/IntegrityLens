FROM maven:3.9.11-eclipse-temurin-25 AS dependencies
WORKDIR /build
COPY api/pom.xml api/pom.xml
RUN mvn -f api/pom.xml dependency:go-offline -q

FROM eclipse-temurin:25-jdk AS build
COPY --from=dependencies /usr/share/maven /usr/share/maven
COPY --from=dependencies /root/.m2 /root/.m2
ENV PATH="/usr/share/maven/bin:${PATH}"
WORKDIR /build
COPY api/pom.xml api/pom.xml
COPY api/src api/src
COPY infra/db/migrations infra/db/migrations
ARG JAVA_BUILD_OPTS=""
RUN MAVEN_OPTS="$JAVA_BUILD_OPTS" mvn -f api/pom.xml package -DskipTests -q

FROM eclipse-temurin:25-jre
WORKDIR /app
ARG APT_MIRROR=""
RUN cp /etc/apt/sources.list.d/ubuntu.sources /tmp/ubuntu.sources.original \
    && if [ -n "$APT_MIRROR" ]; then \
      sed -i "s|http://archive.ubuntu.com/ubuntu|$APT_MIRROR|g; s|http://security.ubuntu.com/ubuntu|$APT_MIRROR|g" /etc/apt/sources.list.d/ubuntu.sources; \
    fi \
    && install_curl() { \
      apt-get -o APT::Update::Error-Mode=any -o Acquire::Retries=1 -o Acquire::http::Timeout=10 update \
      && apt-get -o Acquire::Retries=1 -o Acquire::http::Timeout=10 install -y --no-install-recommends curl; \
    } \
    && if ! install_curl; then \
      test -n "$APT_MIRROR" || exit 1; \
      echo 'APT mirror unavailable; retrying original Ubuntu repositories'; \
      cp /tmp/ubuntu.sources.original /etc/apt/sources.list.d/ubuntu.sources; \
      install_curl || exit 1; \
    fi \
    && rm -rf /var/lib/apt/lists/* /tmp/ubuntu.sources.original \
    && useradd --system --uid 10001 app
COPY --from=build /build/api/target/api-0.1.0.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
