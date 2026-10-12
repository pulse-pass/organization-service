package com.pulsepass.organizationservice.infrastructure.jpa.entities;

import com.pulsepass.organizationservice.domain.entities.OrganizationHistory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "organization_history")
public class OrganizationHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_history_key", nullable = false, unique = true, length = 36)
    private String organizationHistoryKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_status_id")
    private OrganizationStatusEntity oldStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_status_id", nullable = false)
    private OrganizationStatusEntity newStatus;

    @Column(name = "history_data", nullable = false, columnDefinition = "json")
    private String historyData;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public OrganizationHistoryEntity() {
    }

    public OrganizationHistoryEntity(
            Long id,
            String organizationHistoryKey,
            OrganizationEntity organization,
            OrganizationStatusEntity oldStatus,
            OrganizationStatusEntity newStatus,
            String historyData,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.organizationHistoryKey = organizationHistoryKey;
        this.organization = organization;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.historyData = historyData;
        this.createdAt = createdAt;
    }

    public static OrganizationHistoryEntity fromDomain(OrganizationHistory domain) {
        var oldStatus = domain.getOldStatus();
        return new OrganizationHistoryEntity(
                domain.getId(),
                domain.getOrganizationHistoryKey(),
                OrganizationEntity.fromDomain(domain.getOrganization()),
                oldStatus == null ? null : OrganizationStatusEntity.fromDomain(oldStatus),
                OrganizationStatusEntity.fromDomain(domain.getNewStatus()),
                domain.getHistoryData(),
                domain.getCreatedAt()
        );
    }

    public OrganizationHistory toDomain() {
        return new OrganizationHistory(
                id,
                organizationHistoryKey,
                organization.toDomain(),
                oldStatus == null ? null : oldStatus.toDomain(),
                newStatus.toDomain(),
                historyData,
                createdAt
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrganizationHistoryKey() {
        return organizationHistoryKey;
    }

    public void setOrganizationHistoryKey(String organizationHistoryKey) {
        this.organizationHistoryKey = organizationHistoryKey;
    }

    public OrganizationEntity getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationEntity organization) {
        this.organization = organization;
    }

    public OrganizationStatusEntity getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(OrganizationStatusEntity oldStatus) {
        this.oldStatus = oldStatus;
    }

    public OrganizationStatusEntity getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(OrganizationStatusEntity newStatus) {
        this.newStatus = newStatus;
    }

    public String getHistoryData() {
        return historyData;
    }

    public void setHistoryData(String historyData) {
        this.historyData = historyData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
