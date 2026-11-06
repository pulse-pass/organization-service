---
description: MySQL modeling conventions
paths:
  - "infrastructure/database.sql"
  - "src/main/java/**/infrastructure/jpa/**"
---

# Database

- **ORG-001** One schema per service, named after the service without a `_db` suffix (`organization`, `onboarding`, `event`, `purchase_order`, `webhook`, `notification`); the local database has the same name.
- **ORG-002** Tables and columns in snake_case and singular; compound names when a single word is too generic.
- **ORG-003** `id BIGINT AUTO_INCREMENT` as the internal PK (keep the `AUTO_INCREMENT`) and a unique `<table>_key VARCHAR(36)` (UUID) used by the API and between services.
- **ORG-004** Exception: 1:1 specialization tables (`natural_person`, `legal_person`) have no `<table>_key` of their own; the public key is the `person_key` of `person`.
- **ORG-005** Column order: id, public key, references, type, own data, amounts, dates, JSON, status, `created_at`/`updated_at` (`TIMESTAMP(3)`).
- **ORG-006** Types, statuses and predefined options (e.g. rejection reasons) are domain tables `<x>_type`, `<x>_status` and `<x>_<option>` with an `enumerator` (plus `description` when displayed), seeded with `INSERT IGNORE` and referenced by FK; never free text.
- **ORG-007** `<x>_status` tables only have `created_at`.
- **ORG-008** Every entity with a status has `<x>_history` (`<x>_history_key`, `<x>_id`, `old_status_id` null on creation, `new_status_id`, `history_data` JSON, only `created_at`).
- **ORG-009** Monetary values are `DECIMAL(19,2)` named `amount` (or `<x>_amount`).
- **ORG-010** External keys are `external_*_key VARCHAR(36)`.
- **ORG-011** Named constraints: `pk_<table>`, `<table>_<column>_unique`, `fkN_<table>`, `<table>_<column>_index`.
- **ORG-012** A column used as a list filter gets an index (`INDEX <table>_<column>_index (<column>)`) unless a `UNIQUE` or FK already covers it.
- **ORG-014** No relationships across schemas: reference by `<x>_key`; when a service must filter or search by another service's data, it keeps a copy (e.g. CNPJ and company name on `organization`).
- **ORG-015** No migrations: `database.sql` is idempotent (`CREATE SCHEMA/TABLE IF NOT EXISTS`, `INSERT IGNORE` on tables with a unique enumerator) and has no credentials.
