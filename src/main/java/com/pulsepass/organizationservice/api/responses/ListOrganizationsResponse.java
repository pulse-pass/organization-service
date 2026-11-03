package com.pulsepass.organizationservice.api.responses;

import com.pulsepass.organizationservice.domain.entities.Organization;
import java.time.Instant;
import java.time.ZoneOffset;

public record ListOrganizationsResponse(
        String organizationKey,
        String legalPersonKey,
        String name,
        String documentNumber,
        String organizationStatus,
        String organizationRejectionReason,
        String organizationRejectionReasonDescription,
        Instant createdAt,
        Instant updatedAt
) {

    public static ListOrganizationsResponse fromEntity(Organization organization) {
        var rejectionReason = organization.getOrganizationRejectionReason();
        return new ListOrganizationsResponse(
                organization.getOrganizationKey(),
                organization.getLegalPersonKey(),
                organization.getName(),
                organization.getDocumentNumber(),
                organization.getOrganizationStatus().getEnumerator(),
                rejectionReason == null ? null : rejectionReason.getEnumerator(),
                rejectionReason == null ? null : rejectionReason.getDescription(),
                organization.getCreatedAt().toInstant(ZoneOffset.UTC),
                organization.getUpdatedAt().toInstant(ZoneOffset.UTC)
        );
    }
}
