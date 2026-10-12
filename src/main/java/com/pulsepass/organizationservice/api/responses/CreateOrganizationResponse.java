package com.pulsepass.organizationservice.api.responses;

import com.pulsepass.organizationservice.domain.entities.Organization;

public record CreateOrganizationResponse(
        String organizationKey,
        String legalPersonKey,
        String documentNumber,
        String name,
        String organizationStatus
) {

    public static CreateOrganizationResponse fromEntity(Organization organization) {
        return new CreateOrganizationResponse(
                organization.getOrganizationKey(),
                organization.getLegalPersonKey(),
                organization.getDocumentNumber(),
                organization.getName(),
                organization.getOrganizationStatus().getEnumerator()
        );
    }
}
