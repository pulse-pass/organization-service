package com.pulsepass.organizationservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.domain.exceptions.DomainException;
import com.pulsepass.organizationservice.domain.exceptions.LegalPersonRetrievalException;
import org.junit.jupiter.api.Test;

class LegalPersonRetrievalExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new LegalPersonRetrievalException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("ORG00006");
        assertThat(exception.getMessageKey()).isEqualTo("error.organization.legal_person_retrieval");
        assertThat(exception.getMessage()).isEqualTo("Legal person could not be retrieved");
    }
}
