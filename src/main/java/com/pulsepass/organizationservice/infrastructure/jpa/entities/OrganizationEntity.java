package com.pulsepass.organizationservice.infrastructure.jpa.entities;

import com.pulsepass.organizationservice.domain.entities.Organization;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "organization")
public class OrganizationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_key", nullable = false, unique = true, length = 36)
    private String organizationKey;

    @Column(name = "legal_person_key", nullable = false, unique = true, length = 36)
    private String legalPersonKey;

    @Column(name = "external_account_key", length = 36)
    private String externalAccountKey;

    @Column(name = "external_wallet_key", length = 36)
    private String externalWalletKey;

    @Column(name = "document_number", nullable = false, unique = true, length = 14)
    private String documentNumber;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "external_api_key", length = 512)
    private String externalApiKey;

    @Column(name = "balance_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_rejection_reason_id")
    private OrganizationRejectionReasonEntity organizationRejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_status_id", nullable = false)
    private OrganizationStatusEntity organizationStatus;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public OrganizationEntity() {
    }

    public OrganizationEntity(
            Long id,
            String organizationKey,
            String legalPersonKey,
            String externalAccountKey,
            String externalWalletKey,
            String documentNumber,
            String name,
            String externalApiKey,
            BigDecimal balanceAmount,
            OrganizationRejectionReasonEntity organizationRejectionReason,
            OrganizationStatusEntity organizationStatus,
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

    public static OrganizationEntity fromDomain(Organization domain) {
        var rejectionReason = domain.getOrganizationRejectionReason();
        return new OrganizationEntity(
                domain.getId(),
                domain.getOrganizationKey(),
                domain.getLegalPersonKey(),
                domain.getExternalAccountKey(),
                domain.getExternalWalletKey(),
                domain.getDocumentNumber(),
                domain.getName(),
                domain.getExternalApiKey(),
                domain.getBalanceAmount(),
                rejectionReason == null ? null : OrganizationRejectionReasonEntity.fromDomain(rejectionReason),
                OrganizationStatusEntity.fromDomain(domain.getOrganizationStatus()),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public Organization toDomain() {
        return new Organization(
                this.id,
                this.organizationKey,
                this.legalPersonKey,
                this.externalAccountKey,
                this.externalWalletKey,
                this.documentNumber,
                this.name,
                this.externalApiKey,
                this.balanceAmount,
                this.organizationRejectionReason == null ? null : this.organizationRejectionReason.toDomain(),
                this.organizationStatus.toDomain(),
                this.createdAt,
                this.updatedAt
        );
    }
}
