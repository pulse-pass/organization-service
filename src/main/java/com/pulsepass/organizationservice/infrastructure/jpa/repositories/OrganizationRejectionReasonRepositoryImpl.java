package com.pulsepass.organizationservice.infrastructure.jpa.repositories;

import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReason;
import com.pulsepass.organizationservice.domain.repositories.OrganizationRejectionReasonRepository;
import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationRejectionReasonEntity;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class OrganizationRejectionReasonRepositoryImpl implements OrganizationRejectionReasonRepository {

    private final JpaOrganizationRejectionReasonRepository jpaRepository;

    public OrganizationRejectionReasonRepositoryImpl(JpaOrganizationRejectionReasonRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<OrganizationRejectionReason> findByEnumerator(String enumerator) {
        return jpaRepository.findByEnumerator(enumerator).map(OrganizationRejectionReasonEntity::toDomain);
    }
}
