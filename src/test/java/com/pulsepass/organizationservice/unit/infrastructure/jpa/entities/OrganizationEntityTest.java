package com.pulsepass.organizationservice.unit.infrastructure.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.domain.entities.Organization;
import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReason;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class OrganizationEntityTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    @Test
    void fromDomainToDomain_rejectedOrganization_roundTrips() {
        var status = new OrganizationStatus(3L, "REJECTED", NOW);
        var reason = new OrganizationRejectionReason(1L, "INCONSISTENT_DATA", "desc", NOW);
        var domain = new Organization(
                7L, "org", "legal", "acc", "wal", "12345678000195", "Acme", "api",
                new BigDecimal("10.50"), reason, status, NOW, NOW);

        var result = OrganizationEntity.fromDomain(domain).toDomain();

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    void fromDomainToDomain_pendingOrganizationWithoutReason_keepsReasonNull() {
        var status = new OrganizationStatus(1L, "PENDING", NOW);
        var domain = new Organization("org", "legal", "12345678000195", "Acme", status);

        var result = OrganizationEntity.fromDomain(domain).toDomain();

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
        assertThat(result.getOrganizationRejectionReason()).isNull();
        assertThat(result.getBalanceAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
