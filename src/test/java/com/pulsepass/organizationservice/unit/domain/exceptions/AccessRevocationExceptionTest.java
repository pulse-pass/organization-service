package com.pulsepass.organizationservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.domain.exceptions.AccessRevocationException;
import com.pulsepass.organizationservice.domain.exceptions.DomainException;
import org.junit.jupiter.api.Test;

class AccessRevocationExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new AccessRevocationException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("ORG00007");
        assertThat(exception.getMessageKey()).isEqualTo("error.organization.access_revocation");
        assertThat(exception.getMessage()).isEqualTo("Organization collaborators access could not be revoked");
    }
}
