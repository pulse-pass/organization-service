package com.pulsepass.organizationservice.infrastructure.jpa.repositories;

import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReason;
import com.pulsepass.organizationservice.domain.repositories.OrganizationRejectionReasonRepository;
import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationRejectionReasonEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @Override
    public List<OrganizationRejectionReason> findAll(int page, int pageSize) {
        var pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.asc("id")));
        return jpaRepository.findAll(pageable).map(OrganizationRejectionReasonEntity::toDomain).getContent();
    }
}
