package com.pulsepass.organizationservice.domain.exceptions;

public class InvalidOrganizationRejectionReasonException extends DomainException {

    public InvalidOrganizationRejectionReasonException() {
        super("Invalid organization rejection reason");
    }

    @Override
    public String getCode() {
        return "ORG00004";
    }

    @Override
    public String getMessageKey() {
        return "error.organization.invalid_rejection_reason";
    }
}
