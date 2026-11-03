package com.pulsepass.organizationservice.unit.infrastructure.jpa.specifications;

import static com.pulsepass.organizationservice.domain.entities.OrganizationStatusEnum.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyChar;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.organizationservice.domain.entities.OrganizationStatusEnum;
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
        Specification<OrganizationEntity> spec = OrganizationSpecification.withFilters("legal", null, null, null);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(combined);
        verify(criteriaBuilder).equal(legalPersonKeyPath, "legal");
    }

    @Test
    void withFilters_nullLegalPersonKey_returnsConjunctionOnly() {
        Specification<OrganizationEntity> spec = OrganizationSpecification.withFilters(null, null, null, null);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(conjunction);
        verify(criteriaBuilder, never()).equal(any(), any(Object.class));
    }

    @Test
    void withFilters_emptyLegalPersonKey_returnsConjunctionOnly() {
        Specification<OrganizationEntity> spec = OrganizationSpecification.withFilters("", null, null, null);

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(conjunction);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void withFilters_organizationStatusGiven_filtersByEqualOnOrganizationStatusEnumerator() {
        Path<Object> statusPath = mock(Path.class);
        Path<Object> enumeratorPath = mock(Path.class);
        when(root.get("organizationStatus")).thenReturn(statusPath);
        when(statusPath.get("enumerator")).thenReturn(enumeratorPath);
        Predicate statusPredicate = mock(Predicate.class);
        Predicate statusCombined = mock(Predicate.class);
        when(criteriaBuilder.equal(enumeratorPath, "PENDING")).thenReturn(statusPredicate);
        when(criteriaBuilder.and(conjunction, statusPredicate)).thenReturn(statusCombined);

        Predicate result = OrganizationSpecification.withFilters(null, PENDING, null, null)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(statusCombined);
        verify(criteriaBuilder).equal(enumeratorPath, "PENDING");
    }

    @Test
    void withFilters_nameGiven_filtersByPrefixLikeOnName() {
        Path<String> namePath = mock(Path.class);
        when(root.<String>get("name")).thenReturn(namePath);
        Predicate nameLike = mock(Predicate.class);
        Predicate nameCombined = mock(Predicate.class);
        when(criteriaBuilder.like(namePath, "acme%", '\\')).thenReturn(nameLike);
        when(criteriaBuilder.and(conjunction, nameLike)).thenReturn(nameCombined);

        Predicate result = OrganizationSpecification.withFilters(null, null, "acme", null)
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(nameCombined);
        verify(criteriaBuilder, never()).or(any(Predicate.class), any(Predicate.class));
    }

    @Test
    void withFilters_nameWithWildcardsAndBackslash_escapesThemBeforeTheTrailingWildcard() {
        Path<String> namePath = mock(Path.class);
        when(root.<String>get("name")).thenReturn(namePath);

        OrganizationSpecification.withFilters(null, null, "a%b_c\\d", null)
                .toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder).like(namePath, "a\\%b\\_c\\\\d%", '\\');
    }

    @Test
    void withFilters_documentNumberGiven_filtersByEqualOnDocumentNumber() {
        Path<String> documentNumberPath = mock(Path.class);
        when(root.<String>get("documentNumber")).thenReturn(documentNumberPath);
        Predicate documentEqual = mock(Predicate.class);
        Predicate documentCombined = mock(Predicate.class);
        when(criteriaBuilder.equal(documentNumberPath, "12345678000199")).thenReturn(documentEqual);
        when(criteriaBuilder.and(conjunction, documentEqual)).thenReturn(documentCombined);

        Predicate result = OrganizationSpecification.withFilters(null, null, null, "12345678000199")
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(documentCombined);
        verify(criteriaBuilder, never()).like(any(), anyString(), anyChar());
    }

    @Test
    void withFilters_nameAndDocumentNumberGiven_combinesBothWithAnd() {
        Path<String> namePath = mock(Path.class);
        Path<String> documentNumberPath = mock(Path.class);
        when(root.<String>get("name")).thenReturn(namePath);
        when(root.<String>get("documentNumber")).thenReturn(documentNumberPath);
        Predicate nameLike = mock(Predicate.class);
        Predicate documentEqual = mock(Predicate.class);
        Predicate nameCombined = mock(Predicate.class);
        Predicate bothCombined = mock(Predicate.class);
        when(criteriaBuilder.like(namePath, "acme%", '\\')).thenReturn(nameLike);
        when(criteriaBuilder.equal(documentNumberPath, "12345678000199")).thenReturn(documentEqual);
        when(criteriaBuilder.and(conjunction, nameLike)).thenReturn(nameCombined);
        when(criteriaBuilder.and(nameCombined, documentEqual)).thenReturn(bothCombined);

        Predicate result = OrganizationSpecification.withFilters(null, null, "acme", "12345678000199")
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(bothCombined);
    }

    @Test
    void withFilters_nullStatusAndEmptyNameAndDocumentNumber_returnsConjunctionOnly() {
        Predicate result = OrganizationSpecification.withFilters(null, null, "", "")
                .toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(conjunction);
        verify(criteriaBuilder, never()).like(any(), anyString(), anyChar());
        verify(criteriaBuilder, never()).equal(any(), any(Object.class));
    }
}
