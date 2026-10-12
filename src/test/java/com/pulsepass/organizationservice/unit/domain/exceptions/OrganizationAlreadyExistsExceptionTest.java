package com.pulsepass.organizationservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.domain.exceptions.DomainException;
import com.pulsepass.organizationservice.domain.exceptions.OrganizationAlreadyExistsException;
import org.junit.jupiter.api.Test;

class OrganizationAlreadyExistsExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new OrganizationAlreadyExistsException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("ORG00001");
        assertThat(exception.getMessageKey()).isEqualTo("error.organization.already_exists");
        assertThat(exception.getMessage()).isEqualTo("Organization already exists");
    }
}
