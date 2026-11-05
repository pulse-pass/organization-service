package com.pulsepass.organizationservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.domain.exceptions.DomainException;
import com.pulsepass.organizationservice.domain.exceptions.OrganizationNotFoundException;
import org.junit.jupiter.api.Test;

class OrganizationNotFoundExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new OrganizationNotFoundException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("ORG00002");
        assertThat(exception.getMessageKey()).isEqualTo("error.organization.not_found");
        assertThat(exception.getMessage()).isEqualTo("Organization not found");
    }
}
