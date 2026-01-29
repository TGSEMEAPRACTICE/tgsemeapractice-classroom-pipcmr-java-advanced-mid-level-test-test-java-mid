[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/hxxXWQLA)

# Java Technical Challenge: Multi-Source Data Processor

Objective: Implement a module that processes transactions from multiple sources and demonstrates Java Streams, async validation, and the Strategy pattern for commissions.

This repository contains a minimal Spring Boot application that implements the challenge requirements in a single application class for the first approach. A small retry helper and unit tests are included.

## Prerequisites
- JDK 21 installed (project toolchain targets Java 21)
- Git (for cloning/pulling)
- The project uses the Gradle wrapper (`./gradlew`) — you do not need to install Gradle globally.

## How to build
From the repository root run:

```bash
./gradlew clean build
```

This will compile the code and run unit tests. If the build is successful, you'll see `BUILD SUCCESSFUL`.

## How to run the application (dev/demo)
To run the Spring Boot application locally (it runs the small demo pipeline from `main` and prints log output):

```bash
./gradlew bootRun
```

Or build an executable jar and run it:

```bash
./gradlew clean bootJar
java -jar build/libs/*-0.0.1-SNAPSHOT.jar
```

Note: The demo `main` method constructs a small set of sample `Transaction` objects and runs the Streams/Strategy/async validation pipeline. This is intended as a demonstration; production wiring would separate concerns into services and modules.

## How to run tests
Run the unit tests with:

```bash
./gradlew test
```

A small JUnit 5 test (`CommissionStrategyTest`) verifies the commission calculations (happy path).

## Project structure (high level)
- `src/main/java/com/teksystems/pip/midtest/MidTestApplication.java` – primary demo app, contains:
  - `Transaction` model (immutable nested class)
  - Streams pipeline: filter > 50, map status to `PROCESSED`, sum amounts
  - Strategy pattern for commission calculation (USD/EUR/default)
  - Async validation using `CompletableFuture` with retries
- `src/main/java/com/teksystems/pip/midtest/util/RetryExecutor.java` – small helper that runs suppliers asynchronously with retry/backoff and logs attempts
- `src/test/java/com/teksystems/pip/midtest/CommissionStrategyTest.java` – happy-path unit tests for commission strategies
- `.github/workflows/classroom.yml` – GitHub Actions workflow (autograding) now runs `./gradlew clean build` on push

## Logging and exception handling
- The application uses SLF4J for logging; log levels are used sensibly: `INFO` for summaries, `DEBUG` for detailed events, `WARN` for recoverable issues, and `ERROR` for unexpected failures.
- Validation returns a small textual result for the demo; for production consider returning a structured `ValidationResult` object and avoid throwing for expected validation failures.
- `RetryExecutor.shutdown()` is called at application exit to ensure the internal scheduler is stopped.

## Notes about Lombok / Mockito
- During development the Gradle file contains Lombok/Mockito test dependencies; the first implementation and unit tests provided here do not use Lombok or Mockito. If you want me to remove those dependencies from `build.gradle.kts`, say so and I'll remove them.

## Troubleshooting
- If the build fails on CI but succeeds locally, ensure the CI runner uses Java 21 (the included GitHub workflow sets this up).
- If IntelliJ warns about annotation processing for Lombok, enable it in `Settings → Build, Execution, Deployment → Compiler → Annotation Processors`. (Only necessary if you decide to use Lombok later.)

## Changelog
See `CHANGELOG.md` for a short history of changes made during the implementation.

---

If you want, I can:
- Extract `Transaction` and strategy classes into separate files for better modularity and testing.
- Add deterministic unit tests for `RetryExecutor` (simulating failing suppliers) and the async validation flow.
- Remove unused dependencies (`lombok`/`mockito`) from the Gradle file.

Tell me which you prefer and I will implement it next.
