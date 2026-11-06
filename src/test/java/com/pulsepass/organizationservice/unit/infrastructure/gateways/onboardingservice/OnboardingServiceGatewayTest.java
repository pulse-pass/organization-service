package com.pulsepass.organizationservice.unit.infrastructure.gateways.onboardingservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.organizationservice.domain.exceptions.PersonRetrievalException;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.GetPersonInputDTO;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.LegalPersonAddressDTO;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.PersonPhoneNumberDTO;
import com.pulsepass.organizationservice.infrastructure.gateways.onboardingservice.OnboardingServiceGateway;
import com.pulsepass.organizationservice.unit.infrastructure.gateways.StubHttpServer;
import com.pulsepass.organizationservice.unit.infrastructure.gateways.StubHttpServer.Response;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

class OnboardingServiceGatewayTest {

    private static final String PERSON_KEY = "7b0f5e6a-1c3d-4e8f-9a2b-0d1e2f3a4b5c";
    private static final String PATH = "/onboarding/internal/persons/" + PERSON_KEY;
    private static final String TOKEN = "internal-secret";
    private static final GetPersonInputDTO INPUT = new GetPersonInputDTO(PERSON_KEY);
    private static final String BODY = """
            {
              "person_key": "7b0f5e6a-1c3d-4e8f-9a2b-0d1e2f3a4b5c",
              "person_type": "LEGAL",
              "name": "Producoes Exemplo Ltda",
              "document_number": "12345678000195",
              "created_at": "2026-11-03T22:50:27.442Z",
              "phone_numbers": [
                {"country_code": "55", "area_code": "51", "number": "999990000"},
                {"country_code": "55", "area_code": "11", "number": "888880000"}
              ],
              "legal_person": {
                "email": "contato@exemplo.com.br",
                "legal_person_company_type": "LIMITED",
                "monthly_revenue_amount": 25000.00,
                "legal_person_address": {
                  "postal_code": "90110220",
                  "street": "Rua Ismael Chaves Barcelos",
                  "number": "60",
                  "complement": null,
                  "neighborhood": "Praia de Belas"
                }
              },
              "natural_person": null
            }
            """;

    private StubHttpServer server;
    private OnboardingServiceGateway gateway;

    @BeforeEach
    void setUp() throws Exception {
        server = new StubHttpServer();
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(300));
        gateway = new OnboardingServiceGateway(
                server.url(), TOKEN, RestClient.builder().requestFactory(requestFactory));
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    @Test
    void getPerson_ok_returnsPersonData() {
        server.on("GET", PATH, Response.of(200, BODY));

        var output = gateway.getPerson(INPUT);

        assertThat(output.name()).isEqualTo("Producoes Exemplo Ltda");
        assertThat(output.documentNumber()).isEqualTo("12345678000195");
        assertThat(output.phoneNumbers()).containsExactly(
                new PersonPhoneNumberDTO("51", "999990000"),
                new PersonPhoneNumberDTO("11", "888880000"));
        var legalPerson = output.legalPerson();
        assertThat(legalPerson.email()).isEqualTo("contato@exemplo.com.br");
        assertThat(legalPerson.legalPersonCompanyType()).isEqualTo("LIMITED");
        assertThat(legalPerson.monthlyRevenueAmount()).isEqualByComparingTo(new BigDecimal("25000.00"));
        assertThat(legalPerson.legalPersonAddress()).isEqualTo(new LegalPersonAddressDTO(
                "90110220", "Rua Ismael Chaves Barcelos", "60", null, "Praia de Belas"));
    }

    @Test
    void getPerson_ok_sendsGetWithInternalToken() {
        server.on("GET", PATH, Response.of(200, BODY));

        gateway.getPerson(INPUT);

        assertThat(server.received()).hasSize(1);
        var request = server.received().get(0);
        assertThat(request.method()).isEqualTo("GET");
        assertThat(request.path()).isEqualTo(PATH);
        assertThat(request.headers().get("internal-token")).isEqualTo(TOKEN);
    }

    @Test
    void getPerson_notFound_throwsPersonRetrievalException() {
        server.on("GET", PATH, Response.of(404, "{\"code\":\"ONB00005\"}"));

        assertThatThrownBy(() -> gateway.getPerson(INPUT))
                .isInstanceOf(PersonRetrievalException.class);
    }

    @Test
    void getPerson_serverError_throwsPersonRetrievalException() {
        server.on("GET", PATH, Response.of(500, "{\"code\":\"GEN00500\"}"));

        assertThatThrownBy(() -> gateway.getPerson(INPUT))
                .isInstanceOf(PersonRetrievalException.class);
    }

    @Test
    void getPerson_statusNoContent_throwsPersonRetrievalException() {
        server.on("GET", PATH, Response.empty(204));

        assertThatThrownBy(() -> gateway.getPerson(INPUT))
                .isInstanceOf(PersonRetrievalException.class);
    }

    @Test
    void getPerson_okWithEmptyBody_throwsPersonRetrievalException() {
        server.on("GET", PATH, Response.empty(200));

        assertThatThrownBy(() -> gateway.getPerson(INPUT))
                .isInstanceOf(PersonRetrievalException.class);
    }

    @Test
    void getPerson_readTimeoutExceeded_throwsPersonRetrievalException() {
        server.on("GET", PATH, Response.of(200, BODY));
        server.delay("GET", PATH, 1500);

        assertThatThrownBy(() -> gateway.getPerson(INPUT))
                .isInstanceOf(PersonRetrievalException.class);
    }

    @Test
    void getPerson_serverDown_throwsPersonRetrievalException() {
        server.close();

        assertThatThrownBy(() -> gateway.getPerson(INPUT))
                .isInstanceOf(PersonRetrievalException.class);
    }
}
