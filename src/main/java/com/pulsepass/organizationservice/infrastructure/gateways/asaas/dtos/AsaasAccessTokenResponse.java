package com.pulsepass.organizationservice.infrastructure.gateways.asaas.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AsaasAccessTokenResponse(@JsonProperty("apiKey") String apiKey) {
}
