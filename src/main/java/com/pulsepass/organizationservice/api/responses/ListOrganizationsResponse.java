package com.pulsepass.organizationservice.api.responses;

import com.pulsepass.organizationservice.domain.entities.Organization;
import java.time.LocalDateTime;

public record ListOrganizationsResponse(
        String organizationKey,
        String legalPersonKey,
        String name,
        String documentNumber,
        String status,
        String rejectionReason,
        String rejectionReasonDescription,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
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
                organization.getCreatedAt(),
                organization.getUpdatedAt()
        );
    }
}
