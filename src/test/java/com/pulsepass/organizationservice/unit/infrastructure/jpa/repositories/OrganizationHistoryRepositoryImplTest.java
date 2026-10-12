package com.pulsepass.organizationservice.unit.infrastructure.jpa.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.organizationservice.domain.entities.Organization;
import com.pulsepass.organizationservice.domain.entities.OrganizationHistory;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import com.pulsepass.organizationservice.infrastructure.jpa.entities.OrganizationHistoryEntity;
import com.pulsepass.organizationservice.infrastructure.jpa.repositories.JpaOrganizationHistoryRepository;
import com.pulsepass.organizationservice.infrastructure.jpa.repositories.OrganizationHistoryRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class OrganizationHistoryRepositoryImplTest {

    @Test
    void save_history_savesEntityAndReturnsMappedDomain() {
        var jpaRepository = mock(JpaOrganizationHistoryRepository.class);
        var repository = new OrganizationHistoryRepositoryImpl(jpaRepository);
        var status = new OrganizationStatus(1L, "PENDING", null);
        var organization = new Organization("org", "legal", "12345678000195", "Acme", status);
        var history = new OrganizationHistory("hist", organization, null, status, "{}");
        when(jpaRepository.save(any(OrganizationHistoryEntity.class))).thenAnswer(inv -> {
            OrganizationHistory persisted = inv.<OrganizationHistoryEntity>getArgument(0).toDomain();
            persisted.setId(10L);
            return OrganizationHistoryEntity.fromDomain(persisted);
        });

        OrganizationHistory saved = repository.save(history);

        ArgumentCaptor<OrganizationHistoryEntity> captor = ArgumentCaptor.forClass(OrganizationHistoryEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().toDomain().getOrganizationHistoryKey()).isEqualTo("hist");
        assertThat(saved.getId()).isEqualTo(10L);
        assertThat(saved.getOldStatus()).isNull();
        assertThat(saved.getNewStatus().getEnumerator()).isEqualTo("PENDING");
    }
}
