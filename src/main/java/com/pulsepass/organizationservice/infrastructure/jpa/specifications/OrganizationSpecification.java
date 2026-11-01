package com.pulsepass.organizationservice.infrastructure.jpa.specifications;

import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationEntity;
import org.springframework.data.jpa.domain.Specification;

public final class OrganizationSpecification {

    private OrganizationSpecification() {
    }

    public static Specification<OrganizationEntity> withFilters(String legalPersonKey) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();
            if (legalPersonKey != null && !legalPersonKey.isEmpty()) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(root.get("legalPersonKey"), legalPersonKey)
                );
            }
            return predicate;
        };
    }
}
