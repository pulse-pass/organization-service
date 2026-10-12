package com.pulsepass.organizationservice.unit.infrastructure.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationStatusEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class OrganizationStatusEntityTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    @Test
    void fromDomainToDomain_status_roundTrips() {
        var domain = new OrganizationStatus(1L, "PENDING", NOW);

        var entity = OrganizationStatusEntity.fromDomain(domain);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getEnumerator()).isEqualTo("PENDING");
        assertThat(entity.getCreatedAt()).isEqualTo(NOW);
        assertThat(entity.toDomain()).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    void setters_values_areReflectedByGetters() {
        var entity = new OrganizationStatusEntity();

        entity.setId(1L);
        entity.setEnumerator("E");
        entity.setCreatedAt(NOW);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getEnumerator()).isEqualTo("E");
        assertThat(entity.getCreatedAt()).isEqualTo(NOW);
    }
}
