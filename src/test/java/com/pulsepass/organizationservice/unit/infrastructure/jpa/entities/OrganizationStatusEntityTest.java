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

        var result = OrganizationStatusEntity.fromDomain(domain).toDomain();

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    void toDomain_allArgumentsConstructor_mapsEveryField() {
        var entity = new OrganizationStatusEntity(2L, "APPROVED", NOW);

        var result = entity.toDomain();

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getEnumerator()).isEqualTo("APPROVED");
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
    }
}
