---
description: Log format, Map argument, S5145 and what never to log
paths:
  - "src/main/java/**/*.java"
---

# Logs

- **ORG-161** Logs follow `ClassName.method - message`.
- **ORG-162** The message is literal text; data goes in a single `Map` argument (`logger.error("X.y - Failed to create the user {}", Map.of("person_key", key), e)`), never interpolated in the sentence.
- **ORG-163** The exception is passed as the last argument, never `e.getMessage()`.
- **ORG-164** A log never receives a value that came straight from the request (path variable, query string, body), which Sonar flags as log injection (`javasecurity:S5145`) and fails the quality gate.
- **ORG-165** Log the value of the loaded entity (`organization.getOrganizationKey()`) instead, or leave it out when nothing was loaded yet.
- **ORG-166** In the application layer, every `if` that changes the flow has a log as its first line: `logger.warn` when it refuses the request, `logger.info` when it takes another path.
- **ORG-167** Passwords and tokens never appear in a log or a response, and the log `Map` never includes them.
- **ORG-168** Logs never receive a DTO, schema, response or entity: only the single `Map` with the chosen fields; records do not override `toString`.
- **ORG-169** Documents (CPF, CNPJ) and keys may be logged.
