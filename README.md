# 🏪 E-commerce Order Management System
*A distributed system built with Java, Spring Boot, Docker, PostgreSQL, MongoDB, and Redis.*

This project implements a **microservices-based architecture** for managing products, orders, and inventory.  
Each service runs independently, communicates via RESTful APIs, and integrates through a shared Redis cache for optimized performance.

---

## 🚀 Overview

The system simulates a distributed e-commerce backend composed of three core microservices:
- 🧾 **Order Service (PostgreSQL)** – Manages orders, payment flow, and order tracking.
- 📦 **Product Service (MongoDB)** – Handles product catalog, CRUD operations, and search functionality.
- 🏬 **Inventory Service (Redis)** – Manages product availability, reservations, and synchronization between services.

All services are containerized using **Docker Compose**, allowing modular deployment and isolated scaling.

🎥 **Demo Video:**  
- [Project Overview](https://www.youtube.com/watch?v=vuW-PBnc7YY)

---

## 🧠 Features

- ⚙️ **Microservices Communication:** RESTful APIs between independent services.  
- 🧱 **Database Diversity:** PostgreSQL for relational data, MongoDB for product catalog, and Redis for caching.  
- 🗃️ **CRUD Operations:** Create, read, update, and delete implemented in all core services.  
- 🧩 **Dockerized Setup:** Services orchestrated with Docker Compose for modular scalability.  
- 🚦 **API Gateway Ready:** Architecture prepared for future gateway or service registry integration.  
- 🧪 **Integrated Testing:** Postman collections used for end-to-end validation across all services.

---

## 🧰 Tech Stack

| Category | Technologies |
|-----------|---------------|
| **Language** | Java 17 |
| **Framework** | Spring Boot |
| **Databases** | PostgreSQL, MongoDB, Redis |
| **Containerization** | Docker, Docker Compose |
| **Architecture** | RESTful Microservices |
| **Tools** | Postman, Maven |

---

## ⚙️ Setup & Installation

1. **Clone the repository:**
   ```bash
   git clone https://gitlab.com/YOUR_USERNAME/microservices-structure.git
   cd microservices-structure
   ```

2. **Run all containers:**
    docker-compose up --build

3. **Access services:**
- Product Service → http://localhost:8081/products
- Order Service → http://localhost:8082/orders
- Inventory Service → http://localhost:8083/inventory

4. **Test with Postman:**
Use API routes defined in each service folder.

---

## 🧪 Testing
- Verified API communication between services through HTTP requests.
- Tested CRUD operations for each microservice (Product, Order, Inventory).
- Ensured data consistency and caching performance using Redis.

---

## 📚 Skills & Concepts Demonstrated
- Spring Boot Microservices Architecture
- RESTful API Design and Integration
- Docker Compose Orchestration
- Multi-database Integration (SQL + NoSQL + In-memory)
- Redis Caching and Data Synchronization
- Scalable Backend Design and Deployment
- Software Modularity and Reusability

## 📄 License
This project is for educational and portfolio purposes.
© 2025 Breno Lopes Mafra – All rights reserved.
