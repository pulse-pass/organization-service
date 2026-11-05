package com.pulsepass.organizationservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.domain.exceptions.DomainException;
import com.pulsepass.organizationservice.domain.exceptions.OrganizationStatusNotAllowedException;
import org.junit.jupiter.api.Test;

class OrganizationStatusNotAllowedExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new OrganizationStatusNotAllowedException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("ORG00003");
        assertThat(exception.getMessageKey()).isEqualTo("error.organization.status_not_allowed");
        assertThat(exception.getMessage()).isEqualTo("Organization status does not allow this change");
    }
}
