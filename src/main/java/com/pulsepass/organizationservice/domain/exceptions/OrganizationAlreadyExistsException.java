package com.pulsepass.organizationservice.domain.exceptions;

public class OrganizationAlreadyExistsException extends DomainException {

    public OrganizationAlreadyExistsException() {
        super("Organization already exists");
    }

    @Override
    public String getCode() {
        return "ORG00001";
    }

    @Override
    public String getMessageKey() {
        return "error.organization.already_exists";
    }
}
