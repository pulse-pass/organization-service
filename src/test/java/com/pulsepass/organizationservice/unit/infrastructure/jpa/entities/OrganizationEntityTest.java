package com.pulsepass.organizationservice.unit.infrastructure.jpa.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.domain.entities.Organization;
import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReason;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationEntity;
import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationRejectionReasonEntity;
import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationStatusEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class OrganizationEntityTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 6, 7, 8);

    @Test
    void fromDomainToDomain_withRelations_roundTrips() {
        var status = new OrganizationStatus(1L, "REJECTED", NOW);
        var reason = new OrganizationRejectionReason(2L, "INCONSISTENT_DATA", "Inconsistent", NOW);
        var domain = new Organization(
                7L, "org", "legal", "acc", "wal", "12345678000195", "Acme", "api",
                new BigDecimal("5.00"), reason, status, NOW, NOW.plusHours(1));

        var entity = OrganizationEntity.fromDomain(domain);

        assertThat(entity.getOrganizationStatus().getEnumerator()).isEqualTo("REJECTED");
        assertThat(entity.getOrganizationRejectionReason().getEnumerator()).isEqualTo("INCONSISTENT_DATA");
        assertThat(entity.toDomain()).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    void fromDomain_withoutRejectionReason_keepsReasonNull() {
        var domain = new Organization("org", "legal", "12345678000195", "Acme",
                new OrganizationStatus(1L, "PENDING", null));

        var entity = OrganizationEntity.fromDomain(domain);

        assertThat(entity.getOrganizationRejectionReason()).isNull();
        assertThat(entity.toDomain().getOrganizationRejectionReason()).isNull();
        assertThat(entity.getBalanceAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void setters_values_areReflectedByGetters() {
        var entity = new OrganizationEntity();
        var status = new OrganizationStatusEntity();
        var reason = new OrganizationRejectionReasonEntity();

        entity.setId(1L);
        entity.setOrganizationKey("org");
        entity.setLegalPersonKey("legal");
        entity.setExternalAccountKey("acc");
        entity.setExternalWalletKey("wal");
        entity.setDocumentNumber("doc");
        entity.setName("name");
        entity.setExternalApiKey("api");
        entity.setBalanceAmount(BigDecimal.ONE);
        entity.setOrganizationRejectionReason(reason);
        entity.setOrganizationStatus(status);
        entity.setCreatedAt(NOW);
        entity.setUpdatedAt(NOW);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getOrganizationKey()).isEqualTo("org");
        assertThat(entity.getLegalPersonKey()).isEqualTo("legal");
        assertThat(entity.getExternalAccountKey()).isEqualTo("acc");
        assertThat(entity.getExternalWalletKey()).isEqualTo("wal");
        assertThat(entity.getDocumentNumber()).isEqualTo("doc");
        assertThat(entity.getName()).isEqualTo("name");
        assertThat(entity.getExternalApiKey()).isEqualTo("api");
        assertThat(entity.getBalanceAmount()).isEqualTo(BigDecimal.ONE);
        assertThat(entity.getOrganizationRejectionReason()).isSameAs(reason);
        assertThat(entity.getOrganizationStatus()).isSameAs(status);
        assertThat(entity.getCreatedAt()).isEqualTo(NOW);
        assertThat(entity.getUpdatedAt()).isEqualTo(NOW);
    }
}
