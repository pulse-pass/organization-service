package com.pulsepass.organizationservice.domain.exceptions;

public class AccessRevocationException extends DomainException {

    public AccessRevocationException() {
        super("Organization collaborators access could not be revoked");
    }

    @Override
    public String getCode() {
        return "ORG00007";
    }

    @Override
    public String getMessageKey() {
        return "error.organization.access_revocation";
    }
}
