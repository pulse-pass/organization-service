package com.pulsepass.organizationservice.domain.repositories;

import com.pulsepass.organizationservice.domain.entities.Organization;
import java.util.Optional;

public interface OrganizationRepository {

    Organization save(Organization organization);

    Optional<Organization> findByLegalPersonKey(String legalPersonKey);

    Optional<Organization> findByDocumentNumber(String documentNumber);
}
