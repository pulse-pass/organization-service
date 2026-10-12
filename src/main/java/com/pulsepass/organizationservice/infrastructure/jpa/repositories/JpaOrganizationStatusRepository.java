package com.pulsepass.organizationservice.infrastructure.jpa.repositories;

import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationStatusEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrganizationStatusRepository extends JpaRepository<OrganizationStatusEntity, Long> {

    Optional<OrganizationStatusEntity> findByEnumerator(String enumerator);
}
