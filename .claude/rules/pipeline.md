---
description: CI, main ruleset, SonarQube and branch conventions
paths:
  - ".github/**"
---

# Pipeline

- **ORG-204** Polyrepo in the `pulse-pass` GitHub organization, `main` only.
- **ORG-205** PRs come from `feature/<ID>-<slug>`; merge commits only (squash and rebase disabled).
- **ORG-206** `main` ruleset: active on the default branch, no bypass, blocks deletion and force push.
- **ORG-207** The ruleset requires a PR (0 approvals, merge method `merge`).
- **ORG-208** The ruleset requires every CI job to pass (`lint`, `unit-tests`, `integration-tests`, `sonar`, `build`).
- **ORG-209** It can be created with `gh api -X POST repos/pulse-pass/<repo>/rulesets`.
- **ORG-210** SonarQube Cloud: organization `pulse-pass` (Free plan), project `pulse-pass_<repo>` with GitHub Actions analysis.
- **ORG-211** `SONAR_TOKEN` is an organization secret shared by all repositories.
- **ORG-212** Workflows: `pull-request.yml` (name `CI`) and `publish.yml` (name `Publish`, never "release").
- **ORG-213** Third-party actions are pinned by full commit SHA (Sonar rule S7637) with the version in a comment; `actions/*` may use tags. That version is the only comment allowed in workflows.
- **ORG-214** The `sonar` job runs with `-Dsonar.qualitygate.wait=true`, so a failed quality gate fails the job and skips `build`.
- **ORG-215** Pipeline: PR with lint (Checkstyle) → unit tests (JaCoCo) → integration tests (through `service-start.sh test`) → SonarQube Cloud → image build.
- **ORG-216** On merge: version from the commits, `vX.Y.Z` tag and image on GHCR.
