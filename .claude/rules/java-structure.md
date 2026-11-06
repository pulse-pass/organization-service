---
description: Stack, packages, shared code, entities and common/
paths:
  - "src/main/java/**/*.java"
---

# Java structure

- **ORG-191** Java 21, Spring Boot 4.x, Maven (no Gradle, no Lombok).
- **ORG-192** Package `com.pulsepass.<service>`.
- **ORG-193** Packages: `api` (controllers, schemas, responses, exceptions, validators), `application` (services, dtos/input), `domain` (entities, repositories, exceptions, `UnitOfWork`), `infrastructure/jpa` (JPA entities, `Jpa*Repository`, `*RepositoryImpl`, `JpaUnitOfWork`) and `common`.
- **ORG-194** Code shared by every service is identical in all of them except for the package, tests included.
- **ORG-195** The shared code is `InternalRequestValidator` (`hasAdminRole`, `validateInternalTokenOrAdmin`, `validateSelectedAgentKey`) and its test, `DomainException`, the generic exceptions (`UnauthorizedInternalRequestException`, `MissingSelectedAgentKeyException`), `ErrorResponse`, `ExtraFieldErrorResponse`, `PaginatedResponse`/`PaginationResponse`, `UnitOfWork`/`JpaUnitOfWork` and `JsonHandlers`.
- **ORG-196** A change to one of them (a rename, a new case, a new method) is applied to every repository that has it in the same task; before closing a task, diff these files across the repositories.
- **ORG-197** Pure domain entity + JPA `*Entity` with `fromDomain`/`toDomain`; repository interface in the domain, implementation in `infrastructure/jpa`.
- **ORG-198** JPA `*Entity` classes have only fields, constructors and `fromDomain`/`toDomain` — no getters/setters (Hibernate uses field access); this also keeps them from being flagged as duplicates of the domain entities.
- **ORG-199** Inside entities (domain and JPA), always reference the object's own fields with `this.` (getters, `toDomain`, `toHistoryData`).
- **ORG-200** Never read another entity's fields directly (e.g. `history.organization.name`); go through a method such as `toDomain()`, because lazy proxies only load on method calls and their fields stay empty.
- **ORG-201** Logic that builds data from the entity itself (e.g. the history snapshot, `toHistoryData()`) belongs to the domain, not to the service.
- **ORG-202** `common/` holds only general-purpose language helpers (string, JSON, document math), named `<Subject>Handlers` (`JsonHandlers`, `DocumentHandlers`) and placed directly in `common/`, without a `utils` subfolder.
- **ORG-203** Framework concerns (HTTP clients, timeouts, Spring beans) never go to `common/`: prefer the Spring Boot configuration (properties and auto-configured beans such as `RestClient.Builder`); what is left stays in the class that uses it.
