package com.pulsepass.organizationservice.unit.api.responses;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.api.responses.ListInternalOrganizationRejectionReasonsResponse;
import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReason;
import org.junit.jupiter.api.Test;

class ListInternalOrganizationRejectionReasonsResponseTest {

    @Test
    void fromEntity_rejectionReason_mapsEnumeratorAndDescription() {
        var rejectionReason = new OrganizationRejectionReason(1L, "INACTIVE_DOCUMENT", "Inactive CNPJ", null);

        var response = ListInternalOrganizationRejectionReasonsResponse.fromEntity(rejectionReason);

        assertThat(response.organizationRejectionReason()).isEqualTo("INACTIVE_DOCUMENT");
        assertThat(response.organizationRejectionReasonDescription()).isEqualTo("Inactive CNPJ");
    }
}
