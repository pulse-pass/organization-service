package com.pulsepass.organizationservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationStatusEntity;
import com.pulsepass.organizationservice.infrastructure.jpa.repositories.JpaOrganizationStatusRepository;
import com.pulsepass.organizationservice.infrastructure.jpa.repositories.OrganizationStatusRepositoryImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrganizationStatusRepositoryImplTest {

    private JpaOrganizationStatusRepository jpaRepository;
    private OrganizationStatusRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaOrganizationStatusRepository.class);
        repository = new OrganizationStatusRepositoryImpl(jpaRepository);
    }

    @Test
    void findByEnumerator_existing_returnsMappedDomain() {
        when(jpaRepository.findByEnumerator("PENDING"))
                .thenReturn(Optional.of(new OrganizationStatusEntity(1L, "PENDING", null)));

        var result = repository.findByEnumerator("PENDING");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getEnumerator()).isEqualTo("PENDING");
    }

    @Test
    void findByEnumerator_missing_returnsEmpty() {
        when(jpaRepository.findByEnumerator("X")).thenReturn(Optional.empty());

        assertThat(repository.findByEnumerator("X")).isEmpty();
    }
}
