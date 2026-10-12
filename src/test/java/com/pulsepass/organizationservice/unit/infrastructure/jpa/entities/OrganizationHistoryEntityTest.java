package com.pulsepass.organizationservice.unit.infrastructure.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.domain.entities.Organization;
import com.pulsepass.organizationservice.domain.entities.OrganizationHistory;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationHistoryEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class OrganizationHistoryEntityTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    private static Organization organization(OrganizationStatus status) {
        return new Organization(
                3L, "org", "legal", null, null, "12345678000195", "Acme", null,
                BigDecimal.ZERO, null, status, NOW, NOW);
    }

    @Test
    void fromDomainToDomain_nullOldStatus_roundTrips() {
        var status = new OrganizationStatus(1L, "PENDING", NOW);
        var domain = new OrganizationHistory(9L, "hist", organization(status), null, status, "{\"a\":1}", NOW);

        var result = OrganizationHistoryEntity.fromDomain(domain).toDomain();

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
        assertThat(result.getOldStatus()).isNull();
    }

    @Test
    void fromDomainToDomain_withOldStatus_roundTrips() {
        var oldStatus = new OrganizationStatus(1L, "PENDING", NOW);
        var newStatus = new OrganizationStatus(2L, "APPROVED", NOW);
        var domain = new OrganizationHistory(
                9L, "hist", organization(newStatus), oldStatus, newStatus, "{}", NOW);

        var result = OrganizationHistoryEntity.fromDomain(domain).toDomain();

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
    }
}
