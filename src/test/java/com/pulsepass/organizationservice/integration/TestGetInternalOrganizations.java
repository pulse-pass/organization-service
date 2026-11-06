package com.pulsepass.organizationservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.common.CnpjGenerator;
import com.pulsepass.organizationservice.common.SetupTools;
import io.restassured.response.Response;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TestGetInternalOrganizations extends BaseIntegrationTest {

    private static final String GET_INTERNAL_ORGANIZATIONS = "/organization/internal/organizations";

    @Test
    void listInternalOrganizations_internalToken_returnsItemWithAllFields() {
        String prefix = "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        String legalPersonKey = UUID.randomUUID().toString();
        String documentNumber = CnpjGenerator.generateCnpj();
        String name = prefix + " Producoes Ltda";
        String organizationKey = SetupTools
                .createOrganization(internalToken, legalPersonKey, documentNumber, name)
                .getString("organization_key");

        Response response = given()
                    .header("internal-token", internalToken)
                    .queryParam("name", prefix)
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).hasSize(1);
        assertThat(response.jsonPath().getString("data[0].organization_key")).isEqualTo(organizationKey);
        assertThat(response.jsonPath().getString("data[0].legal_person_key")).isEqualTo(legalPersonKey);
        assertThat(response.jsonPath().getString("data[0].name")).isEqualTo(name);
        assertThat(response.jsonPath().getString("data[0].document_number")).isEqualTo(documentNumber);
        assertThat(response.jsonPath().getString("data[0].organization_status")).isEqualTo("PENDING");
        assertThat(response.jsonPath().getString("data[0].organization_rejection_reason")).isNull();
        assertThat(response.jsonPath().getString("data[0].organization_rejection_reason_description")).isNull();
        assertThat(response.jsonPath().getString("data[0].created_at")).endsWith("Z");
        assertThat(response.jsonPath().getString("data[0].updated_at")).endsWith("Z");
        assertThat(response.jsonPath().getInt("pagination.page")).isEqualTo(1);
        assertThat(response.jsonPath().getInt("pagination.page_size")).isEqualTo(30);
    }

    @Test
    void listInternalOrganizations_adminRoleWithoutToken_returnsItem() {
        String prefix = "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(), prefix + " Ltda")
                .getString("organization_key");

        Response response = given()
                    .header("selected-user-roles", "admin")
                    .queryParam("name", prefix)
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).hasSize(1);
        assertThat(response.jsonPath().getString("data[0].organization_key")).isEqualTo(organizationKey);
    }

    @Test
    void listInternalOrganizations_noTokenAndNoRole_returnsUnauthorized() {
        Response response = given()
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
    }

    @Test
    void listInternalOrganizations_organizerRoleWithoutToken_returnsUnauthorized() {
        Response response = given()
                    .header("selected-user-roles", "organizer")
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
    }

    @Test
    void listInternalOrganizations_organizationStatusPending_returnsOnlyPendingOrganizations() {
        String prefix = "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(), prefix + " Ltda")
                .getString("organization_key");

        Response pending = given()
                    .header("internal-token", internalToken)
                    .queryParam("organization_status", "PENDING")
                    .queryParam("name", prefix)
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();
        Response approved = given()
                    .header("internal-token", internalToken)
                    .queryParam("organization_status", "APPROVED")
                    .queryParam("name", prefix)
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(pending.statusCode()).isEqualTo(200);
        assertThat(pending.jsonPath().getList("data")).hasSize(1);
        assertThat(pending.jsonPath().getString("data[0].organization_key")).isEqualTo(organizationKey);
        assertThat(pending.jsonPath().getString("data[0].organization_status")).isEqualTo("PENDING");
        assertThat(approved.statusCode()).isEqualTo(200);
        assertThat(approved.jsonPath().getList("data")).isEmpty();
    }

    @Test
    void listInternalOrganizations_organizationStatusError_returnsOnlyErrorOrganizations() {
        String prefix = "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        String legalPersonKey = UUID.randomUUID().toString();
        String errorOrganizationKey = SetupTools
                .createOrganization(
                        internalToken, legalPersonKey, CnpjGenerator.generateCnpj(), prefix + " Com Erro Ltda")
                .getString("organization_key");
        SetupTools.createOrganization(
                internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(), prefix + " Pendente Ltda");
        SetupTools.approveOrganizationWithError(internalToken, legalPersonKey, errorOrganizationKey);

        Response error = given()
                    .header("internal-token", internalToken)
                    .queryParam("organization_status", "ERROR")
                    .queryParam("name", prefix)
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();
        Response all = given()
                    .header("internal-token", internalToken)
                    .queryParam("name", prefix)
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(error.statusCode()).isEqualTo(200);
        assertThat(error.jsonPath().getList("data")).hasSize(1);
        assertThat(error.jsonPath().getString("data[0].organization_key")).isEqualTo(errorOrganizationKey);
        assertThat(error.jsonPath().getString("data[0].organization_status")).isEqualTo("ERROR");
        assertThat(all.jsonPath().getList("data")).hasSize(2);
    }

    @Test
    void listInternalOrganizations_unknownOrganizationStatus_returns400WithInvalidFormat() {
        Response response = given()
                    .header("internal-token", internalToken)
                    .queryParam("organization_status", "FOO")
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getString("extra_fields[0].field")).isEqualTo("organization_status");
        assertThat(response.jsonPath().getString("extra_fields[0].error")).isEqualTo("INVALID_FORMAT");
    }

    @Test
    void listInternalOrganizations_nameByPrefix_returnsOrganizationIgnoringCase() {
        String prefix = "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken,
                        UUID.randomUUID().toString(),
                        CnpjGenerator.generateCnpj(),
                        prefix + " Producoes Ltda")
                .getString("organization_key");

        Response response = given()
                    .header("internal-token", internalToken)
                    .queryParam("name", prefix.toUpperCase())
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).hasSize(1);
        assertThat(response.jsonPath().getString("data[0].organization_key")).isEqualTo(organizationKey);
    }

    @Test
    void listInternalOrganizations_documentNumberExact_returnsOnlyThatOrganization() {
        String prefix = "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        String documentNumber = CnpjGenerator.generateCnpj();
        SetupTools.createOrganization(
                internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(), prefix + " Outra Ltda");
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), documentNumber, prefix + " Ltda")
                .getString("organization_key");

        Response response = given()
                    .header("internal-token", internalToken)
                    .queryParam("document_number", documentNumber)
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).hasSize(1);
        assertThat(response.jsonPath().getString("data[0].organization_key")).isEqualTo(organizationKey);
    }

    @Test
    void listInternalOrganizations_documentNumberPartial_returnsEmptyList() {
        String documentNumber = CnpjGenerator.generateCnpj();
        SetupTools.createOrganization(
                internalToken,
                UUID.randomUUID().toString(),
                documentNumber,
                "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11) + " Ltda");

        Response response = given()
                    .header("internal-token", internalToken)
                    .queryParam("document_number", documentNumber.substring(0, 12))
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).isEmpty();
    }

    @Test
    void listInternalOrganizations_nameWithMiddleOfName_returnsEmptyList() {
        String prefix = "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        SetupTools.createOrganization(
                internalToken,
                UUID.randomUUID().toString(),
                CnpjGenerator.generateCnpj(),
                prefix + " Producoes Ltda");

        Response response = given()
                    .header("internal-token", internalToken)
                    .queryParam("name", prefix.substring(4) + " Producoes")
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).isEmpty();
    }

    @Test
    void listInternalOrganizations_nameAndDocumentNumberOfDifferentOrganizations_returnsEmptyList() {
        String prefix = "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        String otherDocumentNumber = CnpjGenerator.generateCnpj();
        SetupTools.createOrganization(
                internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(), prefix + " Ltda");
        SetupTools.createOrganization(
                internalToken,
                UUID.randomUUID().toString(),
                otherDocumentNumber,
                "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11) + " Ltda");

        Response response = given()
                    .header("internal-token", internalToken)
                    .queryParam("name", prefix)
                    .queryParam("document_number", otherDocumentNumber)
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).isEmpty();
    }

    @Test
    void listInternalOrganizations_nameAndDocumentNumberOfSameOrganization_returnsOrganization() {
        String prefix = "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        String documentNumber = CnpjGenerator.generateCnpj();
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), documentNumber, prefix + " Ltda")
                .getString("organization_key");

        Response response = given()
                    .header("internal-token", internalToken)
                    .queryParam("name", prefix)
                    .queryParam("document_number", documentNumber)
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).hasSize(1);
        assertThat(response.jsonPath().getString("data[0].organization_key")).isEqualTo(organizationKey);
    }

    @Test
    void listInternalOrganizations_nameWithWildcardCharacters_treatsThemAsLiteralText() {
        String prefix = "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        SetupTools.createOrganization(
                internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(), prefix + " Ltda");

        Response response = given()
                    .header("internal-token", internalToken)
                    .queryParam("name", "%" + prefix)
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).isEmpty();
    }

    @Test
    void listInternalOrganizations_nameWithUnderscore_matchesLiteralUnderscoreOnly() {
        String prefix = "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        SetupTools.createOrganization(
                internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(), prefix + "-x");
        String underscoreKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(), prefix + "_x")
                .getString("organization_key");

        Response response = given()
                    .header("internal-token", internalToken)
                    .queryParam("name", prefix + "_x")
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).hasSize(1);
        assertThat(response.jsonPath().getString("data[0].organization_key")).isEqualTo(underscoreKey);
    }

    @Test
    void listInternalOrganizations_nameWithBackslash_matchesLiteralBackslash() {
        String prefix = "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        SetupTools.createOrganization(
                internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(), prefix + "x");
        String backslashKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(), prefix + "\\x")
                .getString("organization_key");

        Response response = given()
                    .header("internal-token", internalToken)
                    .queryParam("name", prefix + "\\x")
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).hasSize(1);
        assertThat(response.jsonPath().getString("data[0].organization_key")).isEqualTo(backslashKey);
    }

    @Test
    void listInternalOrganizations_pageSizeOne_returnsNewestFirstAndThenTheOlder() {
        String prefix = "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        String olderKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(), prefix + " Antiga")
                .getString("organization_key");
        String newerKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(), prefix + " Nova")
                .getString("organization_key");

        Response firstPage = given()
                    .header("internal-token", internalToken)
                    .queryParam("name", prefix)
                    .queryParam("page", 1)
                    .queryParam("page_size", 1)
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();
        Response secondPage = given()
                    .header("internal-token", internalToken)
                    .queryParam("name", prefix)
                    .queryParam("page", 2)
                    .queryParam("page_size", 1)
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();
        Response thirdPage = given()
                    .header("internal-token", internalToken)
                    .queryParam("name", prefix)
                    .queryParam("page", 3)
                    .queryParam("page_size", 1)
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(firstPage.statusCode()).isEqualTo(200);
        assertThat(firstPage.jsonPath().getList("data")).hasSize(1);
        assertThat(firstPage.jsonPath().getString("data[0].organization_key")).isEqualTo(newerKey);
        assertThat(firstPage.jsonPath().getInt("pagination.page")).isEqualTo(1);
        assertThat(firstPage.jsonPath().getInt("pagination.page_size")).isEqualTo(1);
        assertThat(secondPage.statusCode()).isEqualTo(200);
        assertThat(secondPage.jsonPath().getList("data")).hasSize(1);
        assertThat(secondPage.jsonPath().getString("data[0].organization_key")).isEqualTo(olderKey);
        assertThat(thirdPage.statusCode()).isEqualTo(200);
        assertThat(thirdPage.jsonPath().getList("data")).isEmpty();
    }

    @Test
    void listInternalOrganizations_pageSizeAboveLimit_returnsBadRequest() {
        Response response = given()
                    .header("internal-token", internalToken)
                    .queryParam("page_size", "101")
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactly(Map.of("field", "page_size", "error", "SIZE"));
    }

    @Test
    void listInternalOrganizations_pageSizeZero_returnsBadRequest() {
        Response response = given()
                    .header("internal-token", internalToken)
                    .queryParam("page_size", "0")
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactly(Map.of("field", "page_size", "error", "SIZE"));
    }

    @Test
    void listInternalOrganizations_pageZero_returnsBadRequest() {
        Response response = given()
                    .header("internal-token", internalToken)
                    .queryParam("page", "0")
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactly(Map.of("field", "page", "error", "SIZE"));
    }

    @Test
    void listInternalOrganizations_pageSizeAtLimit_returnsOk() {
        Response response = given()
                    .header("internal-token", internalToken)
                    .queryParam("page_size", "100")
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("pagination.page_size")).isEqualTo(100);
    }
}
