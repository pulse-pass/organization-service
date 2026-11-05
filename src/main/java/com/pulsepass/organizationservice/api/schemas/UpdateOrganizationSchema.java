package com.pulsepass.organizationservice.api.schemas;

import com.pulsepass.organizationservice.api.schemas.definitions.UpdateOrganizationStatusSchema;
import com.pulsepass.organizationservice.application.dtos.input.UpdateOrganizationDTO;
import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReasonEnum;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatusEnum;
import jakarta.validation.constraints.NotNull;

public record UpdateOrganizationSchema(
        @NotNull UpdateOrganizationStatusSchema organizationStatus,
        OrganizationRejectionReasonEnum organizationRejectionReason
) {

    public UpdateOrganizationDTO toDTO() {
        return new UpdateOrganizationDTO(
                OrganizationStatusEnum.valueOf(organizationStatus.name()), organizationRejectionReason);
    }
}
