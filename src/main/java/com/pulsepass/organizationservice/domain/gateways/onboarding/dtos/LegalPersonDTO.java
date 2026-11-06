package com.pulsepass.organizationservice.domain.gateways.onboarding.dtos;

import java.math.BigDecimal;

public record LegalPersonDTO(
        String email,
        String legalPersonCompanyType,
        BigDecimal monthlyRevenueAmount,
        LegalPersonAddressDTO legalPersonAddress
) {
}
