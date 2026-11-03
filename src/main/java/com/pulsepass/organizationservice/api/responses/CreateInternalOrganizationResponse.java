package com.pulsepass.organizationservice.api.responses;

import com.pulsepass.organizationservice.domain.entities.Organization;

public record CreateInternalOrganizationResponse(
        String organizationKey,
        String legalPersonKey,
        String documentNumber,
        String name,
        String organizationStatus
) {

    public static CreateInternalOrganizationResponse fromEntity(Organization organization) {
        return new CreateInternalOrganizationResponse(
                organization.getOrganizationKey(),
                organization.getLegalPersonKey(),
                organization.getDocumentNumber(),
                organization.getName(),
                organization.getOrganizationStatus().getEnumerator()
        );
    }
}
