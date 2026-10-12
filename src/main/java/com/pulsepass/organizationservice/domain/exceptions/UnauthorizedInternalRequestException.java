package com.pulsepass.organizationservice.domain.exceptions;

public class UnauthorizedInternalRequestException extends DomainException {

    public UnauthorizedInternalRequestException() {
        super("Missing or invalid internal token");
    }

    @Override
    public String getCode() {
        return "GEN00401";
    }

    @Override
    public String getMessageKey() {
        return "error.unauthorized";
    }
}
