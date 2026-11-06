package com.pulsepass.organizationservice.domain.gateways.paymentprovider.dtos;

public record CreatePaymentAccountOutputDTO(String accountKey, String walletKey, String apiKey) {
}
