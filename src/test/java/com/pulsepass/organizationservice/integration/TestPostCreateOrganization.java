package com.pulsepass.organizationservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.common.CnpjGenerator;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TestPostCreateOrganization extends BaseIntegrationTest {

    private static final String POST_ORGANIZATION = "/organization/internal/organizations";

    @Test
    void createOrganization_validPayload_returnsCreatedWithPendingStatus() {
        String legalPersonKey = UUID.randomUUID().toString();
        String documentNumber = CnpjGenerator.generateCnpj();
        String name = "Producoes Exemplo Ltda";
        Map<String, Object> payload = new HashMap<>();
        payload.put("legal_person_key", legalPersonKey);
        payload.put("document_number", documentNumber);
        payload.put("name", name);

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(UUID.fromString(response.jsonPath().getString("organization_key"))).isNotNull();
        assertThat(response.jsonPath().getString("legal_person_key")).isEqualTo(legalPersonKey);
        assertThat(response.jsonPath().getString("document_number")).isEqualTo(documentNumber);
        assertThat(response.jsonPath().getString("name")).isEqualTo(name);
        assertThat(response.jsonPath().getString("organization_status")).isEqualTo("PENDING");
    }

    @Test
    void createOrganization_duplicatedLegalPersonKey_returnsConflict() {
        String legalPersonKey = UUID.randomUUID().toString();
        Map<String, Object> firstPayload = new HashMap<>();
        firstPayload.put("legal_person_key", legalPersonKey);
        firstPayload.put("document_number", CnpjGenerator.generateCnpj());
        firstPayload.put("name", "Producoes Exemplo Ltda");
        Response firstResponse = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(firstPayload)
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();
        assertThat(firstResponse.statusCode()).isEqualTo(201);
        Map<String, Object> payload = new HashMap<>();
        payload.put("legal_person_key", legalPersonKey);
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Producoes Exemplo Ltda");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(409);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ORG00001");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void createOrganization_duplicatedDocumentNumber_returnsConflict() {
        String documentNumber = CnpjGenerator.generateCnpj();
        Map<String, Object> firstPayload = new HashMap<>();
        firstPayload.put("legal_person_key", UUID.randomUUID().toString());
        firstPayload.put("document_number", documentNumber);
        firstPayload.put("name", "Producoes Exemplo Ltda");
        Response firstResponse = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(firstPayload)
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();
        assertThat(firstResponse.statusCode()).isEqualTo(201);
        Map<String, Object> payload = new HashMap<>();
        payload.put("legal_person_key", UUID.randomUUID().toString());
        payload.put("document_number", documentNumber);
        payload.put("name", "Producoes Exemplo Ltda");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(409);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ORG00001");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void createOrganization_missingLegalPersonKey_returnsBadRequest() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Producoes Exemplo Ltda");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactlyInAnyOrder(
                Map.of("field", "legal_person_key", "error", "REQUIRED"));
    }

    @Test
    void createOrganization_emptyLegalPersonKey_returnsBadRequest() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Producoes Exemplo Ltda");
        payload.put("legal_person_key", "");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactlyInAnyOrder(
                Map.of("field", "legal_person_key", "error", "REQUIRED"),
                Map.of("field", "legal_person_key", "error", "INVALID_FORMAT"));
    }

    @Test
    void createOrganization_nonUuidLegalPersonKey_returnsBadRequest() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Producoes Exemplo Ltda");
        payload.put("legal_person_key", "not-a-uuid");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactlyInAnyOrder(
                Map.of("field", "legal_person_key", "error", "INVALID_FORMAT"));
    }

    @Test
    void createOrganization_nilLegalPersonKey_returnsBadRequest() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Producoes Exemplo Ltda");
        payload.put("legal_person_key", "00000000-0000-0000-0000-000000000000");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactlyInAnyOrder(
                Map.of("field", "legal_person_key", "error", "INVALID_FORMAT"));
    }

    @Test
    void createOrganization_missingDocumentNumber_returnsBadRequest() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("legal_person_key", UUID.randomUUID().toString());
        payload.put("name", "Producoes Exemplo Ltda");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactlyInAnyOrder(
                Map.of("field", "document_number", "error", "REQUIRED"));
    }

    @Test
    void createOrganization_documentNumberWithThirteenDigits_returnsBadRequest() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("legal_person_key", UUID.randomUUID().toString());
        payload.put("name", "Producoes Exemplo Ltda");
        payload.put("document_number", CnpjGenerator.generateCnpj().substring(0, 13));

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactlyInAnyOrder(
                Map.of("field", "document_number", "error", "INVALID_FORMAT"));
    }

    @Test
    void createOrganization_documentNumberWithLetters_returnsBadRequest() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("legal_person_key", UUID.randomUUID().toString());
        payload.put("name", "Producoes Exemplo Ltda");
        payload.put("document_number", "1234567800019A");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactlyInAnyOrder(
                Map.of("field", "document_number", "error", "INVALID_FORMAT"));
    }

    @Test
    void createOrganization_missingName_returnsBadRequest() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("legal_person_key", UUID.randomUUID().toString());
        payload.put("document_number", CnpjGenerator.generateCnpj());

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactlyInAnyOrder(
                Map.of("field", "name", "error", "REQUIRED"));
    }

    @Test
    void createOrganization_emptyName_returnsBadRequest() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("legal_person_key", UUID.randomUUID().toString());
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactlyInAnyOrder(
                Map.of("field", "name", "error", "REQUIRED"));
    }

    @Test
    void createOrganization_nameWith256Characters_returnsBadRequest() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("legal_person_key", UUID.randomUUID().toString());
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "a".repeat(256));

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactlyInAnyOrder(
                Map.of("field", "name", "error", "SIZE"));
    }

    @Test
    void createOrganization_malformedJson_returnsBadRequest() {
        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body("{\"legal_person_key\": ")
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactlyInAnyOrder(
                Map.of("field", "body", "error", "INVALID_FORMAT"));
    }

    @Test
    void createOrganization_getOnInternalRoute_returnsMethodNotAllowed() {
        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                .when()
                    .get(POST_ORGANIZATION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(405);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00405");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void createOrganization_textPlainContentType_returnsUnsupportedMediaType() {
        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.TEXT)
                    .body("text")
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(415);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00415");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void createOrganization_unknownRoute_returnsNotFound() {
        Response response = given()
                .when()
                    .get("/rota_inexistente")
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00404");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void createOrganization_missingInternalToken_returnsUnauthorized() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("legal_person_key", UUID.randomUUID().toString());
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Producoes Exemplo Ltda");

        Response response = given()
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void createOrganization_wrongInternalToken_returnsUnauthorized() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("legal_person_key", UUID.randomUUID().toString());
        payload.put("document_number", CnpjGenerator.generateCnpj());
        payload.put("name", "Producoes Exemplo Ltda");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, "wrong-token")
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }
}
