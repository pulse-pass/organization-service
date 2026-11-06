package com.pulsepass.organizationservice.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.common.CnpjGenerator;
import com.pulsepass.organizationservice.common.MockTools;
import com.pulsepass.organizationservice.common.SetupTools;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TestPatchUpdateInternalOrganization extends BaseIntegrationTest {

    private static final String PATCH_ORGANIZATION = "/organization/internal/organizations/{organization_key}";
    private static final String GET_INTERNAL_ORGANIZATIONS = "/organization/internal/organizations";
    private static final String GET_ORGANIZATIONS = "/organization/organizations";

    @Test
    void updateOrganization_approvePending_returnsApprovedOrganization() {
        String legalPersonKey = UUID.randomUUID().toString();
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, legalPersonKey, CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");

        Map<String, Object> expectedBody = new HashMap<>();
        expectedBody.put("organization_key", organizationKey);
        expectedBody.put("organization_status", "APPROVED");
        expectedBody.put("organization_rejection_reason", null);
        expectedBody.put("organization_rejection_reason_description", null);
        MockTools.clearMocker();
        MockTools.addExpectation("onboarding-service/get-persons", Map.of("person_key", legalPersonKey));
        MockTools.addExpectation("asaas/post-accounts");

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
        String legalPersonKey = UUID.randomUUID().toString();
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, legalPersonKey, CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");
        MockTools.clearMocker();
        MockTools.addExpectation("onboarding-service/get-persons", Map.of("person_key", legalPersonKey));
        MockTools.addExpectation("asaas/post-accounts");

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
        String legalPersonKey = UUID.randomUUID().toString();
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, legalPersonKey, CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");
        SetupTools.approveOrganization(internalToken, legalPersonKey, organizationKey);

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
        String legalPersonKey = UUID.randomUUID().toString();
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, legalPersonKey, CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");
        SetupTools.approveOrganization(internalToken, legalPersonKey, organizationKey);
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
        String legalPersonKey = UUID.randomUUID().toString();
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, legalPersonKey, CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");
        SetupTools.approveOrganization(internalToken, legalPersonKey, organizationKey);

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

    @Test
    @SuppressWarnings("unchecked")
    void updateOrganization_approvePending_sendsLegalPersonDataToAsaas() {
        String legalPersonKey = UUID.randomUUID().toString();
        String documentNumber = CnpjGenerator.generateCnpj();
        String organizationKey = SetupTools
                .createOrganization(internalToken, legalPersonKey, documentNumber, "Eventos Sul Ltda")
                .getString("organization_key");
        MockTools.clearMocker();
        MockTools.addExpectation(
                "onboarding-service/get-persons",
                Map.of("person_key", legalPersonKey),
                Map.of("payload", Map.ofEntries(
                        Map.entry("name", "Eventos Sul Ltda"),
                        Map.entry("document_number", documentNumber),
                        Map.entry("phone_numbers", List.of(
                                Map.of("country_code", "55", "area_code", "11", "number", "988887777"),
                                Map.of("country_code", "55", "area_code", "51", "number", "999990000"))),
                        Map.entry("legal_person", Map.of(
                                "email", "financeiro@eventossul.com.br",
                                "legal_person_company_type", "MEI",
                                "monthly_revenue_amount", 8500.50,
                                "legal_person_address", Map.of(
                                        "postal_code", "01310100",
                                        "street", "Avenida Paulista",
                                        "number", "1000",
                                        "complement", "Sala 12",
                                        "neighborhood", "Bela Vista"))))));
        MockTools.addExpectation("asaas/post-accounts");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "APPROVED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();
        List<Map<String, Object>> asaasRequests = MockTools.retrieveRequests("POST", "/v3/accounts");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(asaasRequests).hasSize(1);
        Map<String, List<String>> headers = (Map<String, List<String>>) asaasRequests.get(0).get("headers");
        assertThat(headers.get("access_token")).hasSize(1);
        assertThat(headers.get("access_token").get(0)).isNotBlank();
        Map<String, Object> body = (Map<String, Object>) ((Map<String, Object>) asaasRequests.get(0).get("body"))
                .get("json");
        assertThat(body).containsOnlyKeys(
                "name", "email", "loginEmail", "cpfCnpj", "mobilePhone", "companyType", "incomeValue", "address",
                "addressNumber", "complement", "province", "postalCode");
        assertThat(body.get("name")).isEqualTo("Eventos Sul Ltda");
        assertThat(body.get("email")).isEqualTo("financeiro@eventossul.com.br");
        assertThat(body.get("loginEmail")).isEqualTo("financeiro@eventossul.com.br");
        assertThat(body.get("cpfCnpj")).isEqualTo(documentNumber);
        assertThat(body.get("mobilePhone")).isEqualTo("11988887777");
        assertThat(body.get("companyType")).isEqualTo("MEI");
        assertThat(((Number) body.get("incomeValue")).doubleValue()).isEqualTo(8500.50);
        assertThat(body.get("address")).isEqualTo("Avenida Paulista");
        assertThat(body.get("addressNumber")).isEqualTo("1000");
        assertThat(body.get("complement")).isEqualTo("Sala 12");
        assertThat(body.get("province")).isEqualTo("Bela Vista");
        assertThat(body.get("postalCode")).isEqualTo("01310100");
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateOrganization_approvePendingWithoutComplement_omitsComplementFromAsaasBody() {
        String legalPersonKey = UUID.randomUUID().toString();
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, legalPersonKey, CnpjGenerator.generateCnpj(), "Producoes Exemplo Ltda")
                .getString("organization_key");
        MockTools.clearMocker();
        MockTools.addExpectation("onboarding-service/get-persons", Map.of("person_key", legalPersonKey));
        MockTools.addExpectation("asaas/post-accounts");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "APPROVED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();
        List<Map<String, Object>> asaasRequests = MockTools.retrieveRequests("POST", "/v3/accounts");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(asaasRequests).hasSize(1);
        Map<String, Object> body = (Map<String, Object>) ((Map<String, Object>) asaasRequests.get(0).get("body"))
                .get("json");
        assertThat(body).doesNotContainKey("complement");
    }

    @Test
    void updateOrganization_approvePending_responseNeverCarriesApiKey() {
        String prefix = "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        String legalPersonKey = UUID.randomUUID().toString();
        String organizationKey = SetupTools
                .createOrganization(internalToken, legalPersonKey, CnpjGenerator.generateCnpj(), prefix + " Ltda")
                .getString("organization_key");
        String apiKey = "$aact_hmlg_" + UUID.randomUUID();
        MockTools.clearMocker();
        MockTools.addExpectation("onboarding-service/get-persons", Map.of("person_key", legalPersonKey));
        MockTools.addExpectation(
                "asaas/post-accounts",
                Map.of(),
                Map.of("payload", Map.of("accessToken", Map.of("id", UUID.randomUUID().toString(), "apiKey", apiKey))));

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "APPROVED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();
        Response internalListResponse = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .queryParam("name", prefix)
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();
        Response listResponse = given()
                    .header("selected-agent-key", legalPersonKey)
                .when()
                    .get(GET_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.asString()).doesNotContain(apiKey).doesNotContainIgnoringCase("api_key");
        assertThat(internalListResponse.statusCode()).isEqualTo(200);
        assertThat(internalListResponse.jsonPath().getString("data[0].organization_status")).isEqualTo("APPROVED");
        assertThat(internalListResponse.asString()).doesNotContain(apiKey).doesNotContainIgnoringCase("api_key")
                .doesNotContainIgnoringCase("external");
        assertThat(listResponse.statusCode()).isEqualTo(200);
        assertThat(listResponse.jsonPath().getString("data[0].organization_status")).isEqualTo("APPROVED");
        assertThat(listResponse.asString()).doesNotContain(apiKey).doesNotContainIgnoringCase("api_key")
                .doesNotContainIgnoringCase("external");
    }

    @Test
    void updateOrganization_asaasRejectsRequest_returnsBadGatewayAndMarksAsError() {
        String prefix = "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        String legalPersonKey = UUID.randomUUID().toString();
        String organizationKey = SetupTools
                .createOrganization(internalToken, legalPersonKey, CnpjGenerator.generateCnpj(), prefix + " Ltda")
                .getString("organization_key");
        MockTools.clearMocker();
        MockTools.addExpectation("onboarding-service/get-persons", Map.of("person_key", legalPersonKey));
        MockTools.addExpectation("asaas/post-accounts_bad_request");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "APPROVED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();
        Response listResponse = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .queryParam("name", prefix)
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ORG00005");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
        assertThat(response.asString()).doesNotContain("invalid_object");
        assertThat(listResponse.statusCode()).isEqualTo(200);
        assertThat(listResponse.jsonPath().getList("data")).hasSize(1);
        assertThat(listResponse.jsonPath().getString("data[0].organization_key")).isEqualTo(organizationKey);
        assertThat(listResponse.jsonPath().getString("data[0].organization_status")).isEqualTo("ERROR");
    }

    @Test
    void updateOrganization_asaasServerError_returnsBadGatewayAndMarksAsError() {
        String prefix = "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        String legalPersonKey = UUID.randomUUID().toString();
        String organizationKey = SetupTools
                .createOrganization(internalToken, legalPersonKey, CnpjGenerator.generateCnpj(), prefix + " Ltda")
                .getString("organization_key");
        MockTools.clearMocker();
        MockTools.addExpectation("onboarding-service/get-persons", Map.of("person_key", legalPersonKey));
        MockTools.addExpectation("asaas/post-accounts_server_error");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "APPROVED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();
        Response listResponse = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .queryParam("name", prefix)
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ORG00005");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
        assertThat(listResponse.jsonPath().getList("data")).hasSize(1);
        assertThat(listResponse.jsonPath().getString("data[0].organization_status")).isEqualTo("ERROR");
    }

    @Test
    void updateOrganization_onboardingFails_returnsBadGatewayKeepsPendingAndSkipsAsaas() {
        String prefix = "x" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        String legalPersonKey = UUID.randomUUID().toString();
        String organizationKey = SetupTools
                .createOrganization(internalToken, legalPersonKey, CnpjGenerator.generateCnpj(), prefix + " Ltda")
                .getString("organization_key");
        MockTools.clearMocker();
        MockTools.addExpectation(
                "onboarding-service/get-persons_server_error", Map.of("person_key", legalPersonKey));
        MockTools.addExpectation("asaas/post-accounts");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "APPROVED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();
        List<Map<String, Object>> asaasRequests = MockTools.retrieveRequests("POST", "/v3/accounts");
        Response listResponse = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .queryParam("name", prefix)
                .when()
                    .get(GET_INTERNAL_ORGANIZATIONS)
                .then()
                    .extract().response();

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.jsonPath().getString("code")).isEqualTo("ORG00006");
        assertThat(response.jsonPath().getList("extra_fields")).isEmpty();
        assertThat(asaasRequests).isEmpty();
        assertThat(listResponse.jsonPath().getList("data")).hasSize(1);
        assertThat(listResponse.jsonPath().getString("data[0].organization_status")).isEqualTo("PENDING");
    }

    @Test
    void updateOrganization_onboardingFailsThenRecovers_approvesOrganization() {
        String legalPersonKey = UUID.randomUUID().toString();
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, legalPersonKey, CnpjGenerator.generateCnpj(), "Producoes Exemplo Ltda")
                .getString("organization_key");
        MockTools.clearMocker();
        MockTools.addExpectation(
                "onboarding-service/get-persons_server_error", Map.of("person_key", legalPersonKey));
        Response failedResponse = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of("organization_status", "APPROVED"))
                .when()
                    .patch(PATCH_ORGANIZATION, organizationKey)
                .then()
                    .extract().response();
        assertThat(failedResponse.statusCode()).isEqualTo(422);
        MockTools.clearMocker();
        MockTools.addExpectation("onboarding-service/get-persons", Map.of("person_key", legalPersonKey));
        MockTools.addExpectation("asaas/post-accounts");

        Response response = given()
                    .header(INTERNAL_TOKEN_HEADER, internalToken)
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
    void updateOrganization_rejectPending_doesNotCallExternalServices() {
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, UUID.randomUUID().toString(), CnpjGenerator.generateCnpj(),
                        "Producoes Exemplo Ltda")
                .getString("organization_key");
        MockTools.clearMocker();

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
        assertThat(MockTools.retrieveRequests("POST", "/v3/accounts")).isEmpty();
    }

    @Test
    void updateOrganization_errorOrganization_returnsUnprocessableContent() {
        String legalPersonKey = UUID.randomUUID().toString();
        String organizationKey = SetupTools
                .createOrganization(
                        internalToken, legalPersonKey, CnpjGenerator.generateCnpj(), "Producoes Exemplo Ltda")
                .getString("organization_key");
        SetupTools.approveOrganizationWithError(internalToken, legalPersonKey, organizationKey);
        MockTools.clearMocker();
        MockTools.addExpectation("onboarding-service/get-persons", Map.of("person_key", legalPersonKey));
        MockTools.addExpectation("asaas/post-accounts");

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
        assertThat(MockTools.retrieveRequests("POST", "/v3/accounts")).isEmpty();
    }
}
