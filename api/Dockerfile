FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /build
COPY api/pom.xml api/pom.xml
RUN mvn -f api/pom.xml dependency:go-offline -q
COPY api/src api/src
COPY infra/db/migrations infra/db/migrations
ARG JAVA_BUILD_OPTS=""
RUN MAVEN_OPTS="$JAVA_BUILD_OPTS" mvn -f api/pom.xml package -DskipTests -q

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --uid 10001 app
COPY --from=build /build/api/target/api-0.1.0.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
