package com.pulsepass.organizationservice.unit.infrastructure.jpa.specifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationEntity;
import com.pulsepass.organizationservice.infrastructure.jpa.specifications.OrganizationSpecification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

@SuppressWarnings("unchecked")
class OrganizationSpecificationTest {

    private Root<OrganizationEntity> root;
    private CriteriaQuery<?> query;
    private CriteriaBuilder criteriaBuilder;
    private Predicate conjunction;
    private Predicate equalPredicate;
    private Predicate combined;
    private Path<Object> legalPersonKeyPath;

    @BeforeEach
    void setUp() {
        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        criteriaBuilder = mock(CriteriaBuilder.class);
        conjunction = mock(Predicate.class);
        equalPredicate = mock(Predicate.class);
        combined = mock(Predicate.class);
        legalPersonKeyPath = mock(Path.class);
        when(criteriaBuilder.conjunction()).thenReturn(conjunction);
        when(root.get("legalPersonKey")).thenReturn(legalPersonKeyPath);
        when(criteriaBuilder.equal(legalPersonKeyPath, "legal")).thenReturn(equalPredicate);
        when(criteriaBuilder.and(conjunction, equalPredicate)).thenReturn(combined);
    }

    @Test
    void withFilters_legalPersonKeyGiven_filtersByEqualOnLegalPersonKey() {
        Specification<OrganizationEntity> spec = OrganizationSpecification.withFilters("legal");

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(combined);
        verify(criteriaBuilder).equal(legalPersonKeyPath, "legal");
    }

    @Test
    void withFilters_nullLegalPersonKey_returnsConjunctionOnly() {
        Specification<OrganizationEntity> spec = OrganizationSpecification.withFilters(null);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(conjunction);
        verify(criteriaBuilder, never()).equal(any(), any(Object.class));
    }

    @Test
    void withFilters_emptyLegalPersonKey_returnsConjunctionOnly() {
        Specification<OrganizationEntity> spec = OrganizationSpecification.withFilters("");

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(conjunction);
    }
}
