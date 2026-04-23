# Smart Campus Sensor & Room Management API

A RESTful API built with **JAX-RS (Jersey 3.1.3)** and an embedded **Grizzly2** HTTP server for the University of Westminster "Smart Campus" initiative. Manages Rooms and IoT Sensors (temperature, CO2, occupancy) with a full historical readings log.

---

## Table of Contents
- [API Overview](#api-overview)
- [Project Structure](#project-structure)
- [Build & Run Instructions](#build--run-instructions)
- [API Endpoints](#api-endpoints)
- [Sample curl Commands](#sample-curl-commands)
- [Report: Question Answers](#report-question-answers)

---

## API Overview

| Base URL | `http://localhost:8080/api/v1` |
|---|---|
| Format | JSON (`application/json`) |
| Architecture | JAX-RS / Jersey 3.1.3 + Grizzly2 embedded server |
| Storage | In-memory `ConcurrentHashMap` (no database) |
| Version | 1.0.0 |

### Resource Hierarchy
```
/api/v1
  /rooms
    /{roomId}
  /sensors
    /{sensorId}
    /{sensorId}/readings
```

---

## Project Structure

```
smartcampus-api/
├── pom.xml
└── src/main/java/com/smartcampus/
    ├── Main.java                          # Embedded Grizzly server launcher
    ├── AppConfig.java                    # @ApplicationPath("/api/v1")
    ├── DataStore.java                   # Thread-safe in-memory ConcurrentHashMaps
    ├── models/
    │   ├── Room.java
    │   ├── Sensor.java
    │   └── SensorReading.java
    ├── resources/
    │   ├── DiscoveryResource.java        # GET /api/v1
    │   ├── RoomResource.java           # /api/v1/rooms
    │   ├── SensorResource.java          # /api/v1/sensors
    │   └── SensorReadingResource.java  # /api/v1/sensors/{id}/readings
    ├── exceptions/
    │   ├── RoomNotEmptyException.java
    │   ├── LinkedResourceNotFoundException.java
    │   ├── SensorUnavailableException.java
    │   ├── RoomNotEmptyExceptionMapper.java     # 409 Conflict
    │   ├── LinkedResourceNotFoundExceptionMapper.java # 422 Unprocessable Entity
    │   ├── SensorUnavailableExceptionMapper.java    # 503 Service Unavailable
    │   └── GlobalExceptionMapper.java          # 500 catch-all
    └── filters/
        └── LoggingFilter.java           # Request/response logging
```

---

## Build & Run Instructions

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher

### Step 1: Clone the repository
```bash
git clone https://github.com/nadhir-n/Smart-Campus-API.git
cd smartcampus-api
```

### Step 2: Build the fat JAR
```bash
mvn clean package
```
This produces `target/smart-campus-api-1.0-SNAPSHOT.jar` — a self-contained executable JAR with all dependencies bundled via the Maven Shade plugin.

### Step 3: Start the server
```bash
java -jar target/smart-campus-api-1.0-SNAPSHOT.jar
```

You should see:
```
Smart Campus API started at http://localhost:8080/api/v1
Press Enter to stop...
```

### Step 4: Verify it's running
```bash
curl http://localhost:8080/api/v1
```

Press `Enter` to stop the server.

---

## API Endpoints

### Discovery
| Method | Path | Description |
|---|---|---|
| GET | `/api/v1` | API metadata, versioning, and HATEOAS links |

### Rooms — `/api/v1/rooms`
| Method | Path | Description | Success | Error |
|---|---|---|---|---|
| GET | `/api/v1/rooms` | List all rooms | 200 | — |
| POST | `/api/v1/rooms` | Create a new room | 201 | 400 |
| GET | `/api/v1/rooms/{roomId}` | Get a specific room | 200 | 404 |
| DELETE | `/api/v1/rooms/{roomId}` | Delete room (blocked if sensors present) | 204 | 404, 409 |

### Sensors — `/api/v1/sensors`
| Method | Path | Description | Success | Error |
|---|---|---|---|---|
| GET | `/api/v1/sensors` | List all sensors (optional `?type=`) | 200 | — |
| POST | `/api/v1/sensors` | Register sensor (validates roomId exists) | 201 | 400, 422 |
| GET | `/api/v1/sensors/{sensorId}` | Get a specific sensor | 200 | 404 |

### Sensor Readings — Sub-Resource
| Method | Path | Description | Success | Error |
|---|---|---|---|---|
| GET | `/api/v1/sensors/{sensorId}/readings` | Full reading history | 200 | 404 |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add reading (updates currentValue) | 201 | 404, 503 |

---

## Sample curl Commands

### 1. API Discovery
```bash
curl -X GET http://localhost:8080/api/v1 -H "Accept: application/json"
```

### 2. List all rooms
```bash
curl -X GET http://localhost:8080/api/v1/rooms -H "Accept: application/json"
```

### 3. Create a new room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"CONF-2B","name":"Conference Room 2B","capacity":20}'
```

### 4. Get a specific room
```bash
curl -X GET http://localhost:8080/api/v1/rooms/CONF-2B -H "Accept: application/json"
```

### 5. Attempt to delete a room that has sensors — expects 409 Conflict
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/CONF-2B
```

### 6. List all sensors
```bash
curl -X GET http://localhost:8080/api/v1/sensors -H "Accept: application/json"
```

### 7. Filter sensors by type
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature" -H "Accept: application/json"
```

### 8. Register a sensor with a valid roomId
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"CONF-2B"}'
```

### 9. Register a sensor with a non-existent roomId — expects 422
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-999","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"GHOST-99"}'
```

### 10. Post a reading to an ACTIVE sensor (updates currentValue)
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":24.7}'
```

### 11. Post a reading to a MAINTENANCE sensor — expects 503
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":15.0}'
```

### 12. Get all readings for a sensor
```bash
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings -H "Accept: application/json"
```

---

## Report: Question Answers

### Part 1.1 — JAX-RS Resource Lifecycle & In-Memory Data Management

By default, JAX-RS creates a **new instance of each resource class for every incoming HTTP request** (per-request / prototype scope). This means no instance variables are shared between different requests — each request gets its own fresh object.

While this simplifies thread safety at the instance level, it creates a critical challenge for in-memory data management: if data were stored as instance fields, it would be destroyed at the end of every request.

To address this, all shared state in this project is stored in **`DataStore.java`** as **`static ConcurrentHashMap` fields**. Being static, these maps exist at the JVM class level and persist for the entire lifetime of the running process, surviving across all request instances.

`ConcurrentHashMap` is chosen over a plain `HashMap` because it provides internal lock-striping, allowing multiple threads to read simultaneously and perform atomic put/remove operations without requiring explicit `synchronized` blocks. This prevents race conditions — for example, two simultaneous `POST /sensors` requests both passing a uniqueness check before either completes its `put`, which would silently overwrite data. All reads and single-key writes are therefore safe without additional synchronisation code.

---

### Part 1.2 — HATEOAS and Hypermedia in RESTful Design

**HATEOAS** (Hypermedia as the Engine of Application State) is the practice of embedding navigational links inside API responses so clients can discover available operations dynamically, rather than relying on hard-coded URLs.

This is a hallmark of advanced REST design because it **decouples the client from the server's URL structure**. If the server changes a URI (e.g., renames `/rooms` to `/campus-rooms`), clients following embedded links adapt automatically without requiring a code update.

The `DiscoveryResource.java` implements HATEOAS by returning links to `/api/v1/rooms` and `/api/v1/sensors` in the discovery endpoint response, allowing clients to dynamically discover available endpoints.

---

### Part 2.1 — Returning IDs vs Full Objects in List Responses

Returning **full room objects** in `GET /api/v1/rooms` minimises client round-trips — everything needed to render a room list is available in one response. However, at scale (e.g., 10,000 rooms, each with a large `sensorIds` list), the payload size grows proportionally, consuming significant network bandwidth and increasing serialisation/parse time on both sides.

Returning **only IDs** produces tiny payloads but forces clients to make N sequential `GET /rooms/{id}` requests to retrieve any detail — the classic N+1 problem, which can overwhelm the server and introduce latency proportional to the collection size.

The practical industry approach (used here) is to return **full summary objects** in list responses — enough fields for common UI rendering (id, name, capacity) — while the complete object (including `sensorIds`) is available via `GET /rooms/{id}`. This balances bandwidth efficiency against client usability without either extreme.

---

### Part 2.2 — DELETE Idempotency

Yes, the `DELETE` operation in this implementation is **idempotent** in terms of server state. RFC 7231 defines idempotency as: the intended effect on the server is the same whether the request is made once or multiple times.

- **First `DELETE /rooms/CS-101`** (room exists, no sensors): removes the room, returns `204 No Content`.
- **Second `DELETE /rooms/CS-101`** (room already gone): returns `404 Not Found`.

The server state — *"room CS-101 does not exist"* — is identical after the first call and after every subsequent call. This is the widely accepted and semantically honest pattern: the end state is idempotent even when the status code varies, consistent with how most production REST APIs handle repeated deletes.

---

### Part 3.1 — @Consumes(APPLICATION_JSON) and Content-Type Mismatch

The `@Consumes(MediaType.APPLICATION_JSON)` annotation declares a contract: this method only handles requests with `Content-Type: application/json`. JAX-RS inspects the incoming `Content-Type` header and matches it against all registered `@Consumes` annotations before dispatching to a method.

If a client sends `Content-Type: text/plain` or `Content-Type: application/xml`, JAX-RS finds no matching method and **automatically returns HTTP `415 Unsupported Media Type`** before the method body executes. This happens entirely within the framework layer — no manual content-type checking is needed in the resource code.

---

### Part 3.2 — @QueryParam vs Path Segment for Filtering

`@QueryParam` (`/sensors?type=CO2`) is the semantically correct and technically superior approach for filtering:

1. **Semantic correctness**: A filter is a constraint on how a collection is retrieved, not a new resource.
2. **Composability**: Multiple query parameters chain naturally without changing URL structure — `?type=CO2&status=ACTIVE` adds a second filter trivially.
3. **Optionality**: Query parameters are inherently optional. Omitting `?type` returns all sensors.
4. **REST convention**: RFC 3986 defines query strings as the standard mechanism for providing search or filter parameters on a URI.

---

### Part 4.1 — Sub-Resource Locator Pattern Benefits

The Sub-Resource Locator pattern allows a resource class to dynamically delegate handling of a sub-path to another class. In this project, `SensorResource.getReadingsResource()` matches `/{sensorId}/readings` and returns a `SensorReadingResource` instance; JAX-RS then dispocks the remaining path to that object.

**Advantages:**
- **Single Responsibility Principle**: Each class handles one concern.
- **Reduced complexity**: Delegation keeps each class concise.
- **Testability**: `SensorReadingResource` can be unit-tested in complete isolation.
- **Team scalability**: Multiple developers can work on different resources simultaneously.
- **Extensibility**: Adding operations only requires changes inside the sub-resource class.

---

### Part 5.2 — HTTP 422 vs HTTP 404 for Missing Referenced Resources

When a client sends `POST /sensors` with a valid JSON body but a `roomId` that does not exist, the request was **received, parsed, and understood correctly by the server**. The problem is a **semantic validation failure within the payload content** — a foreign-key reference that resolves to nothing.

**HTTP 404 Not Found** means "the requested URL/resource does not exist on this server" — it is appropriate when the client is calling a wrong endpoint. Using `404` here would mislead clients into thinking they have the wrong URL.

**HTTP 422 Unprocessable Entity** means "the server understands the content type and syntax of the request, but cannot process the contained semantic instructions." This precisely describes the situation: the JSON is valid and the endpoint is correct, but a referenced entity inside the payload does not exist.

This project uses a custom `LinkedResourceNotFoundException` mapped to HTTP 422 for this scenario.

---

### Part 5.4 — Security Risks of Exposing Java Stack Traces

Returning raw Java stack traces to external consumers constitutes a significant information disclosure vulnerability:

1. **Internal architecture exposure**: Stack traces reveal full package and class names, disclosing the application's internal structure.
2. **Framework and library versions**: Attackers can cross-reference these with CVE databases.
3. **Logic and code-path disclosure**: Line numbers and call chains reveal exactly how request processing flows.
4. **Data structure leakage**: Exception traces can expose database table names, column names, or configuration values.

The `GlobalExceptionMapper` in this project eliminates this risk: it catches all unhandled `Throwable`s, logs the full stack trace **server-side**, and returns only a generic, sanitised `500 Internal Server Error` JSON body to the client.

---

### Part 5.5 — JAX-RS Filters vs Manual Logging Statements

Using a `ContainerRequestFilter` / `ContainerResponseFilter` for logging is architecturally superior to placing logging calls inside individual resource methods:

1. **DRY (Don't Repeat Yourself)**: One filter class automatically covers every endpoint.
2. **Separation of concerns**: Resource methods contain only business logic.
3. **Guaranteed coverage**: Filters intercept every request, including framework-level responses.
4. **Centralised control**: To change log format, only one class needs updating.
5. **Ordering and composition**: Multiple filters can be declaratively ordered using `@Priority`.

The `LoggingFilter.java` in this project implements both `ContainerRequestFilter` and `ContainerResponseFilter` to log all incoming requests and outgoing responses.