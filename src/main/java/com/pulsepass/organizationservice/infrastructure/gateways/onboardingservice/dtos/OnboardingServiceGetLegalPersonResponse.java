package com.pulsepass.organizationservice.infrastructure.gateways.onboardingservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OnboardingServiceGetLegalPersonResponse(
        @JsonProperty("name") String name,
        @JsonProperty("document_number") String documentNumber,
        @JsonProperty("email") String email,
        @JsonProperty("phone_numbers") List<OnboardingServicePhoneNumberResponse> phoneNumbers,
        @JsonProperty("legal_person_company_type") String legalPersonCompanyType,
        @JsonProperty("monthly_revenue_amount") BigDecimal monthlyRevenueAmount,
        @JsonProperty("legal_person_address") OnboardingServiceAddressResponse legalPersonAddress
) {
}
