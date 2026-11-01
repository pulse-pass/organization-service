package com.pulsepass.organizationservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.organizationservice.domain.entities.Organization;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationEntity;
import com.pulsepass.organizationservice.infrastructure.jpa.repositories.JpaOrganizationRepository;
import com.pulsepass.organizationservice.infrastructure.jpa.repositories.OrganizationRepositoryImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

class OrganizationRepositoryImplTest {

    private JpaOrganizationRepository jpaRepository;
    private OrganizationRepositoryImpl repository;
    private Organization organization;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaOrganizationRepository.class);
        repository = new OrganizationRepositoryImpl(jpaRepository);
        organization = new Organization("org", "legal", "12345678000195", "Acme",
                new OrganizationStatus(1L, "PENDING", null));
    }

    @Test
    void save_organization_savesEntityAndReturnsMappedDomain() {
        when(jpaRepository.save(any(OrganizationEntity.class))).thenAnswer(inv -> {
            Organization persisted = inv.<OrganizationEntity>getArgument(0).toDomain();
            persisted.setId(42L);
            return OrganizationEntity.fromDomain(persisted);
        });

        Organization saved = repository.save(organization);

        ArgumentCaptor<OrganizationEntity> captor = ArgumentCaptor.forClass(OrganizationEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().toDomain().getOrganizationKey()).isEqualTo("org");
        assertThat(saved.getId()).isEqualTo(42L);
        assertThat(saved.getOrganizationKey()).isEqualTo("org");
    }

    @Test
    void findByLegalPersonKey_existing_returnsMappedDomain() {
        when(jpaRepository.findByLegalPersonKey("legal"))
                .thenReturn(Optional.of(OrganizationEntity.fromDomain(organization)));

        assertThat(repository.findByLegalPersonKey("legal")).get()
                .extracting(Organization::getLegalPersonKey).isEqualTo("legal");
    }

    @Test
    void findByLegalPersonKey_missing_returnsEmpty() {
        when(jpaRepository.findByLegalPersonKey("x")).thenReturn(Optional.empty());

        assertThat(repository.findByLegalPersonKey("x")).isEmpty();
    }

    @Test
    void findByDocumentNumber_existing_returnsMappedDomain() {
        when(jpaRepository.findByDocumentNumber("12345678000195"))
                .thenReturn(Optional.of(OrganizationEntity.fromDomain(organization)));

        assertThat(repository.findByDocumentNumber("12345678000195")).get()
                .extracting(Organization::getDocumentNumber).isEqualTo("12345678000195");
    }

    @Test
    void findByDocumentNumber_missing_returnsEmpty() {
        when(jpaRepository.findByDocumentNumber("x")).thenReturn(Optional.empty());

        assertThat(repository.findByDocumentNumber("x")).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_existingPage_returnsMappedDomainsUsingZeroBasedPageable() {
        Page<OrganizationEntity> page = new PageImpl<>(List.of(OrganizationEntity.fromDomain(organization)));
        when(jpaRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        List<Organization> result = repository.findAll("legal", 3, 10);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jpaRepository).findAll(any(Specification.class), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(captor.getValue().getPageSize()).isEqualTo(10);
        assertThat(result).extracting(Organization::getLegalPersonKey).containsExactly("legal");
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_emptyPage_returnsEmptyList() {
        when(jpaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<OrganizationEntity>(List.of()));

        assertThat(repository.findAll("legal", 1, 30)).isEmpty();
    }
}
