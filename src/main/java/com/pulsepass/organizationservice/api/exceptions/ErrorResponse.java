package com.pulsepass.organizationservice.api.exceptions;

public record ErrorResponse(
        String title,
        String message,
        String code
) {
}
