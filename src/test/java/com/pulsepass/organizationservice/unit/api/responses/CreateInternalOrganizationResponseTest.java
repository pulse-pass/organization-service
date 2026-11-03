package com.pulsepass.organizationservice.unit.api.responses;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.api.responses.CreateInternalOrganizationResponse;
import com.pulsepass.organizationservice.domain.entities.Organization;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import org.junit.jupiter.api.Test;

class CreateInternalOrganizationResponseTest {

    @Test
    void fromEntity_organization_mapsAllFields() {
        Organization organization = new Organization(
                "org-key", "legal-key", "12345678000195", "Acme", new OrganizationStatus(1L, "PENDING", null));

        CreateInternalOrganizationResponse response = CreateInternalOrganizationResponse.fromEntity(organization);

        assertThat(response.organizationKey()).isEqualTo("org-key");
        assertThat(response.legalPersonKey()).isEqualTo("legal-key");
        assertThat(response.documentNumber()).isEqualTo("12345678000195");
        assertThat(response.name()).isEqualTo("Acme");
        assertThat(response.organizationStatus()).isEqualTo("PENDING");
    }
}
