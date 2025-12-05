# Order Management System
Author: Breno Lopes Mafra, Computer Programmer and Analyst

Project Description  
Spring Boot microservices sample: API Gateway + product, inventory, order, and notification services. Products live in MongoDB with Redis cache; inventory and orders live in PostgreSQL (Flyway migrations). Orders publish Avro events to Kafka (Confluent Schema Registry), and notification-service consumes them to send emails. Gateway enforces JWT (Keycloak) and aggregates service Swagger docs.

Tech stack
- Services: api-gateway (9000), product-service (8084), inventory-service (8083), order-service (8082), notification-service (8085)
- Infra: Kafka + Schema Registry + Kafka UI (8086), Zookeeper, Keycloak (8080), MongoDB (27017) + Mongo Express (8081), Redis + Redis Insight (8001), Postgres for services + pgAdmin (8888)
- Languages/Frameworks: Java 17, Spring Boot 3.5.x, Spring Cloud Gateway MVC, Resilience4J, Spring Kafka (Avro), Spring Mail
- Data/Schema: MongoDB, Redis cache, PostgreSQL + Flyway, Avro shared schema module
- Testing/Build: Gradle 8, Testcontainers, RestAssured, JUnit 5

Application flow  
Client → api-gateway (JWT protected, circuit breakers) → product-service (Mongo + Redis cache) and order-service → order-service calls inventory-service via RestClient + Resilience4J → inventory-service (PostgreSQL) → order-service publishes `OrderPlacedEvent` (Avro) to Kafka → notification-service consumes from Kafka and emails customer.

Quick start (Docker Compose - recommended)
1) From repo root: `docker-compose -p comp3095-integrated -f docker-compose.yml up -d --build`
2) Gateway entry point: http://localhost:9000 (Swagger aggregator at `/swagger-ui`)
3) Helpful UIs: Mongo Express http://localhost:8081, Redis Insight http://localhost:8001, pgAdmin http://localhost:8888, Kafka UI http://localhost:8086, Keycloak http://localhost:8080 (admin/password)
4) Create/import a realm named `spring-microservices-security-realm` in Keycloak and a client for the gateway (JWT required for non-swagger endpoints).
5) Tear down: `docker-compose -p comp3095-integrated -f docker-compose.yml down`

Local dev (no Docker)
- Prereqs: Java 17; Postgres on 5433 (inventory) and 5434 (orders); MongoDB on 27017 with admin/password; Redis on 6379; Kafka + Schema Registry reachable (standalone compose under `docker/standalone/kafka`).
- Update `application.properties` if you change ports/credentials.
- Run in separate terminals:
  - `./gradlew :product-service:bootRun`
  - `./gradlew :inventory-service:bootRun`
  - `./gradlew :order-service:bootRun`
  - `./gradlew :notification-service:bootRun`
  - `./gradlew :api-gateway:bootRun`
- Call everything through the gateway at http://localhost:9000.

Main endpoints
- Product: `POST /api/product`, `GET /api/product`, `PUT /api/product/{id}`, `DELETE /api/product/{id}`
- Inventory: `GET /api/inventory?skuCode=SKU&quantity=1` → boolean
- Order: `POST /api/order` (validated against inventory, publishes Kafka event) with JSON body:
  ```json
  {
    "skuCode": "SKU_001",
    "price": 10.50,
    "quantity": 1,
    "userDetails": {
      "email": "user@example.com",
      "firstName": "Test",
      "lastName": "User"
    }
  }
  ```
- Notification: consumes `order-placed` topic and emails `userDetails.email`.

Build and tests
- Build: `./gradlew clean build`
- Tests (with Testcontainers): `./gradlew test`

Data and seeds
- Mongo/Redis init scripts: `docker/integrated/mongo/init`, `docker/integrated/redis/init`
- Postgres init: `docker/integrated/postgres/**/init/init.sql` (order/inventory) + persistent data under `docker/integrated/postgres`
- Kafka/Schema Registry: integrated in root compose or standalone at `docker/standalone/kafka`
