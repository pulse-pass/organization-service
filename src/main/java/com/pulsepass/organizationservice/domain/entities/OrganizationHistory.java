package com.pulsepass.organizationservice.domain.entities;

import com.pulsepass.organizationservice.common.JsonHandlers;
import java.time.LocalDateTime;
import java.util.UUID;

public class OrganizationHistory {

    private Long id;
    private String organizationHistoryKey;
    private Organization organization;
    private OrganizationStatus oldStatus;
    private OrganizationStatus newStatus;
    private String historyData;
    private LocalDateTime createdAt;

    public OrganizationHistory() {
    }

    public OrganizationHistory(
            String organizationHistoryKey,
            Organization organization,
            OrganizationStatus oldStatus,
            OrganizationStatus newStatus,
            String historyData
    ) {
        this.organizationHistoryKey = organizationHistoryKey;
        this.organization = organization;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.historyData = historyData;
    }

    public OrganizationHistory(
            Long id,
            String organizationHistoryKey,
            Organization organization,
            OrganizationStatus oldStatus,
            OrganizationStatus newStatus,
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

    public static OrganizationHistory fromOrganization(Organization organization, OrganizationStatus oldStatus) {
        return new OrganizationHistory(
                UUID.randomUUID().toString(),
                organization,
                oldStatus,
                organization.getOrganizationStatus(),
                JsonHandlers.toJson(organization.toHistoryData())
        );
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrganizationHistoryKey() {
        return this.organizationHistoryKey;
    }

    public void setOrganizationHistoryKey(String organizationHistoryKey) {
        this.organizationHistoryKey = organizationHistoryKey;
    }

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public OrganizationStatus getOldStatus() {
        return this.oldStatus;
    }

    public void setOldStatus(OrganizationStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public OrganizationStatus getNewStatus() {
        return this.newStatus;
    }

    public void setNewStatus(OrganizationStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getHistoryData() {
        return this.historyData;
    }

    public void setHistoryData(String historyData) {
        this.historyData = historyData;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
