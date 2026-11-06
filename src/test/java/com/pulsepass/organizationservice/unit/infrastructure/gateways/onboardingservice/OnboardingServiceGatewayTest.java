package com.pulsepass.organizationservice.unit.infrastructure.gateways.onboardingservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.organizationservice.domain.exceptions.LegalPersonRetrievalException;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.GetLegalPersonInputDTO;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.LegalPersonAddressDTO;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.LegalPersonPhoneNumberDTO;
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

    private static final String LEGAL_PERSON_KEY = "7b0f5e6a-1c3d-4e8f-9a2b-0d1e2f3a4b5c";
    private static final String PATH = "/onboarding/internal/legal_persons/" + LEGAL_PERSON_KEY;
    private static final String TOKEN = "internal-secret";
    private static final GetLegalPersonInputDTO INPUT = new GetLegalPersonInputDTO(LEGAL_PERSON_KEY);
    private static final String BODY = """
            {
              "legal_person_key": "7b0f5e6a-1c3d-4e8f-9a2b-0d1e2f3a4b5c",
              "name": "Producoes Exemplo Ltda",
              "document_number": "12345678000195",
              "email": "contato@exemplo.com.br",
              "phone_numbers": [{"area_code": "51", "number": "999990000"}, {"area_code": "11", "number": "888880000"}],
              "legal_person_company_type": "LIMITED",
              "monthly_revenue_amount": 25000.00,
              "legal_person_address": {
                "postal_code": "90110220",
                "street": "Rua Ismael Chaves Barcelos",
                "number": "60",
                "complement": null,
                "neighborhood": "Praia de Belas"
              },
              "created_at": "2026-11-03T22:50:27.442Z",
              "owner": {"name": "Maria"}
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
    void getLegalPerson_ok_returnsLegalPersonData() {
        server.on("GET", PATH, Response.of(200, BODY));

        var output = gateway.getLegalPerson(INPUT);

        assertThat(output.name()).isEqualTo("Producoes Exemplo Ltda");
        assertThat(output.documentNumber()).isEqualTo("12345678000195");
        assertThat(output.email()).isEqualTo("contato@exemplo.com.br");
        assertThat(output.phoneNumbers()).containsExactly(
                new LegalPersonPhoneNumberDTO("51", "999990000"),
                new LegalPersonPhoneNumberDTO("11", "888880000"));
        assertThat(output.legalPersonCompanyType()).isEqualTo("LIMITED");
        assertThat(output.monthlyRevenueAmount()).isEqualByComparingTo(new BigDecimal("25000.00"));
        assertThat(output.legalPersonAddress()).isEqualTo(new LegalPersonAddressDTO(
                "90110220", "Rua Ismael Chaves Barcelos", "60", null, "Praia de Belas"));
    }

    @Test
    void getLegalPerson_ok_sendsGetWithInternalToken() {
        server.on("GET", PATH, Response.of(200, BODY));

        gateway.getLegalPerson(INPUT);

        assertThat(server.received()).hasSize(1);
        var request = server.received().get(0);
        assertThat(request.method()).isEqualTo("GET");
        assertThat(request.path()).isEqualTo(PATH);
        assertThat(request.headers().get("internal-token")).isEqualTo(TOKEN);
    }

    @Test
    void getLegalPerson_notFound_throwsLegalPersonRetrievalException() {
        server.on("GET", PATH, Response.of(404, "{\"code\":\"ONB00005\"}"));

        assertThatThrownBy(() -> gateway.getLegalPerson(INPUT))
                .isInstanceOf(LegalPersonRetrievalException.class);
    }

    @Test
    void getLegalPerson_serverError_throwsLegalPersonRetrievalException() {
        server.on("GET", PATH, Response.of(500, "{\"code\":\"GEN00500\"}"));

        assertThatThrownBy(() -> gateway.getLegalPerson(INPUT))
                .isInstanceOf(LegalPersonRetrievalException.class);
    }

    @Test
    void getLegalPerson_statusNoContent_throwsLegalPersonRetrievalException() {
        server.on("GET", PATH, Response.empty(204));

        assertThatThrownBy(() -> gateway.getLegalPerson(INPUT))
                .isInstanceOf(LegalPersonRetrievalException.class);
    }

    @Test
    void getLegalPerson_okWithEmptyBody_throwsLegalPersonRetrievalException() {
        server.on("GET", PATH, Response.empty(200));

        assertThatThrownBy(() -> gateway.getLegalPerson(INPUT))
                .isInstanceOf(LegalPersonRetrievalException.class);
    }

    @Test
    void getLegalPerson_readTimeoutExceeded_throwsLegalPersonRetrievalException() {
        server.on("GET", PATH, Response.of(200, BODY));
        server.delay("GET", PATH, 1500);

        assertThatThrownBy(() -> gateway.getLegalPerson(INPUT))
                .isInstanceOf(LegalPersonRetrievalException.class);
    }

    @Test
    void getLegalPerson_serverDown_throwsLegalPersonRetrievalException() {
        server.close();

        assertThatThrownBy(() -> gateway.getLegalPerson(INPUT))
                .isInstanceOf(LegalPersonRetrievalException.class);
    }
}
