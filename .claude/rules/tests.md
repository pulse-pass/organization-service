---
description: Testing strategy and conventions
paths:
  - "src/test/**"
---

# Tests

- Unit tests (JUnit 5 + Mockito + AssertJ) in `src/test/java/.../unit/`, mirroring the main packages: **one test class per production class**, never grouping several classes in one test.
- Test names `method_scenario_expectedResult`.
- Black-box integration tests with REST Assured against the service running in Docker Compose (no Testcontainers), in `src/test/java/.../integration/`: `BaseIntegrationTest` (URL, port and token from environment variables) and one class per route `Test<Method><Action>` (e.g. `TestPostCreateOrganization`). They run with `./mvnw test -Pintegration`; `./mvnw test` runs only the unit tests.
- The test class of a route builds its own payloads and makes its own calls through private helpers in the class itself.
- `SetupTools` only creates prerequisites for **other** scopes (e.g. creating an organization to test the GET route); it does not hold payload builders.
- Shared helpers (`SetupTools`, `DocumentHandlers`) live in `src/test/java/.../common/`.
- REST Assured must be 6.x: Spring Boot 4 brings Groovy 5 and Jackson 3, which REST Assured 5.x cannot use.
- Integration tests are always run against the real compose (`./infrastructure/service-start.sh test` + `./mvnw test -Pintegration`) before a task is closed; unit tests with mocks do not catch persistence problems (lazy loading, constraints, schema validation).
- Each test generates its own data (UUIDs, random valid CNPJ); no teardown.
- Every route has success and error tests; a 409 on duplicates proves the write when there is no GET route yet.
