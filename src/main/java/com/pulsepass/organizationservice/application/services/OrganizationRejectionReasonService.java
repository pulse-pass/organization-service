package com.pulsepass.organizationservice.application.services;

import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReason;
import com.pulsepass.organizationservice.domain.repositories.OrganizationRejectionReasonRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrganizationRejectionReasonService {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationRejectionReasonService.class);

    private final OrganizationRejectionReasonRepository organizationRejectionReasonRepository;

    public OrganizationRejectionReasonService(
            OrganizationRejectionReasonRepository organizationRejectionReasonRepository
    ) {
        this.organizationRejectionReasonRepository = organizationRejectionReasonRepository;
    }

    public List<OrganizationRejectionReason> listOrganizationRejectionReasons(int page, int pageSize) {
        logger.info("OrganizationRejectionReasonService.listOrganizationRejectionReasons - Starting");
        var organizationRejectionReasons = organizationRejectionReasonRepository.findAll(page, pageSize);
        logger.info("OrganizationRejectionReasonService.listOrganizationRejectionReasons - Successfully finished");
        return organizationRejectionReasons;
    }
}
