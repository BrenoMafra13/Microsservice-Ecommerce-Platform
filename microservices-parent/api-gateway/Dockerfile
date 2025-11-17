# ------
# Build Stage
# ------
FROM gradle:8.10.2-jdk21 AS builder

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN ./gradlew build -x test

# ------
# Package Stage
# ------
FROM eclipse-temurin:21-jre

RUN mkdir /app

COPY --from=builder /home/gradle/src/build/libs/*.jar /app/api-gateway.jar

EXPOSE 9000

ENTRYPOINT ["java", "-jar", "/app/api-gateway.jar"]