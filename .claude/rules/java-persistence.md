---
description: UnitOfWork, repositories, locking, LAZY and secrets in the database
paths:
  - "**/application/**"
  - "**/domain/**"
  - "**/infrastructure/jpa/**"
  - "**/infrastructure/security/**"
---

# Persistence

## Transactions

- **ORG-170** Explicit transaction control through the `UnitOfWork` (request-scoped `JpaUnitOfWork`), never `@Transactional`.
- **ORG-171** Reads go before `unitOfWork.begin()`, validations first (e.g. duplicate checks) and only then the lookups the writes need (e.g. the PENDING status); `begin()`/`commit()` wraps only the writes.
- **ORG-172** No manual rollback in services: the `JpaUnitOfWork` `cleanup()` rolls back at the end of the request.
- **ORG-173** Updating an existing record: the lookup of the item is `findForUpdateBy<Key>`, with the same name in the domain repository, the `*RepositoryImpl` and the `Jpa*Repository`.
- **ORG-174** In the `Jpa*Repository` it is a derived query with `@Lock(LockModeType.PESSIMISTIC_WRITE)` and no `@Query`; `ForUpdate` goes before `By` because Spring Data reads everything after `By` as criteria.
- **ORG-175** The lookup with the lock is the first call after `unitOfWork.begin()`.
- **ORG-176** The validations that depend on the item come right after it, inside the transaction (the `cleanup()` rolls back).
- **ORG-177** Lookups of domain tables still go before `begin()`.

## Repositories

- **ORG-178** Repositories use only derived queries (no `@Query`/JPQL, no `@EntityGraph`).
- **ORG-179** Each repository returns the entity of its own table with `toDomain()` already assembling the relationships; the service uses what the domain loaded, without fetching the same entity again.
- **ORG-180** Always take the simplest path: plain derived lookups in sequence (e.g. the person by key, then the collaborator by key and role), no custom queries, extra abstractions or redundant calls when a direct one solves it.
- **ORG-181** `*RepositoryImpl` calls the plain `jpaRepository.save` (no `saveAndFlush`). With `AUTO_INCREMENT` the INSERT runs on `save`, inside the transaction.
- **ORG-182** When updating a loaded object, call `repository.save(object)` and keep using the same object: never capture the return in a variable such as `saved` or `failed`.
- **ORG-183** The return of `save` is used only when creating, to get the generated data.
- **ORG-184** Existence checks use `findBy...().isPresent()`, never `existsBy...` methods.

## Lazy loading

- **ORG-185** Relationships are `FetchType.LAZY`; `spring.jpa.open-in-view` stays at the Spring Boot default (enabled), so `toDomain()` can load them during the HTTP request even outside `begin()`/`commit()`.
- **ORG-186** Non-HTTP entry points (Kafka consumers) must read lazy relationships inside a transaction.
- **ORG-187** List routes keep `FetchType.LAZY` too: the page comes in a single query and each relationship loads lazily in `toDomain()`; the persistence context fetches each distinct related row once (type tables have few rows), so no `@EntityGraph`/`JOIN FETCH` for them.
- **ORG-188** Revisit only when a relationship points to data that differs per item (e.g. each company's owner).

## Secrets

- **ORG-189** Secrets stored in the database (e.g. the Asaas API key of an organization) go through the `SecretCipher` interface in the domain (`encrypt`/`decrypt`), implemented in `infrastructure/security`, and are never returned by a response nor logged.
- **ORG-190** `AesGcmSecretCipher` uses AES-256-GCM, a random 12-byte IV and a 128-bit tag, stores `base64(iv + ciphertext)` and takes the key from a base64 property of 32 bytes.
