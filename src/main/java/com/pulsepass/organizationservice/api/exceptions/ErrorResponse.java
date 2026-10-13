package com.pulsepass.organizationservice.api.exceptions;

import java.util.List;

public record ErrorResponse(
        String title,
        String message,
        String code,
        List<FieldErrorResponse> extraFields
) {
}
