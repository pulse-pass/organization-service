package com.pulsepass.organizationservice.unit.infrastructure.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.domain.entities.Organization;
import com.pulsepass.organizationservice.domain.entities.OrganizationHistory;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationEntity;
import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationHistoryEntity;
import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationStatusEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class OrganizationHistoryEntityTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    @Test
    void fromDomainToDomain_nullOldStatus_roundTrips() {
        var status = new OrganizationStatus(1L, "PENDING", NOW);
        var organization = new Organization(
                3L, "org", "legal", null, null, "12345678000195", "Acme", null,
                BigDecimal.ZERO, null, status, NOW, NOW);
        var domain = new OrganizationHistory(9L, "hist", organization, null, status, "{\"a\":1}", NOW);

        var entity = OrganizationHistoryEntity.fromDomain(domain);

        assertThat(entity.getOldStatus()).isNull();
        assertThat(entity.getNewStatus().getEnumerator()).isEqualTo("PENDING");
        assertThat(entity.getOrganization().getOrganizationKey()).isEqualTo("org");
        assertThat(entity.toDomain()).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    void fromDomain_withOldStatus_mapsOldStatus() {
        var oldStatus = new OrganizationStatus(1L, "PENDING", NOW);
        var newStatus = new OrganizationStatus(2L, "APPROVED", NOW);
        var organization = new Organization("org", "legal", "12345678000195", "Acme", newStatus);
        var domain = new OrganizationHistory("hist", organization, oldStatus, newStatus, "{}");

        var entity = OrganizationHistoryEntity.fromDomain(domain);

        assertThat(entity.getOldStatus().getEnumerator()).isEqualTo("PENDING");
        assertThat(entity.toDomain().getOldStatus().getEnumerator()).isEqualTo("PENDING");
    }

    @Test
    void setters_values_areReflectedByGetters() {
        var entity = new OrganizationHistoryEntity();
        var organization = new OrganizationEntity();
        var oldStatus = new OrganizationStatusEntity();
        var newStatus = new OrganizationStatusEntity();

        entity.setId(1L);
        entity.setOrganizationHistoryKey("k");
        entity.setOrganization(organization);
        entity.setOldStatus(oldStatus);
        entity.setNewStatus(newStatus);
        entity.setHistoryData("{}");
        entity.setCreatedAt(NOW);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getOrganizationHistoryKey()).isEqualTo("k");
        assertThat(entity.getOrganization()).isSameAs(organization);
        assertThat(entity.getOldStatus()).isSameAs(oldStatus);
        assertThat(entity.getNewStatus()).isSameAs(newStatus);
        assertThat(entity.getHistoryData()).isEqualTo("{}");
        assertThat(entity.getCreatedAt()).isEqualTo(NOW);
    }
}
