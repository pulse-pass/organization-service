package com.pulsepass.organizationservice.domain.exceptions;

public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    public abstract String getCode();

    public abstract String getMessageKey();
}
