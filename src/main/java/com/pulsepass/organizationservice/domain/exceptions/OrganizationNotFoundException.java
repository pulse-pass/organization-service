package com.pulsepass.organizationservice.domain.exceptions;

public class OrganizationNotFoundException extends DomainException {

    public OrganizationNotFoundException() {
        super("Organization not found");
    }

    @Override
    public String getCode() {
        return "ORG00002";
    }

    @Override
    public String getMessageKey() {
        return "error.organization.not_found";
    }
}
