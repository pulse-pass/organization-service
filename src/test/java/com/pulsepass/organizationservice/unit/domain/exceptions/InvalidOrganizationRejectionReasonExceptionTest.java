package com.pulsepass.organizationservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.domain.exceptions.DomainException;
import com.pulsepass.organizationservice.domain.exceptions.InvalidOrganizationRejectionReasonException;
import org.junit.jupiter.api.Test;

class InvalidOrganizationRejectionReasonExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new InvalidOrganizationRejectionReasonException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("ORG00004");
        assertThat(exception.getMessageKey()).isEqualTo("error.organization.invalid_rejection_reason");
        assertThat(exception.getMessage()).isEqualTo("Invalid organization rejection reason");
    }
}
