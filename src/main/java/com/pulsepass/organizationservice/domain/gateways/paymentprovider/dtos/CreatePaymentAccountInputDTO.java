package com.pulsepass.organizationservice.domain.gateways.paymentprovider.dtos;

import java.math.BigDecimal;

public record CreatePaymentAccountInputDTO(
        String organizationKey,
        String name,
        String email,
        String documentNumber,
        String mobilePhone,
        String companyType,
        BigDecimal monthlyRevenueAmount,
        String street,
        String number,
        String complement,
        String neighborhood,
        String postalCode
) {
}
