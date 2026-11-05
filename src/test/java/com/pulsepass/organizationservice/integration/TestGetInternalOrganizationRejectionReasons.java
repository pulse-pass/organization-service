package com.pulsepass.organizationservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TestGetInternalOrganizationRejectionReasons extends BaseIntegrationTest {

    private static final String GET_REJECTION_REASONS = "/organization/internal/organization_rejection_reasons";

    @Test
    void listInternalOrganizationRejectionReasons_internalToken_returnsAllReasonsOrderedById() {
        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                .when()
                    .get(GET_REJECTION_REASONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).hasSize(4);
        assertThat(response.jsonPath().getList("data.organization_rejection_reason", String.class))
                .containsExactly(
                        "INACTIVE_DOCUMENT", "INCOMPATIBLE_ACTIVITY", "INCONSISTENT_DATA", "UNVERIFIED_OWNER");
        assertThat(response.jsonPath().getList("data.organization_rejection_reason_description", String.class))
                .doesNotContainNull();
        assertThat(response.jsonPath().getString("data[0].organization_rejection_reason_description")).isNotBlank();
        assertThat(response.jsonPath().getInt("pagination.page")).isEqualTo(1);
        assertThat(response.jsonPath().getInt("pagination.page_size")).isEqualTo(30);
    }

    @Test
    void listInternalOrganizationRejectionReasons_adminRoleWithoutToken_returnsReasons() {
        Response response = given()
                    .header("selected-user-roles", "admin")
                .when()
                    .get(GET_REJECTION_REASONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).hasSize(4);
    }

    @Test
    void listInternalOrganizationRejectionReasons_secondPageOfTwo_returnsLastTwoReasons() {
        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .queryParam("page", 2)
                    .queryParam("page_size", 2)
                .when()
                    .get(GET_REJECTION_REASONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data.organization_rejection_reason", String.class))
                .containsExactly("INCONSISTENT_DATA", "UNVERIFIED_OWNER");
        assertThat(response.jsonPath().getInt("pagination.page")).isEqualTo(2);
        assertThat(response.jsonPath().getInt("pagination.page_size")).isEqualTo(2);
    }

    @Test
    void listInternalOrganizationRejectionReasons_pageBeyondLast_returnsEmptyList() {
        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .queryParam("page", 2)
                    .queryParam("page_size", 4)
                .when()
                    .get(GET_REJECTION_REASONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("data")).isEmpty();
    }

    @Test
    void listInternalOrganizationRejectionReasons_missingInternalToken_returnsUnauthorized() {
        Response response = given()
                .when()
                    .get(GET_REJECTION_REASONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00401");
    }

    @Test
    void listInternalOrganizationRejectionReasons_pageSizeAboveLimit_returnsBadRequest() {
        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .queryParam("page_size", 101)
                .when()
                    .get(GET_REJECTION_REASONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactly(Map.of("field", "page_size", "error", "SIZE"));
    }

    @Test
    void listInternalOrganizationRejectionReasons_pageZero_returnsBadRequest() {
        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .queryParam("page", 0)
                .when()
                    .get(GET_REJECTION_REASONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("code")).isEqualTo("GEN00400");
        assertThat(response.jsonPath().getList("extra_fields", Map.class))
                .containsExactly(Map.of("field", "page", "error", "SIZE"));
    }
}
