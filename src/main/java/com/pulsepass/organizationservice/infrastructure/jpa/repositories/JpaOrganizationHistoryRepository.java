package com.pulsepass.organizationservice.infrastructure.jpa.repositories;

import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrganizationHistoryRepository extends JpaRepository<OrganizationHistoryEntity, Long> {
}
