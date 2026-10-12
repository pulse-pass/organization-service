package com.pulsepass.organizationservice.infrastructure.jpa.entities;

import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "organization_status")
public class OrganizationStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enumerator", nullable = false, unique = true, length = 50)
    private String enumerator;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public OrganizationStatusEntity() {
    }

    public OrganizationStatusEntity(Long id, String enumerator, LocalDateTime createdAt) {
        this.id = id;
        this.enumerator = enumerator;
        this.createdAt = createdAt;
    }

    public static OrganizationStatusEntity fromDomain(OrganizationStatus domain) {
        return new OrganizationStatusEntity(domain.getId(), domain.getEnumerator(), domain.getCreatedAt());
    }

    public OrganizationStatus toDomain() {
        return new OrganizationStatus(this.id, this.enumerator, this.createdAt);
    }
}
