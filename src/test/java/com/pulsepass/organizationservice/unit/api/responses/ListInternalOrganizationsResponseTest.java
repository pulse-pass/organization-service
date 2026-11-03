package com.pulsepass.organizationservice.unit.api.responses;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.api.responses.ListInternalOrganizationsResponse;
import com.pulsepass.organizationservice.domain.entities.Organization;
import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReason;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ListInternalOrganizationsResponseTest {

    @Test
    void fromEntity_pendingOrganization_mapsAllFieldsWithNullRejectionReason() {
        Organization organization = new Organization(
                "org-key", "legal-key", "12345678000195", "Acme", new OrganizationStatus(1L, "PENDING", null));
        organization.setCreatedAt(LocalDateTime.of(2026, 10, 9, 14, 12));
        organization.setUpdatedAt(LocalDateTime.of(2026, 10, 9, 14, 13));

        ListInternalOrganizationsResponse response = ListInternalOrganizationsResponse.fromEntity(organization);

        assertThat(response.organizationKey()).isEqualTo("org-key");
        assertThat(response.legalPersonKey()).isEqualTo("legal-key");
        assertThat(response.name()).isEqualTo("Acme");
        assertThat(response.documentNumber()).isEqualTo("12345678000195");
        assertThat(response.organizationStatus()).isEqualTo("PENDING");
        assertThat(response.organizationRejectionReason()).isNull();
        assertThat(response.organizationRejectionReasonDescription()).isNull();
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-10-09T14:12:00Z"));
        assertThat(response.updatedAt()).isEqualTo(Instant.parse("2026-10-09T14:13:00Z"));
    }

    @Test
    void fromEntity_rejectedOrganization_mapsRejectionReason() {
        Organization organization = new Organization(
                "org-key", "legal-key", "12345678000195", "Acme", new OrganizationStatus(3L, "REJECTED", null));
        organization.setOrganizationRejectionReason(
                new OrganizationRejectionReason(1L, "INACTIVE_DOCUMENT", "Inactive CNPJ", null));
        organization.setCreatedAt(LocalDateTime.of(2026, 10, 9, 14, 12));
        organization.setUpdatedAt(LocalDateTime.of(2026, 10, 9, 14, 13));

        ListInternalOrganizationsResponse response = ListInternalOrganizationsResponse.fromEntity(organization);

        assertThat(response.organizationStatus()).isEqualTo("REJECTED");
        assertThat(response.organizationRejectionReason()).isEqualTo("INACTIVE_DOCUMENT");
        assertThat(response.organizationRejectionReasonDescription()).isEqualTo("Inactive CNPJ");
    }
}
