# ------
# Build Stage
# ------
FROM gradle:8.10.2-jdk17 AS builder

WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .

RUN ./gradlew :api-gateway:build -x test

# ------
# Package Stage
# ------
FROM eclipse-temurin:17-jre

RUN mkdir /app

COPY --from=builder /home/gradle/src/api-gateway/build/libs/*SNAPSHOT.jar /app/api-gateway.jar

EXPOSE 9000

ENTRYPOINT ["java", "-jar", "/app/api-gateway.jar"]
