package com.pulsepass.organizationservice.unit.application.services;

import static com.pulsepass.organizationservice.domain.entities.OrganizationStatusEnum.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.pulsepass.organizationservice.application.dtos.input.CreateOrganizationDTO;
import com.pulsepass.organizationservice.application.dtos.input.UpdateOrganizationDTO;
import com.pulsepass.organizationservice.application.services.OrganizationService;
import com.pulsepass.organizationservice.domain.SecretCipher;
import com.pulsepass.organizationservice.domain.UnitOfWork;
import com.pulsepass.organizationservice.domain.entities.Organization;
import com.pulsepass.organizationservice.domain.entities.OrganizationHistory;
import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReason;
import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReasonEnum;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatusEnum;
import com.pulsepass.organizationservice.domain.exceptions.InvalidOrganizationRejectionReasonException;
import com.pulsepass.organizationservice.domain.exceptions.LegalPersonRetrievalException;
import com.pulsepass.organizationservice.domain.exceptions.OrganizationAlreadyExistsException;
import com.pulsepass.organizationservice.domain.exceptions.OrganizationNotFoundException;
import com.pulsepass.organizationservice.domain.exceptions.OrganizationStatusNotAllowedException;
import com.pulsepass.organizationservice.domain.exceptions.PaymentAccountCreationException;
import com.pulsepass.organizationservice.domain.gateways.onboarding.OnboardingGateway;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.GetLegalPersonInputDTO;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.GetLegalPersonOutputDTO;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.LegalPersonAddressDTO;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.LegalPersonPhoneNumberDTO;
import com.pulsepass.organizationservice.domain.gateways.paymentprovider.PaymentProviderGateway;
import com.pulsepass.organizationservice.domain.gateways.paymentprovider.dtos.CreatePaymentAccountInputDTO;
import com.pulsepass.organizationservice.domain.gateways.paymentprovider.dtos.CreatePaymentAccountOutputDTO;
import com.pulsepass.organizationservice.domain.repositories.OrganizationHistoryRepository;
import com.pulsepass.organizationservice.domain.repositories.OrganizationRejectionReasonRepository;
import com.pulsepass.organizationservice.domain.repositories.OrganizationRepository;
import com.pulsepass.organizationservice.domain.repositories.OrganizationStatusRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

    @Mock
    private OrganizationRejectionReasonRepository organizationRejectionReasonRepository;

    @Mock
    private OnboardingGateway onboardingGateway;

    @Mock
    private PaymentProviderGateway paymentProviderGateway;

    @Mock
    private SecretCipher secretCipher;

    private OrganizationService service;
    private OrganizationStatus pendingStatus;
    private CreateOrganizationDTO dto;

    @BeforeEach
    void setUp() {
        service = new OrganizationService(
                unitOfWork, organizationRepository, organizationStatusRepository, organizationHistoryRepository,
                organizationRejectionReasonRepository, onboardingGateway, paymentProviderGateway, secretCipher);
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

    private Organization organizationWithStatus(String enumerator) {
        return new Organization(
                1L, "org-key", LEGAL_PERSON_KEY, null, null, DOCUMENT_NUMBER, NAME, null, BigDecimal.ZERO,
                null, new OrganizationStatus(1L, enumerator, null), null, null);
    }

    private void stubOrganization(String enumerator) {
        when(organizationRepository.findForUpdateByOrganizationKey("org-key"))
                .thenReturn(Optional.of(organizationWithStatus(enumerator)));
    }

    private void stubStatus(String enumerator) {
        when(organizationStatusRepository.findByEnumerator(enumerator))
                .thenReturn(Optional.of(new OrganizationStatus(2L, enumerator, null)));
    }

    private void stubRejectionReason() {
        when(organizationRejectionReasonRepository.findByEnumerator("INACTIVE_DOCUMENT"))
                .thenReturn(Optional.of(
                        new OrganizationRejectionReason(1L, "INACTIVE_DOCUMENT", "Inactive CNPJ", null)));
    }

    private GetLegalPersonOutputDTO legalPersonOutput() {
        return new GetLegalPersonOutputDTO(
                "Acme Events Ltda",
                DOCUMENT_NUMBER,
                "contact@acme.com",
                List.of(
                        new LegalPersonPhoneNumberDTO("51", "999990000"),
                        new LegalPersonPhoneNumberDTO("11", "888880000")),
                "LIMITED",
                new BigDecimal("25000.00"),
                new LegalPersonAddressDTO("90110220", "Rua Ismael Chaves Barcelos", "60", null, "Praia de Belas"));
    }

    private void stubLegalPerson() {
        when(onboardingGateway.getLegalPerson(new GetLegalPersonInputDTO(LEGAL_PERSON_KEY)))
                .thenReturn(legalPersonOutput());
    }

    @Test
    void updateOrganization_approve_savesApprovedOrganizationWithExternalIdentifiersAndNullReason() {
        stubOrganization("PENDING");
        stubStatus("APPROVED");
        stubStatus("ERROR");
        stubLegalPerson();
        when(paymentProviderGateway.createPaymentAccount(any()))
                .thenReturn(new CreatePaymentAccountOutputDTO("account-key", "wallet-key", "plain-api-key"));
        when(secretCipher.encrypt("plain-api-key")).thenReturn("encrypted-api-key");
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        Organization result = service.updateOrganization(
                "org-key", new UpdateOrganizationDTO(OrganizationStatusEnum.APPROVED, null));

        assertThat(result.getOrganizationStatus().getEnumerator()).isEqualTo("APPROVED");
        assertThat(result.getOrganizationRejectionReason()).isNull();
        assertThat(result.getExternalAccountKey()).isEqualTo("account-key");
        assertThat(result.getExternalWalletKey()).isEqualTo("wallet-key");
        assertThat(result.getExternalApiKey()).isEqualTo("encrypted-api-key");
        verifyNoInteractions(organizationRejectionReasonRepository);
    }

    @Test
    void updateOrganization_approve_sendsLegalPersonDataToPaymentProviderGateway() {
        stubOrganization("PENDING");
        stubStatus("APPROVED");
        stubStatus("ERROR");
        stubLegalPerson();
        when(paymentProviderGateway.createPaymentAccount(any()))
                .thenReturn(new CreatePaymentAccountOutputDTO("account-key", "wallet-key", "plain-api-key"));
        when(secretCipher.encrypt("plain-api-key")).thenReturn("encrypted-api-key");
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        service.updateOrganization("org-key", new UpdateOrganizationDTO(OrganizationStatusEnum.APPROVED, null));

        ArgumentCaptor<CreatePaymentAccountInputDTO> captor =
                ArgumentCaptor.forClass(CreatePaymentAccountInputDTO.class);
        verify(paymentProviderGateway).createPaymentAccount(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new CreatePaymentAccountInputDTO(
                "org-key", "Acme Events Ltda", "contact@acme.com", DOCUMENT_NUMBER, "51999990000", "LIMITED",
                new BigDecimal("25000.00"), "Rua Ismael Chaves Barcelos", "60", null, "Praia de Belas", "90110220"));
    }

    @Test
    void updateOrganization_approve_savesHistoryWithOldAndNewStatusInsideTransaction() {
        stubOrganization("PENDING");
        stubStatus("APPROVED");
        stubStatus("ERROR");
        stubLegalPerson();
        when(paymentProviderGateway.createPaymentAccount(any()))
                .thenReturn(new CreatePaymentAccountOutputDTO("account-key", "wallet-key", "plain-api-key"));
        when(secretCipher.encrypt("plain-api-key")).thenReturn("encrypted-api-key");
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        service.updateOrganization("org-key", new UpdateOrganizationDTO(OrganizationStatusEnum.APPROVED, null));

        ArgumentCaptor<OrganizationHistory> captor = ArgumentCaptor.forClass(OrganizationHistory.class);
        var order = inOrder(unitOfWork, organizationRepository, onboardingGateway, paymentProviderGateway,
                organizationHistoryRepository);
        order.verify(unitOfWork).begin();
        order.verify(organizationRepository).findForUpdateByOrganizationKey("org-key");
        order.verify(onboardingGateway).getLegalPerson(any());
        order.verify(paymentProviderGateway).createPaymentAccount(any());
        order.verify(organizationRepository).save(any(Organization.class));
        order.verify(organizationHistoryRepository).save(captor.capture());
        order.verify(unitOfWork).commit();
        verify(unitOfWork, never()).rollback();
        assertThat(captor.getValue().getOldStatus().getEnumerator()).isEqualTo("PENDING");
        assertThat(captor.getValue().getNewStatus().getEnumerator()).isEqualTo("APPROVED");
        assertThat(captor.getValue().getHistoryData()).contains("\"organization_status\":\"APPROVED\"");
    }

    @Test
    void updateOrganization_unexpectedOnboardingFailure_throwsDomainExceptionWithoutCommitOrPaymentCall() {
        stubOrganization("PENDING");
        stubStatus("APPROVED");
        stubStatus("ERROR");
        when(onboardingGateway.getLegalPerson(any())).thenThrow(new NullPointerException());

        assertThatThrownBy(() -> service.updateOrganization(
                "org-key", new UpdateOrganizationDTO(OrganizationStatusEnum.APPROVED, null)))
                .isInstanceOf(LegalPersonRetrievalException.class);

        verify(unitOfWork, never()).commit();
        verifyNoInteractions(paymentProviderGateway);
    }

    @Test
    void updateOrganization_unexpectedPaymentProviderFailure_savesErrorStatusCommitsAndThrowsDomainException() {
        stubOrganization("PENDING");
        stubStatus("APPROVED");
        stubStatus("ERROR");
        stubLegalPerson();
        when(paymentProviderGateway.createPaymentAccount(any())).thenThrow(new NullPointerException());
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> service.updateOrganization(
                "org-key", new UpdateOrganizationDTO(OrganizationStatusEnum.APPROVED, null)))
                .isInstanceOf(PaymentAccountCreationException.class);

        ArgumentCaptor<Organization> organizationCaptor = ArgumentCaptor.forClass(Organization.class);
        verify(organizationRepository).save(organizationCaptor.capture());
        verify(unitOfWork).commit();
        assertThat(organizationCaptor.getValue().getOrganizationStatus().getEnumerator()).isEqualTo("ERROR");
    }

    @Test
    void updateOrganization_paymentAccountCreationFails_savesErrorStatusWithHistoryCommitsAndRethrows() {
        stubOrganization("PENDING");
        stubStatus("APPROVED");
        stubStatus("ERROR");
        stubLegalPerson();
        when(paymentProviderGateway.createPaymentAccount(any())).thenThrow(new PaymentAccountCreationException());
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> service.updateOrganization(
                "org-key", new UpdateOrganizationDTO(OrganizationStatusEnum.APPROVED, null)))
                .isInstanceOf(PaymentAccountCreationException.class);

        ArgumentCaptor<Organization> organizationCaptor = ArgumentCaptor.forClass(Organization.class);
        ArgumentCaptor<OrganizationHistory> historyCaptor = ArgumentCaptor.forClass(OrganizationHistory.class);
        var order = inOrder(organizationRepository, organizationHistoryRepository, unitOfWork);
        order.verify(organizationRepository).save(organizationCaptor.capture());
        order.verify(organizationHistoryRepository).save(historyCaptor.capture());
        order.verify(unitOfWork).commit();
        assertThat(organizationCaptor.getValue().getOrganizationStatus().getEnumerator()).isEqualTo("ERROR");
        assertThat(organizationCaptor.getValue().getExternalAccountKey()).isNull();
        assertThat(organizationCaptor.getValue().getExternalWalletKey()).isNull();
        assertThat(organizationCaptor.getValue().getExternalApiKey()).isNull();
        assertThat(historyCaptor.getValue().getOldStatus().getEnumerator()).isEqualTo("PENDING");
        assertThat(historyCaptor.getValue().getNewStatus().getEnumerator()).isEqualTo("ERROR");
        verifyNoInteractions(secretCipher);
    }

    @Test
    void updateOrganization_legalPersonFetchFails_rethrowsWithoutSavingNorCommitting() {
        stubOrganization("PENDING");
        stubStatus("APPROVED");
        stubStatus("ERROR");
        when(onboardingGateway.getLegalPerson(new GetLegalPersonInputDTO(LEGAL_PERSON_KEY)))
                .thenThrow(new LegalPersonRetrievalException());

        assertThatThrownBy(() -> service.updateOrganization(
                "org-key", new UpdateOrganizationDTO(OrganizationStatusEnum.APPROVED, null)))
                .isInstanceOf(LegalPersonRetrievalException.class);

        verify(unitOfWork).begin();
        verify(unitOfWork, never()).commit();
        verify(organizationRepository, never()).save(any());
        verifyNoInteractions(paymentProviderGateway, organizationHistoryRepository, secretCipher);
    }

    @Test
    void updateOrganization_rejectWithReason_savesRejectedOrganizationWithReason() {
        stubStatus("ERROR");
        stubOrganization("PENDING");
        stubStatus("REJECTED");
        stubRejectionReason();
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        Organization result = service.updateOrganization("org-key", new UpdateOrganizationDTO(
                OrganizationStatusEnum.REJECTED, OrganizationRejectionReasonEnum.INACTIVE_DOCUMENT));

        assertThat(result.getOrganizationStatus().getEnumerator()).isEqualTo("REJECTED");
        assertThat(result.getOrganizationRejectionReason().getEnumerator()).isEqualTo("INACTIVE_DOCUMENT");
        verifyNoInteractions(onboardingGateway, paymentProviderGateway, secretCipher);
    }

    @Test
    void updateOrganization_unknownKey_throwsNotFoundWithoutCommit() {
        stubStatus("ERROR");
        stubStatus("APPROVED");
        when(organizationRepository.findForUpdateByOrganizationKey("org-key")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateOrganization(
                "org-key", new UpdateOrganizationDTO(OrganizationStatusEnum.APPROVED, null)))
                .isInstanceOf(OrganizationNotFoundException.class);

        verify(unitOfWork).begin();
        verify(unitOfWork, never()).commit();
        verifyNoInteractions(organizationHistoryRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"APPROVED", "REJECTED", "DELETED", "ERROR"})
    void updateOrganization_notPending_throwsStatusNotAllowedWithoutCommit(String currentStatus) {
        stubStatus("ERROR");
        stubOrganization(currentStatus);
        stubStatus("APPROVED");

        assertThatThrownBy(() -> service.updateOrganization(
                "org-key", new UpdateOrganizationDTO(OrganizationStatusEnum.APPROVED, null)))
                .isInstanceOf(OrganizationStatusNotAllowedException.class);

        verify(unitOfWork).begin();
        verify(unitOfWork, never()).commit();
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void updateOrganization_notPendingWithInvalidReason_throwsStatusNotAllowedFirst() {
        stubStatus("ERROR");
        stubOrganization("APPROVED");
        stubStatus("REJECTED");

        assertThatThrownBy(() -> service.updateOrganization(
                "org-key", new UpdateOrganizationDTO(OrganizationStatusEnum.REJECTED, null)))
                .isInstanceOf(OrganizationStatusNotAllowedException.class);
    }

    @Test
    void updateOrganization_rejectWithoutReason_throwsInvalidReasonWithoutCommit() {
        stubStatus("ERROR");
        stubOrganization("PENDING");
        stubStatus("REJECTED");

        assertThatThrownBy(() -> service.updateOrganization(
                "org-key", new UpdateOrganizationDTO(OrganizationStatusEnum.REJECTED, null)))
                .isInstanceOf(InvalidOrganizationRejectionReasonException.class);

        verify(unitOfWork).begin();
        verify(unitOfWork, never()).commit();
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void updateOrganization_approveWithReason_throwsInvalidReasonWithoutCommit() {
        stubStatus("ERROR");
        stubOrganization("PENDING");
        stubStatus("APPROVED");
        stubRejectionReason();

        assertThatThrownBy(() -> service.updateOrganization("org-key", new UpdateOrganizationDTO(
                OrganizationStatusEnum.APPROVED, OrganizationRejectionReasonEnum.INACTIVE_DOCUMENT)))
                .isInstanceOf(InvalidOrganizationRejectionReasonException.class);

        verify(unitOfWork).begin();
        verify(unitOfWork, never()).commit();
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void updateOrganization_newStatusMissing_throwsIllegalStateWithoutBeginningTransaction() {
        when(organizationStatusRepository.findByEnumerator("APPROVED")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateOrganization(
                "org-key", new UpdateOrganizationDTO(OrganizationStatusEnum.APPROVED, null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Organization status not found");

        verifyNoInteractions(unitOfWork);
    }

    @Test
    void updateOrganization_errorStatusMissing_throwsIllegalStateWithoutBeginningTransaction() {
        stubStatus("APPROVED");
        when(organizationStatusRepository.findByEnumerator("ERROR")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateOrganization(
                "org-key", new UpdateOrganizationDTO(OrganizationStatusEnum.APPROVED, null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Organization status ERROR not found");

        verifyNoInteractions(unitOfWork);
    }

    @Test
    void updateOrganization_rejectionReasonMissing_throwsIllegalStateWithoutBeginningTransaction() {
        stubStatus("ERROR");
        stubStatus("REJECTED");
        when(organizationRejectionReasonRepository.findByEnumerator("INACTIVE_DOCUMENT"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateOrganization("org-key", new UpdateOrganizationDTO(
                OrganizationStatusEnum.REJECTED, OrganizationRejectionReasonEnum.INACTIVE_DOCUMENT)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Organization rejection reason not found");

        verifyNoInteractions(unitOfWork);
    }

    @Test
    void deleteOrganization_approved_savesDeletedOrganizationWithNullReason() {
        stubOrganization("APPROVED");
        stubStatus("DELETED");
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        service.deleteOrganization("org-key");

        ArgumentCaptor<Organization> captor = ArgumentCaptor.forClass(Organization.class);
        verify(organizationRepository).save(captor.capture());
        assertThat(captor.getValue().getOrganizationStatus().getEnumerator()).isEqualTo("DELETED");
        assertThat(captor.getValue().getOrganizationRejectionReason()).isNull();
    }

    @Test
    void deleteOrganization_approved_savesHistoryWithOldAndNewStatusInsideTransaction() {
        stubOrganization("APPROVED");
        stubStatus("DELETED");
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        service.deleteOrganization("org-key");

        ArgumentCaptor<OrganizationHistory> captor = ArgumentCaptor.forClass(OrganizationHistory.class);
        var order = inOrder(unitOfWork, organizationRepository, organizationHistoryRepository);
        order.verify(unitOfWork).begin();
        order.verify(organizationRepository).findForUpdateByOrganizationKey("org-key");
        order.verify(organizationRepository).save(any(Organization.class));
        order.verify(organizationHistoryRepository).save(captor.capture());
        order.verify(unitOfWork).commit();
        verify(unitOfWork, never()).rollback();
        assertThat(captor.getValue().getOldStatus().getEnumerator()).isEqualTo("APPROVED");
        assertThat(captor.getValue().getNewStatus().getEnumerator()).isEqualTo("DELETED");
    }

    @Test
    void deleteOrganization_unknownKey_throwsNotFoundWithoutCommit() {
        stubStatus("DELETED");
        when(organizationRepository.findForUpdateByOrganizationKey("org-key")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteOrganization("org-key"))
                .isInstanceOf(OrganizationNotFoundException.class);

        verify(unitOfWork).begin();
        verify(unitOfWork, never()).commit();
    }

    @ParameterizedTest
    @ValueSource(strings = {"PENDING", "REJECTED", "DELETED"})
    void deleteOrganization_notApproved_throwsStatusNotAllowedWithoutCommit(String currentStatus) {
        stubOrganization(currentStatus);
        stubStatus("DELETED");

        assertThatThrownBy(() -> service.deleteOrganization("org-key"))
                .isInstanceOf(OrganizationStatusNotAllowedException.class);

        verify(unitOfWork).begin();
        verify(unitOfWork, never()).commit();
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void deleteOrganization_deletedStatusMissing_throwsIllegalStateWithoutBeginningTransaction() {
        when(organizationStatusRepository.findByEnumerator("DELETED")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteOrganization("org-key"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Organization status DELETED not found");

        verifyNoInteractions(unitOfWork);
    }

    @Test
    void listOrganizations_existingOrganizations_returnsRepositoryResult() {
        Organization organization = new Organization("org", LEGAL_PERSON_KEY, DOCUMENT_NUMBER, NAME, pendingStatus);
        when(organizationRepository.findAll(LEGAL_PERSON_KEY, null, null, null, 2, 5))
                .thenReturn(List.of(organization));

        List<Organization> result = service.listOrganizations(LEGAL_PERSON_KEY, 2, 5);

        assertThat(result).containsExactly(organization);
        verify(organizationRepository).findAll(LEGAL_PERSON_KEY, null, null, null, 2, 5);
        verifyNoInteractions(unitOfWork);
    }

    @Test
    void listOrganizations_noOrganizations_returnsEmptyList() {
        when(organizationRepository.findAll(LEGAL_PERSON_KEY, null, null, null, 1, 30)).thenReturn(List.of());

        assertThat(service.listOrganizations(LEGAL_PERSON_KEY, 1, 30)).isEmpty();
    }

    @Test
    void listInternalOrganizations_filtersGiven_returnsRepositoryResultWithoutLegalPersonKey() {
        Organization organization = new Organization("org", LEGAL_PERSON_KEY, DOCUMENT_NUMBER, NAME, pendingStatus);
        when(organizationRepository.findAll(null, PENDING, "acme", "12345678000199", 2, 5))
                .thenReturn(List.of(organization));

        List<Organization> result = service.listInternalOrganizations(PENDING, "acme", "12345678000199", 2, 5);

        assertThat(result).containsExactly(organization);
        verify(organizationRepository).findAll(null, PENDING, "acme", "12345678000199", 2, 5);
        verifyNoInteractions(unitOfWork);
    }

    @Test
    void listInternalOrganizations_noFilters_returnsEmptyList() {
        when(organizationRepository.findAll(null, null, null, null, 1, 30)).thenReturn(List.of());

        assertThat(service.listInternalOrganizations(null, null, null, 1, 30)).isEmpty();
    }
}
