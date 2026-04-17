# Movie Ticket Booking System

A **RESTful backend service** for booking movie tickets, built with Spring Boot 3.4.5 and Java 21. The application provides APIs for browsing movies, managing shows, and booking seats — with built-in security, caching, and transactional seat reservation.

---

## Features

- Browse Movies & Theatres — List all available movies and find shows by movie, city, and date
- Ticket Booking — Book multiple seats in a single transaction with pessimistic locking to prevent race conditions
-  Show Management — Create, update, and delete shows (admin operations)
-  Dynamic Pricing — Automatic discount calculation (afternoon shows, bulk booking)
-  HTTP Basic Authentication — All API endpoints are secured
-  Response Caching — Spring Cache reduces database load for read-heavy endpoints
-  Actuator Endpoints — Health, metrics, and monitoring out of the box
-  Input Validation — Bean Validation on all request payloads
-  Global Exception Handling — Consistent error responses across the API

---

## Technology Stack

Language -> Java 21
Framework -> Spring Boot 3.4.5
Security -> Spring Security (HTTP Basic)
Persistence -> Spring Data JPA / Hibernate
Database -> PostgreSQL
Caching -> Spring Cache (in-memory)
Validation -> Jakarta Bean Validation
Monitoring -> Spring Boot Actuator
Boilerplate -> Lombok
Build Tool -> Maven (with Maven Wrapper)
Testing -> JUnit 5, Mockito, Spring Boot Test

---

## Architecture Overview

The application follows a **layered architecture**:

REST Controllers -> HTTP layer, input validation

Service Layer -> Business logic, transactions

Repository Layer -> JPA repositories, custom queries

PostgreSQL -> Persistent storage

---

**Key design decisions:**
- **Pessimistic locking** on `ShowSeat` rows during booking prevents double-booking under concurrent requests
- **`@Transactional`** on the booking flow ensures all-or-nothing seat reservation
- **Spring Cache** on browse endpoints caches results and is evicted on new bookings
- **DTOs** are used at the controller boundary — domain models are never exposed directly

---

## Concurrency Handling

- Uses **Pessimistic Write Locking** (`@Lock(PESSIMISTIC_WRITE)`)
- Locks selected `ShowSeat` rows during booking
- Prevents double booking under concurrent requests
- Ensures consistency at DB level instead of relying on application logic

---

## Enums
- `BookingStatus` — `CONFIRMED`, `CANCELLED`
- `ShowSeatStatus` — `AVAILABLE`, `BOOKED`
- `SeatType` — seat category (e.g. STANDARD, RECLINER)
- `Role` — user roles for authorization

---

## API Reference

All endpoints require **HTTP Basic Authentication**.

---

### Actuator (Public)

`/actuator/health` -> Application health status
`/actuator/metrics` -> Application metrics
`/actuator/*` -> All actuator endpoints (public)

---

### Discount Logic
Applied automatically during booking:

Afternoon show (12:00 PM – 4:59 PM) -> 20% off each ticket
Every 3rd ticket in a booking -> 50% off that ticket

---

### Booking Validation
- Cannot book tickets for shows that have already started or passed
- Cannot book seats that are already BOOKED
- All requested seat IDs must belong to the specified show
- Seat status is updated atomically using pessimistic write locks

---

## Getting Started

### Prerequisites

- **Java 21** or later
- **Maven 3.9+** (or use the included `mvnw` wrapper)
- **PostgreSQL** running locally on port `5432`

### 1. Create the Database

```sql
CREATE DATABASE movietickets;
```

### 2. Configure Credentials

Edit `src/main/resources/application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/movietickets
    username: your_postgres_username
    password: your_postgres_password
  security:
    user:
      name: your_api_username
      password: your_api_password
```

### 3. Build & Run

```bash
# Using Maven Wrapper (recommended)
./mvnw spring-boot:run
```

The application starts on **`http://localhost:8080`** by default.

### 4. Test an Endpoint

```bash
curl -u {userName}:{password} http://localhost:8080/api/v1/browse/movies
```

##  Running Tests

```bash
# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=BookingServiceImplTest

# Run with verbose output
./mvnw test -Dsurefire.useFile=false
```

Tests use **Mockito** to mock repository dependencies making them pure unit tests with no database required.
