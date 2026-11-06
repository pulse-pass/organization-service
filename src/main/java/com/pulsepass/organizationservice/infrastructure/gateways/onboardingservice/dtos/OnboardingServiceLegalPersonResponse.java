package com.pulsepass.organizationservice.infrastructure.gateways.onboardingservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OnboardingServiceLegalPersonResponse(
        @JsonProperty("email") String email,
        @JsonProperty("legal_person_company_type") String legalPersonCompanyType,
        @JsonProperty("monthly_revenue_amount") BigDecimal monthlyRevenueAmount,
        @JsonProperty("legal_person_address") OnboardingServiceAddressResponse legalPersonAddress
) {
}
