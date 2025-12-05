# ------
# Build Stage
# ------
FROM gradle:8.10.2-jdk17 AS builder

WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .

RUN ./gradlew :notification-service:build -x test

# ------
# Package Stage
# ------
FROM eclipse-temurin:17-jre

RUN mkdir /app

COPY --from=builder /home/gradle/src/notification-service/build/libs/*SNAPSHOT.jar /app/notification-service.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "/app/notification-service.jar"]
