package com.pulsepass.organizationservice.unit.api.schemas;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.api.schemas.UpdateOrganizationSchema;
import com.pulsepass.organizationservice.api.schemas.definitions.UpdateOrganizationStatusSchema;
import com.pulsepass.organizationservice.application.dtos.input.UpdateOrganizationDTO;
import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReasonEnum;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatusEnum;
import org.junit.jupiter.api.Test;

class UpdateOrganizationSchemaTest {

    @Test
    void toDTO_rejectedWithReason_copiesStatusAndReason() {
        var schema = new UpdateOrganizationSchema(
                UpdateOrganizationStatusSchema.REJECTED, OrganizationRejectionReasonEnum.INACTIVE_DOCUMENT);

        UpdateOrganizationDTO dto = schema.toDTO();

        assertThat(dto.organizationStatus()).isEqualTo(OrganizationStatusEnum.REJECTED);
        assertThat(dto.organizationRejectionReason()).isEqualTo(OrganizationRejectionReasonEnum.INACTIVE_DOCUMENT);
    }

    @Test
    void toDTO_approvedWithoutReason_keepsReasonNull() {
        var schema = new UpdateOrganizationSchema(UpdateOrganizationStatusSchema.APPROVED, null);

        UpdateOrganizationDTO dto = schema.toDTO();

        assertThat(dto.organizationStatus()).isEqualTo(OrganizationStatusEnum.APPROVED);
        assertThat(dto.organizationRejectionReason()).isNull();
    }
}
