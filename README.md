# TaskForge
This is a Project build to learn Java Concurrency and Multithreading.

## Requirements
- Java 25 (the Maven wrapper is included)
- Docker (optional)

## Running

```bash
./mvnw spring-boot:run          # local
docker compose up --build       # Docker
./mvnw clean verify             # compile, run all tests (unit, web-slice, integration, module boundaries)
./mvnw test -Dtest=ModuleBoundaryTest   # check feature boundaries only
```

The base path is `http://localhost:8080/api/v1`. Health check: `curl -i http://localhost:8080/api/v1/actuator/health`.

## Architecture

TaskForge is a **modular monolith**: one Maven module, one Spring Boot application, one JVM, one container. The code is organised **by feature**, not by technical layer. Each feature package owns its controller, service, DTOs and exceptions. See [ADR 0001](docs/adr/0001-package-by-feature-modular-monolith.md) for the reasoning.

```text
com.varun.taskforge
├── TaskForgeApplication.java
├── system/                         # system diagnostics
│   ├── api/
│   │   ├── SystemController.java
│   │   └── dto/SystemResponseDTO.java
│   └── application/SystemService.java
├── task/                           # task execution (future concurrency engine)
│   ├── api/
│   │   ├── TaskController.java
│   │   └── dto/
│   │       ├── TaskSimulationRequestDTO.java
│   │       └── TaskSimulationResponseDTO.java
│   ├── application/TaskSimulationService.java
│   └── exception/TaskInterruptedException.java
└── shared/
    └── web/                        # cross-cutting HTTP error handling
        ├── GlobalExceptionHandler.java
        └── ErrorResponseDTO.java
```

| Package  | Responsibility |
| -------- | -------------- |
| `system` | Reports runtime diagnostics (`/system/info`). |
| `task`   | Accepts and executes tasks. Today: synchronous simulation. Later: threads, executors, queues, cancellation, task states. |
| `shared` | Only genuinely cross-cutting infrastructure. Currently the uniform JSON error response and the `@RestControllerAdvice` that maps exceptions to HTTP status codes. No business logic. |

Inside a feature: `api` (HTTP controllers + request/response DTOs) → `application` (services holding the logic) → `exception` (the feature's own exceptions).

**Dependency rules** (enforced by `ModuleBoundaryTest`):
- `system` and `task` never import each other.
- Features never import `shared`; only `shared.web` may reference a feature's exceptions to translate them into HTTP responses.

**Adding a feature:**
1. New capabilities grow inside `task` first. Create a new top-level package only when a responsibility is genuinely independent.
2. A new feature gets its own top-level package with only the sub-packages it actually needs. No empty placeholders.
3. Use concrete classes with constructor injection. Add an interface only when there is a real second implementation.
4. Move code into `shared` only when two or more features genuinely need it.
5. Add the new package to `ModuleBoundaryTest`.

## Endpoints

### `GET /system/info`
Returns the application name, Java version, the request thread name, available processors and a timestamp.

### `POST /tasks/simulate`
Runs a simulated task synchronously on the request thread by calling `Thread.sleep(durationMs)`.

Request:
```json
{ "taskName": "report", "durationMs": 2000 }
```

| Field        | Rule                          |
| ------------ | ----------------------------- |
| `taskName`   | required, not blank           |
| `durationMs` | required, between 100 and 10000 |

Successful response (`200 OK`):
```json
{ "taskName": "report", "status": "COMPLETED", "threadName": "http-nio-8080-exec-2", "executionTimeMs": 2000 }
```

Error responses all use the same shape:
```json
{ "status": 400, "error": "VALIDATION_ERROR", "message": "durationMs must be between 100 and 10000", "timestamp": "2026-11-09T10:00:40.519Z" }
```

| HTTP | `error`             | Cause                                          |
| ---- | ------------------- | ---------------------------------------------- |
| 400  | `VALIDATION_ERROR`  | A field is missing or out of range             |
| 400  | `MALFORMED_REQUEST` | The body is missing or is not valid JSON       |
| 503  | `TASK_INTERRUPTED`  | The request-handling thread was interrupted mid-task |

Example:
```bash
curl -s -H 'Content-Type: application/json' \
  -d '{"taskName":"report","durationMs":2000}' \
  http://localhost:8080/api/v1/tasks/simulate
```

## Execution model (Job 01)

The endpoint is **synchronous**. Embedded Tomcat assigns each incoming HTTP request to a **Tomcat request-handling thread** from its connector thread pool (`http-nio-<port>-exec-N`, 200 threads by default). The task runs directly on that thread; there are no dedicated task worker threads yet. The request-handling thread stays blocked for the whole `durationMs` before the response is written.

If the thread is interrupted during the sleep, the service restores the interrupt flag, stops the normal path, and throws `TaskInterruptedException`. `GlobalExceptionHandler` turns this into `503 Service Unavailable`. An interrupted task is never reported as `COMPLETED`.

## Concurrency experiment

Three requests, each with `durationMs = 2000`, measured on 2026-11-09 against the Docker container (`docker compose up --build -d`) on the default port 8080.

**Sequential** (each `curl` waits for the previous one to finish):

| Task  | Thread                 | executionTimeMs |
| ----- | ---------------------- | --------------- |
| seq-1 | `http-nio-8080-exec-4` | 2000            |
| seq-2 | `http-nio-8080-exec-6` | 2000            |
| seq-3 | `http-nio-8080-exec-8` | 2000            |

Wall-clock time: **6481 ms**

**Concurrent** (three `curl` processes started together in the background):

| Task  | Thread                 | executionTimeMs |
| ----- | ---------------------- | --------------- |
| con-1 | `http-nio-8080-exec-10` | 2000           |
| con-2 | `http-nio-8080-exec-2`  | 2000           |
| con-3 | `http-nio-8080-exec-1`  | 2001           |

Wall-clock time: **2050 ms**

**What this shows:** each task still takes 2 s, but Tomcat ran the concurrent requests on three different request-handling threads at the same time, so the batch finished in about 2 s instead of 6 s. The concurrency comes entirely from the servlet container's thread pool, not from the application code. Each request still blocks a thread, so throughput is limited by the pool size. Once all request-handling threads are busy, new requests queue up.

Reproduce:
```bash
U=http://localhost:8080/api/v1/tasks/simulate; H='Content-Type: application/json'
# sequential
time (for i in 1 2 3; do curl -s -H "$H" -d "{\"taskName\":\"seq-$i\",\"durationMs\":2000}" $U; echo; done)
# concurrent
time (for i in 1 2 3; do curl -s -H "$H" -d "{\"taskName\":\"con-$i\",\"durationMs\":2000}" $U & done; wait)
```
