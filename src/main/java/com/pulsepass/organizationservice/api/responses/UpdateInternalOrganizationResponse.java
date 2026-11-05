package com.pulsepass.organizationservice.api.responses;

import com.pulsepass.organizationservice.domain.entities.Organization;

public record UpdateInternalOrganizationResponse(
        String organizationKey,
        String organizationStatus,
        String organizationRejectionReason,
        String organizationRejectionReasonDescription
) {

    public static UpdateInternalOrganizationResponse fromEntity(Organization organization) {
        var rejectionReason = organization.getOrganizationRejectionReason();
        return new UpdateInternalOrganizationResponse(
                organization.getOrganizationKey(),
                organization.getOrganizationStatus().getEnumerator(),
                rejectionReason == null ? null : rejectionReason.getEnumerator(),
                rejectionReason == null ? null : rejectionReason.getDescription()
        );
    }
}
