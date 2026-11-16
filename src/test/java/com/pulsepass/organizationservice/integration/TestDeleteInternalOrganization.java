package com.pulsepass.organizationservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.common.CnpjGenerator;
import com.pulsepass.organizationservice.common.MockTools;
import com.pulsepass.organizationservice.common.SetupTools;
import io.restassured.response.Response;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TestDeleteInternalOrganization extends BaseIntegrationTest {

    private static final String DELETE_ORGANIZATION = "/organization/internal/organizations/{organization_key}";
    private static final String GET_INTERNAL_ORGANIZATIONS = "/organization/internal/organizations";

    @Test
    void deleteOrganization_approvedOrganization_returnsNoContentAndMarksAsDeleted() {
        String prefix = "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        String legalPersonKey = UUID.randomUUID().toString();
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, legalPersonKey, CnpjGenerator.generateCnpj(), prefix + " Ltda")
                .getString("organization_key");
        SetupTools.approveOrganization(internalToken, legalPersonKey, organizationKey);
        MockTools.clearMocker();
        MockTools.addExpectation(
                "onboarding-service/delete-legal_persons_collaborators",
                Map.of("legal_person_key", legalPersonKey));

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                .when()
                    .delete(DELETE_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();
        var revocationRequests = MockTools.retrieveRequests(
                "DELETE", "/onboarding/internal/legal_persons/" + legalPersonKey + "/collaborators");
        Response listResponse = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .queryParam("name", prefix)
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(204);
        assertThat(response.body().asString()).isEmpty();
        assertThat(revocationRequests).hasSize(1);
        assertThat(listResponse.statusCode()).isEqualTo(200);
        assertThat(listResponse.jsonPath().getList("data")).hasSize(1);
        assertThat(listResponse.jsonPath().getString("data[0].organization_key")).isEqualTo(organizationKey);
        assertThat(listResponse.jsonPath().getString("data[0].organization_status")).isEqualTo("DELETED");
        assertThat(listResponse.jsonPath().getString("data[0].organization_rejection_reason")).isNull();
    }

    @Test
    void deleteOrganization_adminRoleWithoutToken_returnsNoContent() {
        String legalPersonKey = UUID.randomUUID().toString();
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, legalPersonKey, CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");
        SetupTools.approveOrganization(internalToken, legalPersonKey, organizationKey);
        MockTools.clearMocker();
        MockTools.addExpectation(
                "onboarding-service/delete-legal_persons_collaborators",
                Map.of("legal_person_key", legalPersonKey));

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
        MockTools.clearMocker();

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                .when()
                    .delete(DELETE_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();
        var revocationRequests = MockTools.retrieveRequests("DELETE", "/onboarding/internal/legal_persons/.*");

        assertThat(revocationRequests).isEmpty();
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

        MockTools.clearMocker();

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                .when()
                    .delete(DELETE_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();
        var revocationRequests = MockTools.retrieveRequests("DELETE", "/onboarding/internal/legal_persons/.*");

        assertThat(revocationRequests).isEmpty();
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

        MockTools.clearMocker();

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                .when()
                    .delete(DELETE_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();
        var revocationRequests = MockTools.retrieveRequests("DELETE", "/onboarding/internal/legal_persons/.*");

        assertThat(revocationRequests).isEmpty();
        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ORG00003");
    }

    @Test
    void deleteOrganization_alreadyDeletedOrganization_returnsUnprocessableContent() {
        String legalPersonKey = UUID.randomUUID().toString();
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, legalPersonKey, CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");
        SetupTools.approveOrganization(internalToken, legalPersonKey, organizationKey);
        MockTools.clearMocker();
        MockTools.addExpectation(
                "onboarding-service/delete-legal_persons_collaborators",
                Map.of("legal_person_key", legalPersonKey));
        Response firstDeleteResponse = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                .when()
                    .delete(DELETE_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();
        assertThat(firstDeleteResponse.statusCode()).isEqualTo(204);

        MockTools.clearMocker();

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                .when()
                    .delete(DELETE_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(MockTools.retrieveRequests("DELETE", "/onboarding/internal/legal_persons/.*")).isEmpty();
        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ORG00003");
    }

    @Test
    void deleteOrganization_missingInternalToken_returnsUnauthorized() {
        String legalPersonKey = UUID.randomUUID().toString();
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, legalPersonKey, CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");
        SetupTools.approveOrganization(internalToken, legalPersonKey, organizationKey);

        Response response = given()
                    .header("selected-user-roles", "organizer")
                .when()
                    .delete(DELETE_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
    }

    @Test
    void deleteOrganization_errorOrganization_returnsUnprocessableContent() {
        String legalPersonKey = UUID.randomUUID().toString();
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, legalPersonKey, CnpjGenerator.generateCnpj(), "Producoes Exemplo Ltda")
                .getString("organization_key");
        SetupTools.approveOrganizationWithError(internalToken, legalPersonKey, organizationKey);

        MockTools.clearMocker();

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                .when()
                    .delete(DELETE_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();
        var revocationRequests = MockTools.retrieveRequests("DELETE", "/onboarding/internal/legal_persons/.*");

        assertThat(revocationRequests).isEmpty();
        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ORG00003");
    }

    @Test
    void deleteOrganization_revocationFails_returnsUnprocessableContentAndKeepsOrganizationDeleted() {
        String prefix = "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        String legalPersonKey = UUID.randomUUID().toString();
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, legalPersonKey, CnpjGenerator.generateCnpj(), prefix + " Ltda")
                .getString("organization_key");
        SetupTools.approveOrganization(internalToken, legalPersonKey, organizationKey);
        MockTools.clearMocker();
        MockTools.addExpectation(
                "onboarding-service/delete-legal_persons_collaborators_server_error",
                Map.of("legal_person_key", legalPersonKey));

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                .when()
                    .delete(DELETE_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();
        var revocationRequests = MockTools.retrieveRequests(
                "DELETE", "/onboarding/internal/legal_persons/" + legalPersonKey + "/collaborators");
        Response listResponse = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .queryParam("name", prefix)
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(revocationRequests).hasSize(1);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ORG00007");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
        assertThat(listResponse.statusCode()).isEqualTo(200);
        assertThat(listResponse.jsonPath().getList("data")).hasSize(1);
        assertThat(listResponse.jsonPath().getString("data[0].organization_key")).isEqualTo(organizationKey);
        assertThat(listResponse.jsonPath().getString("data[0].organization_status")).isEqualTo("DELETED");
    }
}
