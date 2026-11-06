package com.pulsepass.organizationservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.domain.exceptions.DomainException;
import com.pulsepass.organizationservice.domain.exceptions.PaymentAccountCreationException;
import org.junit.jupiter.api.Test;

class PaymentAccountCreationExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new PaymentAccountCreationException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("ORG00005");
        assertThat(exception.getMessageKey()).isEqualTo("error.organization.payment_account_creation");
        assertThat(exception.getMessage()).isEqualTo("Payment account could not be created");
    }
}
