package com.pulsepass.organizationservice.infrastructure.gateways.onboardingservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OnboardingServicePhoneNumberResponse(
        @JsonProperty("area_code") String areaCode,
        @JsonProperty("number") String number
) {
}
