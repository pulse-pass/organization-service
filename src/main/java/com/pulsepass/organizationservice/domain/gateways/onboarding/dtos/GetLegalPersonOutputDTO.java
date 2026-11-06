package com.pulsepass.organizationservice.domain.gateways.onboarding.dtos;

import java.math.BigDecimal;
import java.util.List;

public record GetLegalPersonOutputDTO(
        String name,
        String documentNumber,
        String email,
        List<LegalPersonPhoneNumberDTO> phoneNumbers,
        String legalPersonCompanyType,
        BigDecimal monthlyRevenueAmount,
        LegalPersonAddressDTO legalPersonAddress
) {
}
