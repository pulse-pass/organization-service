---
description: Local infrastructure, Docker and scripts conventions
paths:
  - "infrastructure/**"
  - "Dockerfile"
  - "README.md"
  - "src/main/resources/application*.properties"
---

# Infrastructure

- **ORG-016** `infrastructure/` holds `docker-compose.yml`, `.env.example`, `database.sql`, `service-start.sh` and `service-stop.sh`, plus the external `pulse-pass` network.
- **ORG-017** The compose declares `name: <repo>-test` and gives every variable the scripts export (`SERVICE_PREFIX`, `MYSQL_HOST`, `PUBLISHED_API_PORT`, each `<X>_URL`) a `test` mode default, e.g. `${SERVICE_PREFIX:-<repo>-test}`.
- **ORG-018** Because of those defaults, `docker compose` without the scripts resolves to test mode (the CI log step relies on it); the scripts still pass `-p` and export those variables for the chosen mode.
- **ORG-019** The service keys of the compose are generic (`api`, `mysql`, `mockserver`) and the real name comes from `container_name` with `SERVICE_PREFIX`, because the key becomes a DNS alias on the `pulse-pass` network and both modes run together.
- **ORG-020** Always address another container by its `container_name`, never by the compose key.
- **ORG-021** `test` mode (`./infrastructure/service-start.sh test`, default, used by the CI) is self-sufficient: project `<repo>-test`, containers `<repo>-test-api`, `<repo>-test-mysql` and `<repo>-test-mockserver`; it never depends on `pulse-pass-infrastructure`.
- **ORG-022** `test` mode has its own MySQL (profile `test`, `database.sql` mounted in `initdb`) and MockServer, and the API on `API_PORT` (8082).
- **ORG-023** The MySQL healthcheck pings over TCP (`mysqladmin ping -h 127.0.0.1`) so it only passes after `database.sql` has run.
- **ORG-024** `development` mode starts only the API: project `<repo>`, container `<repo>-api` (the name other services use in `<NAME>_DEVELOPMENT_URL`).
- **ORG-025** The API is published on `DEVELOPMENT_API_PORT` (8092), using the shared `pulse-pass-mysql` (host `pulse-pass-mysql`, port 3306) from `pulse-pass-infrastructure`.
- **ORG-026** Before anything else (before calling `service-stop.sh`, so a missing infrastructure never takes down a running API), the script requires `pulse-pass-mysql` to be `healthy`, otherwise it exits asking to start the shared infrastructure.
- **ORG-027** As root (`MYSQL_ROOT_PASSWORD`, `root` like the infrastructure), the script creates the `.env` user with `CREATE USER IF NOT EXISTS` and a `GRANT ALL PRIVILEGES` only on the service schema, then applies `database.sql`.
- **ORG-028** The `.env` user, password and schema must not contain quotes or backslashes, because the script writes them into SQL.
- **ORG-029** Table changes only reach the shared MySQL when the script runs again; it is idempotent and never recreates what exists.
- **ORG-030** Both modes can run at the same time (distinct projects, container names and ports).
- **ORG-031** `service-stop.sh [test|development]` (default `test`) tears down only that mode's project (`down -v --remove-orphans --rmi local`) and never touches `pulse-pass-mysql` or the schema; `service-start.sh <mode>` calls it for the same mode first.
- **ORG-032** Redis and Kafka are added to the compose (test mode, own containers) and to `.env.example` (development uses `pulse-pass-redis` and `pulse-pass-kafka`) only in the task that makes the service use them.
- **ORG-033** The ports 3306, 6379, 9094 and 8180 belong to the shared infrastructure and the service must not publish them.
- **ORG-034** The mode comes from the argument of the scripts, never from editing `.env`.
- **ORG-035** Each external dependency has a `<X>_TEST_URL` / `<X>_DEVELOPMENT_URL` pair in `.env`, and the script exports `<X>_URL`, which feeds the gateway property `app.<name>.url`; the test URLs point to the `<repo>-test-mockserver` container.
- **ORG-036** The start script creates the `pulse-pass` network if missing and copies `.env.example` on first run.
- **ORG-037** Redis, Kafka, Kafka UI, Keycloak, Mailpit and MinIO live in the `pulse-pass-infrastructure` repository.
- **ORG-038** README: running the service only needs Docker (the image builds with its own JDK); Java 21 is only needed to run `./mvnw` (lint and tests) outside the container.
- **ORG-039** No comments in `infrastructure/`, `Dockerfile` or `application.properties`: names and structure must explain them, and decisions go to the rules or the README.
- **ORG-040** Generated or third-party files are kept as they come, comments included (`mvnw`, `checkstyle.xml`).
- **ORG-041** Secrets in `.env.example` are placeholders (`your-asaas-sandbox-api-key`) or fixed test values (a base64 test key for the encryption); the real value goes only to the git-ignored `.env`.
- **ORG-042** A provider URL never includes the version path, the gateway adds it (`ASAAS_URL` is `https://api-sandbox.asaas.com`, the gateway calls `/v3/accounts`).
- **ORG-043** A secret in `.env` that may contain `$`, such as the Asaas API key, goes in single quotes (`ASAAS_API_KEY='...'`).
- **ORG-044** Quotes are needed because `service-start.sh` sources the file and Docker Compose interpolates it, and without quotes the value silently becomes empty.
