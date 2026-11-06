---
description: Routes, JSON, schemas, responses and internal routes
paths:
  - "**/api/**"
  - "**/application/dtos/**"
---

# API

- **ORG-070** Dates and times in responses are `Instant`, serialized as ISO 8601 in UTC with `Z` (`"created_at": "2026-11-03T22:50:27.442Z"`), never `LocalDateTime`, which goes out without a zone and is read as local time by the clients.
- **ORG-071** Entities keep `LocalDateTime` in UTC (the containers and MySQL run in UTC), and the response converts it in `from*` with `toInstant(ZoneOffset.UTC)`.
- **ORG-072** UUID fields are validated with `@UUID(allowNil = false, letterCase = UUID.LetterCase.INSENSITIVE)`.
- **ORG-073** No nested records in schemas, DTOs or responses: each nested object is its own file.
- **ORG-074** Nested input objects live in `definitions/` (`api/schemas/definitions`, `application/dtos/input/definitions`) with generic names, without the operation (e.g. `PersonPhoneNumberSchema`, `PersonPhoneNumberDTO`); create a specific variant only when the validations diverge.
- **ORG-075** Schemas validate only the shape of the request (required fields, size, digits-only format, list size).
- **ORG-076** Business rules (document check digits, allowed values such as the collaborator role) live in the service and throw domain exceptions; no custom Bean Validation annotations.
- **ORG-077** Route paths start with the service name (`/onboarding/legal_persons`), in snake_case with underscores, never hyphens.
- **ORG-078** Service-to-service routes live under `/<service>/internal/**` (e.g. `/organization/internal/organizations`).
- **ORG-079** The gateways expose routes without the service prefix and add it when forwarding.
- **ORG-080** The `backoffice-gateway` also exposes some internal routes, without the service prefix as `/internal/**` (e.g. `/internal/organizations`), only to the admin role; no other gateway exposes them.
- **ORG-081** JSON in snake_case.
- **ORG-082** Accepted values that are a subset of a domain enum use an enum of their own in the schema layer (`<Action><Resource><Field>Schema` in `api/schemas/definitions`), never `@Pattern`; `toDTO()` converts it with `name()`.
- **ORG-084** JSON fields (request, query and response) and parameters follow the name of the database column or table, never abbreviated (`organization_status`, `organization_rejection_reason`, never `status` or `rejection_reason`).
- **ORG-085** In Java, the same full name in camelCase (`organizationStatus`).
- **ORG-086** Internal route (`/<service>/internal/**`): the controller takes `@RequestHeader(value = "internal-token", required = false)` and `@RequestHeader(value = "selected-user-roles", required = false)`.
- **ORG-087** It calls `internalRequestValidator.validateInternalTokenOrAdmin(internalToken, selectedUserRoles)` on its first line (no filter).
- **ORG-088** The call is valid when the token equals the configured one (shared by every application, `INTERNAL_TOKEN`, compared with `equals`) or when `selected-user-roles` (comma-separated, each item trimmed) contains `admin`; otherwise `UnauthorizedInternalRequestException` (401 `GEN00401`).
- **ORG-089** There is no token-only validator.
- **ORG-090** Service-to-service calls use the token; the admin reaches the route through the `backoffice-gateway`, which sends `selected-user-roles`.
- **ORG-061** The `selected-agent-key` of a list route resolved by the token is read with `required = false` and validated by `InternalRequestValidator` (`validateSelectedAgentKey`; absent or blank → 401 `GEN00401`, `MissingSelectedAgentKeyException`), never with Bean Validation or a required header.
- **ORG-091** A create endpoint returns only the created resource (no child lists).
- **ORG-092** Top-level responses in `api/responses/` are named `<Action><Resource>Response` (`ListOrganizationsResponse`); nested objects in `definitions/` take only the resource name.
- **ORG-093** Responses of internal routes are named `<Action>Internal<Resource>Response` (`CreateInternalOrganizationResponse`, `ListInternalOrganizationsResponse`) and their output DTOs `<Action>Internal<Resource>DTO`.
- **ORG-094** Reusable nested responses live in `api/responses/definitions/` as `<Entity>Response` with `fromEntity`.
- **ORG-095** Pagination lives in `api/responses/pagination/`.
- **ORG-096** Input DTOs (`application/dtos/input`) exist only for fields that come from the request body through a schema; header, path and query values are passed to the service as plain parameters (e.g. `getSession(String personKey, String legalPersonKey, String roles)`).
- **ORG-097** The `OutputDTO` suffix (and `from*Output` in responses) is only for what an external gateway returns.
- **ORG-098** Service outputs that do not come from a gateway are `<Action><Resource>DTO` in `application/dtos/output`, named after the response they build with the suffix swapped, and the response uses `fromDTO`.
