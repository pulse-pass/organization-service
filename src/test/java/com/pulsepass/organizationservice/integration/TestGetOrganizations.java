package com.pulsepass.organizationservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.common.CnpjGenerator;
import com.pulsepass.organizationservice.common.SetupTools;
import io.restassured.response.Response;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TestGetOrganizations extends BaseIntegrationTest {

    private static final String GET_ORGANIZATIONS = "/organization/organizations";

    @Test
    void listOrganizations_pendingOrganization_returnsItemWithPendingStatus() {
        String legalPersonKey = UUID.randomUUID().toString();
        String documentNumber = CnpjGenerator.generateCnpj();
        String name = "Producoes Exemplo Ltda";
        String organizationKey = SetupTools
                .createOrganization(internalToken, legalPersonKey, documentNumber, name)
                .getString("organization_key");

        Response response = given()
                    .header("selected-agent-key", legalPersonKey)
                .when()
                    .get(GET_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).hasSize(1);
        assertThat(response.jsonPath().getString("data[0].organization_key")).isEqualTo(organizationKey);
        assertThat(response.jsonPath().getString("data[0].legal_person_key")).isEqualTo(legalPersonKey);
        assertThat(response.jsonPath().getString("data[0].name")).isEqualTo(name);
        assertThat(response.jsonPath().getString("data[0].document_number")).isEqualTo(documentNumber);
        assertThat(response.jsonPath().getString("data[0].status")).isEqualTo("PENDING");
        assertThat(response.jsonPath().getString("data[0].rejection_reason")).isNull();
        assertThat(response.jsonPath().getString("data[0].rejection_reason_description")).isNull();
        assertThat(response.jsonPath().getString("data[0].created_at")).isNotBlank();
        assertThat(response.jsonPath().getString("data[0].updated_at")).isNotBlank();
        assertThat(response.jsonPath().getInt("pagination.page")).isEqualTo(1);
        assertThat(response.jsonPath().getInt("pagination.page_size")).isEqualTo(30);
    }

    @Test
    void listOrganizations_otherLegalPersonKey_returnsEmptyList() {
        SetupTools.createOrganization(
                internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(), "Producoes Exemplo Ltda");

        Response response = given()
                    .header("selected-agent-key", UUID.randomUUID().toString())
                .when()
                    .get(GET_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).isEmpty();
        assertThat(response.jsonPath().getInt("pagination.page")).isEqualTo(1);
        assertThat(response.jsonPath().getInt("pagination.page_size")).isEqualTo(30);
    }

    @Test
    void listOrganizations_pageSizeOne_returnsFirstPageAndEmptySecondPage() {
        String legalPersonKey = UUID.randomUUID().toString();
        SetupTools.createOrganization(
                internalToken, legalPersonKey, CnpjGenerator.generateCnpj(), "Producoes Exemplo Ltda");

        Response firstPage = given()
                    .header("selected-agent-key", legalPersonKey)
                    .queryParam("page", 1)
                    .queryParam("page_size", 1)
                .when()
                    .get(GET_ORGANIZATIONS)
                .then()
                    .extract().response();
        Response secondPage = given()
                    .header("selected-agent-key", legalPersonKey)
                    .queryParam("page", 2)
                    .queryParam("page_size", 1)
                .when()
                    .get(GET_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(firstPage.statusCode()).isEqualTo(200);
        assertThat(firstPage.jsonPath().getList("data")).hasSize(1);
        assertThat(firstPage.jsonPath().getInt("pagination.page")).isEqualTo(1);
        assertThat(firstPage.jsonPath().getInt("pagination.page_size")).isEqualTo(1);
        assertThat(secondPage.statusCode()).isEqualTo(200);
        assertThat(secondPage.jsonPath().getList("data")).isEmpty();
        assertThat(secondPage.jsonPath().getInt("pagination.page")).isEqualTo(2);
        assertThat(secondPage.jsonPath().getInt("pagination.page_size")).isEqualTo(1);
    }

    @Test
    void listOrganizations_missingSelectedAgentKey_returnsUnauthorized() {
        Response response = given()
                .when()
                    .get(GET_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
    }

    @Test
    void listOrganizations_emptySelectedAgentKey_returnsUnauthorized() {
        Response response = given()
                    .header("selected-agent-key", "")
                .when()
                    .get(GET_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
    }

    @Test
    void listOrganizations_nonNumericPage_returnsBadRequest() {
        Response response = given()
                    .header("selected-agent-key", UUID.randomUUID().toString())
                    .queryParam("page", "abc")
                .when()
                    .get(GET_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactly(Map.of("field", "page", "error", "INVALID_FORMAT"));
    }

    @Test
    void listOrganizations_pageSizeAboveLimit_returnsBadRequest() {
        Response response = given()
                    .header("selected-agent-key", UUID.randomUUID().toString())
                    .queryParam("page_size", "101")
                .when()
                    .get(GET_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactly(Map.of("field", "page_size", "error", "SIZE"));
    }

    @Test
    void listOrganizations_pageSizeAtLimit_returnsOk() {
        Response response = given()
                    .header("selected-agent-key", UUID.randomUUID().toString())
                    .queryParam("page_size", "100")
                .when()
                    .get(GET_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    void listOrganizations_pageZero_returnsBadRequest() {
        Response response = given()
                    .header("selected-agent-key", UUID.randomUUID().toString())
                    .queryParam("page", "0")
                .when()
                    .get(GET_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactly(Map.of("field", "page", "error", "SIZE"));
    }

    @Test
    void listOrganizations_pageSizeZero_returnsBadRequest() {
        Response response = given()
                    .header("selected-agent-key", UUID.randomUUID().toString())
                    .queryParam("page_size", "0")
                .when()
                    .get(GET_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactly(Map.of("field", "page_size", "error", "SIZE"));
    }
}
