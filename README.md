# Library Management API

A REST API for managing a library's books, members, and borrowing/returning of books.
Built with Spring Boot as a hands-on learning project.

> ℹ️ **This project is heavily commented on purpose.**
> It's a personal learning / practice project, so the code contains far more inline
> comments than you'd normally write in production. Almost every annotation and
> non-obvious line is explained so the reasoning is easy to revisit later. Treat the
> comments as part of the learning material, not as production style.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Running the Tests](#running-the-tests)
- [API Endpoints](#api-endpoints)
- [Configuration](#configuration)
- [Roadmap / Milestones](#roadmap--milestones)
- [Troubleshooting](#troubleshooting)

---

## Tech Stack

| Concern            | Choice                                              |
|--------------------|-----------------------------------------------------|
| Language           | Java 21                                             |
| Framework          | Spring Boot 4.0.6                                    |
| Build tool         | Maven (via the included Maven Wrapper `mvnw`)       |
| Persistence        | Spring Data JPA + Hibernate                         |
| Database           | PostgreSQL (run in Docker)                          |
| Validation         | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| Boilerplate        | Lombok (`@Getter`, `@Setter`, `@NoArgsConstructor`) |
| Testing            | JUnit 5 + Testcontainers (throwaway PostgreSQL)     |

---

## Architecture

The app follows the standard layered Spring Boot structure. A request flows top to bottom:

```
HTTP request
    │
    ▼
Controller   (@RestController)   – maps URLs/verbs to methods, handles HTTP concerns
    │
    ▼
Service      (@Service)          – business logic and rules
    │
    ▼
Repository   (JpaRepository)     – database access (Spring generates the implementation)
    │
    ▼
Entity       (@Entity)           – maps a Java class to a database table
    │
    ▼
PostgreSQL
```

**DTOs (Data Transfer Objects)** sit between the controller and the outside world:
- `BookRequest` – the shape a client is allowed to *send* (no `id`, no server-controlled fields).
- `BookResponse` – the shape we *send back*.

Keeping DTOs separate from entities means the public API stays stable even if the
internal database model changes.

---

## Project Structure

```
src/
├── main/
│   ├── java/com/library/library_api/
│   │   ├── LibraryApiApplication.java     # app entry point (main method)
│   │   ├── controller/
│   │   │   ├── BookController.java         # CRUD endpoints for books
│   │   │   └── AboutController.java        # demo: reads custom properties via @Value
│   │   ├── service/
│   │   │   └── BookService.java            # book business logic + entity<->DTO mapping
│   │   ├── repository/
│   │   │   ├── BookRepository.java
│   │   │   ├── MemberRepository.java
│   │   │   └── BorrowRecordRepository.java
│   │   ├── entity/
│   │   │   ├── Book.java
│   │   │   ├── Member.java
│   │   │   └── BorrowRecord.java
│   │   ├── dto/
│   │   │   ├── BookRequest.java
│   │   │   └── BookResponse.java
│   │   └── exception/
│   │       └── BookNotFoundException.java
│   └── resources/
│       └── application.properties          # DB connection, JPA settings, custom props
└── test/
    └── java/com/library/library_api/
        ├── repository/
        │   └── BorrowRecordRepositoryTest.java   # integration test (Testcontainers)
        ├── TestcontainersConfiguration.java      # spins up a temp Postgres for tests
        └── ...
```

---

## Prerequisites

You need these installed before running anything:

1. **JDK 21** — a full JDK, **not** just a JRE (a JRE cannot compile code).
   Verify with:
   ```bash
   javac -version
   ```
   If this errors or shows an old version, see [Troubleshooting](#troubleshooting).

2. **Docker Desktop** — used to run PostgreSQL (and for the Testcontainers tests).
   Make sure it's **running** before you start the app or the tests:
   ```bash
   docker ps
   ```
   If that command errors, Docker Desktop isn't started.

> You do **not** need to install PostgreSQL natively — it runs entirely inside Docker.

---

## Getting Started

### 1. Start a PostgreSQL container

The app connects to a database named `library` with user/password `library`
(see `application.properties`). Start a matching container:

```bash
docker run --name library-postgres \
  -e POSTGRES_DB=library \
  -e POSTGRES_USER=library \
  -e POSTGRES_PASSWORD=library \
  -p 5432:5432 \
  -d postgres:16
```

- The first run downloads the `postgres:16` image (one-time).
- Later, you don't need to recreate it — just restart the existing container:
  ```bash
  docker start library-postgres
  ```
- To confirm it's up:
  ```bash
  docker ps
  ```

### 2. Run the application

From the project root:

```bash
# macOS / Linux
./mvnw spring-boot:run

# Windows (PowerShell / cmd)
.\mvnw.cmd spring-boot:run
```

When you see `Started LibraryApiApplication in ... seconds`, it's ready on
**http://localhost:8080**.

On startup, Hibernate auto-creates the `books`, `members`, and `borrow_records`
tables (because `spring.jpa.hibernate.ddl-auto=update`).

### 3. Quick smoke test

```bash
# demo endpoint – reads custom values from application.properties
curl http://localhost:8080/api/about

# create a book
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Clean Code","author":"Robert C. Martin","isbn":"9780132350884","genre":"Programming","totalCopies":3}'

# list all books
curl http://localhost:8080/api/books
```

---

## Running the Tests

Tests use **Testcontainers**, which automatically starts its *own* temporary
PostgreSQL container (separate from `library-postgres`) and throws it away afterward.
**Docker Desktop must be running.**

```bash
# run all tests
./mvnw test

# run a single test class
./mvnw test -Dtest=BorrowRecordRepositoryTest
```

---

## API Endpoints

### Books — `/api/books`

| Method   | Path              | Description                | Success status |
|----------|-------------------|----------------------------|----------------|
| `POST`   | `/api/books`      | Create a book              | `201 Created`  |
| `GET`    | `/api/books`      | List all books             | `200 OK`       |
| `GET`    | `/api/books/{id}` | Get one book by id         | `200 OK`       |
| `PUT`    | `/api/books/{id}` | Update a book              | `200 OK`       |
| `DELETE` | `/api/books/{id}` | Delete a book              | `204 No Content` |

**Create/Update request body (`BookRequest`):**
```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "9780132350884",
  "genre": "Programming",
  "totalCopies": 3
}
```
Validation: `title`, `author`, `isbn` are required (not blank); `totalCopies` must be ≥ 1.

### Demo — `/api/about`

| Method | Path         | Description                                        |
|--------|--------------|----------------------------------------------------|
| `GET`  | `/api/about` | Returns custom properties injected via `@Value`    |

---

## Configuration

All configuration lives in `src/main/resources/application.properties`:

| Property                             | Purpose                                                        |
|--------------------------------------|----------------------------------------------------------------|
| `spring.datasource.url`              | JDBC URL of the PostgreSQL database                            |
| `spring.datasource.username/password`| DB credentials (match the Docker container above)             |
| `spring.jpa.hibernate.ddl-auto=update`| Auto-create/update tables from entities (learning-friendly; use migrations in production) |
| `spring.jpa.show-sql=true`           | Log every SQL statement Hibernate runs                        |
| `coach.name`, `team.name`            | Example custom properties (read by `AboutController`)         |

---

## Roadmap / Milestones

High-level phases toward a complete project. Each milestone is a meaningful chunk of
functionality, not an individual task — the detail underneath describes what "done"
means for that phase.

| # | Milestone | Status |
|---|-----------|--------|
| 1  | Data model & persistence          | ✅ Done |
| 2  | Book catalog (CRUD)               | ✅ Done |
| 3  | Member management                 | ✅ Done |
| 4  | Borrowing & returning workflow    | ⬜ Not started |
| 5  | Validation & error handling       | 🚧 In progress |
| 6  | Search & pagination               | ⬜ Not started |
| 7  | Security (authentication/authorization) | 🚧 Temporary (open for dev) |
| 8  | API documentation (Swagger)       | ⬜ Not started |
| 9  | Containerization (Docker Compose) | ⬜ Not started |
| 10 | Automated testing                 | 🚧 In progress |

**1. Data model & persistence** — ✅
Entities (`Book`, `Member`, `BorrowRecord`) with relationships, repositories, and a
working PostgreSQL connection with auto-generated tables.

**2. Book catalog (CRUD)** — ✅
Create, read, update, and delete books through `/api/books`, with request/response DTOs.

**3. Member management** — ✅
Register and manage library members through `/api/members` (create, read, list, update,
delete), with validation. `membershipDate` is set by the server, not the client.

**4. Borrowing & returning workflow** — ⬜ *(the core of the app)*
Borrow a book (decrement available copies, record the loan, block duplicate unreturned
loans), return a book (restore the copy, close the record), and query currently borrowed
and overdue books.

**5. Validation & error handling** — 🚧
Request validation across all endpoints, plus a global exception handler that turns
domain errors (e.g. book-not-found, no-copies-available) into clean, consistent HTTP
responses. *(Basic validation on book creation is in; the global handler is still to come.)*

**6. Search & pagination** — ⬜
Search books by title / author / genre, and paginate the book listing.

**7. Security (authentication/authorization)** — 🚧 *temporary posture*
`spring-boot-starter-security` is on the classpath. Right now `SecurityConfig` deliberately
leaves the API **open** (CSRF disabled, all requests permitted) so features can be built and
tested without auth. This milestone is complete only once real rules are in place: protected
vs. public routes, actual users/roles, and a login mechanism. **Replace the temporary config
before considering the project done.**

**8. API documentation (Swagger)** — ⬜
Interactive OpenAPI / Swagger UI so the API is self-documenting.

**9. Containerization (Docker Compose)** — ⬜
A `docker-compose.yml` that starts the app and database together with one command.

**10. Automated testing** — 🚧
Integration and unit tests covering the key flows. *(One repository integration test
exists; more coverage to follow, especially around borrow/return.)*

---

## Troubleshooting

**`docker ps` errors / tests fail with "Could not find a valid Docker environment"**
Docker Desktop isn't running. Start it, wait ~30–60s, then retry.
If your DB container is stopped, restart it: `docker start library-postgres`.

**`No compiler is provided in this environment. Perhaps you are running on a JRE rather than a JDK?`**
Maven is using a JRE (or an old Java) instead of JDK 21. Point it at a real JDK 21:
```bash
# example (adjust the path to your JDK 21 install)
export JAVA_HOME=/path/to/jdk-21        # macOS/Linux
$env:JAVA_HOME = "C:\path\to\jdk-21"    # Windows PowerShell
```
Running from IntelliJ usually avoids this, since the IDE uses its configured project SDK.

**`Application run failed` on startup**
This red line is only a *summary*. Scroll **up** to the
`***************************  APPLICATION FAILED TO START  ***************************`
block (or the `Caused by:` line) for the real reason. Two common causes:
- **Port 8080 already in use** — a previous run is still alive. Stop it (IntelliJ's red
  ■ stop button, or close the old Run tab) and try again.
- **Database unreachable** — the PostgreSQL container isn't running (see above).

---

*This README is a living document — update the checklist and endpoint tables as the
project grows.*
