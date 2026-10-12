package com.pulsepass.organizationservice.application.services;

import com.pulsepass.organizationservice.application.dtos.input.CreateOrganizationDTO;
import com.pulsepass.organizationservice.domain.UnitOfWork;
import com.pulsepass.organizationservice.domain.entities.Organization;
import com.pulsepass.organizationservice.domain.entities.OrganizationHistory;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatusEnum;
import com.pulsepass.organizationservice.domain.exceptions.OrganizationAlreadyExistsException;
import com.pulsepass.organizationservice.domain.repositories.OrganizationHistoryRepository;
import com.pulsepass.organizationservice.domain.repositories.OrganizationRepository;
import com.pulsepass.organizationservice.domain.repositories.OrganizationStatusRepository;
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

    public OrganizationService(
            UnitOfWork unitOfWork,
            OrganizationRepository organizationRepository,
            OrganizationStatusRepository organizationStatusRepository,
            OrganizationHistoryRepository organizationHistoryRepository
    ) {
        this.unitOfWork = unitOfWork;
        this.organizationRepository = organizationRepository;
        this.organizationStatusRepository = organizationStatusRepository;
        this.organizationHistoryRepository = organizationHistoryRepository;
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
            logger.warn("OrganizationService.createOrganization - Unique constraint violation: {}", e.getMessage());
            throw new OrganizationAlreadyExistsException();
        }

        organizationHistoryRepository.save(OrganizationHistory.fromOrganization(organization, null));

        unitOfWork.commit();

        logger.info("OrganizationService.createOrganization - Successfully finished");
        return organization;
    }
}
