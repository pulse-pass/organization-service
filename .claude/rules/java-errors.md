---
description: Errors: DomainException, handler, codes and extra_fields
paths:
  - "**/exceptions/**"
  - "**/application/**"
  - "src/main/resources/messages*.properties"
---

# Errors

- **ORG-115** Handlers take no `Locale` parameter; `build` reads the request language from `LocaleContextHolder.getLocale()` (fed by `Accept-Language`) to pick `messages.properties` or `messages_pt.properties`.
- **ORG-116** Every error goes through `build` and answers `ErrorResponse(title, message, code, extra_fields)`; `extra_fields` is always present, an empty list when there is nothing to add.
- **ORG-117** Business exceptions extend `DomainException` (`getCode()`, `getMessageKey()`), mapped to an HTTP status in the `GlobalExceptionHandler` `STATUS_MAP`, with the message from the `MessageSource` (`messages.properties` in English and `messages_pt.properties`).
- **ORG-118** A schema validation failure (`MethodArgumentNotValidException`) answers 400 `GEN00400` with `ExtraFieldErrorResponse(field, error)` items where `field` is the JSON path in snake_case (e.g. `legal_person_collaborators[0].password`).
- **ORG-119** `error` is `REQUIRED` (`@NotBlank`, `@NotNull`), `INVALID_FORMAT` (`@Pattern`, `@Email`, `@Positive`, `@Digits`) or `SIZE` (`@Size`, `@Min`, `@Max`).
- **ORG-121** A violation on a `@RequestParam` (native method validation, `HandlerMethodValidationException`) answers 400 `GEN00400` with `[{"field": "page_size", "error": "SIZE"}]` (`ExtraFieldErrorResponse.fromParameterError`).
- **ORG-122** A parameter of the wrong type (`MethodArgumentTypeMismatchException`) answers `[{"field": "organization_status", "error": "INVALID_FORMAT"}]`.
- **ORG-123** An unreadable body or a value that does not parse, such as a value outside a schema enum (`HttpMessageNotReadableException`), answers `[{"field": "body", "error": "INVALID_FORMAT"}]`.
- **ORG-124** A missing required header (`MissingRequestHeaderException`) is handled by `handleMissingHeader` → 400 `GEN00400` with `[{"field": "<header-name>", "error": "REQUIRED"}]` (the header name as sent, not snake_case).
- **ORG-125** `GEN00400` always carries a non-empty `extra_fields`, and no other error uses `GEN00400`.
- **ORG-126** Never branch on types with `instanceof` (including pattern matching); write one method per type instead (e.g. one `@ExceptionHandler` per exception).
- **ORG-127** Application setup errors (e.g. a domain table enumerator not found) throw a generic exception (`IllegalStateException`) → 500 `GEN00500`.
- **ORG-128** Exception messages are plain literal text (e.g. `"Organization status PENDING not found"`), no string concatenation.
- **ORG-129** Generic errors (schema validation, authentication, permission, unknown route or method, unsupported media type and unexpected failures) use `GEN` + the HTTP status (`GEN00400`, `GEN00401`, `GEN00403`, `GEN00404`, `GEN00405`, `GEN00415`, `GEN00500`).
- **ORG-130** Authentication and permission exceptions (e.g. `UnauthorizedInternalRequestException`) are generic too: they use `GEN00401` / `GEN00403` and map to 401 / 403.
- **ORG-131** Every other `DomainException` carries the service code `<SVC>0000N` (e.g. `ORG00001`, `ONB00006`) and maps to a 4xx status in the `STATUS_MAP`.
- **ORG-132** Every `DomainException` must be mapped: the 500 default of `getOrDefault` exists only for a forgotten mapping, which is a bug.
- **ORG-133** 500 is always `GEN00500` with the generic `error.internal` message.
