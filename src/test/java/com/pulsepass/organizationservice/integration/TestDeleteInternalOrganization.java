package com.pulsepass.organizationservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.common.CnpjGenerator;
import com.pulsepass.organizationservice.common.SetupTools;
import io.restassured.response.Response;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TestDeleteInternalOrganization extends BaseIntegrationTest {

    private static final String DELETE_ORGANIZATION = "/organization/internal/organizations/{organization_key}";
    private static final String GET_INTERNAL_ORGANIZATIONS = "/organization/internal/organizations";

    @Test
    void deleteOrganization_approvedOrganization_returnsNoContentAndMarksAsDeleted() {
        String prefix = "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(), prefix + " Ltda")
                .getString("organization_key");
        SetupTools.updateOrganization(internalToken, organizationKey, "APPROVED", null);

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                .when()
                    .delete(DELETE_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();
        Response listResponse = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .queryParam("name", prefix)
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(204);
        assertThat(response.body().asString()).isEmpty();
        assertThat(listResponse.statusCode()).isEqualTo(200);
        assertThat(listResponse.jsonPath().getList("data")).hasSize(1);
        assertThat(listResponse.jsonPath().getString("data[0].organization_key")).isEqualTo(organizationKey);
        assertThat(listResponse.jsonPath().getString("data[0].organization_status")).isEqualTo("DELETED");
        assertThat(listResponse.jsonPath().getString("data[0].organization_rejection_reason")).isNull();
    }

    @Test
    void deleteOrganization_adminRoleWithoutToken_returnsNoContent() {
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");
        SetupTools.updateOrganization(internalToken, organizationKey, "APPROVED", null);

        Response response = given()
                    .header("selected-user-roles", "admin")
                .when()
                    .delete(DELETE_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(204);
    }

    @Test
    void deleteOrganization_unknownOrganizationKey_returnsNotFound() {
        String organizationKey = UUID.randomUUID().toString();

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                .when()
                    .delete(DELETE_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ORG00002");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void deleteOrganization_pendingOrganization_returnsUnprocessableContent() {
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                .when()
                    .delete(DELETE_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ORG00003");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
    }

    @Test
    void deleteOrganization_rejectedOrganization_returnsUnprocessableContent() {
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");
        SetupTools.updateOrganization(internalToken, organizationKey, "REJECTED", "INACTIVE_DOCUMENT");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                .when()
                    .delete(DELETE_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ORG00003");
    }

    @Test
    void deleteOrganization_alreadyDeletedOrganization_returnsUnprocessableContent() {
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");
        SetupTools.updateOrganization(internalToken, organizationKey, "APPROVED", null);
        Response firstDeleteResponse = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                .when()
                    .delete(DELETE_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();
        assertThat(firstDeleteResponse.statusCode()).isEqualTo(204);

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                .when()
                    .delete(DELETE_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ORG00003");
    }

    @Test
    void deleteOrganization_missingInternalToken_returnsUnauthorized() {
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");
        SetupTools.updateOrganization(internalToken, organizationKey, "APPROVED", null);

        Response response = given()
                    .header("selected-user-roles", "organizer")
                .when()
                    .delete(DELETE_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
    }
}
