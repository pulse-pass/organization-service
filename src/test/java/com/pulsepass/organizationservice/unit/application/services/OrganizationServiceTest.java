package com.pulsepass.organizationservice.unit.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.pulsepass.organizationservice.application.dtos.input.CreateOrganizationDTO;
import com.pulsepass.organizationservice.application.services.OrganizationService;
import com.pulsepass.organizationservice.domain.UnitOfWork;
import com.pulsepass.organizationservice.domain.entities.Organization;
import com.pulsepass.organizationservice.domain.entities.OrganizationHistory;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import com.pulsepass.organizationservice.domain.exceptions.OrganizationAlreadyExistsException;
import com.pulsepass.organizationservice.domain.repositories.OrganizationHistoryRepository;
import com.pulsepass.organizationservice.domain.repositories.OrganizationRepository;
import com.pulsepass.organizationservice.domain.repositories.OrganizationStatusRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    private static final String LEGAL_PERSON_KEY = "7b0f5e6a-1c3d-4e8f-9a2b-0d1e2f3a4b5c";
    private static final String DOCUMENT_NUMBER = "12345678000195";
    private static final String NAME = "Acme Events";

    @Mock
    private UnitOfWork unitOfWork;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationStatusRepository organizationStatusRepository;

    @Mock
    private OrganizationHistoryRepository organizationHistoryRepository;

    private OrganizationService service;
    private OrganizationStatus pendingStatus;
    private CreateOrganizationDTO dto;

    @BeforeEach
    void setUp() {
        service = new OrganizationService(
                unitOfWork, organizationRepository, organizationStatusRepository, organizationHistoryRepository);
        pendingStatus = new OrganizationStatus(1L, "PENDING", null);
        dto = new CreateOrganizationDTO(LEGAL_PERSON_KEY, DOCUMENT_NUMBER, NAME);
    }

    private void stubPendingStatus() {
        when(organizationStatusRepository.findByEnumerator("PENDING")).thenReturn(Optional.of(pendingStatus));
    }

    private void stubNoDuplicates() {
        when(organizationRepository.findByLegalPersonKey(LEGAL_PERSON_KEY)).thenReturn(Optional.empty());
        when(organizationRepository.findByDocumentNumber(DOCUMENT_NUMBER)).thenReturn(Optional.empty());
    }

    @Test
    void createOrganization_validData_returnsSavedPendingOrganization() {
        stubPendingStatus();
        stubNoDuplicates();
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        Organization result = service.createOrganization(dto);

        assertThat(result.getOrganizationKey()).isNotBlank();
        assertThat(result.getLegalPersonKey()).isEqualTo(LEGAL_PERSON_KEY);
        assertThat(result.getDocumentNumber()).isEqualTo(DOCUMENT_NUMBER);
        assertThat(result.getName()).isEqualTo(NAME);
        assertThat(result.getBalanceAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getOrganizationStatus().getEnumerator()).isEqualTo("PENDING");
    }

    @Test
    void createOrganization_validData_savesOrganizationWithPendingStatusAndZeroBalance() {
        stubPendingStatus();
        stubNoDuplicates();
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        service.createOrganization(dto);

        ArgumentCaptor<Organization> captor = ArgumentCaptor.forClass(Organization.class);
        verify(organizationRepository).save(captor.capture());
        Organization saved = captor.getValue();
        assertThat(saved.getOrganizationStatus()).isSameAs(pendingStatus);
        assertThat(saved.getBalanceAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saved.getLegalPersonKey()).isEqualTo(LEGAL_PERSON_KEY);
        assertThat(saved.getDocumentNumber()).isEqualTo(DOCUMENT_NUMBER);
    }

    @Test
    void createOrganization_validData_savesHistoryWithNullOldStatusAndHistoryData() {
        stubPendingStatus();
        stubNoDuplicates();
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        Organization result = service.createOrganization(dto);

        ArgumentCaptor<OrganizationHistory> captor = ArgumentCaptor.forClass(OrganizationHistory.class);
        verify(organizationHistoryRepository).save(captor.capture());
        OrganizationHistory history = captor.getValue();
        assertThat(history.getOrganizationHistoryKey()).isNotBlank();
        assertThat(history.getOrganization()).isSameAs(result);
        assertThat(history.getOldStatus()).isNull();
        assertThat(history.getNewStatus()).isSameAs(pendingStatus);
        assertThat(history.getHistoryData())
                .contains("\"organization_key\":\"" + result.getOrganizationKey() + "\"")
                .contains("\"legal_person_key\":\"" + LEGAL_PERSON_KEY + "\"")
                .contains("\"document_number\":\"" + DOCUMENT_NUMBER + "\"")
                .contains("\"name\":\"" + NAME + "\"")
                .contains("\"balance_amount\":0")
                .contains("\"organization_status\":\"PENDING\"");
    }

    @Test
    void createOrganization_validData_beginsAndCommitsTransactionWithoutRollback() {
        stubPendingStatus();
        stubNoDuplicates();
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        service.createOrganization(dto);

        var order = inOrder(unitOfWork, organizationRepository, organizationHistoryRepository);
        order.verify(unitOfWork).begin();
        order.verify(organizationRepository).save(any(Organization.class));
        order.verify(organizationHistoryRepository).save(any(OrganizationHistory.class));
        order.verify(unitOfWork).commit();
        verify(unitOfWork, never()).rollback();
    }

    @Test
    void createOrganization_legalPersonKeyAlreadyExists_throwsAlreadyExistsWithoutTransaction() {
        when(organizationRepository.findByLegalPersonKey(LEGAL_PERSON_KEY))
                .thenReturn(Optional.of(new Organization()));

        assertThatThrownBy(() -> service.createOrganization(dto))
                .isInstanceOf(OrganizationAlreadyExistsException.class);

        verifyNoInteractions(unitOfWork);
        verifyNoInteractions(organizationStatusRepository);
        verify(organizationRepository, never()).save(any());
        verifyNoInteractions(organizationHistoryRepository);
    }

    @Test
    void createOrganization_documentNumberAlreadyExists_throwsAlreadyExistsWithoutTransaction() {
        when(organizationRepository.findByLegalPersonKey(LEGAL_PERSON_KEY)).thenReturn(Optional.empty());
        when(organizationRepository.findByDocumentNumber(DOCUMENT_NUMBER))
                .thenReturn(Optional.of(new Organization()));

        assertThatThrownBy(() -> service.createOrganization(dto))
                .isInstanceOf(OrganizationAlreadyExistsException.class);

        verifyNoInteractions(unitOfWork);
        verifyNoInteractions(organizationStatusRepository);
        verify(organizationRepository, never()).save(any());
        verifyNoInteractions(organizationHistoryRepository);
    }

    @Test
    void createOrganization_integrityViolationOnSave_throwsAlreadyExistsWithoutCommit() {
        stubPendingStatus();
        stubNoDuplicates();
        when(organizationRepository.save(any(Organization.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service.createOrganization(dto))
                .isInstanceOf(OrganizationAlreadyExistsException.class);

        verify(unitOfWork, never()).commit();
        verifyNoInteractions(organizationHistoryRepository);
    }

    @Test
    void createOrganization_pendingStatusMissing_throwsIllegalStateWithoutBeginningTransaction() {
        when(organizationStatusRepository.findByEnumerator("PENDING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createOrganization(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Organization status PENDING not found");

        verifyNoInteractions(unitOfWork);
        verify(organizationRepository, never()).save(any());
        verifyNoInteractions(organizationHistoryRepository);
    }

    @Test
    void createOrganization_unexpectedRuntimeException_rethrowsWithoutCommit() {
        stubPendingStatus();
        stubNoDuplicates();
        IllegalStateException failure = new IllegalStateException("boom");
        when(organizationRepository.save(any(Organization.class))).thenThrow(failure);

        assertThatThrownBy(() -> service.createOrganization(dto)).isSameAs(failure);

        verify(unitOfWork, never()).commit();
    }
}
