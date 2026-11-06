package com.pulsepass.organizationservice.domain.exceptions;

public class LegalPersonRetrievalException extends DomainException {

    public LegalPersonRetrievalException() {
        super("Legal person could not be retrieved");
    }

    @Override
    public String getCode() {
        return "ORG00006";
    }

    @Override
    public String getMessageKey() {
        return "error.organization.legal_person_retrieval";
    }
}
