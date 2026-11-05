package com.pulsepass.organizationservice.api.responses;

import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReason;

public record ListInternalOrganizationRejectionReasonsResponse(
        String organizationRejectionReason,
        String organizationRejectionReasonDescription
) {

    public static ListInternalOrganizationRejectionReasonsResponse fromEntity(
            OrganizationRejectionReason organizationRejectionReason) {
        return new ListInternalOrganizationRejectionReasonsResponse(
                organizationRejectionReason.getEnumerator(),
                organizationRejectionReason.getDescription()
        );
    }
}
