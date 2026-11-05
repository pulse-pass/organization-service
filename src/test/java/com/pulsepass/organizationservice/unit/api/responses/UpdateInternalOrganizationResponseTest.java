package com.pulsepass.organizationservice.unit.api.responses;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.api.responses.UpdateInternalOrganizationResponse;
import com.pulsepass.organizationservice.domain.entities.Organization;
import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReason;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import org.junit.jupiter.api.Test;

class UpdateInternalOrganizationResponseTest {

    @Test
    void fromEntity_approvedOrganization_mapsKeyAndStatusWithNullRejectionReason() {
        Organization organization = new Organization(
                "org-key", "legal-key", "12345678000195", "Acme", new OrganizationStatus(2L, "APPROVED", null));

        UpdateInternalOrganizationResponse response = UpdateInternalOrganizationResponse.fromEntity(organization);

        assertThat(response.organizationKey()).isEqualTo("org-key");
        assertThat(response.organizationStatus()).isEqualTo("APPROVED");
        assertThat(response.organizationRejectionReason()).isNull();
        assertThat(response.organizationRejectionReasonDescription()).isNull();
    }

    @Test
    void fromEntity_rejectedOrganization_mapsRejectionReason() {
        Organization organization = new Organization(
                "org-key", "legal-key", "12345678000195", "Acme", new OrganizationStatus(3L, "REJECTED", null));
        organization.setOrganizationRejectionReason(
                new OrganizationRejectionReason(1L, "INACTIVE_DOCUMENT", "Inactive CNPJ", null));

        UpdateInternalOrganizationResponse response = UpdateInternalOrganizationResponse.fromEntity(organization);

        assertThat(response.organizationKey()).isEqualTo("org-key");
        assertThat(response.organizationStatus()).isEqualTo("REJECTED");
        assertThat(response.organizationRejectionReason()).isEqualTo("INACTIVE_DOCUMENT");
        assertThat(response.organizationRejectionReasonDescription()).isEqualTo("Inactive CNPJ");
    }
}
