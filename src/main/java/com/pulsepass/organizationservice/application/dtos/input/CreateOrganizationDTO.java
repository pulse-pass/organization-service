package com.pulsepass.organizationservice.application.dtos.input;

public record CreateOrganizationDTO(
        String legalPersonKey,
        String documentNumber,
        String name
) {
}
