---
description: MySQL modeling conventions
paths:
  - "infrastructure/database.sql"
  - "src/main/java/**/infrastructure/jpa/**"
---

# Database

- One schema per service, named after the service **without** a `_db` suffix (`organization`, `onboarding`, `event`, `purchase_order`, `webhook`, `notification`); the local database has the same name.
- Tables and columns in snake_case and singular; compound names when a single word is too generic.
- `id BIGINT AUTO_INCREMENT` as the internal PK (keep the `AUTO_INCREMENT`) and a unique `<table>_key VARCHAR(36)` (UUID) used by the API and between services.
- Column order: id, public key, references, type, own data, amounts, dates, JSON, status, `created_at`/`updated_at` (`TIMESTAMP(3)`).
- Types, statuses and predefined options (e.g. rejection reasons) are domain tables `<x>_type`, `<x>_status`, `<x>_<option>` with an `enumerator` (plus `description` when displayed), seeded with `INSERT IGNORE` and referenced by FK; never free text. `<x>_status` tables only have `created_at`.
- Every entity with a status has `<x>_history` (`<x>_history_key`, `<x>_id`, `old_status_id` null on creation, `new_status_id`, `history_data` JSON, only `created_at`).
- Monetary values `DECIMAL(19,2)` named `amount` (or `<x>_amount`); external keys `external_*_key VARCHAR(36)`.
- Named constraints: `pk_<table>`, `<table>_<column>_unique`, `fkN_<table>`.
- No relationships across schemas: reference by `<x>_key`; when a service must filter or search by another service's data, it keeps a copy (e.g. CNPJ and company name on `organization`).
- No migrations: `database.sql` is idempotent (`CREATE SCHEMA/TABLE IF NOT EXISTS`, `INSERT IGNORE` on tables with a unique enumerator) and has no credentials. In `test` mode it runs in the MySQL `initdb` of the compose; in `development` the start script applies it to the shared `pulse-pass-mysql` as root, after creating the service user with a `GRANT` only on the service schema.
