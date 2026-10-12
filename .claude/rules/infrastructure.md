---
description: Local infrastructure, Docker and pipeline conventions
paths:
  - "infrastructure/**"
  - "Dockerfile"
  - ".github/**"
  - "README.md"
---

# Infrastructure and pipeline

- Polyrepo in the `pulse-pass` GitHub organization, `main` only, PRs from `feature/<ID>-<slug>`, merge commits only (squash and rebase disabled).
- `main` ruleset: active on the default branch, no bypass, blocks deletion and force push, requires a PR (0 approvals, merge method `merge`) and requires **every** CI job to pass (`lint`, `unit-tests`, `integration-tests`, `sonar`, `build`). It can be created with `gh api -X POST repos/pulse-pass/<repo>/rulesets`.
- SonarQube Cloud: organization `pulse-pass` (Free plan), project `pulse-pass_<repo>` with GitHub Actions analysis; `SONAR_TOKEN` is an organization secret shared by all repositories.
- `infrastructure/` holds `docker-compose.yml`, `.env.example`, `database.sql`, `service-start.sh` and `service-stop.sh`; the service has its own MySQL in the compose (no shared local MySQL), a MockServer and the external `pulse-pass` network. The MySQL healthcheck pings over TCP (`mysqladmin ping -h 127.0.0.1`) so it only passes after `database.sql` has run.
- The mode comes from the argument `./infrastructure/service-start.sh [test|development]` (default `test`), never from editing `.env`: each external dependency has a `<X>_TEST_URL` / `<X>_DEVELOPMENT_URL` pair in `.env`, and the script exports `<X>_URL`. The MockServer runs under the `test` profile.
- The start script creates the `pulse-pass` network if missing, copies `.env.example` on first run and tears down the previous environment; `service-stop.sh` removes containers, volumes and locally built images.
- Redis, Kafka, Kafka UI, Keycloak, Mailpit and MinIO live in the `pulse-pass-infrastructure` repository, created only when a task needs it.
- README: running the service only needs Docker (the image builds with its own JDK); Java 21 is only needed to run `./mvnw` (lint and tests) outside the container.
- Workflows: `pull-request.yml` (name `CI`) and `publish.yml` (name `Publish`, never "release"). Third-party actions are pinned by full commit SHA with the version in a comment (Sonar rule S7637); `actions/*` may use tags. The `sonar` job runs with `-Dsonar.qualitygate.wait=true`, so a failed quality gate fails the job and skips `build`.
- Pipeline: PR with lint (Checkstyle) → unit tests (JaCoCo) → integration tests (through `service-start.sh test`) → SonarQube Cloud → image build; on merge, version from the commits, `vX.Y.Z` tag and image on GHCR.
