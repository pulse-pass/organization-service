package com.pulsepass.organizationservice.domain.gateways.onboarding.dtos;

import java.util.List;

public record GetPersonOutputDTO(
        String name,
        String documentNumber,
        List<PersonPhoneNumberDTO> phoneNumbers,
        LegalPersonDTO legalPerson
) {
}
