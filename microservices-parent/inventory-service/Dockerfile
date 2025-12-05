# ------
# Build Stage
# ------
FROM gradle:8.10.2-jdk17 AS builder

WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .

RUN ./gradlew :inventory-service:build -x test

# ------
# Package Stage
# ------
FROM eclipse-temurin:17-jre

RUN mkdir /app

COPY --from=builder /home/gradle/src/inventory-service/build/libs/*SNAPSHOT.jar /app/inventory-service.jar

ENV POSTGRES_USER=admin \
    POSTGRES_PWD=password

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/inventory-service.jar"]
