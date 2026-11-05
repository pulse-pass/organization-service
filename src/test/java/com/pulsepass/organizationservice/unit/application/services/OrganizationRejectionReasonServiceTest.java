package com.pulsepass.organizationservice.unit.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.organizationservice.application.services.OrganizationRejectionReasonService;
import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReason;
import com.pulsepass.organizationservice.domain.repositories.OrganizationRejectionReasonRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizationRejectionReasonServiceTest {

    @Mock
    private OrganizationRejectionReasonRepository organizationRejectionReasonRepository;

    private OrganizationRejectionReasonService service;

    @BeforeEach
    void setUp() {
        service = new OrganizationRejectionReasonService(organizationRejectionReasonRepository);
    }

    @Test
    void listOrganizationRejectionReasons_pageParameters_returnsRepositoryResult() {
        var reasons = List.of(new OrganizationRejectionReason(1L, "INACTIVE_DOCUMENT", "Inactive CNPJ", null));
        when(organizationRejectionReasonRepository.findAll(2, 5)).thenReturn(reasons);

        var result = service.listOrganizationRejectionReasons(2, 5);

        assertThat(result).isSameAs(reasons);
        verify(organizationRejectionReasonRepository).findAll(2, 5);
    }
}
