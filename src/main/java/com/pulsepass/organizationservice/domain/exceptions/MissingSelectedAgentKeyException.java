package com.pulsepass.organizationservice.domain.exceptions;

public class MissingSelectedAgentKeyException extends DomainException {

    public MissingSelectedAgentKeyException() {
        super("Missing selected agent key");
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
