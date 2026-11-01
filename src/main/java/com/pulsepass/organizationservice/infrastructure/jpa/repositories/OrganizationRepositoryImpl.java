package com.pulsepass.organizationservice.infrastructure.jpa.repositories;

import com.pulsepass.organizationservice.domain.entities.Organization;
import com.pulsepass.organizationservice.domain.repositories.OrganizationRepository;
import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationEntity;
import com.pulsepass.organizationservice.infrastructure.jpa.specifications.OrganizationSpecification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class OrganizationRepositoryImpl implements OrganizationRepository {

    private final JpaOrganizationRepository jpaRepository;

    public OrganizationRepositoryImpl(JpaOrganizationRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Organization save(Organization organization) {
        var saved = jpaRepository.save(OrganizationEntity.fromDomain(organization));
        return saved.toDomain();
    }

    @Override
    public Optional<Organization> findByLegalPersonKey(String legalPersonKey) {
        return jpaRepository.findByLegalPersonKey(legalPersonKey).map(OrganizationEntity::toDomain);
    }

    @Override
    public Optional<Organization> findByDocumentNumber(String documentNumber) {
        return jpaRepository.findByDocumentNumber(documentNumber).map(OrganizationEntity::toDomain);
    }

    @Override
    public List<Organization> findAll(String legalPersonKey, int page, int pageSize) {
        var spec = OrganizationSpecification.withFilters(legalPersonKey);
        var pageable = PageRequest.of(page - 1, pageSize);
        return jpaRepository.findAll(spec, pageable).map(OrganizationEntity::toDomain).getContent();
    }
}
