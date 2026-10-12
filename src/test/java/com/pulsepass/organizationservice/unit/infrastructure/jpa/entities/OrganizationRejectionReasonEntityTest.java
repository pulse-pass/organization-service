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
        var domain = new OrganizationRejectionReason(1L, "INACTIVE_DOCUMENT", "desc", NOW);

        var result = OrganizationRejectionReasonEntity.fromDomain(domain).toDomain();

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    void toDomain_allArgumentsConstructor_mapsEveryField() {
        var entity = new OrganizationRejectionReasonEntity(2L, "UNVERIFIED_OWNER", "other", NOW);

        var result = entity.toDomain();

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getEnumerator()).isEqualTo("UNVERIFIED_OWNER");
        assertThat(result.getDescription()).isEqualTo("other");
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
    }
}
