package com.pulsepass.organizationservice.domain.entities;

import java.time.LocalDateTime;

public class OrganizationStatus {

    private Long id;
    private String enumerator;
    private LocalDateTime createdAt;

    public OrganizationStatus() {
    }

    public OrganizationStatus(Long id, String enumerator, LocalDateTime createdAt) {
        this.id = id;
        this.enumerator = enumerator;
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

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
