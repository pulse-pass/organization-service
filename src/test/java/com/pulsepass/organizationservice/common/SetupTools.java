package com.pulsepass.organizationservice.common;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;

public final class SetupTools {

    private static final String POST_ORGANIZATION = "/organization/internal/organizations";
    private static final String PATCH_ORGANIZATION = "/organization/internal/organizations/{organization_key}";
    private static final String DELETE_ORGANIZATION = "/organization/internal/organizations/{organization_key}";

    private SetupTools() {
    }

    public static JsonPath createOrganization(
            String internalToken,
            String legalPersonKey,
            String documentNumber,
            String name
    ) {
        Map<String, Object> payload = Map.of(
                "legal_person_key", legalPersonKey,
                "document_number", documentNumber,
                "name", name);

        Response response = given()
                    .header("internal-token", internalToken)
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .post(POST_ORGANIZATION)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(201);
        return response.jsonPath();
    }

    public static JsonPath approveOrganization(
            String internalToken,
            String legalPersonKey,
            String organizationKey
    ) {
        MockTools.clearMocker();
        MockTools.addExpectation("onboarding-service/get-persons", Map.of("person_key", legalPersonKey));
        MockTools.addExpectation("asaas/post-accounts");

        Response response = given()
                    .header("internal-token", internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "APPROVED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        return response.jsonPath();
    }

    public static JsonPath approveOrganizationWithError(
            String internalToken,
            String legalPersonKey,
            String organizationKey
    ) {
        MockTools.clearMocker();
        MockTools.addExpectation("onboarding-service/get-persons", Map.of("person_key", legalPersonKey));
        MockTools.addExpectation("asaas/post-accounts_server_error");

        Response response = given()
                    .header("internal-token", internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "APPROVED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        return response.jsonPath();
    }

    public static JsonPath updateOrganization(
            String internalToken,
            String organizationKey,
            String organizationStatus,
            String organizationRejectionReason
    ) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("organization_status", organizationStatus);
        payload.put("organization_rejection_reason", organizationRejectionReason);

        Response response = given()
                    .header("internal-token", internalToken)
                    .contentType(ContentType.JSON)
                    .body(payload)
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        return response.jsonPath();
    }

    public static void deleteOrganization(String internalToken, String organizationKey) {
        Response response = given()
                    .header("internal-token", internalToken)
                .when()
                    .delete(DELETE_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(204);
    }
}
