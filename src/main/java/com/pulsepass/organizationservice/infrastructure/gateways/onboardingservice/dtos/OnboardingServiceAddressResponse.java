package com.pulsepass.organizationservice.infrastructure.gateways.onboardingservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OnboardingServiceAddressResponse(
        @JsonProperty("postal_code") String postalCode,
        @JsonProperty("street") String street,
        @JsonProperty("number") String number,
        @JsonProperty("complement") String complement,
        @JsonProperty("neighborhood") String neighborhood
) {
}
