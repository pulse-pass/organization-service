package com.pulsepass.organizationservice.common;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.Map;

public final class SetupTools {

    private static final String POST_ORGANIZATION = "/organization/internal/organizations";

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
}
