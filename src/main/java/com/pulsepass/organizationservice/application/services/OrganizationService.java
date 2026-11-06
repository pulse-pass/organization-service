package com.pulsepass.organizationservice.application.services;

import com.pulsepass.organizationservice.application.dtos.input.CreateOrganizationDTO;
import com.pulsepass.organizationservice.application.dtos.input.UpdateOrganizationDTO;
import com.pulsepass.organizationservice.domain.SecretCipher;
import com.pulsepass.organizationservice.domain.UnitOfWork;
import com.pulsepass.organizationservice.domain.entities.Organization;
import com.pulsepass.organizationservice.domain.entities.OrganizationHistory;
import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReason;
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
import com.pulsepass.organizationservice.domain.gateways.paymentprovider.PaymentProviderGateway;
import com.pulsepass.organizationservice.domain.gateways.paymentprovider.dtos.CreatePaymentAccountInputDTO;
import com.pulsepass.organizationservice.domain.gateways.paymentprovider.dtos.CreatePaymentAccountOutputDTO;
import com.pulsepass.organizationservice.domain.repositories.OrganizationHistoryRepository;
import com.pulsepass.organizationservice.domain.repositories.OrganizationRejectionReasonRepository;
import com.pulsepass.organizationservice.domain.repositories.OrganizationRepository;
import com.pulsepass.organizationservice.domain.repositories.OrganizationStatusRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationService.class);

    private final UnitOfWork unitOfWork;
    private final OrganizationRepository organizationRepository;
    private final OrganizationStatusRepository organizationStatusRepository;
    private final OrganizationHistoryRepository organizationHistoryRepository;
    private final OrganizationRejectionReasonRepository organizationRejectionReasonRepository;
    private final OnboardingGateway onboardingGateway;
    private final PaymentProviderGateway paymentProviderGateway;
    private final SecretCipher secretCipher;

    public OrganizationService(
            UnitOfWork unitOfWork,
            OrganizationRepository organizationRepository,
            OrganizationStatusRepository organizationStatusRepository,
            OrganizationHistoryRepository organizationHistoryRepository,
            OrganizationRejectionReasonRepository organizationRejectionReasonRepository,
            OnboardingGateway onboardingGateway,
            PaymentProviderGateway paymentProviderGateway,
            SecretCipher secretCipher
    ) {
        this.unitOfWork = unitOfWork;
        this.organizationRepository = organizationRepository;
        this.organizationStatusRepository = organizationStatusRepository;
        this.organizationHistoryRepository = organizationHistoryRepository;
        this.organizationRejectionReasonRepository = organizationRejectionReasonRepository;
        this.onboardingGateway = onboardingGateway;
        this.paymentProviderGateway = paymentProviderGateway;
        this.secretCipher = secretCipher;
    }

    public Organization createOrganization(CreateOrganizationDTO dto) {
        logger.info("OrganizationService.createOrganization - Starting");

        if (organizationRepository.findByLegalPersonKey(dto.legalPersonKey()).isPresent()) {
            logger.warn("OrganizationService.createOrganization - Organization already exists for legal person key");
            throw new OrganizationAlreadyExistsException();
        }

        if (organizationRepository.findByDocumentNumber(dto.documentNumber()).isPresent()) {
            logger.warn("OrganizationService.createOrganization - Organization already exists for document number");
            throw new OrganizationAlreadyExistsException();
        }

        OrganizationStatus pendingStatus = organizationStatusRepository
                .findByEnumerator(OrganizationStatusEnum.PENDING.name())
                .orElseThrow(() -> new IllegalStateException("Organization status PENDING not found"));

        unitOfWork.begin();
        Organization organization;
        try {
            organization = organizationRepository.save(new Organization(
                    UUID.randomUUID().toString(),
                    dto.legalPersonKey(),
                    dto.documentNumber(),
                    dto.name(),
                    pendingStatus
            ));
        } catch (DataIntegrityViolationException e) {
            logger.warn("OrganizationService.createOrganization - Unique constraint violation", e);
            throw new OrganizationAlreadyExistsException();
        }

        organizationHistoryRepository.save(OrganizationHistory.fromOrganization(organization, null));

        unitOfWork.commit();

        logger.info("OrganizationService.createOrganization - Successfully finished");
        return organization;
    }

    public Organization updateOrganization(String organizationKey, UpdateOrganizationDTO dto) {
        logger.info("OrganizationService.updateOrganization - Starting");

        final OrganizationStatus newStatus = organizationStatusRepository
                .findByEnumerator(dto.organizationStatus().name())
                .orElseThrow(() -> new IllegalStateException("Organization status not found"));

        final OrganizationStatus errorStatus = organizationStatusRepository
                .findByEnumerator(OrganizationStatusEnum.ERROR.name())
                .orElseThrow(() -> new IllegalStateException("Organization status ERROR not found"));

        OrganizationRejectionReason rejectionReason = null;
        if (dto.organizationRejectionReason() != null) {
            logger.info("OrganizationService.updateOrganization - Resolving the rejection reason");
            rejectionReason = organizationRejectionReasonRepository
                    .findByEnumerator(dto.organizationRejectionReason().name())
                    .orElseThrow(() -> new IllegalStateException("Organization rejection reason not found"));
        }

        unitOfWork.begin();

        Organization organization = organizationRepository
                .findForUpdateByOrganizationKey(organizationKey)
                .orElseThrow(OrganizationNotFoundException::new);

        if (!organization.getOrganizationStatus().getEnumerator().equals(OrganizationStatusEnum.PENDING.name())) {
            logger.warn("OrganizationService.updateOrganization - Organization is not pending");
            throw new OrganizationStatusNotAllowedException();
        }

        if (dto.organizationStatus() == OrganizationStatusEnum.REJECTED && dto.organizationRejectionReason() == null) {
            logger.warn("OrganizationService.updateOrganization - Rejection reason is required to reject");
            throw new InvalidOrganizationRejectionReasonException();
        }

        if (dto.organizationStatus() == OrganizationStatusEnum.APPROVED && dto.organizationRejectionReason() != null) {
            logger.warn("OrganizationService.updateOrganization - Rejection reason is not allowed to approve");
            throw new InvalidOrganizationRejectionReasonException();
        }

        OrganizationStatus oldStatus = organization.getOrganizationStatus();

        if (dto.organizationStatus() == OrganizationStatusEnum.APPROVED) {
            logger.info(
                    "OrganizationService.updateOrganization - Creating the payment account for the approval {}",
                    Map.of("organization_key", organization.getOrganizationKey())
            );
            GetLegalPersonOutputDTO legalPerson;
            try {
                legalPerson = onboardingGateway.getLegalPerson(
                        new GetLegalPersonInputDTO(organization.getLegalPersonKey()));
            } catch (Exception e) {
                logger.error(
                        "OrganizationService.updateOrganization - Legal person retrieval failed {}",
                        Map.of(
                                "organization_key", organization.getOrganizationKey(),
                                "failure", e.getClass().getSimpleName()
                        )
                );
                throw new LegalPersonRetrievalException();
            }
            var phoneNumber = legalPerson.phoneNumbers().get(0);
            var address = legalPerson.legalPersonAddress();

            CreatePaymentAccountOutputDTO account;
            try {
                account = paymentProviderGateway.createPaymentAccount(new CreatePaymentAccountInputDTO(
                        organization.getOrganizationKey(),
                        legalPerson.name(),
                        legalPerson.email(),
                        legalPerson.documentNumber(),
                        phoneNumber.areaCode() + phoneNumber.number(),
                        legalPerson.legalPersonCompanyType(),
                        legalPerson.monthlyRevenueAmount(),
                        address.street(),
                        address.number(),
                        address.complement(),
                        address.neighborhood(),
                        address.postalCode()
                ));
            } catch (Exception e) {
                logger.error(
                        "OrganizationService.updateOrganization - Payment account creation failed {}",
                        Map.of(
                                "organization_key", organization.getOrganizationKey(),
                                "failure", e.getClass().getSimpleName()
                        )
                );
                organization.setOrganizationStatus(errorStatus);
                organizationRepository.save(organization);
                organizationHistoryRepository.save(OrganizationHistory.fromOrganization(organization, oldStatus));
                unitOfWork.commit();
                throw new PaymentAccountCreationException();
            }

            organization.setExternalAccountKey(account.accountKey());
            organization.setExternalWalletKey(account.walletKey());
            organization.setExternalApiKey(secretCipher.encrypt(account.apiKey()));
        }

        organization.setOrganizationStatus(newStatus);
        organization.setOrganizationRejectionReason(rejectionReason);

        organizationRepository.save(organization);
        organizationHistoryRepository.save(OrganizationHistory.fromOrganization(organization, oldStatus));
        unitOfWork.commit();

        logger.info("OrganizationService.updateOrganization - Successfully finished");
        return organization;
    }

    public void deleteOrganization(String organizationKey) {
        logger.info("OrganizationService.deleteOrganization - Starting");

        OrganizationStatus deletedStatus = organizationStatusRepository
                .findByEnumerator(OrganizationStatusEnum.DELETED.name())
                .orElseThrow(() -> new IllegalStateException("Organization status DELETED not found"));

        unitOfWork.begin();

        Organization organization = organizationRepository
                .findForUpdateByOrganizationKey(organizationKey)
                .orElseThrow(OrganizationNotFoundException::new);

        if (!organization.getOrganizationStatus().getEnumerator().equals(OrganizationStatusEnum.APPROVED.name())) {
            logger.warn("OrganizationService.deleteOrganization - Organization is not approved");
            throw new OrganizationStatusNotAllowedException();
        }

        OrganizationStatus oldStatus = organization.getOrganizationStatus();
        organization.setOrganizationStatus(deletedStatus);

        organizationRepository.save(organization);
        organizationHistoryRepository.save(OrganizationHistory.fromOrganization(organization, oldStatus));
        unitOfWork.commit();

        logger.info("OrganizationService.deleteOrganization - Successfully finished");
    }

    public List<Organization> listOrganizations(String legalPersonKey, int page, int pageSize) {
        logger.info("OrganizationService.listOrganizations - Starting");
        var organizations = organizationRepository.findAll(legalPersonKey, null, null, null, page, pageSize);
        logger.info("OrganizationService.listOrganizations - Successfully finished");
        return organizations;
    }

    public List<Organization> listInternalOrganizations(
            OrganizationStatusEnum organizationStatus, String name, String documentNumber, int page, int pageSize) {
        logger.info("OrganizationService.listInternalOrganizations - Starting");
        var organizations = organizationRepository.findAll(
                null, organizationStatus, name, documentNumber, page, pageSize);
        logger.info("OrganizationService.listInternalOrganizations - Successfully finished");
        return organizations;
    }
}
