package com.pulsepass.organizationservice.domain.entities;

import java.time.LocalDateTime;

public class OrganizationRejectionReason {

    private Long id;
    private String enumerator;
    private String description;
    private LocalDateTime createdAt;

    public OrganizationRejectionReason() {
    }

    public OrganizationRejectionReason(
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

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEnumerator() {
        return this.enumerator;
    }

    public void setEnumerator(String enumerator) {
        this.enumerator = enumerator;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
