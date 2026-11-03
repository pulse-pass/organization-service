package com.pulsepass.organizationservice.domain.repositories;

import com.pulsepass.organizationservice.domain.entities.Organization;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatusEnum;
import java.util.List;
import java.util.Optional;

public interface OrganizationRepository {

    Organization save(Organization organization);

    Optional<Organization> findByLegalPersonKey(String legalPersonKey);

    Optional<Organization> findByDocumentNumber(String documentNumber);

    List<Organization> findAll(
            String legalPersonKey,
            OrganizationStatusEnum organizationStatus,
            String name,
            String documentNumber,
            int page,
            int pageSize);
}
