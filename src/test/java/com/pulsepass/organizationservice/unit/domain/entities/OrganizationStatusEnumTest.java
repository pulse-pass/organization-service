package com.pulsepass.organizationservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.domain.entities.OrganizationStatusEnum;
import org.junit.jupiter.api.Test;

class OrganizationStatusEnumTest {

    @Test
    void values_all_matchSeededEnumerators() {
        assertThat(OrganizationStatusEnum.values()).extracting(Enum::name)
                .containsExactly("PENDING", "APPROVED", "REJECTED", "DELETED");
    }
}
