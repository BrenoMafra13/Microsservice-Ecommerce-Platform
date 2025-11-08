# ------
# Build Stage
# ------
FROM gradle:8.10.2-jdk17 AS builder

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN ./gradlew build -x test

# ------
# Package Stage
# ------
FROM eclipse-temurin:21-jre

RUN mkdir /app

COPY --from=builder /home/gradle/src/build/libs/*.jar /app/product-service.jar

ENV MONGO_DB_USERNAME=admin \
    MONGO_DB_PWD=password

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "/app/product-service.jar"]