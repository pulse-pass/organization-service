package com.pulsepass.organizationservice.infrastructure.jpa.repositories;

import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JpaOrganizationRepository extends JpaRepository<OrganizationEntity, Long>,
        JpaSpecificationExecutor<OrganizationEntity> {

    Optional<OrganizationEntity> findByLegalPersonKey(String legalPersonKey);

    Optional<OrganizationEntity> findByDocumentNumber(String documentNumber);
}
