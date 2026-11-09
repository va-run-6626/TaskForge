# ADR 0001: Package-by-feature modular monolith

- **Status:** Accepted
- **Date:** 2026-11-09

## Context

TaskForge started with a package-by-layer layout (`controller`, `service`, `dto`, `exception`) and one interface per service (`ISystemService`, `ITaskService`). Each interface had exactly one implementation. The project will grow a concurrency engine around tasks: threads, executors, queues, cancellation, task states, retries, persistence, and metrics. Under a layered layout, that work would spread across every package and mix with unrelated code.

## Decision

1. **Modular monolith.** Keep one Maven module, one Spring Boot application, one JVM, and one container. TaskForge teaches in-process concurrency, so network boundaries would add operational cost without teaching anything. Running in one process also keeps thread behavior easy to observe.
2. **Package by feature.** The top-level packages are business features (`system`, `task`) plus a small `shared` package. Each feature owns its `api` (controllers and DTOs), `application` (services), and `exception` sub-packages. A change to task execution therefore stays inside `task`.
3. **No speculative abstractions.** The single-implementation service interfaces were removed, and controllers depend on concrete services. An interface will be added when a second implementation actually exists.
4. **Deferred: separate Maven modules and Spring Modulith.** With two small features, multi-module builds and Modulith's verification and event infrastructure would cost more than they return. Package boundaries plus `ModuleBoundaryTest` are enough for now. Revisit when a feature has an independent lifecycle, or when cross-feature events appear.

## Module communication

- Features currently do not communicate. `system` and `task` are fully independent.
- When communication becomes necessary, one feature calls the other's public `application` service directly through constructor injection. The dependency must stay one-way and must be documented here. Reaching into another feature's internals is not allowed.
- `shared.web.GlobalExceptionHandler` is the single place that translates exceptions into HTTP responses. It may reference feature exceptions (such as `TaskInterruptedException` → 503). Features never depend on `shared`.

## Maintaining boundaries

- `ModuleBoundaryTest` fails the build if `system` and `task` import each other, or if either imports `shared`.
- New capabilities grow inside `task` first. A new top-level feature is created only when a responsibility is genuinely independent.
- `shared` takes code only when two or more features need it, and never takes business logic.

## Consequences

- Each feature's code is in one place, so new contributors can read one feature at a time.
- `shared` depends on feature exception types. If many features add exceptions, consider per-feature `@RestControllerAdvice` classes or a common base exception that carries an HTTP status.
- The boundary check is text-based (it reads import statements). It does not catch fully qualified class names used inline. If that becomes a risk, switch to ArchUnit or Spring Modulith.
