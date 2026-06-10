FROM maven:3.9.16-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -ntp -DskipTests package

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN useradd -u 1000 -m -d /home/app app \
    && mkdir -p /app/data \
    && chown -R app:app /app /home/app

COPY --from=build --chown=app:app /workspace/target/aichat.jar /app/app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
