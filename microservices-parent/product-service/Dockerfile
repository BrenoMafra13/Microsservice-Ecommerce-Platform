# ------
# Build Stage
# ------
FROM gradle:8.10.2-jdk17 AS builder

WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .

RUN ./gradlew :product-service:build -x test

# ------
# Package Stage
# ------
FROM eclipse-temurin:17-jre

RUN mkdir /app

COPY --from=builder /home/gradle/src/product-service/build/libs/*SNAPSHOT.jar /app/product-service.jar

ENV MONGO_DB_USERNAME=admin \
    MONGO_DB_PWD=password

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "/app/product-service.jar"]
