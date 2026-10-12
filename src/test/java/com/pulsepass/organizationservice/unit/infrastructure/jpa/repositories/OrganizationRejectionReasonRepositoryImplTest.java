package com.pulsepass.organizationservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationRejectionReasonEntity;
import com.pulsepass.organizationservice.infrastructure.jpa.repositories.JpaOrganizationRejectionReasonRepository;
import com.pulsepass.organizationservice.infrastructure.jpa.repositories.OrganizationRejectionReasonRepositoryImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrganizationRejectionReasonRepositoryImplTest {

    private JpaOrganizationRejectionReasonRepository jpaRepository;
    private OrganizationRejectionReasonRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaOrganizationRejectionReasonRepository.class);
        repository = new OrganizationRejectionReasonRepositoryImpl(jpaRepository);
    }

    @Test
    void findByEnumerator_existing_returnsMappedDomain() {
        when(jpaRepository.findByEnumerator("INACTIVE_DOCUMENT"))
                .thenReturn(Optional.of(new OrganizationRejectionReasonEntity(2L, "INACTIVE_DOCUMENT", "desc", null)));

        var result = repository.findByEnumerator("INACTIVE_DOCUMENT");

        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("desc");
    }

    @Test
    void findByEnumerator_missing_returnsEmpty() {
        when(jpaRepository.findByEnumerator("X")).thenReturn(Optional.empty());

        assertThat(repository.findByEnumerator("X")).isEmpty();
    }
}
