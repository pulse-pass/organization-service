package com.pulsepass.organizationservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.common.CnpjGenerator;
import com.pulsepass.organizationservice.common.SetupTools;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TestPatchUpdateInternalOrganization extends BaseIntegrationTest {

    private static final String PATCH_ORGANIZATION = "/organization/internal/organizations/{organization_key}";

    @Test
    void updateOrganization_approvePending_returnsApprovedOrganization() {
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");

        Map<String, Object> expectedBody = new HashMap<>();
        expectedBody.put("organization_key", organizationKey);
        expectedBody.put("organization_status", "APPROVED");
        expectedBody.put("organization_rejection_reason", null);
        expectedBody.put("organization_rejection_reason_description", null);

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "APPROVED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getMap("$")).isEqualTo(expectedBody);
    }

    @Test
    void updateOrganization_rejectPendingWithReason_returnsRejectedOrganizationWithReason() {
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "organization_status", "REJECTED",
                            "organization_rejection_reason", "INACTIVE_DOCUMENT"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getMap("$")).containsOnlyKeys(
                "organization_key", "organization_status", "organization_rejection_reason",
                "organization_rejection_reason_description");
        assertThat(response.jsonPath().getString("organization_key")).isEqualTo(organizationKey);
        assertThat(response.jsonPath().getString("organization_status")).isEqualTo("REJECTED");
        assertThat(response.jsonPath().getString("organization_rejection_reason")).isEqualTo("INACTIVE_DOCUMENT");
        assertThat(response.jsonPath().getString("organization_rejection_reason_description")).isNotBlank();
    }

    @Test
    void updateOrganization_adminRoleWithoutToken_returnsApprovedOrganization() {
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");

        Response response = given()
                    .header("selected-user-roles", "admin")
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "APPROVED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("organization_status")).isEqualTo("APPROVED");
    }

    @Test
    void updateOrganization_unknownOrganizationKey_returnsNotFound() {
        String organizationKey = UUID.randomUUID().toString();

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "APPROVED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ORG00002");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void updateOrganization_alreadyApproved_returnsUnprocessableContent() {
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");
        Response approveResponse = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "APPROVED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();
        assertThat(approveResponse.statusCode()).isEqualTo(200);

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "APPROVED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ORG00003");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void updateOrganization_alreadyRejected_returnsUnprocessableContent() {
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");
        Response rejectResponse = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "organization_status", "REJECTED",
                            "organization_rejection_reason", "INACTIVE_DOCUMENT"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();
        assertThat(rejectResponse.statusCode()).isEqualTo(200);

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "APPROVED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ORG00003");
    }

    @Test
    void updateOrganization_rejectWithoutReason_returnsUnprocessableContent() {
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "REJECTED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ORG00004");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void updateOrganization_deletedOrganization_returnsUnprocessableContent() {
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");
        Response approveResponse = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "APPROVED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();
        assertThat(approveResponse.statusCode()).isEqualTo(200);
        SetupTools.deleteOrganization(internalToken, organizationKey);

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "APPROVED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ORG00003");
    }

    @Test
    void updateOrganization_rejectWithoutReasonOnApprovedOrganization_returnsStatusNotAllowedFirst() {
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");
        Response approveResponse = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "APPROVED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();
        assertThat(approveResponse.statusCode()).isEqualTo(200);

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "REJECTED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ORG00003");
    }

    @Test
    void updateOrganization_rejectWithNullReason_returnsUnprocessableContent() {
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");
        Map<String, Object> body = new HashMap<>();
        body.put("organization_status", "REJECTED");
        body.put("organization_rejection_reason", null);

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(body)
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ORG00004");
    }

    @Test
    void updateOrganization_approveWithReason_returnsUnprocessableContent() {
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "organization_status", "APPROVED",
                            "organization_rejection_reason", "INACTIVE_DOCUMENT"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ORG00004");
    }

    @Test
    void updateOrganization_missingOrganizationStatus_returnsBadRequest() {
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_rejection_reason", "INACTIVE_DOCUMENT"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactly(Map.of("field", "organization_status", "error", "REQUIRED"));
    }

    @Test
    void updateOrganization_pendingStatus_returnsBadRequest() {
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "PENDING"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactly(Map.of("field", "body", "error", "INVALID_FORMAT"));
    }

    @Test
    void updateOrganization_deletedStatus_returnsBadRequest() {
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "DELETED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactly(Map.of("field", "body", "error", "INVALID_FORMAT"));
    }

    @Test
    void updateOrganization_rejectionReasonOutsideEnum_returnsBadRequest() {
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "organization_status", "REJECTED",
                            "organization_rejection_reason", "OTHER"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactly(Map.of("field", "body", "error", "INVALID_FORMAT"));
    }

    @Test
    void updateOrganization_missingInternalToken_returnsUnauthorized() {
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");

        Response response = given()
                    .header("selected-user-roles", "organizer")
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "APPROVED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
    }
}
