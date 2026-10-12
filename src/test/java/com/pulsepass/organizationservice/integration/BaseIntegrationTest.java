package com.pulsepass.organizationservice.integration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseIntegrationTest {

    protected static final String INTERNAL_TOKEN_HEADER = "internal-token";

    protected static String internalToken;

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = env("API_BASE_URL", "http://localhost");
        RestAssured.port = Integer.parseInt(env("API_PORT", "8082"));
        internalToken = env("INTERNAL_TOKEN", "pulse-pass-internal-token");
    }
}
