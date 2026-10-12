package com.pulsepass.organizationservice.infrastructure.jpa.entities;

import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReason;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "organization_rejection_reason")
public class OrganizationRejectionReasonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enumerator", nullable = false, unique = true, length = 50)
    private String enumerator;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public OrganizationRejectionReasonEntity() {
    }

    public OrganizationRejectionReasonEntity(
            Long id,
            String enumerator,
            String description,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.enumerator = enumerator;
        this.description = description;
        this.createdAt = createdAt;
    }

    public static OrganizationRejectionReasonEntity fromDomain(OrganizationRejectionReason domain) {
        return new OrganizationRejectionReasonEntity(
                domain.getId(),
                domain.getEnumerator(),
                domain.getDescription(),
                domain.getCreatedAt()
        );
    }

    public OrganizationRejectionReason toDomain() {
        return new OrganizationRejectionReason(this.id, this.enumerator, this.description, this.createdAt);
    }
}
