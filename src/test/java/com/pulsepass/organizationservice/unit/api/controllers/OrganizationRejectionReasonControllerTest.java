package com.pulsepass.organizationservice.unit.api.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pulsepass.organizationservice.api.controllers.OrganizationRejectionReasonController;
import com.pulsepass.organizationservice.api.exceptions.GlobalExceptionHandler;
import com.pulsepass.organizationservice.api.validators.InternalRequestValidator;
import com.pulsepass.organizationservice.application.services.OrganizationRejectionReasonService;
import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReason;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrganizationRejectionReasonController.class)
@Import({InternalRequestValidator.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "app.internal-token=test-token",
        "spring.messages.basename=messages",
        "spring.messages.fallback-to-system-locale=false"
})
class OrganizationRejectionReasonControllerTest {

    private static final String URL = "/organization/internal/organization_rejection_reasons";
    private static final String TOKEN = "test-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrganizationRejectionReasonService organizationRejectionReasonService;

    @MockitoSpyBean
    private InternalRequestValidator internalRequestValidator;

    @Test
    void listInternalOrganizationRejectionReasons_internalToken_returns200WithItemsAndPagination() throws Exception {
        when(organizationRejectionReasonService.listOrganizationRejectionReasons(1, 30)).thenReturn(List.of(
                new OrganizationRejectionReason(1L, "INACTIVE_DOCUMENT", "Inactive CNPJ", null),
                new OrganizationRejectionReason(2L, "INCONSISTENT_DATA", "Inconsistent data", null)));

        mockMvc.perform(get(URL).header("internal-token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].organization_rejection_reason").value("INACTIVE_DOCUMENT"))
                .andExpect(jsonPath("$.data[0].organization_rejection_reason_description").value("Inactive CNPJ"))
                .andExpect(jsonPath("$.data[1].organization_rejection_reason").value("INCONSISTENT_DATA"))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.page_size").value(30));
    }

    @Test
    void listInternalOrganizationRejectionReasons_pageParameters_forwardsThemToService() throws Exception {
        when(organizationRejectionReasonService.listOrganizationRejectionReasons(2, 5)).thenReturn(List.of());

        mockMvc.perform(get(URL).header("internal-token", TOKEN).param("page", "2").param("page_size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.pagination.page").value(2))
                .andExpect(jsonPath("$.pagination.page_size").value(5));

        verify(organizationRejectionReasonService).listOrganizationRejectionReasons(2, 5);
    }

    @Test
    void listInternalOrganizationRejectionReasons_adminRoleWithoutToken_returns200() throws Exception {
        when(organizationRejectionReasonService.listOrganizationRejectionReasons(1, 30)).thenReturn(List.of());

        mockMvc.perform(get(URL).header("selected-user-roles", "admin"))
                .andExpect(status().isOk());

        verify(internalRequestValidator).validateInternalTokenOrAdmin(null, "admin");
    }

    @Test
    void listInternalOrganizationRejectionReasons_missingToken_returns401() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("GEN00401"));

        verifyNoInteractions(organizationRejectionReasonService);
    }

    @Test
    void listInternalOrganizationRejectionReasons_nonNumericPage_returns400WithParameterField() throws Exception {
        mockMvc.perform(get(URL).header("internal-token", TOKEN).param("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.extra_fields[0].field").value("page"))
                .andExpect(jsonPath("$.extra_fields[0].error").value("INVALID_FORMAT"));

        verifyNoInteractions(organizationRejectionReasonService);
    }

    @ParameterizedTest
    @CsvSource({
            "page,0,page", "page_size,0,page_size", "page_size,101,page_size"
    })
    void listInternalOrganizationRejectionReasons_outOfRangeParameter_returns400WithParameterField(
            String name, String value, String expectedField) throws Exception {
        mockMvc.perform(get(URL).header("internal-token", TOKEN).param(name, value))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN00400"))
                .andExpect(jsonPath("$.extra_fields.length()").value(1))
                .andExpect(jsonPath("$.extra_fields[0].field").value(expectedField))
                .andExpect(jsonPath("$.extra_fields[0].error").value("SIZE"));

        verifyNoInteractions(organizationRejectionReasonService);
    }
}
