package com.pulsepass.organizationservice.infrastructure.gateways.onboardingservice;

import com.pulsepass.organizationservice.domain.exceptions.AccessRevocationException;
import com.pulsepass.organizationservice.domain.exceptions.PersonRetrievalException;
import com.pulsepass.organizationservice.domain.gateways.onboarding.OnboardingGateway;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.DeleteLegalPersonCollaboratorsInputDTO;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.GetPersonInputDTO;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.GetPersonOutputDTO;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.LegalPersonAddressDTO;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.LegalPersonDTO;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.PersonPhoneNumberDTO;
import com.pulsepass.organizationservice.infrastructure.gateways.onboardingservice.dtos.OnboardingServiceGetPersonResponse;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
    public GetPersonOutputDTO getPerson(GetPersonInputDTO input) {
        logger.info(
                "OnboardingServiceGateway.getPerson - Fetching person {}",
                Map.of("person_key", input.personKey())
        );

        ResponseEntity<OnboardingServiceGetPersonResponse> response;
        try {
            response = restClient.get()
                    .uri("/onboarding/internal/persons/{person_key}", input.personKey())
                    .header("internal-token", this.internalToken)
                    .retrieve()
                    .toEntity(OnboardingServiceGetPersonResponse.class);
        } catch (RestClientException ex) {
            logger.error(
                    "OnboardingServiceGateway.getPerson - Request failed {}",
                    Map.of("person_key", input.personKey()),
                    ex
            );
            throw new PersonRetrievalException();
        }

        if (response.getStatusCode() != HttpStatus.OK) {
            logger.error(
                    "OnboardingServiceGateway.getPerson - Unexpected status {}",
                    Map.of("person_key", input.personKey(), "status", response.getStatusCode().value())
            );
            throw new PersonRetrievalException();
        }
        var body = response.getBody();
        if (body == null) {
            logger.error(
                    "OnboardingServiceGateway.getPerson - Response without body {}",
                    Map.of("person_key", input.personKey())
            );
            throw new PersonRetrievalException();
        }

        var phoneNumbers = body.phoneNumbers().stream()
                .map(phone -> new PersonPhoneNumberDTO(phone.areaCode(), phone.number()))
                .toList();
        var legalPerson = body.legalPerson();
        var address = legalPerson.legalPersonAddress();
        return new GetPersonOutputDTO(
                body.name(),
                body.documentNumber(),
                phoneNumbers,
                new LegalPersonDTO(
                        legalPerson.email(),
                        legalPerson.legalPersonCompanyType(),
                        legalPerson.monthlyRevenueAmount(),
                        new LegalPersonAddressDTO(
                                address.postalCode(),
                                address.street(),
                                address.number(),
                                address.complement(),
                                address.neighborhood()
                        )
                )
        );
    }

    @Override
    public void deleteLegalPersonCollaborators(DeleteLegalPersonCollaboratorsInputDTO input) {
        logger.info(
                "OnboardingServiceGateway.deleteLegalPersonCollaborators - Deleting collaborators {}",
                Map.of("legal_person_key", input.legalPersonKey())
        );

        ResponseEntity<Void> response;
        try {
            response = restClient.delete()
                    .uri("/onboarding/internal/legal_persons/{legal_person_key}/collaborators",
                            input.legalPersonKey())
                    .header("internal-token", this.internalToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {})
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            logger.error(
                    "OnboardingServiceGateway.deleteLegalPersonCollaborators - Request failed {}",
                    Map.of("legal_person_key", input.legalPersonKey()),
                    ex
            );
            throw new AccessRevocationException();
        }

        if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
            logger.error(
                    "OnboardingServiceGateway.deleteLegalPersonCollaborators - Unexpected status {}",
                    Map.of("legal_person_key", input.legalPersonKey(), "status", response.getStatusCode().value())
            );
            throw new AccessRevocationException();
        }
    }
}
