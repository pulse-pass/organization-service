package com.pulsepass.organizationservice.unit.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pulsepass.organizationservice.api.controllers.OrganizationController;
import com.pulsepass.organizationservice.api.exceptions.GlobalExceptionHandler;
import com.pulsepass.organizationservice.api.validators.InternalRequestValidator;
import com.pulsepass.organizationservice.application.dtos.input.CreateOrganizationDTO;
import com.pulsepass.organizationservice.application.services.OrganizationService;
import com.pulsepass.organizationservice.domain.entities.Organization;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import com.pulsepass.organizationservice.domain.exceptions.OrganizationAlreadyExistsException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrganizationController.class)
@Import({InternalRequestValidator.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "app.internal-token=test-token",
        "spring.messages.basename=messages",
        "spring.messages.fallback-to-system-locale=false"
})
class OrganizationControllerTest {

    private static final String URL = "/internal/organizations";
    private static final String TOKEN = "test-token";
    private static final String LEGAL_PERSON_KEY = "7b0f5e6a-1c3d-4e8f-9a2b-0d1e2f3a4b5c";
    private static final String DOCUMENT_NUMBER = "12345678000195";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrganizationService organizationService;

    private static String body(String legalPersonKey, String documentNumber, String name) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        if (legalPersonKey != null) {
            json.append("\"legal_person_key\":\"").append(legalPersonKey).append('"');
            first = false;
        }
        if (documentNumber != null) {
            json.append(first ? "" : ",").append("\"document_number\":\"").append(documentNumber).append('"');
            first = false;
        }
        if (name != null) {
            json.append(first ? "" : ",").append("\"name\":\"").append(name).append('"');
        }
        return json.append('}').toString();
    }

    private static Organization pendingOrganization() {
        Organization organization = new Organization(
                "11111111-2222-4333-8444-555555555555",
                LEGAL_PERSON_KEY,
                DOCUMENT_NUMBER,
                "Acme Events",
                new OrganizationStatus(1L, "PENDING", null));
        return organization;
    }

    @Test
    void createOrganization_validRequest_returns201WithSnakeCaseBody() throws Exception {
        when(organizationService.createOrganization(any(CreateOrganizationDTO.class)))
                .thenReturn(pendingOrganization());

        mockMvc.perform(post(URL)
                        .header("internal-token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(LEGAL_PERSON_KEY, DOCUMENT_NUMBER, "Acme Events")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.organization_key").value("11111111-2222-4333-8444-555555555555"))
                .andExpect(jsonPath("$.legal_person_key").value(LEGAL_PERSON_KEY))
                .andExpect(jsonPath("$.document_number").value(DOCUMENT_NUMBER))
                .andExpect(jsonPath("$.name").value("Acme Events"))
                .andExpect(jsonPath("$.organization_status").value("PENDING"));

        ArgumentCaptor<CreateOrganizationDTO> captor = ArgumentCaptor.forClass(CreateOrganizationDTO.class);
        verify(organizationService).createOrganization(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue())
                .isEqualTo(new CreateOrganizationDTO(LEGAL_PERSON_KEY, DOCUMENT_NUMBER, "Acme Events"));
    }

    static Stream<Arguments> invalidBodies() {
        return Stream.of(
                Arguments.of("legal_person_key missing", body(null, DOCUMENT_NUMBER, "Acme")),
                Arguments.of("legal_person_key empty", body("", DOCUMENT_NUMBER, "Acme")),
                Arguments.of("legal_person_key not uuid", body("not-a-uuid", DOCUMENT_NUMBER, "Acme")),
                Arguments.of("legal_person_key nil uuid",
                        body("00000000-0000-0000-0000-000000000000", DOCUMENT_NUMBER, "Acme")),
                Arguments.of("document_number missing", body(LEGAL_PERSON_KEY, null, "Acme")),
                Arguments.of("document_number 13 digits", body(LEGAL_PERSON_KEY, "1234567800019", "Acme")),
                Arguments.of("document_number with letters", body(LEGAL_PERSON_KEY, "1234567800019A", "Acme")),
                Arguments.of("name missing", body(LEGAL_PERSON_KEY, DOCUMENT_NUMBER, null)),
                Arguments.of("name empty", body(LEGAL_PERSON_KEY, DOCUMENT_NUMBER, "")),
                Arguments.of("name 256 chars", body(LEGAL_PERSON_KEY, DOCUMENT_NUMBER, "a".repeat(256))),
                Arguments.of("malformed json", "{\"legal_person_key\": "),
                Arguments.of("empty body", "")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidBodies")
    void createOrganization_invalidBody_returns400WithBadRequestCode(String scenario, String content)
            throws Exception {
        mockMvc.perform(post(URL)
                        .header("internal-token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN00400"))
                .andExpect(jsonPath("$.title").value("Bad Request"));

        verifyNoInteractions(organizationService);
    }

    @Test
    void createOrganization_upperCaseLegalPersonKey_returns201() throws Exception {
        when(organizationService.createOrganization(any(CreateOrganizationDTO.class)))
                .thenReturn(pendingOrganization());

        mockMvc.perform(post(URL)
                        .header("internal-token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(LEGAL_PERSON_KEY.toUpperCase(), DOCUMENT_NUMBER, "Acme Events")))
                .andExpect(status().isCreated());
    }

    @Test
    void createOrganization_nameWith255Chars_returns201() throws Exception {
        when(organizationService.createOrganization(any(CreateOrganizationDTO.class)))
                .thenReturn(pendingOrganization());

        mockMvc.perform(post(URL)
                        .header("internal-token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(LEGAL_PERSON_KEY, DOCUMENT_NUMBER, "a".repeat(255))))
                .andExpect(status().isCreated());
    }

    @Test
    void createOrganization_serviceThrowsAlreadyExists_returns409() throws Exception {
        when(organizationService.createOrganization(any(CreateOrganizationDTO.class)))
                .thenThrow(new OrganizationAlreadyExistsException());

        mockMvc.perform(post(URL)
                        .header("internal-token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(LEGAL_PERSON_KEY, DOCUMENT_NUMBER, "Acme Events")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORG00001"))
                .andExpect(jsonPath("$.title").value("Conflict"));
    }

    @Test
    void createOrganization_missingTokenHeader_returns401() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(LEGAL_PERSON_KEY, DOCUMENT_NUMBER, "Acme Events")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("GEN00401"));

        verifyNoInteractions(organizationService);
    }

    @Test
    void createOrganization_wrongToken_returns401() throws Exception {
        mockMvc.perform(post(URL)
                        .header("internal-token", "wrong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(LEGAL_PERSON_KEY, DOCUMENT_NUMBER, "Acme Events")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("GEN00401"));

        verifyNoInteractions(organizationService);
    }
}
