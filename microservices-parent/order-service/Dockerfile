# ------
# Build Stage
# ------
FROM gradle:8-jdk21 AS builder

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN ./gradlew build -x test

# ------
# Package Stage
# ------
FROM openjdk:21-jdk

RUN mkdir /app

COPY --from=builder /home/gradle/src/build/libs/*.jar /app/order-service.jar

ENV POSTGRES_USER=admin \
    POSTGRES_PWD=password

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/order-service.jar"]
