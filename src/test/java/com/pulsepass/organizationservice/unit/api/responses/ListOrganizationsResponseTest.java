package com.pulsepass.organizationservice.unit.api.responses;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.api.responses.ListOrganizationsResponse;
import com.pulsepass.organizationservice.domain.entities.Organization;
import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReason;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ListOrganizationsResponseTest {

    @Test
    void fromEntity_pendingOrganization_mapsAllFieldsWithNullRejectionReason() {
        Organization organization = new Organization(
                "org-key", "legal-key", "12345678000195", "Acme", new OrganizationStatus(1L, "PENDING", null));
        organization.setCreatedAt(LocalDateTime.of(2026, 10, 9, 14, 12));
        organization.setUpdatedAt(LocalDateTime.of(2026, 10, 9, 14, 13));

        ListOrganizationsResponse response = ListOrganizationsResponse.fromEntity(organization);

        assertThat(response.organizationKey()).isEqualTo("org-key");
        assertThat(response.legalPersonKey()).isEqualTo("legal-key");
        assertThat(response.name()).isEqualTo("Acme");
        assertThat(response.documentNumber()).isEqualTo("12345678000195");
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.rejectionReason()).isNull();
        assertThat(response.rejectionReasonDescription()).isNull();
        assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2026, 10, 9, 14, 12));
        assertThat(response.updatedAt()).isEqualTo(LocalDateTime.of(2026, 10, 9, 14, 13));
    }

    @Test
    void fromEntity_rejectedOrganization_mapsRejectionReason() {
        Organization organization = new Organization(
                "org-key", "legal-key", "12345678000195", "Acme", new OrganizationStatus(3L, "REJECTED", null));
        organization.setOrganizationRejectionReason(
                new OrganizationRejectionReason(1L, "INACTIVE_DOCUMENT", "Inactive CNPJ", null));

        ListOrganizationsResponse response = ListOrganizationsResponse.fromEntity(organization);

        assertThat(response.status()).isEqualTo("REJECTED");
        assertThat(response.rejectionReason()).isEqualTo("INACTIVE_DOCUMENT");
        assertThat(response.rejectionReasonDescription()).isEqualTo("Inactive CNPJ");
    }
}
