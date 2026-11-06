package com.pulsepass.organizationservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.domain.exceptions.DomainException;
import com.pulsepass.organizationservice.domain.exceptions.PersonRetrievalException;
import org.junit.jupiter.api.Test;

class PersonRetrievalExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new PersonRetrievalException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("ORG00006");
        assertThat(exception.getMessageKey()).isEqualTo("error.organization.person_retrieval");
        assertThat(exception.getMessage()).isEqualTo("Person could not be retrieved");
    }
}
