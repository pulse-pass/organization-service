package com.pulsepass.organizationservice.common;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

public final class JsonHandlers {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build();

    private JsonHandlers() {
    }

    public static String toJson(Object value) {
        try {
            return JSON_MAPPER.writeValueAsString(value);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Failed to serialize object to JSON", e);
        }
    }
}
