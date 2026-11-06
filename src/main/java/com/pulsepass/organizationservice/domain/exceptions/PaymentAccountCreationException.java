package com.pulsepass.organizationservice.domain.exceptions;

public class PaymentAccountCreationException extends DomainException {

    public PaymentAccountCreationException() {
        super("Payment account could not be created");
    }

    @Override
    public String getCode() {
        return "ORG00005";
    }

    @Override
    public String getMessageKey() {
        return "error.organization.payment_account_creation";
    }
}
