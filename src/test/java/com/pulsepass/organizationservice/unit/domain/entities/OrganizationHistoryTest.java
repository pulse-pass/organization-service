package com.pulsepass.organizationservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.domain.entities.Organization;
import com.pulsepass.organizationservice.domain.entities.OrganizationHistory;
import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class OrganizationHistoryTest {

    @Test
    void constructor_creationArguments_setsFieldsAndLeavesIdNull() {
        var organization = new Organization();
        var status = new OrganizationStatus(1L, "PENDING", null);

        var history = new OrganizationHistory("key", organization, null, status, "{}");

        assertThat(history.getId()).isNull();
        assertThat(history.getOrganizationHistoryKey()).isEqualTo("key");
        assertThat(history.getOrganization()).isSameAs(organization);
        assertThat(history.getOldStatus()).isNull();
        assertThat(history.getNewStatus()).isSameAs(status);
        assertThat(history.getHistoryData()).isEqualTo("{}");
        assertThat(history.getCreatedAt()).isNull();
    }

    @Test
    void constructor_allArgumentsAndSetters_roundTripValues() {
        var now = LocalDateTime.now();
        var oldStatus = new OrganizationStatus(1L, "PENDING", now);
        var newStatus = new OrganizationStatus(2L, "APPROVED", now);
        var organization = new Organization();

        var history = new OrganizationHistory(5L, "key", organization, oldStatus, newStatus, "{}", now);

        assertThat(history.getId()).isEqualTo(5L);
        assertThat(history.getOldStatus()).isSameAs(oldStatus);
        assertThat(history.getCreatedAt()).isEqualTo(now);

        var empty = new OrganizationHistory();
        empty.setId(1L);
        empty.setOrganizationHistoryKey("k");
        empty.setOrganization(organization);
        empty.setOldStatus(oldStatus);
        empty.setNewStatus(newStatus);
        empty.setHistoryData("{\"a\":1}");
        empty.setCreatedAt(now);
        assertThat(empty.getId()).isEqualTo(1L);
        assertThat(empty.getOrganizationHistoryKey()).isEqualTo("k");
        assertThat(empty.getOrganization()).isSameAs(organization);
        assertThat(empty.getNewStatus()).isSameAs(newStatus);
        assertThat(empty.getHistoryData()).isEqualTo("{\"a\":1}");
    }

    @Test
    void fromOrganization_organizationAndOldStatus_buildsHistoryWithCurrentStatusAndSnapshot() {
        var oldStatus = new OrganizationStatus(1L, "PENDING", null);
        var currentStatus = new OrganizationStatus(2L, "APPROVED", null);
        var organization = new Organization("org", "legal", "12345678000195", "Acme", currentStatus);

        var history = OrganizationHistory.fromOrganization(organization, oldStatus);

        assertThat(history.getOrganizationHistoryKey()).matches("[0-9a-f-]{36}");
        assertThat(history.getOrganization()).isSameAs(organization);
        assertThat(history.getOldStatus()).isSameAs(oldStatus);
        assertThat(history.getNewStatus()).isSameAs(currentStatus);
        assertThat(history.getHistoryData())
                .contains("\"organization_key\":\"org\"")
                .contains("\"document_number\":\"12345678000195\"")
                .contains("\"organization_status\":\"APPROVED\"");
    }

    @Test
    void fromOrganization_withoutOldStatus_leavesOldStatusNull() {
        var status = new OrganizationStatus(1L, "PENDING", null);
        var organization = new Organization("org", "legal", "12345678000195", "Acme", status);

        var history = OrganizationHistory.fromOrganization(organization, null);

        assertThat(history.getOldStatus()).isNull();
        assertThat(history.getNewStatus()).isSameAs(status);
    }
}
