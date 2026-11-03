package com.pulsepass.organizationservice.infrastructure.jpa.specifications;

import com.pulsepass.organizationservice.domain.entities.OrganizationStatusEnum;
import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationEntity;
import org.springframework.data.jpa.domain.Specification;

public final class OrganizationSpecification {

    private static final char ESCAPE_CHARACTER = '\\';

    private OrganizationSpecification() {
    }

    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    public static Specification<OrganizationEntity> withFilters(
            String legalPersonKey, OrganizationStatusEnum organizationStatus, String name, String documentNumber) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();
            if (legalPersonKey != null && !legalPersonKey.isEmpty()) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(root.get("legalPersonKey"), legalPersonKey)
                );
            }
            if (organizationStatus != null) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(
                                root.get("organizationStatus").get("enumerator"), organizationStatus.name())
                );
            }
            if (name != null && !name.isEmpty()) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.like(root.get("name"), escapeLike(name) + "%", ESCAPE_CHARACTER)
                );
            }
            if (documentNumber != null && !documentNumber.isEmpty()) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(root.get("documentNumber"), documentNumber)
                );
            }
            return predicate;
        };
    }
}
