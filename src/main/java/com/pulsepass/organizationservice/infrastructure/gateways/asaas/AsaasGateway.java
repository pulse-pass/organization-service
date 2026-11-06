package com.pulsepass.organizationservice.infrastructure.gateways.asaas;

import com.pulsepass.organizationservice.domain.exceptions.PaymentAccountCreationException;
import com.pulsepass.organizationservice.domain.gateways.paymentprovider.PaymentProviderGateway;
import com.pulsepass.organizationservice.domain.gateways.paymentprovider.dtos.CreatePaymentAccountInputDTO;
import com.pulsepass.organizationservice.domain.gateways.paymentprovider.dtos.CreatePaymentAccountOutputDTO;
import com.pulsepass.organizationservice.infrastructure.gateways.asaas.dtos.AsaasCreateAccountRequest;
import com.pulsepass.organizationservice.infrastructure.gateways.asaas.dtos.AsaasCreateAccountResponse;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class AsaasGateway implements PaymentProviderGateway {

    private static final Logger logger = LoggerFactory.getLogger(AsaasGateway.class);

    private final RestClient restClient;
    private final String apiKey;

    public AsaasGateway(
            @Value("${app.asaas.url}") String url,
            @Value("${app.asaas.api-key}") String apiKey,
            RestClient.Builder restClientBuilder
    ) {
        this.restClient = restClientBuilder
                .baseUrl(url)
                .build();
        this.apiKey = apiKey;
    }

    @Override
    public CreatePaymentAccountOutputDTO createPaymentAccount(CreatePaymentAccountInputDTO input) {
        logger.info(
                "AsaasGateway.createPaymentAccount - Creating payment account {}",
                Map.of("organization_key", input.organizationKey())
        );

        var request = new AsaasCreateAccountRequest(
                input.name(),
                input.email(),
                input.email(),
                input.documentNumber(),
                input.mobilePhone(),
                input.companyType(),
                input.monthlyRevenueAmount(),
                input.street(),
                input.number(),
                input.complement(),
                input.neighborhood(),
                input.postalCode()
        );

        ResponseEntity<AsaasCreateAccountResponse> response;
        try {
            response = restClient.post()
                    .uri("/v3/accounts")
                    .header("access_token", this.apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toEntity(AsaasCreateAccountResponse.class);
        } catch (RestClientResponseException ex) {
            logger.error(
                    "AsaasGateway.createPaymentAccount - Request rejected {}",
                    Map.of("organization_key", input.organizationKey(), "status", ex.getStatusCode().value())
            );
            throw new PaymentAccountCreationException();
        } catch (RestClientException ex) {
            logger.error(
                    "AsaasGateway.createPaymentAccount - Request failed {}",
                    Map.of("organization_key", input.organizationKey(), "failure", ex.getClass().getSimpleName())
            );
            throw new PaymentAccountCreationException();
        }

        if (response.getStatusCode() != HttpStatus.OK) {
            logger.error(
                    "AsaasGateway.createPaymentAccount - Unexpected status {}",
                    Map.of("organization_key", input.organizationKey(), "status", response.getStatusCode().value())
            );
            throw new PaymentAccountCreationException();
        }
        var body = response.getBody();
        if (body == null) {
            logger.error(
                    "AsaasGateway.createPaymentAccount - Response without body {}",
                    Map.of("organization_key", input.organizationKey())
            );
            throw new PaymentAccountCreationException();
        }

        return new CreatePaymentAccountOutputDTO(body.id(), body.walletId(), body.accessToken().apiKey());
    }
}
