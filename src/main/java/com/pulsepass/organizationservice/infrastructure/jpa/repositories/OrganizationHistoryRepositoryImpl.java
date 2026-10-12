package com.pulsepass.organizationservice.infrastructure.jpa.repositories;

import com.pulsepass.organizationservice.domain.entities.OrganizationHistory;
import com.pulsepass.organizationservice.domain.repositories.OrganizationHistoryRepository;
import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationHistoryEntity;
import org.springframework.stereotype.Repository;

@Repository
public class OrganizationHistoryRepositoryImpl implements OrganizationHistoryRepository {

    private final JpaOrganizationHistoryRepository jpaRepository;

    public OrganizationHistoryRepositoryImpl(JpaOrganizationHistoryRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public OrganizationHistory save(OrganizationHistory organizationHistory) {
        var saved = jpaRepository.save(OrganizationHistoryEntity.fromDomain(organizationHistory));
        return saved.toDomain();
    }
}
