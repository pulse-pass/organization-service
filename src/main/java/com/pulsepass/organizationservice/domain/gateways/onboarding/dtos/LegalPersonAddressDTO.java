package com.pulsepass.organizationservice.domain.gateways.onboarding.dtos;

public record LegalPersonAddressDTO(
        String postalCode,
        String street,
        String number,
        String complement,
        String neighborhood
) {
}
