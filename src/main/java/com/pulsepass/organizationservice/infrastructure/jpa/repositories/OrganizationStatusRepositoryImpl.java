package com.pulsepass.organizationservice.infrastructure.jpa.repositories;

import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import com.pulsepass.organizationservice.domain.repositories.OrganizationStatusRepository;
import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationStatusEntity;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class OrganizationStatusRepositoryImpl implements OrganizationStatusRepository {

    private final JpaOrganizationStatusRepository jpaRepository;

    public OrganizationStatusRepositoryImpl(JpaOrganizationStatusRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<OrganizationStatus> findByEnumerator(String enumerator) {
        return jpaRepository.findByEnumerator(enumerator).map(OrganizationStatusEntity::toDomain);
    }
}
