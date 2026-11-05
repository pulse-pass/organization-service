package com.pulsepass.organizationservice.unit.api.controllers;

import static com.pulsepass.organizationservice.domain.entities.OrganizationStatusEnum.PENDING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pulsepass.organizationservice.api.controllers.OrganizationController;
import com.pulsepass.organizationservice.api.exceptions.GlobalExceptionHandler;
import com.pulsepass.organizationservice.api.validators.InternalRequestValidator;
import com.pulsepass.organizationservice.application.dtos.input.CreateOrganizationDTO;
import com.pulsepass.organizationservice.application.dtos.input.UpdateOrganizationDTO;
import com.pulsepass.organizationservice.application.services.OrganizationService;
import com.pulsepass.organizationservice.domain.entities.Organization;
import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReason;
import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReasonEnum;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatusEnum;
import com.pulsepass.organizationservice.domain.exceptions.InvalidOrganizationRejectionReasonException;
import com.pulsepass.organizationservice.domain.exceptions.OrganizationAlreadyExistsException;
import com.pulsepass.organizationservice.domain.exceptions.OrganizationNotFoundException;
import com.pulsepass.organizationservice.domain.exceptions.OrganizationStatusNotAllowedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrganizationController.class)
@Import({InternalRequestValidator.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "app.internal-token=test-token",
        "spring.messages.basename=messages",
        "spring.messages.fallback-to-system-locale=false"
})
class OrganizationControllerTest {

    private static final String INTERNAL_URL = "/organization/internal/organizations";
    private static final String LIST_URL = "/organization/organizations";
    private static final String TOKEN = "test-token";
    private static final String LEGAL_PERSON_KEY = "7b0f5e6a-1c3d-4e8f-9a2b-0d1e2f3a4b5c";
    private static final String DOCUMENT_NUMBER = "12345678000195";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrganizationService organizationService;

    @MockitoSpyBean
    private InternalRequestValidator internalRequestValidator;

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

        mockMvc.perform(post(INTERNAL_URL)
                        .header("internal-token", TOKEN)
                        .header("selected-user-roles", "organizer")
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
        mockMvc.perform(post(INTERNAL_URL)
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

        mockMvc.perform(post(INTERNAL_URL)
                        .header("internal-token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(LEGAL_PERSON_KEY.toUpperCase(), DOCUMENT_NUMBER, "Acme Events")))
                .andExpect(status().isCreated());
    }

    @Test
    void createOrganization_nameWith255Chars_returns201() throws Exception {
        when(organizationService.createOrganization(any(CreateOrganizationDTO.class)))
                .thenReturn(pendingOrganization());

        mockMvc.perform(post(INTERNAL_URL)
                        .header("internal-token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(LEGAL_PERSON_KEY, DOCUMENT_NUMBER, "a".repeat(255))))
                .andExpect(status().isCreated());
    }

    @Test
    void createOrganization_serviceThrowsAlreadyExists_returns409() throws Exception {
        when(organizationService.createOrganization(any(CreateOrganizationDTO.class)))
                .thenThrow(new OrganizationAlreadyExistsException());

        mockMvc.perform(post(INTERNAL_URL)
                        .header("internal-token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(LEGAL_PERSON_KEY, DOCUMENT_NUMBER, "Acme Events")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORG00001"))
                .andExpect(jsonPath("$.title").value("Conflict"));
    }

    @Test
    void createOrganization_adminRoleWithoutToken_returns201() throws Exception {
        when(organizationService.createOrganization(any(CreateOrganizationDTO.class)))
                .thenReturn(pendingOrganization());

        mockMvc.perform(post(INTERNAL_URL)
                        .header("selected-user-roles", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(LEGAL_PERSON_KEY, DOCUMENT_NUMBER, "Acme Events")))
                .andExpect(status().isCreated());

        verify(internalRequestValidator).validateInternalTokenOrAdmin(null, "admin");
    }

    @Test
    void createOrganization_organizerRoleWithoutToken_returns401() throws Exception {
        mockMvc.perform(post(INTERNAL_URL)
                        .header("selected-user-roles", "organizer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(LEGAL_PERSON_KEY, DOCUMENT_NUMBER, "Acme Events")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("GEN00401"));

        verifyNoInteractions(organizationService);
    }

    @Test
    void createOrganization_missingTokenHeader_returns401() throws Exception {
        mockMvc.perform(post(INTERNAL_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(LEGAL_PERSON_KEY, DOCUMENT_NUMBER, "Acme Events")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("GEN00401"));

        verifyNoInteractions(organizationService);
    }

    @Test
    void createOrganization_wrongToken_returns401() throws Exception {
        mockMvc.perform(post(INTERNAL_URL)
                        .header("internal-token", "wrong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(LEGAL_PERSON_KEY, DOCUMENT_NUMBER, "Acme Events")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("GEN00401"));

        verifyNoInteractions(organizationService);
    }

    @Test
    void unknownRoute_returns404WithGen00404() throws Exception {
        mockMvc.perform(get("/unknown_route"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GEN00404"))
                .andExpect(jsonPath("$.title").value("Not Found"));
    }

    @Test
    void createRoute_withPut_returns405WithGen00405() throws Exception {
        mockMvc.perform(put(INTERNAL_URL).header("internal-token", TOKEN))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("GEN00405"))
                .andExpect(jsonPath("$.title").value("Method Not Allowed"));
    }

    @Test
    void createRoute_withTextPlain_returns415WithGen00415() throws Exception {
        mockMvc.perform(post(INTERNAL_URL).header("internal-token", TOKEN)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("GEN00415"))
                .andExpect(jsonPath("$.title").value("Unsupported Media Type"));
    }

    private static final String ORGANIZATION_KEY = "11111111-2222-4333-8444-555555555555";
    private static final String ITEM_URL = INTERNAL_URL + "/" + ORGANIZATION_KEY;

    private static Organization rejectedOrganization() {
        Organization organization = new Organization(
                ORGANIZATION_KEY, LEGAL_PERSON_KEY, DOCUMENT_NUMBER, "Acme Events",
                new OrganizationStatus(3L, "REJECTED", null));
        organization.setOrganizationRejectionReason(
                new OrganizationRejectionReason(1L, "INACTIVE_DOCUMENT", "Inactive CNPJ", null));
        organization.setCreatedAt(LocalDateTime.of(2026, 10, 9, 14, 12));
        organization.setUpdatedAt(LocalDateTime.of(2026, 10, 9, 14, 13));
        return organization;
    }

    @Test
    void updateOrganization_rejectWithReason_returns200WithRejectedBody() throws Exception {
        when(organizationService.updateOrganization(any(String.class), any(UpdateOrganizationDTO.class)))
                .thenReturn(rejectedOrganization());

        mockMvc.perform(patch(ITEM_URL)
                        .header("internal-token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organization_status\":\"REJECTED\","
                                + "\"organization_rejection_reason\":\"INACTIVE_DOCUMENT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organization_key").value(ORGANIZATION_KEY))
                .andExpect(jsonPath("$.organization_status").value("REJECTED"))
                .andExpect(jsonPath("$.organization_rejection_reason").value("INACTIVE_DOCUMENT"))
                .andExpect(jsonPath("$.organization_rejection_reason_description").value("Inactive CNPJ"));

        verify(organizationService).updateOrganization(ORGANIZATION_KEY, new UpdateOrganizationDTO(
                OrganizationStatusEnum.REJECTED, OrganizationRejectionReasonEnum.INACTIVE_DOCUMENT));
    }

    @Test
    void updateOrganization_approveWithNullReason_returns200AndForwardsDto() throws Exception {
        when(organizationService.updateOrganization(any(String.class), any(UpdateOrganizationDTO.class)))
                .thenReturn(rejectedOrganization());

        mockMvc.perform(patch(ITEM_URL)
                        .header("internal-token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organization_status\":\"APPROVED\",\"organization_rejection_reason\":null}"))
                .andExpect(status().isOk());

        verify(organizationService).updateOrganization(
                ORGANIZATION_KEY, new UpdateOrganizationDTO(OrganizationStatusEnum.APPROVED, null));
    }

    @Test
    void updateOrganization_adminRoleWithoutToken_returns200() throws Exception {
        when(organizationService.updateOrganization(any(String.class), any(UpdateOrganizationDTO.class)))
                .thenReturn(rejectedOrganization());

        mockMvc.perform(patch(ITEM_URL)
                        .header("selected-user-roles", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organization_status\":\"APPROVED\"}"))
                .andExpect(status().isOk());

        verify(internalRequestValidator).validateInternalTokenOrAdmin(null, "admin");
    }

    @Test
    void updateOrganization_missingToken_returns401() throws Exception {
        mockMvc.perform(patch(ITEM_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organization_status\":\"APPROVED\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("GEN00401"));

        verifyNoInteractions(organizationService);
    }

    @Test
    void updateOrganization_missingStatus_returns400WithRequired() throws Exception {
        mockMvc.perform(patch(ITEM_URL)
                        .header("internal-token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN00400"))
                .andExpect(jsonPath("$.extra_fields[0].field").value("organization_status"))
                .andExpect(jsonPath("$.extra_fields[0].error").value("REQUIRED"));

        verifyNoInteractions(organizationService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"PENDING", "DELETED", "approved", "OTHER"})
    void updateOrganization_statusOutsideAllowedValues_returns400WithInvalidFormat(String status) throws Exception {
        mockMvc.perform(patch(ITEM_URL)
                        .header("internal-token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organization_status\":\"" + status + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.extra_fields[0].field").value("body"))
                .andExpect(jsonPath("$.extra_fields[0].error").value("INVALID_FORMAT"));

        verifyNoInteractions(organizationService);
    }

    @Test
    void updateOrganization_reasonOutsideEnum_returns400WithBodyInvalidFormat() throws Exception {
        mockMvc.perform(patch(ITEM_URL)
                        .header("internal-token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organization_status\":\"REJECTED\","
                                + "\"organization_rejection_reason\":\"OTHER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.extra_fields.length()").value(1))
                .andExpect(jsonPath("$.extra_fields[0].field").value("body"))
                .andExpect(jsonPath("$.extra_fields[0].error").value("INVALID_FORMAT"));

        verifyNoInteractions(organizationService);
    }

    @Test
    void updateOrganization_serviceThrowsNotFound_returns404() throws Exception {
        when(organizationService.updateOrganization(any(String.class), any(UpdateOrganizationDTO.class)))
                .thenThrow(new OrganizationNotFoundException());

        mockMvc.perform(patch(ITEM_URL)
                        .header("internal-token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organization_status\":\"APPROVED\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORG00002"));
    }

    @Test
    void updateOrganization_serviceThrowsStatusNotAllowed_returns422() throws Exception {
        when(organizationService.updateOrganization(any(String.class), any(UpdateOrganizationDTO.class)))
                .thenThrow(new OrganizationStatusNotAllowedException());

        mockMvc.perform(patch(ITEM_URL)
                        .header("internal-token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organization_status\":\"APPROVED\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("ORG00003"));
    }

    @Test
    void updateOrganization_serviceThrowsInvalidReason_returns422() throws Exception {
        when(organizationService.updateOrganization(any(String.class), any(UpdateOrganizationDTO.class)))
                .thenThrow(new InvalidOrganizationRejectionReasonException());

        mockMvc.perform(patch(ITEM_URL)
                        .header("internal-token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organization_status\":\"REJECTED\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("ORG00004"));
    }

    @Test
    void deleteOrganization_internalToken_returns204WithoutBody() throws Exception {
        mockMvc.perform(delete(ITEM_URL).header("internal-token", TOKEN))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(organizationService).deleteOrganization(ORGANIZATION_KEY);
    }

    @Test
    void deleteOrganization_adminRoleWithoutToken_returns204() throws Exception {
        mockMvc.perform(delete(ITEM_URL).header("selected-user-roles", "admin"))
                .andExpect(status().isNoContent());

        verify(internalRequestValidator).validateInternalTokenOrAdmin(null, "admin");
    }

    @Test
    void deleteOrganization_missingToken_returns401() throws Exception {
        mockMvc.perform(delete(ITEM_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("GEN00401"));

        verifyNoInteractions(organizationService);
    }

    @Test
    void deleteOrganization_serviceThrowsNotFound_returns404() throws Exception {
        doThrow(new OrganizationNotFoundException()).when(organizationService).deleteOrganization(ORGANIZATION_KEY);

        mockMvc.perform(delete(ITEM_URL).header("internal-token", TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORG00002"));
    }

    @Test
    void deleteOrganization_serviceThrowsStatusNotAllowed_returns422() throws Exception {
        doThrow(new OrganizationStatusNotAllowedException())
                .when(organizationService).deleteOrganization(ORGANIZATION_KEY);

        mockMvc.perform(delete(ITEM_URL).header("internal-token", TOKEN))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("ORG00003"));
    }

    @Test
    void listOrganizations_pendingOrganization_returns200WithItemAndPagination() throws Exception {
        Organization organization = pendingOrganization();
        organization.setCreatedAt(LocalDateTime.of(2026, 10, 9, 14, 12));
        organization.setUpdatedAt(LocalDateTime.of(2026, 10, 9, 14, 13));
        when(organizationService.listOrganizations(LEGAL_PERSON_KEY, 1, 30)).thenReturn(List.of(organization));

        mockMvc.perform(get(LIST_URL).header("selected-agent-key", LEGAL_PERSON_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].organization_key").value("11111111-2222-4333-8444-555555555555"))
                .andExpect(jsonPath("$.data[0].legal_person_key").value(LEGAL_PERSON_KEY))
                .andExpect(jsonPath("$.data[0].name").value("Acme Events"))
                .andExpect(jsonPath("$.data[0].document_number").value(DOCUMENT_NUMBER))
                .andExpect(jsonPath("$.data[0].organization_status").value("PENDING"))
                .andExpect(jsonPath("$.data[0].organization_rejection_reason").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.data[0].organization_rejection_reason_description")
                        .value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.data[0].created_at").value("2026-10-09T14:12:00Z"))
                .andExpect(jsonPath("$.data[0].updated_at").value("2026-10-09T14:13:00Z"))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.page_size").value(30));
    }

    @Test
    void listOrganizations_rejectedOrganization_returnsRejectionReason() throws Exception {
        Organization organization = pendingOrganization();
        organization.setOrganizationStatus(new OrganizationStatus(3L, "REJECTED", null));
        organization.setOrganizationRejectionReason(
                new OrganizationRejectionReason(1L, "INACTIVE_DOCUMENT", "Inactive CNPJ", null));
        organization.setCreatedAt(LocalDateTime.of(2026, 10, 9, 14, 12));
        organization.setUpdatedAt(LocalDateTime.of(2026, 10, 9, 14, 13));
        when(organizationService.listOrganizations(LEGAL_PERSON_KEY, 1, 30)).thenReturn(List.of(organization));

        mockMvc.perform(get(LIST_URL).header("selected-agent-key", LEGAL_PERSON_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].organization_status").value("REJECTED"))
                .andExpect(jsonPath("$.data[0].organization_rejection_reason").value("INACTIVE_DOCUMENT"))
                .andExpect(jsonPath("$.data[0].organization_rejection_reason_description").value("Inactive CNPJ"));
    }

    @Test
    void listOrganizations_pageParameters_forwardsThemToService() throws Exception {
        when(organizationService.listOrganizations(LEGAL_PERSON_KEY, 2, 5)).thenReturn(List.of());

        mockMvc.perform(get(LIST_URL)
                        .header("selected-agent-key", LEGAL_PERSON_KEY)
                        .param("page", "2")
                        .param("page_size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.pagination.page").value(2))
                .andExpect(jsonPath("$.pagination.page_size").value(5));

        verify(organizationService).listOrganizations(LEGAL_PERSON_KEY, 2, 5);
    }

    @Test
    void listOrganizations_validRequest_callsValidatorWithSelectedAgentKey() throws Exception {
        when(organizationService.listOrganizations(LEGAL_PERSON_KEY, 1, 30)).thenReturn(List.of());

        mockMvc.perform(get(LIST_URL).header("selected-agent-key", LEGAL_PERSON_KEY))
                .andExpect(status().isOk());

        verify(internalRequestValidator).validateSelectedAgentKey(LEGAL_PERSON_KEY);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void listOrganizations_blankSelectedAgentKey_returns401WithGen00401(String selectedAgentKey) throws Exception {
        mockMvc.perform(get(LIST_URL).header("selected-agent-key", selectedAgentKey))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("GEN00401"))
                .andExpect(jsonPath("$.extra_fields.length()").value(0));

        verify(internalRequestValidator).validateSelectedAgentKey(selectedAgentKey);
        verifyNoInteractions(organizationService);
    }

    @Test
    void listOrganizations_missingSelectedAgentKey_returns401WithGen00401() throws Exception {
        mockMvc.perform(get(LIST_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("GEN00401"));

        verify(internalRequestValidator).validateSelectedAgentKey(null);
        verifyNoInteractions(organizationService);
    }

    @Test
    void listOrganizations_nonNumericPage_returns400WithParameterField() throws Exception {
        mockMvc.perform(get(LIST_URL)
                        .header("selected-agent-key", LEGAL_PERSON_KEY)
                        .param("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN00400"))
                .andExpect(jsonPath("$.extra_fields[0].field").value("page"))
                .andExpect(jsonPath("$.extra_fields[0].error").value("INVALID_FORMAT"));

        verifyNoInteractions(organizationService);
    }

    @Test
    void listOrganizations_nonNumericPageSize_returns400WithParameterField() throws Exception {
        mockMvc.perform(get(LIST_URL)
                        .header("selected-agent-key", LEGAL_PERSON_KEY)
                        .param("page_size", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.extra_fields[0].field").value("page_size"));

        verifyNoInteractions(organizationService);
    }

    @ParameterizedTest
    @CsvSource({
            "page,0,page", "page,-1,page", "page_size,0,page_size", "page_size,-1,page_size", "page_size,101,page_size"
    })
    void listOrganizations_outOfRangeParameter_returns400WithParameterField(
            String name, String value, String expectedField) throws Exception {
        mockMvc.perform(get(LIST_URL)
                        .header("selected-agent-key", LEGAL_PERSON_KEY)
                        .param(name, value))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN00400"))
                .andExpect(jsonPath("$.extra_fields.length()").value(1))
                .andExpect(jsonPath("$.extra_fields[0].field").value(expectedField))
                .andExpect(jsonPath("$.extra_fields[0].error").value("SIZE"));

        verifyNoInteractions(organizationService);
    }

    @Test
    void listOrganizations_pageSizeAtLimit_returns200() throws Exception {
        when(organizationService.listOrganizations(LEGAL_PERSON_KEY, 1, 100)).thenReturn(List.of());

        mockMvc.perform(get(LIST_URL)
                        .header("selected-agent-key", LEGAL_PERSON_KEY)
                        .param("page_size", "100"))
                .andExpect(status().isOk());

        verify(organizationService).listOrganizations(LEGAL_PERSON_KEY, 1, 100);
    }

    @Test
    void listInternalOrganizations_validToken_returns200WithItemAndPagination() throws Exception {
        Organization organization = pendingOrganization();
        organization.setCreatedAt(LocalDateTime.of(2026, 10, 9, 14, 12));
        organization.setUpdatedAt(LocalDateTime.of(2026, 10, 9, 14, 13));
        when(organizationService.listInternalOrganizations(null, null, null, 1, 30)).thenReturn(List.of(organization));

        mockMvc.perform(get(INTERNAL_URL).header("internal-token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].organization_key").value("11111111-2222-4333-8444-555555555555"))
                .andExpect(jsonPath("$.data[0].legal_person_key").value(LEGAL_PERSON_KEY))
                .andExpect(jsonPath("$.data[0].name").value("Acme Events"))
                .andExpect(jsonPath("$.data[0].document_number").value(DOCUMENT_NUMBER))
                .andExpect(jsonPath("$.data[0].organization_status").value("PENDING"))
                .andExpect(jsonPath("$.data[0].created_at").value("2026-10-09T14:12:00Z"))
                .andExpect(jsonPath("$.data[0].updated_at").value("2026-10-09T14:13:00Z"))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.page_size").value(30));

        verify(internalRequestValidator).validateInternalTokenOrAdmin(TOKEN, null);
    }

    @Test
    void listInternalOrganizations_adminRoleWithoutToken_returns200() throws Exception {
        when(organizationService.listInternalOrganizations(null, null, null, 1, 30)).thenReturn(List.of());

        mockMvc.perform(get(INTERNAL_URL).header("selected-user-roles", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(internalRequestValidator).validateInternalTokenOrAdmin(null, "admin");
    }

    @Test
    void listInternalOrganizations_filtersAndPage_forwardsThemToService() throws Exception {
        when(organizationService.listInternalOrganizations(PENDING, "acme", "12345678000199", 2, 5))
                .thenReturn(List.of());

        mockMvc.perform(get(INTERNAL_URL)
                        .header("internal-token", TOKEN)
                        .param("organization_status", "PENDING")
                        .param("name", "acme")
                        .param("document_number", "12345678000199")
                        .param("page", "2")
                        .param("page_size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.page").value(2))
                .andExpect(jsonPath("$.pagination.page_size").value(5));

        verify(organizationService).listInternalOrganizations(PENDING, "acme", "12345678000199", 2, 5);
    }

    @Test
    void listInternalOrganizations_noTokenAndNoAdminRole_returns401WithGen00401() throws Exception {
        mockMvc.perform(get(INTERNAL_URL).header("selected-user-roles", "organizer"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("GEN00401"));

        verify(internalRequestValidator).validateInternalTokenOrAdmin(null, "organizer");
        verifyNoInteractions(organizationService);
    }

    @ParameterizedTest
    @CsvSource({"page,0,page", "page_size,0,page_size", "page_size,101,page_size"})
    void listInternalOrganizations_outOfRangeParameter_returns400WithParameterField(
            String name, String value, String expectedField) throws Exception {
        mockMvc.perform(get(INTERNAL_URL).header("internal-token", TOKEN).param(name, value))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN00400"))
                .andExpect(jsonPath("$.extra_fields[0].field").value(expectedField))
                .andExpect(jsonPath("$.extra_fields[0].error").value("SIZE"));

        verifyNoInteractions(organizationService);
    }

    @Test
    void listInternalOrganizations_unknownOrganizationStatus_returns400WithInvalidFormat() throws Exception {
        mockMvc.perform(get(INTERNAL_URL).header("internal-token", TOKEN).param("organization_status", "FOO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN00400"))
                .andExpect(jsonPath("$.extra_fields[0].field").value("organization_status"))
                .andExpect(jsonPath("$.extra_fields[0].error").value("INVALID_FORMAT"));

        verifyNoInteractions(organizationService);
    }
}
