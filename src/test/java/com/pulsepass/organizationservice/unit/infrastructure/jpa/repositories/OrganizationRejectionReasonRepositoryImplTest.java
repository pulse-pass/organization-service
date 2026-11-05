package com.pulsepass.organizationservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationRejectionReasonEntity;
import com.pulsepass.organizationservice.infrastructure.jpa.repositories.JpaOrganizationRejectionReasonRepository;
import com.pulsepass.organizationservice.infrastructure.jpa.repositories.OrganizationRejectionReasonRepositoryImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

    @Test
    void findAll_existingPage_returnsMappedDomainsOrderedByIdUsingZeroBasedPageable() {
        when(jpaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(
                List.of(new OrganizationRejectionReasonEntity(2L, "INACTIVE_DOCUMENT", "desc", null))));

        var result = repository.findAll(3, 10);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jpaRepository).findAll(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(captor.getValue().getPageSize()).isEqualTo(10);
        assertThat(captor.getValue().getSort()).containsExactly(Sort.Order.asc("id"));
        assertThat(result).extracting(reason -> reason.getEnumerator()).containsExactly("INACTIVE_DOCUMENT");
    }

    @Test
    void findAll_emptyPage_returnsEmptyList() {
        when(jpaRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<OrganizationRejectionReasonEntity>(List.of()));

        assertThat(repository.findAll(1, 30)).isEmpty();
    }
}
