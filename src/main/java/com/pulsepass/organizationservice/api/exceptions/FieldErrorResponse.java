package com.pulsepass.organizationservice.api.exceptions;

import com.pulsepass.organizationservice.common.JsonHandlers;
import java.util.Map;
import org.springframework.validation.FieldError;

public record FieldErrorResponse(
        String field,
        String error
) {

    private static final Map<String, String> ERRORS = Map.of(
            "NotBlank", "REQUIRED",
            "NotNull", "REQUIRED",
            "NotEmpty", "REQUIRED",
            "Pattern", "INVALID_FORMAT",
            "Email", "INVALID_FORMAT",
            "Size", "SIZE"
    );

    public static FieldErrorResponse fromFieldError(FieldError fieldError) {
        return new FieldErrorResponse(
                JsonHandlers.toSnakeCase(fieldError.getField()),
                ERRORS.getOrDefault(fieldError.getCode(), "INVALID_FORMAT")
        );
    }
}
