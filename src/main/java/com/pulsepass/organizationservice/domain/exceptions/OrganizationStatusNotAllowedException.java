package com.pulsepass.organizationservice.domain.exceptions;

public class OrganizationStatusNotAllowedException extends DomainException {

    public OrganizationStatusNotAllowedException() {
        super("Organization status does not allow this change");
    }

    @Override
    public String getCode() {
        return "ORG00003";
    }

    @Override
    public String getMessageKey() {
        return "error.organization.status_not_allowed";
    }
}
