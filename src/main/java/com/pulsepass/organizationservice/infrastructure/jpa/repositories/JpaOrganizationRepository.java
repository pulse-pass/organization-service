package com.pulsepass.organizationservice.infrastructure.jpa.repositories;

import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;

public interface JpaOrganizationRepository extends JpaRepository<OrganizationEntity, Long>,
        JpaSpecificationExecutor<OrganizationEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OrganizationEntity> findForUpdateByOrganizationKey(String organizationKey);

    Optional<OrganizationEntity> findByLegalPersonKey(String legalPersonKey);

    Optional<OrganizationEntity> findByDocumentNumber(String documentNumber);
}
