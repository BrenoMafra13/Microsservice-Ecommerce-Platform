# Order Management System
Author: Breno Lopes Mafra, Computer Programmer and Analyst

Project Description
Spring Boot microservices sample: product catalog in MongoDB with Redis cache, inventory checks in PostgreSQL, and order orchestration exposed through an API Gateway.

Demo videos
- Video 1 — Architecture + local tooling walkthrough (services, data stores, and how Mongo Express, Redis Insight, and pgAdmin are wired in Docker Compose). Watch: https://youtu.be/vuW-PBnc7YY?si=BWTkc369dwniFexL
- Video 2 — API flow + demo (gateway routing, product CRUD, inventory check, and order placement via OpenFeign). Watch: https://youtu.be/DFeRhN09lWI?si=UK7-1ieAGKUENTEd

Tech stack
- Services: api-gateway (9000), product-service (8084), inventory-service (8083), order-service (8082)
- Languages/Frameworks: Java 17, Spring Boot 3.5 (Web, Actuator, Data JPA, Data MongoDB, Data Redis)
- Spring Cloud: Gateway MVC, OpenFeign
- Data: PostgreSQL + Flyway, MongoDB, Redis cache
- Testing: Testcontainers, RestAssured, JUnit 5
- Containers/Build: Docker & Docker Compose, Gradle 8
- Tooling: Mongo Express (8081), Redis Insight (8001), pgAdmin (8888)

Application flow
Client → api-gateway (routes `/api/product` and `/api/order`) → product-service (MongoDB + Redis) and order-service (OpenFeign call to inventory-service) → inventory-service (PostgreSQL) → responses flow back through the gateway. All HTTP; business logic lives inside each service; Flyway manages order/inventory schemas.

Quick start (Docker Compose - recommended)
1) From repo root: `docker-compose -p comp3095-integrated -f docker-compose.yml up -d --build`
2) Hit services via gateway: http://localhost:9000
3) Helpful UIs: Mongo Express http://localhost:8081, Redis Insight http://localhost:8001, pgAdmin http://localhost:8888
4) Tear down: `docker-compose -p comp3095-integrated -f docker-compose.yml down`

Quick start (local dev, no Docker)
Prereqs: Java 17; Postgres on 5433 (inventory) and 5434 (orders); MongoDB on 28017; Redis on 6379.
- Update `application.properties` if you change ports/credentials.
- Run in separate terminals:
  - `./gradlew :product-service:bootRun`
  - `./gradlew :inventory-service:bootRun`
  - `./gradlew :order-service:bootRun`
  - `./gradlew :api-gateway:bootRun`
- Call everything through the gateway at http://localhost:9000.

Main endpoints
- Product: `POST /api/product`, `GET /api/product`, `PUT /api/product/{id}`, `DELETE /api/product/{id}`
- Inventory: `GET /api/inventory?skuCode=SKU&quantity=1` → boolean
- Order: `POST /api/order` with JSON body:
  ```json
  {
    "skuCode": "SKU-123",
    "price": 10.50,
    "quantity": 1
  }
  ```
  (validated against inventory via OpenFeign before completion)

Build and tests
- Build: `./gradlew clean build`
- Tests (with Testcontainers): `./gradlew test`

Data and seeds
- Mongo/Redis init scripts: `docker/integrated/mongo/init`, `docker/integrated/redis/init`
- Postgres users/databases: `docker/standalone/postgres/*/init/init.sql`; persistent data under `docker/integrated/postgres`
