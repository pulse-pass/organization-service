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
- JPA `*Entity` classes have only fields, constructors and `fromDomain`/`toDomain` — no getters/setters (Hibernate uses field access); this also keeps them from being flagged as duplicates of the domain entities.
- Inside entities (domain and JPA), always reference the object's own fields with `this.` (getters, `toDomain`, `toHistoryData`).
- Never read another entity's fields directly (e.g. `history.organization.name`); go through a method such as `toDomain()`, because lazy proxies only load on method calls and their fields stay empty.
- `common/` holds only general-purpose language helpers (string, JSON, document math), named `<Subject>Handlers` (`JsonHandlers`, `DocumentHandlers`) and placed directly in `common/`, without a `utils` subfolder. Framework concerns (HTTP clients, timeouts, Spring beans) never go to `common/`: prefer the Spring Boot configuration (properties and auto-configured beans such as `RestClient.Builder`); what is left stays in the class that uses it.
- Static factories follow `from<Source>` (`fromEntity`, `fromDomain`, `OrganizationHistory.fromOrganization`), not `create`.
- Logic that builds data from the entity itself (e.g. the history snapshot, `toHistoryData()`) belongs to the domain, not to the service.

## Method order
- Private methods always **before** public ones, in the order they are used.
- In services and controllers, public methods go: create, then update, then get.
- Prefer separate, clear `if`s over compound `||` conditions when each case has its own meaning.

## Code
- No comments in code, configuration, scripts or Dockerfile (Javadoc included): names and structure must explain the code, and decisions go to the rules, the README or the task notes. The only exceptions are the version next to a workflow action pinned by SHA and generated or third-party files kept as they come (`mvnw`, `checkstyle.xml`).
- No defensive validation (e.g. checking that a value from the environment is not empty); `try/catch` only around the call that actually throws.
- Keep it simple: compare strings with `equals`, no `MessageDigest.isEqual` or similar tricks.

## Transactions and repositories
- Explicit transaction control through the `UnitOfWork` (request-scoped `JpaUnitOfWork`), never `@Transactional`.
- Reads go **before** `unitOfWork.begin()`, validations first (e.g. duplicate checks) and only then the lookups the writes need (e.g. the PENDING status); `begin()`/`commit()` wraps only the writes.
- No manual rollback in services: the `JpaUnitOfWork` `cleanup()` rolls back at the end of the request.
- `*RepositoryImpl` calls the plain `jpaRepository.save` (no `saveAndFlush`). With `AUTO_INCREMENT` the INSERT runs on `save`, inside the transaction.
- Existence checks use `findBy...().isPresent()`, never `existsBy...` methods.
- Relationships are `FetchType.LAZY`; `spring.jpa.open-in-view` stays at the Spring Boot default (enabled), so `toDomain()` can load them during the HTTP request even outside `begin()`/`commit()`. Non-HTTP entry points (Kafka consumers) must read lazy relationships inside a transaction.
- `try/catch` only around the call that actually throws (e.g. the `save` hitting a `UNIQUE` → `DataIntegrityViolationException` becomes the domain duplicate exception).

## Errors
- Handlers take no `Locale` parameter; `build` reads the request language from `LocaleContextHolder.getLocale()` (fed by `Accept-Language`) to pick `messages.properties` or `messages_pt.properties`.
- Every error goes through `build` and answers `ErrorResponse(title, message, code, extra_fields)`; `extra_fields` is always present, an empty list when there is nothing to add. A schema validation failure (`MethodArgumentNotValidException`) answers 400 `GEN00400` with `extra_fields`: `ExtraFieldErrorResponse(field, error)` items where `field` is the JSON path in snake_case (e.g. `legal_person_collaborators[0].password`) and `error` is `REQUIRED` (`@NotBlank`, `@NotNull`), `INVALID_FORMAT` (`@Pattern`, `@Email`) or `SIZE` (`@Size`). `GEN00400` always carries a non-empty `extra_fields`: an unreadable body (`HttpMessageNotReadableException`) answers `[{"field": "body", "error": "INVALID_FORMAT"}]`. No other error uses `GEN00400`. The password never appears in a log, a response or a `toString` (records that carry a password override `toString` with `***`); documents and other data may be logged.
- Business exceptions extend `DomainException` (`getCode()`, `getMessageKey()`), mapped to an HTTP status in the `GlobalExceptionHandler` `STATUS_MAP`; response `ErrorResponse(title, message, code, extra_fields)` with the message from the `MessageSource` (`messages.properties` in English and `messages_pt.properties`).
- Never branch on types with `instanceof` (including pattern matching); write one method per type instead (e.g. one `@ExceptionHandler` per exception).
- Application setup errors (e.g. a domain table enumerator not found) throw a generic exception (`IllegalStateException`) → 500 `GEN00500`.
- Exception messages are plain literal text (e.g. `"Organization status PENDING not found"`), no string concatenation.
- Codes: generic errors (schema validation, authentication, permission, unknown route or method, unsupported media type and unexpected failures) use `GEN` + the HTTP status (`GEN00400`, `GEN00401`, `GEN00403`, `GEN00404`, `GEN00405`, `GEN00415`, `GEN00500`). Authentication and permission exceptions (e.g. `UnauthorizedInternalRequestException`) are generic too: they use `GEN00401` / `GEN00403` and map to 401 / 403. Every other `DomainException` carries the service code `<SVC>0000N` (e.g. `ORG00001`, `ONB00006`) and maps to a 4xx status in the `STATUS_MAP`. Every `DomainException` must be mapped: the 500 default of `getOrDefault` exists only for a forgotten mapping, which is a bug. 500 is always `GEN00500` with the generic `error.internal` message.

## API
- UUID fields are validated with `@UUID(allowNil = false, letterCase = UUID.LetterCase.INSENSITIVE)`.
- No nested records in schemas, DTOs or responses: each nested object is its own file. Nested input objects live in `definitions/` (`api/schemas/definitions`, `application/dtos/input/definitions`) with generic names, without the operation (e.g. `PersonPhoneNumberSchema`, `PersonPhoneNumberDTO`); create a specific variant only when the validations diverge.
- Schemas validate only the shape of the request (required fields, size, digits-only format, list size). Business rules (document check digits, allowed values such as the collaborator role) live in the service and throw domain exceptions; no custom Bean Validation annotations.
- Route paths start with the service name (`/onboarding/legal_persons`), in snake_case with underscores, never hyphens. Service-to-service routes live under `/<service>/internal/**` (e.g. `/organization/internal/organizations`) and are never exposed by the gateways. The gateways expose routes without the prefix and add it when forwarding.
- JSON in snake_case.
- Internal route (`/<service>/internal/**`): the controller takes `@RequestHeader(value = "internal-token", required = false)` and calls `internalRequestValidator.validateInternalRequest(token)` on its first line (no filter). The token is shared by every application (`INTERNAL_TOKEN`) and compared with `equals`.
- Logs follow `ClassName.method - message`. The message is literal text; data goes in a single `Map` argument (`logger.error("X.y - Failed to create the user {}", Map.of("person_key", key), e)`), never interpolated in the sentence. The exception is passed as the last argument, never `e.getMessage()`.
- A create endpoint returns only the created resource (no child lists).

## External gateways
- Each gateway has its own folder in `domain/gateways/<gateway>/` (named after the gateway, not the provider), holding the interface and a `dtos/` folder (e.g. `identityprovider/IdentityProviderGateway`, `identityprovider/dtos/CreateUserInputDTO`). The interface receives and returns those DTOs, named after the operation, never loose parameters.
- The implementation lives in `infrastructure/gateways/<provider>/` named `<Provider><Interface>` (e.g. `keycloak/KeycloakIdentityProviderGateway`, `organizationservice/OrganizationServiceOrganizationGateway`), using `RestClient`. Request and response bodies are typed records in `infrastructure/gateways/<provider>/dtos` (`<Provider><Operation>Request` / `Response`) with `@JsonProperty` on every field; no `Map` bodies.
- Timeouts come from `spring.http.clients.connect-timeout` and `spring.http.clients.read-timeout`, which Spring Boot applies to the auto-configured `RestClient.Builder`; gateways receive the builder injected and never build a request factory by hand.
- The base URL comes from the property `app.<name>.url`, fed by the `<NAME>_URL` variable that `service-start.sh` resolves from the `<NAME>_TEST_URL` / `<NAME>_DEVELOPMENT_URL` pair.
- A transport failure or unexpected status in the operation itself is logged in the gateway and becomes a domain exception for the operation (e.g. `UserCreationException`), mapped to 422 in the `STATUS_MAP`, with the service code and a generic "try again later" message. Each `try` wraps only the HTTP call (one `try` per call); status and body checks come after it. The access token is part of the operation: its failure (transport, error status or no token in the body) is logged and becomes the same domain exception (e.g. `UserCreationException`). Other auxiliary calls (lookups such as the role) run before the operation, outside its `try`, and fail with a generic exception (`IllegalStateException` or the `RestClientException` itself) → 500 `GEN00500`.
