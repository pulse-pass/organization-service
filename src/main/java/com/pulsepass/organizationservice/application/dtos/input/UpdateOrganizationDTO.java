package com.pulsepass.organizationservice.application.dtos.input;

import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReasonEnum;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatusEnum;

public record UpdateOrganizationDTO(
        OrganizationStatusEnum organizationStatus,
        OrganizationRejectionReasonEnum organizationRejectionReason
) {
}
