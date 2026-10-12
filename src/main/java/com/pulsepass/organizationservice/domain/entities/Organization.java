package com.pulsepass.organizationservice.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class Organization {

    private Long id;
    private String organizationKey;
    private String legalPersonKey;
    private String externalAccountKey;
    private String externalWalletKey;
    private String documentNumber;
    private String name;
    private String externalApiKey;
    private BigDecimal balanceAmount;
    private OrganizationRejectionReason organizationRejectionReason;
    private OrganizationStatus organizationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Organization() {
    }

    public Organization(
            String organizationKey,
            String legalPersonKey,
            String documentNumber,
            String name,
            OrganizationStatus organizationStatus
    ) {
        this.organizationKey = organizationKey;
        this.legalPersonKey = legalPersonKey;
        this.documentNumber = documentNumber;
        this.name = name;
        this.balanceAmount = BigDecimal.ZERO;
        this.organizationStatus = organizationStatus;
    }

    public Organization(
            Long id,
            String organizationKey,
            String legalPersonKey,
            String externalAccountKey,
            String externalWalletKey,
            String documentNumber,
            String name,
            String externalApiKey,
            BigDecimal balanceAmount,
            OrganizationRejectionReason organizationRejectionReason,
            OrganizationStatus organizationStatus,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.organizationKey = organizationKey;
        this.legalPersonKey = legalPersonKey;
        this.externalAccountKey = externalAccountKey;
        this.externalWalletKey = externalWalletKey;
        this.documentNumber = documentNumber;
        this.name = name;
        this.externalApiKey = externalApiKey;
        this.balanceAmount = balanceAmount;
        this.organizationRejectionReason = organizationRejectionReason;
        this.organizationStatus = organizationStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrganizationKey() {
        return organizationKey;
    }

    public void setOrganizationKey(String organizationKey) {
        this.organizationKey = organizationKey;
    }

    public String getLegalPersonKey() {
        return legalPersonKey;
    }

    public void setLegalPersonKey(String legalPersonKey) {
        this.legalPersonKey = legalPersonKey;
    }

    public String getExternalAccountKey() {
        return externalAccountKey;
    }

    public void setExternalAccountKey(String externalAccountKey) {
        this.externalAccountKey = externalAccountKey;
    }

    public String getExternalWalletKey() {
        return externalWalletKey;
    }

    public void setExternalWalletKey(String externalWalletKey) {
        this.externalWalletKey = externalWalletKey;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExternalApiKey() {
        return externalApiKey;
    }

    public void setExternalApiKey(String externalApiKey) {
        this.externalApiKey = externalApiKey;
    }

    public BigDecimal getBalanceAmount() {
        return balanceAmount;
    }

    public void setBalanceAmount(BigDecimal balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public OrganizationRejectionReason getOrganizationRejectionReason() {
        return organizationRejectionReason;
    }

    public void setOrganizationRejectionReason(OrganizationRejectionReason organizationRejectionReason) {
        this.organizationRejectionReason = organizationRejectionReason;
    }

    public OrganizationStatus getOrganizationStatus() {
        return organizationStatus;
    }

    public void setOrganizationStatus(OrganizationStatus organizationStatus) {
        this.organizationStatus = organizationStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Map<String, Object> toHistoryData() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("organization_key", organizationKey);
        data.put("legal_person_key", legalPersonKey);
        data.put("document_number", documentNumber);
        data.put("name", name);
        data.put("balance_amount", balanceAmount);
        data.put("organization_rejection_reason",
                organizationRejectionReason != null ? organizationRejectionReason.getEnumerator() : null);
        data.put("organization_status", organizationStatus.getEnumerator());
        return data;
    }
}
