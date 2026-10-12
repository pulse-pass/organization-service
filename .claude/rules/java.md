---
description: Java/Spring Boot code conventions
paths:
  - "src/main/java/**/*.java"
---

# Java code

## Structure
- Java 21, Spring Boot 4.x, Maven (no Gradle, no Lombok). Package `com.pulsepass.<service>`.
- Packages: `api` (controllers, schemas, responses, exceptions, validators), `application` (services, dtos/input), `domain` (entities, repositories, exceptions, `UnitOfWork`), `infrastructure/jpa` (JPA entities, `Jpa*Repository`, `*RepositoryImpl`, `JpaUnitOfWork`) and `common`.
- Pure domain entity + JPA `*Entity` with `fromDomain`/`toDomain`; repository interface in the domain, implementation in `infrastructure/jpa`.
- Helpers are named `<Subject>Handlers` (`JsonHandlers`) and live directly in `common/`, without a `utils` subfolder.
- Static factories follow `from<Source>` (`fromEntity`, `fromDomain`, `OrganizationHistory.fromOrganization`), not `create`.
- Logic that builds data from the entity itself (e.g. the history snapshot, `toHistoryData()`) belongs to the domain, not to the service.

## Method order
- Private methods always **before** public ones, in the order they are used.
- In services and controllers, public methods go: create, then update, then get.
- Prefer separate, clear `if`s over compound `||` conditions when each case has its own meaning.

## Transactions and repositories
- Explicit transaction control through the `UnitOfWork` (request-scoped `JpaUnitOfWork`), never `@Transactional`.
- Reads go **before** `unitOfWork.begin()`, validations first (e.g. duplicate checks) and only then the lookups the writes need (e.g. the PENDING status); `begin()`/`commit()` wraps only the writes.
- No manual rollback in services: the `JpaUnitOfWork` `cleanup()` rolls back at the end of the request.
- `*RepositoryImpl` calls the plain `jpaRepository.save` (no `saveAndFlush`). With `AUTO_INCREMENT` the INSERT runs on `save`, inside the transaction.
- Existence checks use `findBy...().isPresent()`, never `existsBy...` methods.
- Relationships are `FetchType.LAZY`; `spring.jpa.open-in-view` stays at the Spring Boot default (enabled), so `toDomain()` can load them during the HTTP request even outside `begin()`/`commit()`. Non-HTTP entry points (Kafka consumers) must read lazy relationships inside a transaction.
- `try/catch` only around the call that actually throws (e.g. the `save` hitting a `UNIQUE` → `DataIntegrityViolationException` becomes the domain duplicate exception).

## Errors
- Business exceptions extend `DomainException` (`getCode()`, `getMessageKey()`), mapped to an HTTP status in the `GlobalExceptionHandler` `STATUS_MAP`; response `ErrorResponse(title, message, code)` with the message from the `MessageSource` (`messages.properties` in English and `messages_pt.properties`).
- Application setup errors (e.g. a domain table enumerator not found) throw a generic exception (`IllegalStateException`) → 500 `GEN00500`.
- Exception messages are plain literal text (e.g. `"Organization status PENDING not found"`), no string concatenation.
- Codes: generic `GEN00400`, `GEN00401`, `GEN00500`; per service `<SVC>0000N` (e.g. `ORG00001`).

## API
- UUID fields are validated with `@UUID(allowNil = false, letterCase = UUID.LetterCase.INSENSITIVE)`.
- JSON in snake_case. Service-to-service routes live under `/internal/**` and are never exposed by the gateways.
- Internal route: the controller takes `@RequestHeader(value = "internal-token", required = false)` and calls `internalRequestValidator.validateInternalRequest(token)` on its first line (no filter). The token is shared by every application (`INTERNAL_TOKEN`) and compared with `equals`.
- Logs follow `ClassName.method - message`.
