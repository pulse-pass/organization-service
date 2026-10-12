package com.pulsepass.organizationservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class OrganizationStatusTest {

    @Test
    void constructor_allArguments_setsFields() {
        var now = LocalDateTime.now();

        var status = new OrganizationStatus(1L, "PENDING", now);

        assertThat(status.getId()).isEqualTo(1L);
        assertThat(status.getEnumerator()).isEqualTo("PENDING");
        assertThat(status.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void setters_values_areReflectedByGetters() {
        var now = LocalDateTime.now();
        var status = new OrganizationStatus();

        status.setId(2L);
        status.setEnumerator("APPROVED");
        status.setCreatedAt(now);

        assertThat(status.getId()).isEqualTo(2L);
        assertThat(status.getEnumerator()).isEqualTo("APPROVED");
        assertThat(status.getCreatedAt()).isEqualTo(now);
    }
}
