package com.pulsepass.organizationservice.unit.infrastructure.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReason;
import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationRejectionReasonEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class OrganizationRejectionReasonEntityTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    @Test
    void fromDomainToDomain_reason_roundTrips() {
        var domain = new OrganizationRejectionReason(2L, "INACTIVE_DOCUMENT", "Inactive", NOW);

        var entity = OrganizationRejectionReasonEntity.fromDomain(domain);

        assertThat(entity.getDescription()).isEqualTo("Inactive");
        assertThat(entity.toDomain()).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    void setters_values_areReflectedByGetters() {
        var entity = new OrganizationRejectionReasonEntity();

        entity.setId(2L);
        entity.setEnumerator("R");
        entity.setDescription("D");
        entity.setCreatedAt(NOW);

        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getEnumerator()).isEqualTo("R");
        assertThat(entity.getDescription()).isEqualTo("D");
        assertThat(entity.getCreatedAt()).isEqualTo(NOW);
    }
}
