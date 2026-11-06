package com.pulsepass.organizationservice.domain.exceptions;

public class PersonRetrievalException extends DomainException {

    public PersonRetrievalException() {
        super("Person could not be retrieved");
    }

    @Override
    public String getCode() {
        return "ORG00006";
    }

    @Override
    public String getMessageKey() {
        return "error.organization.person_retrieval";
    }
}
