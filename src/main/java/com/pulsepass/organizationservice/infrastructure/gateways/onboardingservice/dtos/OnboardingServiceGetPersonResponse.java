package com.pulsepass.organizationservice.infrastructure.gateways.onboardingservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OnboardingServiceGetPersonResponse(
        @JsonProperty("name") String name,
        @JsonProperty("document_number") String documentNumber,
        @JsonProperty("phone_numbers") List<OnboardingServicePhoneNumberResponse> phoneNumbers,
        @JsonProperty("legal_person") OnboardingServiceLegalPersonResponse legalPerson
) {
}
