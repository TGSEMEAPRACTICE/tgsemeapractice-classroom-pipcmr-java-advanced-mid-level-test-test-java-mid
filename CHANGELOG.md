# Changelog

All notable changes to this project are documented in this file.

## [Unreleased] - 2026-01-29
### Added
- Implemented transaction processing demo in `MidTestApplication`:
  - Java Streams pipeline (filter > 50, set status to PROCESSED, sum amounts)
  - Async validation using `CompletableFuture` with retries
  - Strategy pattern for commission calculation (USD/EUR/default)
- Added `RetryExecutor` helper (`src/main/java/com/teksystems/pip/midtest/util/RetryExecutor.java`) with logging and shutdown
- Added `CommissionStrategyTest` unit test for happy-path commission calculations
- Added SLF4J logging across application
- Updated GitHub Actions workflow `.github/workflows/classroom.yml` to use Gradle (`./gradlew clean build`) and set up JDK 21
- Updated `README.md` with run/test instructions and project overview

### Changed
- Converted `Transaction` to an immutable nested class and adjusted streams to use `withStatus()` to avoid mutable state
- Cleaned up IDE warnings and simplified code where applicable (use of `.toList()`, enhanced `switch`, etc.)

### Removed
- Removed demo `Person.java` file used during exploration

---

For future releases, follow semantic versioning and add an entry here for each release.
