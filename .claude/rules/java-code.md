---
description: Java code style: method order, flow, naming, ifs, catch, comments
paths:
  - "src/main/java/**/*.java"
---

# Java code

## Method order and flow

- **ORG-099** Private methods always before public ones, in the order they are used.
- **ORG-100** In services and controllers, public methods go: create, then update, then delete, then get/list.
- **ORG-101** Private methods only for real auxiliary steps used by several public methods or with a clear business name of their own (e.g. `saveLegalPerson`).
- **ORG-102** The flow of a public method stays inline and linear, in the method itself: never extract its branches or its save, history and commit sequence to a private method (`approveOrganization`, `saveAndCommit`), even when lines repeat, and never to shorten the method.
- **ORG-103** Lookups of domain tables by enumerator (`findByEnumerator(...).orElseThrow(...)`) stay inline in the public method, even when repeated, and are never extracted to a private method.
- **ORG-104** Prefer separate, clear `if`s over compound `||` conditions when each case has its own meaning.
- **ORG-105** One `catch` per different handling: two `catch` blocks that do the same thing become a single `catch (Exception ex)`. A separate `catch` exists only when that exception has specific handling.
- **ORG-106** `try/catch` only around the call that actually throws (e.g. the `save` hitting a `UNIQUE` → `DataIntegrityViolationException` becomes the domain duplicate exception).

## Style

- **ORG-107** No defensive validation (e.g. checking that a value from the environment is not empty).
- **ORG-108** Keep it simple: compare strings with `equals`, no `MessageDigest.isEqual` or similar tricks.
- **ORG-109** No comments in Java code (Javadoc included): names and structure must explain the code, and decisions go to the rules, the README or the task notes.

## Conversions and naming

- **ORG-110** Static factories follow `from<Source>` (`fromEntity`, `fromDomain`, `OrganizationHistory.fromOrganization`), not `create`.
- **ORG-111** `to*`/`from*` methods exist only in classes that represent data, converting themselves: `toDTO()` in schemas, `toDomain()` and static `fromDomain(...)` in JPA entities, `toHistoryData()` in domain entities.
- **ORG-112** Static `from*` also exist in responses and history entities (`fromEntity`, `fromOrganization`), plus the static helpers of `common/` (`JsonHandlers.toJson`, `toSnakeCase`).
- **ORG-113** Services, gateways, controllers and other components never have a `to*`/`from*` method; `to*` is reserved for conversions of the object itself and static helpers, never for a private step that validates.
- **ORG-114** A private step that checks a value is `validate*` and returns nothing (e.g. `validateTokenResponse`); one that builds something is named after the action (`fetch*`, `build*`).
