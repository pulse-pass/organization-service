package com.pulsepass.organizationservice.infrastructure.jpa.repositories;

import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationRejectionReasonEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrganizationRejectionReasonRepository
        extends JpaRepository<OrganizationRejectionReasonEntity, Long> {

    Optional<OrganizationRejectionReasonEntity> findByEnumerator(String enumerator);
}
