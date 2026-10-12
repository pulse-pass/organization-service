package com.pulsepass.organizationservice.unit.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReasonEnum;
import org.junit.jupiter.api.Test;

class OrganizationRejectionReasonEnumTest {

    @Test
    void values_all_matchSeededEnumerators() {
        assertThat(OrganizationRejectionReasonEnum.values()).extracting(Enum::name)
                .containsExactly("INACTIVE_DOCUMENT", "INCOMPATIBLE_ACTIVITY", "INCONSISTENT_DATA",
                        "UNVERIFIED_OWNER");
    }
}
