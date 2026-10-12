package com.pulsepass.organizationservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.common.DocumentHandlers;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TestPostCreateOrganization extends BaseIntegrationTest {

    private static final String POST_ORGANIZATION = "/internal/organizations";
    private static final String NAME = "Producoes Exemplo Ltda";

    private static Map<String, Object> payload(String legalPersonKey, String documentNumber, String name) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("legal_person_key", legalPersonKey);
        payload.put("document_number", documentNumber);
        payload.put("name", name);
        return payload;
    }

    private static Map<String, Object> validPayload() {
        return payload(UUID.randomUUID().toString(), DocumentHandlers.generateCnpj(), NAME);
    }

    private static Response post(String token, Object payload) {
        return given()
                    .header(INTERNAL_TOKEN_HEADER, token)
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();
    }

    private static Response post(Object payload) {
        return post(internalToken, payload);
    }

    private static void assertError(Response response, int status, String code) {
        assertThat(response.statusCode()).isEqualTo(status);
        assertThat(response.jsonPath().getString("code")).isEqualTo(code);
    }

    @Test
    void createOrganization_validPayload_returnsCreatedWithPendingStatus() {
        String legalPersonKey = UUID.randomUUID().toString();
        String documentNumber = DocumentHandlers.generateCnpj();

        Response response = post(payload(legalPersonKey, documentNumber, NAME));

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(UUID.fromString(response.jsonPath().getString("organization_key"))).isNotNull();
        assertThat(response.jsonPath().getString("legal_person_key")).isEqualTo(legalPersonKey);
        assertThat(response.jsonPath().getString("document_number")).isEqualTo(documentNumber);
        assertThat(response.jsonPath().getString("name")).isEqualTo(NAME);
        assertThat(response.jsonPath().getString("organization_status")).isEqualTo("PENDING");
    }

    @Test
    void createOrganization_duplicatedLegalPersonKey_returnsConflict() {
        String legalPersonKey = UUID.randomUUID().toString();
        assertThat(post(payload(legalPersonKey, DocumentHandlers.generateCnpj(), NAME)).statusCode()).isEqualTo(201);

        Response response = post(payload(legalPersonKey, DocumentHandlers.generateCnpj(), NAME));

        assertError(response, 409, "ORG00001");
    }

    @Test
    void createOrganization_duplicatedDocumentNumber_returnsConflict() {
        String documentNumber = DocumentHandlers.generateCnpj();
        assertThat(post(payload(UUID.randomUUID().toString(), documentNumber, NAME)).statusCode()).isEqualTo(201);

        Response response = post(payload(UUID.randomUUID().toString(), documentNumber, NAME));

        assertError(response, 409, "ORG00001");
    }

    @Test
    void createOrganization_missingLegalPersonKey_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        payload.remove("legal_person_key");

        assertError(post(payload), 400, "GEN00400");
    }

    @Test
    void createOrganization_emptyLegalPersonKey_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        payload.put("legal_person_key", "");

        assertError(post(payload), 400, "GEN00400");
    }

    @Test
    void createOrganization_nonUuidLegalPersonKey_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        payload.put("legal_person_key", "not-a-uuid");

        assertError(post(payload), 400, "GEN00400");
    }

    @Test
    void createOrganization_nilLegalPersonKey_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        payload.put("legal_person_key", "00000000-0000-0000-0000-000000000000");

        assertError(post(payload), 400, "GEN00400");
    }

    @Test
    void createOrganization_missingDocumentNumber_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        payload.remove("document_number");

        assertError(post(payload), 400, "GEN00400");
    }

    @Test
    void createOrganization_documentNumberWithThirteenDigits_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        payload.put("document_number", DocumentHandlers.generateCnpj().substring(0, 13));

        assertError(post(payload), 400, "GEN00400");
    }

    @Test
    void createOrganization_documentNumberWithLetters_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        payload.put("document_number", "1234567800019A");

        assertError(post(payload), 400, "GEN00400");
    }

    @Test
    void createOrganization_missingName_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        payload.remove("name");

        assertError(post(payload), 400, "GEN00400");
    }

    @Test
    void createOrganization_emptyName_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        payload.put("name", "");

        assertError(post(payload), 400, "GEN00400");
    }

    @Test
    void createOrganization_nameWith256Characters_returnsBadRequest() {
        Map<String, Object> payload = validPayload();
        payload.put("name", "a".repeat(256));

        assertError(post(payload), 400, "GEN00400");
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

        assertError(response, 400, "GEN00400");
    }

    @Test
    void createOrganization_missingInternalToken_returnsUnauthorized() {
        Response response = given()
                    .contentType(ContentType.JSON)
                    .body(validPayload())
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();

        assertError(response, 401, "GEN00401");
    }

    @Test
    void createOrganization_wrongInternalToken_returnsUnauthorized() {
        Response response = post("wrong-token", validPayload());

        assertError(response, 401, "GEN00401");
    }
}
