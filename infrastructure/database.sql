SET NAMES utf8mb4;

CREATE SCHEMA IF NOT EXISTS organization;

CREATE TABLE IF NOT EXISTS organization.organization_status (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    enumerator    VARCHAR(50)   NOT NULL,
    created_at    TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_organization_status PRIMARY KEY (id),
    CONSTRAINT organization_status_enumerator_unique UNIQUE (enumerator)
);

INSERT IGNORE INTO organization.organization_status (enumerator)
VALUES ('PENDING'), ('APPROVED'), ('REJECTED'), ('DELETED');

CREATE TABLE IF NOT EXISTS organization.organization_rejection_reason (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    enumerator    VARCHAR(50)   NOT NULL,
    description   VARCHAR(255)  NOT NULL,
    created_at    TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_organization_rejection_reason PRIMARY KEY (id),
    CONSTRAINT organization_rejection_reason_enumerator_unique UNIQUE (enumerator)
);

INSERT IGNORE INTO organization.organization_rejection_reason (enumerator, description) VALUES
    ('INACTIVE_DOCUMENT',     'CNPJ inapto, suspenso ou baixado na Receita Federal'),
    ('INCOMPATIBLE_ACTIVITY', 'Atividade da empresa incompatível com a venda de ingressos'),
    ('INCONSISTENT_DATA',     'Dados cadastrais inconsistentes com os registros da empresa'),
    ('UNVERIFIED_OWNER',      'Não foi possível confirmar o vínculo da dona da conta com a empresa');

CREATE TABLE IF NOT EXISTS organization.organization (
    id                                BIGINT         NOT NULL AUTO_INCREMENT,
    organization_key                  VARCHAR(36)    NOT NULL,
    legal_person_key                  VARCHAR(36)    NOT NULL,
    external_account_key              VARCHAR(36)    NULL,
    external_wallet_key               VARCHAR(36)    NULL,
    document_number                   VARCHAR(14)    NOT NULL,
    name                              VARCHAR(255)   NOT NULL,
    external_api_key                  VARCHAR(512)   NULL,
    balance_amount                    DECIMAL(19,2)  NOT NULL DEFAULT 0.00,
    organization_rejection_reason_id  BIGINT         NULL,
    organization_status_id            BIGINT         NOT NULL,
    created_at                        TIMESTAMP(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at                        TIMESTAMP(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_organization PRIMARY KEY (id),
    CONSTRAINT organization_key_unique UNIQUE (organization_key),
    CONSTRAINT organization_legal_person_key_unique UNIQUE (legal_person_key),
    CONSTRAINT organization_document_number_unique UNIQUE (document_number),
    CONSTRAINT fk1_organization FOREIGN KEY (organization_rejection_reason_id)
        REFERENCES organization.organization_rejection_reason (id),
    CONSTRAINT fk2_organization FOREIGN KEY (organization_status_id)
        REFERENCES organization.organization_status (id)
);

CREATE TABLE IF NOT EXISTS organization.organization_history (
    id                        BIGINT        NOT NULL AUTO_INCREMENT,
    organization_history_key  VARCHAR(36)   NOT NULL,
    organization_id           BIGINT        NOT NULL,
    old_status_id             BIGINT        NULL,
    new_status_id             BIGINT        NOT NULL,
    history_data              JSON          NOT NULL,
    created_at                TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_organization_history PRIMARY KEY (id),
    CONSTRAINT organization_history_key_unique UNIQUE (organization_history_key),
    CONSTRAINT fk1_organization_history FOREIGN KEY (organization_id)
        REFERENCES organization.organization (id),
    CONSTRAINT fk2_organization_history FOREIGN KEY (old_status_id)
        REFERENCES organization.organization_status (id),
    CONSTRAINT fk3_organization_history FOREIGN KEY (new_status_id)
        REFERENCES organization.organization_status (id)
);
