package com.pulsepass.organizationservice.infrastructure.gateways.onboardingservice;

import com.pulsepass.organizationservice.domain.exceptions.LegalPersonRetrievalException;
import com.pulsepass.organizationservice.domain.gateways.onboarding.OnboardingGateway;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.GetLegalPersonInputDTO;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.GetLegalPersonOutputDTO;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.LegalPersonAddressDTO;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.LegalPersonPhoneNumberDTO;
import com.pulsepass.organizationservice.infrastructure.gateways.onboardingservice.dtos.OnboardingServiceGetLegalPersonResponse;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OnboardingServiceGateway implements OnboardingGateway {

    private static final Logger logger = LoggerFactory.getLogger(OnboardingServiceGateway.class);

    private final RestClient restClient;
    private final String internalToken;

    public OnboardingServiceGateway(
            @Value("${app.onboarding.url}") String url,
            @Value("${app.internal-token}") String internalToken,
            RestClient.Builder restClientBuilder
    ) {
        this.restClient = restClientBuilder
                .baseUrl(url)
                .build();
        this.internalToken = internalToken;
    }

    @Override
    public GetLegalPersonOutputDTO getLegalPerson(GetLegalPersonInputDTO input) {
        logger.info(
                "OnboardingServiceGateway.getLegalPerson - Fetching legal person {}",
                Map.of("legal_person_key", input.legalPersonKey())
        );

        ResponseEntity<OnboardingServiceGetLegalPersonResponse> response;
        try {
            response = restClient.get()
                    .uri("/onboarding/internal/legal_persons/{legal_person_key}", input.legalPersonKey())
                    .header("internal-token", this.internalToken)
                    .retrieve()
                    .toEntity(OnboardingServiceGetLegalPersonResponse.class);
        } catch (RestClientException ex) {
            logger.error(
                    "OnboardingServiceGateway.getLegalPerson - Request failed {}",
                    Map.of("legal_person_key", input.legalPersonKey()),
                    ex
            );
            throw new LegalPersonRetrievalException();
        }

        if (response.getStatusCode() != HttpStatus.OK) {
            logger.error(
                    "OnboardingServiceGateway.getLegalPerson - Unexpected status {}",
                    Map.of("legal_person_key", input.legalPersonKey(), "status", response.getStatusCode().value())
            );
            throw new LegalPersonRetrievalException();
        }
        var body = response.getBody();
        if (body == null) {
            logger.error(
                    "OnboardingServiceGateway.getLegalPerson - Response without body {}",
                    Map.of("legal_person_key", input.legalPersonKey())
            );
            throw new LegalPersonRetrievalException();
        }

        var phoneNumbers = body.phoneNumbers().stream()
                .map(phone -> new LegalPersonPhoneNumberDTO(phone.areaCode(), phone.number()))
                .toList();
        var address = body.legalPersonAddress();
        return new GetLegalPersonOutputDTO(
                body.name(),
                body.documentNumber(),
                body.email(),
                phoneNumbers,
                body.legalPersonCompanyType(),
                body.monthlyRevenueAmount(),
                new LegalPersonAddressDTO(
                        address.postalCode(),
                        address.street(),
                        address.number(),
                        address.complement(),
                        address.neighborhood()
                )
        );
    }
}
