---
description: MockServer expectations and MockTools
paths:
  - "src/test/**"
---

# Tests with MockServer

- **ORG-217** External services are played by the MockServer of the compose, driven by `MockTools` in `src/test/java/.../common/`.
- **ORG-218** `MockTools` offers `clearMocker()`, `addExpectation(mockCode)`, `addExpectation(mockCode, pathParameters)`, `addExpectation(mockCode, pathParameters, expectationChanges)` and `retrieveRequests(method, path)`.
- **ORG-219** Each external route has a default expectation in `src/test/resources/expectations/<api>/<method>-<name>.json`, in the MockServer format: `httpRequest` with `{param}` placeholders in the path and `httpResponse` with a realistic default body.
- **ORG-220** Other responses of the same route are variants in their own file (`<method>-<name>_<variant>.json`, e.g. `asaas/post-accounts_server_error.json`).
- **ORG-221** `addExpectation` loads the template, sets `pathParameters` on the request and applies `expectationChanges` to the response: `payload` overrides keys of the body, `status_code` the status and `headers` the headers.
- **ORG-222** Every integration test starts with `MockTools.clearMocker()` and registers only the expectations it needs, with the values (ids, keys, tokens) declared in the test itself.
- **ORG-223** No expectations in `@BeforeAll`, none shared between tests, and no `priority` or body matching to tell tests apart.
- **ORG-224** Integration tests run sequentially because they share the MockServer.
- **ORG-225** `SetupTools` methods register on the MockServer the expectations of the route they call, and clear the MockServer before registering their own expectations.
- **ORG-226** A test that uses `SetupTools` calls it first and only then its own `MockTools.clearMocker()`, followed by its expectations (`SetupTools...`, `MockTools.clearMocker()`, `MockTools.addExpectation(...)`), so the setup expectations never answer the calls of the test.
- **ORG-227** `MockTools` has no route-specific methods and no constants other than its configurable address: paths live in the templates, and tests write the external paths inline in `retrieveRequests`.
