package com.pulsepass.organizationservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.domain.entities.Organization;
import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReason;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class OrganizationTest {

    @Test
    void constructor_creationArguments_setsZeroBalanceAndLeavesOthersNull() {
        var status = new OrganizationStatus(1L, "PENDING", null);

        var organization = new Organization("org", "legal", "12345678000195", "Acme", status);

        assertThat(organization.getOrganizationKey()).isEqualTo("org");
        assertThat(organization.getLegalPersonKey()).isEqualTo("legal");
        assertThat(organization.getDocumentNumber()).isEqualTo("12345678000195");
        assertThat(organization.getName()).isEqualTo("Acme");
        assertThat(organization.getOrganizationStatus()).isSameAs(status);
        assertThat(organization.getBalanceAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(organization.getId()).isNull();
        assertThat(organization.getExternalAccountKey()).isNull();
        assertThat(organization.getExternalWalletKey()).isNull();
        assertThat(organization.getExternalApiKey()).isNull();
        assertThat(organization.getOrganizationRejectionReason()).isNull();
    }

    @Test
    void constructor_allArguments_setsEveryField() {
        var now = LocalDateTime.of(2026, 1, 2, 3, 4);
        var status = new OrganizationStatus(2L, "REJECTED", now);
        var reason = new OrganizationRejectionReason(3L, "INACTIVE_DOCUMENT", "desc", now);

        var organization = new Organization(
                9L, "org", "legal", "acc", "wal", "12345678000195", "Acme", "api",
                new BigDecimal("10.50"), reason, status, now, now.plusDays(1));

        assertThat(organization.getId()).isEqualTo(9L);
        assertThat(organization.getExternalAccountKey()).isEqualTo("acc");
        assertThat(organization.getExternalWalletKey()).isEqualTo("wal");
        assertThat(organization.getExternalApiKey()).isEqualTo("api");
        assertThat(organization.getBalanceAmount()).isEqualByComparingTo("10.50");
        assertThat(organization.getOrganizationRejectionReason()).isSameAs(reason);
        assertThat(organization.getOrganizationStatus()).isSameAs(status);
        assertThat(organization.getCreatedAt()).isEqualTo(now);
        assertThat(organization.getUpdatedAt()).isEqualTo(now.plusDays(1));
    }

    @Test
    void setters_values_areReflectedByGetters() {
        var now = LocalDateTime.now();
        var status = new OrganizationStatus(1L, "APPROVED", now);
        var reason = new OrganizationRejectionReason();
        var organization = new Organization();

        organization.setId(1L);
        organization.setOrganizationKey("org");
        organization.setLegalPersonKey("legal");
        organization.setExternalAccountKey("acc");
        organization.setExternalWalletKey("wal");
        organization.setDocumentNumber("doc");
        organization.setName("name");
        organization.setExternalApiKey("api");
        organization.setBalanceAmount(BigDecimal.TEN);
        organization.setOrganizationRejectionReason(reason);
        organization.setOrganizationStatus(status);
        organization.setCreatedAt(now);
        organization.setUpdatedAt(now);

        assertThat(organization.getId()).isEqualTo(1L);
        assertThat(organization.getOrganizationKey()).isEqualTo("org");
        assertThat(organization.getLegalPersonKey()).isEqualTo("legal");
        assertThat(organization.getExternalAccountKey()).isEqualTo("acc");
        assertThat(organization.getExternalWalletKey()).isEqualTo("wal");
        assertThat(organization.getDocumentNumber()).isEqualTo("doc");
        assertThat(organization.getName()).isEqualTo("name");
        assertThat(organization.getExternalApiKey()).isEqualTo("api");
        assertThat(organization.getBalanceAmount()).isEqualTo(BigDecimal.TEN);
        assertThat(organization.getOrganizationRejectionReason()).isSameAs(reason);
        assertThat(organization.getOrganizationStatus()).isSameAs(status);
        assertThat(organization.getCreatedAt()).isEqualTo(now);
        assertThat(organization.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toHistoryData_pendingOrganization_returnsSnapshotWithoutInternalFields() {
        var status = new OrganizationStatus(1L, "PENDING", null);
        var organization = new Organization("org", "legal", "12345678000195", "Acme", status);

        var data = organization.toHistoryData();

        assertThat(data).containsOnlyKeys(
                "organization_key", "legal_person_key", "document_number", "name",
                "balance_amount", "organization_rejection_reason", "organization_status");
        assertThat(data.get("organization_key")).isEqualTo("org");
        assertThat(data.get("legal_person_key")).isEqualTo("legal");
        assertThat(data.get("document_number")).isEqualTo("12345678000195");
        assertThat(data.get("name")).isEqualTo("Acme");
        assertThat(data.get("balance_amount")).isEqualTo(BigDecimal.ZERO);
        assertThat(data.get("organization_rejection_reason")).isNull();
        assertThat(data.get("organization_status")).isEqualTo("PENDING");
    }

    @Test
    void toHistoryData_rejectedOrganization_includesRejectionReasonEnumerator() {
        var status = new OrganizationStatus(3L, "REJECTED", null);
        var organization = new Organization("org", "legal", "12345678000195", "Acme", status);
        organization.setOrganizationRejectionReason(
                new OrganizationRejectionReason(1L, "INACTIVE_DOCUMENT", "desc", null));

        var data = organization.toHistoryData();

        assertThat(data.get("organization_rejection_reason")).isEqualTo("INACTIVE_DOCUMENT");
        assertThat(data.get("organization_status")).isEqualTo("REJECTED");
    }
}
