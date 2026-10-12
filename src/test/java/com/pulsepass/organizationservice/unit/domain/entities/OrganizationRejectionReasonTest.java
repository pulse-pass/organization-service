package com.pulsepass.organizationservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReason;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class OrganizationRejectionReasonTest {

    @Test
    void constructor_allArguments_setsFields() {
        var now = LocalDateTime.now();

        var reason = new OrganizationRejectionReason(1L, "INACTIVE_DOCUMENT", "desc", now);

        assertThat(reason.getId()).isEqualTo(1L);
        assertThat(reason.getEnumerator()).isEqualTo("INACTIVE_DOCUMENT");
        assertThat(reason.getDescription()).isEqualTo("desc");
        assertThat(reason.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void setters_values_areReflectedByGetters() {
        var now = LocalDateTime.now();
        var reason = new OrganizationRejectionReason();

        reason.setId(2L);
        reason.setEnumerator("X");
        reason.setDescription("d");
        reason.setCreatedAt(now);

        assertThat(reason.getId()).isEqualTo(2L);
        assertThat(reason.getEnumerator()).isEqualTo("X");
        assertThat(reason.getDescription()).isEqualTo("d");
        assertThat(reason.getCreatedAt()).isEqualTo(now);
    }
}
