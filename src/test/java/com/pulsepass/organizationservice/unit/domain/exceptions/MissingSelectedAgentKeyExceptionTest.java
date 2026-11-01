package com.pulsepass.organizationservice.unit.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.domain.exceptions.DomainException;
import com.pulsepass.organizationservice.domain.exceptions.MissingSelectedAgentKeyException;
import org.junit.jupiter.api.Test;

class MissingSelectedAgentKeyExceptionTest {

    @Test
    void new_default_exposesCodeAndMessageKey() {
        var exception = new MissingSelectedAgentKeyException();

        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getCode()).isEqualTo("GEN00401");
        assertThat(exception.getMessageKey()).isEqualTo("error.unauthorized");
        assertThat(exception.getMessage()).isEqualTo("Missing selected agent key");
    }
}
