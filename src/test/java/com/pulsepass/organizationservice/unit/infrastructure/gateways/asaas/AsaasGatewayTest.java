package com.pulsepass.organizationservice.unit.infrastructure.gateways.asaas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.organizationservice.domain.exceptions.PaymentAccountCreationException;
import com.pulsepass.organizationservice.domain.gateways.paymentprovider.dtos.CreatePaymentAccountInputDTO;
import com.pulsepass.organizationservice.infrastructure.gateways.asaas.AsaasGateway;
import com.pulsepass.organizationservice.unit.infrastructure.gateways.StubHttpServer;
import com.pulsepass.organizationservice.unit.infrastructure.gateways.StubHttpServer.Response;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

class AsaasGatewayTest {

    private static final String PATH = "/v3/accounts";
    private static final String API_KEY = "gateway-api-key";
    private static final String SUCCESS_BODY = """
            {
              "object": "account",
              "id": "account-id",
              "walletId": "wallet-id",
              "accessToken": {"id": "token-id", "apiKey": "created-api-key"}
            }
            """;

    private final JsonMapper mapper = JsonMapper.builder().build();
    private StubHttpServer server;
    private AsaasGateway gateway;

    @BeforeEach
    void setUp() throws Exception {
        server = new StubHttpServer();
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(300));
        gateway = new AsaasGateway(
                server.url(), API_KEY, RestClient.builder().requestFactory(requestFactory));
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    private CreatePaymentAccountInputDTO input(String complement) {
        return new CreatePaymentAccountInputDTO(
                "org-key", "Producoes Exemplo Ltda", "contato@exemplo.com.br", "12345678000195", "51999990000",
                "LIMITED", new BigDecimal("25000.00"), "Rua Ismael Chaves Barcelos", "60", complement,
                "Praia de Belas", "90110220");
    }

    @Test
    void createPaymentAccount_ok_returnsAccountWalletAndNestedApiKey() {
        server.on("POST", PATH, Response.of(200, SUCCESS_BODY));

        var output = gateway.createPaymentAccount(input(null));

        assertThat(output.accountKey()).isEqualTo("account-id");
        assertThat(output.walletKey()).isEqualTo("wallet-id");
        assertThat(output.apiKey()).isEqualTo("created-api-key");
    }

    @Test
    void createPaymentAccount_ok_sendsCamelCaseBodyWithAccessTokenHeaderAndNoComplement() throws Exception {
        server.on("POST", PATH, Response.of(200, SUCCESS_BODY));

        gateway.createPaymentAccount(input(null));

        assertThat(server.received()).hasSize(1);
        var request = server.received().get(0);
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.path()).isEqualTo(PATH);
        assertThat(request.headers().get("access_token")).isEqualTo(API_KEY);
        assertThat(request.headers().get("content-type")).startsWith("application/json");
        var body = mapper.readTree(request.body());
        assertThat(body.propertyNames()).containsExactlyInAnyOrder(
                "name", "email", "loginEmail", "cpfCnpj", "mobilePhone", "companyType", "incomeValue", "address",
                "addressNumber", "province", "postalCode");
        assertThat(body.get("name").asString()).isEqualTo("Producoes Exemplo Ltda");
        assertThat(body.get("email").asString()).isEqualTo("contato@exemplo.com.br");
        assertThat(body.get("loginEmail").asString()).isEqualTo("contato@exemplo.com.br");
        assertThat(body.get("cpfCnpj").asString()).isEqualTo("12345678000195");
        assertThat(body.get("mobilePhone").asString()).isEqualTo("51999990000");
        assertThat(body.get("companyType").asString()).isEqualTo("LIMITED");
        assertThat(body.get("incomeValue").decimalValue()).isEqualByComparingTo(new BigDecimal("25000.00"));
        assertThat(body.get("address").asString()).isEqualTo("Rua Ismael Chaves Barcelos");
        assertThat(body.get("addressNumber").asString()).isEqualTo("60");
        assertThat(body.get("province").asString()).isEqualTo("Praia de Belas");
        assertThat(body.get("postalCode").asString()).isEqualTo("90110220");
    }

    @Test
    void createPaymentAccount_withComplement_sendsComplement() throws Exception {
        server.on("POST", PATH, Response.of(200, SUCCESS_BODY));

        gateway.createPaymentAccount(input("Sala 12"));

        var body = mapper.readTree(server.received().get(0).body());
        assertThat(body.get("complement").asString()).isEqualTo("Sala 12");
    }

    @Test
    void createPaymentAccount_badRequest_throwsPaymentAccountCreationException() {
        server.on("POST", PATH, Response.of(400, "{\"errors\":[{\"code\":\"invalid_object\",\"description\":\"x\"}]}"));

        assertThatThrownBy(() -> gateway.createPaymentAccount(input(null)))
                .isInstanceOf(PaymentAccountCreationException.class);
    }

    @Test
    void createPaymentAccount_unauthorizedWithHtmlBody_throwsPaymentAccountCreationException() {
        server.on("POST", PATH, new Response(401, Map.of("Content-Type", "text/html"), "<html></html>"));

        assertThatThrownBy(() -> gateway.createPaymentAccount(input(null)))
                .isInstanceOf(PaymentAccountCreationException.class);
    }

    @Test
    void createPaymentAccount_serverError_throwsPaymentAccountCreationException() {
        server.on("POST", PATH, Response.empty(500));

        assertThatThrownBy(() -> gateway.createPaymentAccount(input(null)))
                .isInstanceOf(PaymentAccountCreationException.class);
    }

    @Test
    void createPaymentAccount_createdInsteadOfOk_throwsPaymentAccountCreationException() {
        server.on("POST", PATH, Response.of(201, SUCCESS_BODY));

        assertThatThrownBy(() -> gateway.createPaymentAccount(input(null)))
                .isInstanceOf(PaymentAccountCreationException.class);
    }

    @Test
    void createPaymentAccount_okWithEmptyBody_throwsPaymentAccountCreationException() {
        server.on("POST", PATH, Response.empty(200));

        assertThatThrownBy(() -> gateway.createPaymentAccount(input(null)))
                .isInstanceOf(PaymentAccountCreationException.class);
    }

    @Test
    void createPaymentAccount_readTimeoutExceeded_throwsPaymentAccountCreationException() {
        server.on("POST", PATH, Response.of(200, SUCCESS_BODY));
        server.delay("POST", PATH, 1500);

        assertThatThrownBy(() -> gateway.createPaymentAccount(input(null)))
                .isInstanceOf(PaymentAccountCreationException.class);
    }

    @Test
    void createPaymentAccount_serverDown_throwsPaymentAccountCreationException() {
        server.close();

        assertThatThrownBy(() -> gateway.createPaymentAccount(input(null)))
                .isInstanceOf(PaymentAccountCreationException.class);
    }
}
