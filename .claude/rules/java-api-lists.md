---
description: List routes, filters, Specification and pagination
paths:
  - "**/api/**"
  - "**/application/**"
  - "**/infrastructure/jpa/**"
---

# List routes

- **ORG-045** List routes: each filter is a loose `@RequestParam` (no query schema), plus `page` (default 1, `@Min(1)`) and `page_size` (default 30, `@Min(1)` `@Max(100)`) validated on the `@RequestParam` itself with native method validation.
- **ORG-046** The service `list<X>s(filters..., page, pageSize)` returns a `List` from `xRepository.findAll(filters..., page, pageSize)`.
- **ORG-047** The `*RepositoryImpl` builds `<X>Specification.withFilters(...)` (in `infrastructure/jpa/specifications`), starting from `criteriaBuilder.conjunction()` and adding one predicate per filter given: one filter per parameter.
- **ORG-048** Use `equal` for exact values (documents, keys, enumerators) and a prefix `like` with escape only for free text (the name), with the `like` overload with the escape character `\`, escaping `\`, `%` and `_` in the value.
- **ORG-049** Never a generic search parameter that looks in several columns, and never `like '%x%'` (a contains search does not use an index).
- **ORG-050** The `*RepositoryImpl` calls `jpaRepository.findAll(spec, PageRequest.of(page - 1, pageSize, Sort.by(...)))`, so the `Jpa*Repository` also extends `JpaSpecificationExecutor`.
- **ORG-051** Every list declares its order in the `PageRequest` (e.g. `Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))`), with `id` as the last tie-breaker.
- **ORG-052** One `findAll` serves every list of the resource, and a list that does not use a filter passes `null`.
- **ORG-053** The response is the generic `PaginatedResponse<XResponse>(data, PaginationResponse(page, page_size))`, both in `api/responses/pagination/`.
- **ORG-054** The item is the route's `List<X>sResponse` in `api/responses/` (e.g. `PaginatedResponse<ListOrganizationsResponse>`); objects nested inside it go to `definitions/`.
- **ORG-055** A list filter with fixed values (the enumerator of a domain table) is received as the domain enum in the `@RequestParam` (e.g. `OrganizationStatusEnum organizationStatus`).
- **ORG-056** The service, repository and Specification compare the `enumerator` with `organizationStatus.name()`.
- **ORG-058** A domain table list (e.g. `GET /<service>/internal/organization_rejection_reasons`) is a list route without filters, in its own controller and service named after the table, paginated like the other lists.
- **ORG-059** A domain table list is ordered by `id` (`Sort.by(Sort.Order.asc("id"))`) and the item exposes the `enumerator` under the table name and the `<table>_description`.
- **ORG-060** A list route resolved by the token reads `selected-agent-key` and always filters by it.
- **ORG-062** An item fetched by its own key returns the object or 404; with `selected-agent-key`, a key different from the token's also answers 404 (same `<X>NotFoundException`, never revealing the existence).
