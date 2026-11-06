---
description: External gateways: structure, RestClient, DTOs, failures and secrets
paths:
  - "**/domain/gateways/**"
  - "**/infrastructure/gateways/**"
  - "**/application/**"
  - "**/domain/exceptions/**"
  - "src/main/resources/application*.properties"
---

# Gateways

## Structure and naming

- **ORG-134** Nothing outside `infrastructure/` carries the name of an external provider, and domain exceptions and message keys describe the capability (`PaymentAccountCreationException`, `PersonRetrievalException`), never who implements it.
- **ORG-135** One gateway per external system, not per resource: all calls to a system go through the same class.
- **ORG-136** The domain interface is named after the role or the internal service (`PaymentProviderGateway`, `OnboardingGateway`).
- **ORG-137** Each gateway has its own folder in `domain/gateways/<gateway>/` (named after the gateway, not the provider), holding the interface and a `dtos/` folder (e.g. `paymentprovider/PaymentProviderGateway`, `paymentprovider/dtos/CreatePaymentAccountInputDTO`).
- **ORG-138** The interface receives and returns those DTOs, named after the operation, never loose parameters.
- **ORG-139** The implementation lives in `infrastructure/gateways/<provider>/`, carries the name of the system, one class per system (`AsaasGateway`, `OnboardingServiceGateway`), and uses `RestClient`.
- **ORG-140** Request and response bodies are typed records in `infrastructure/gateways/<provider>/dtos` (`<Provider><Operation>Request` / `Response`) with `@JsonProperty` on every field; no `Map` bodies.

## Configuration

- **ORG-141** A service that calls another through a gateway has the `spring-boot-starter-restclient` dependency (Spring Boot 4 does not auto-configure `RestClient.Builder` without it).
- **ORG-142** Timeouts come from `spring.http.clients.connect-timeout` and `spring.http.clients.read-timeout` in `application.properties`, which Spring Boot applies to the auto-configured `RestClient.Builder`.
- **ORG-143** Gateways receive the builder injected and never build a request factory by hand.

## Calls and failures

- **ORG-145** Each public operation of a gateway makes its own main HTTP call inline (request, `try`, status and body checks, mapping of the response), even when that duplicates code across operations.
- **ORG-146** Private methods are only for auxiliary steps that are not the operation itself (a token or role lookup, small predicates such as `isInvalidGrant`).
- **ORG-147** The checks of a response body shared by several operations may be a private `validate*` method that only validates and returns nothing; the mapping to the output DTO stays inline in each operation. This keeps the new-code duplication below the 3% of the SonarQube quality gate.
- **ORG-148** A gateway checks the transport failure, the response status and an empty body, and nothing else: it never validates the fields of a response one by one (`body.id() == null`), and it trusts the contract of the system it calls.
- **ORG-149** A transport failure or unexpected status in the operation itself is logged in the gateway and becomes a domain exception for the operation, mapped to 422 (`HttpStatus.UNPROCESSABLE_CONTENT`, not the deprecated `UNPROCESSABLE_ENTITY`) in the `STATUS_MAP`, never to a 5xx.
- **ORG-151** Each `try` wraps only the HTTP call (one `try` per call); status and body checks come after it.
- **ORG-152** Every gateway call in a service is inside a `try` with a `catch (Exception ex)`: any failure of the call is logged and becomes the domain exception of the operation, so it never leaves as a 500.
- **ORG-153** A domain exception with a different meaning for the client gets its own `catch` before it and is rethrown.
- **ORG-154** A provider that does not use snake_case (e.g. Asaas, camelCase) has request and response records with an explicit `@JsonProperty` on every field, which wins over `spring.jackson.property-naming-strategy`.
- **ORG-155** Response records of such a provider carry `@JsonIgnoreProperties(ignoreUnknown = true)` and only the fields the service uses; an optional request field is omitted when null with `@JsonInclude(JsonInclude.Include.NON_NULL)`.
- **ORG-156** A gateway whose response or request carries a secret (API key, token) never logs the response body nor passes the exception to the logger, because `RestClientResponseException` messages embed the body.
- **ORG-157** Such a gateway logs only the `Map` with the key of the resource, the HTTP status, the error codes and the exception class name.
- **ORG-158** A flow that calls a gateway inside the transaction (`begin()`/`commit()`) calls it after the lock and the validations.
- **ORG-159** When only the call to the provider failed and the failure must be persisted (e.g. status `ERROR`), the service catches `Exception` around that call, saves the status and its history, commits and throws the domain exception of the operation.
- **ORG-160** When nothing was written before the failure, the exception just propagates and the `cleanup()` rolls back.
