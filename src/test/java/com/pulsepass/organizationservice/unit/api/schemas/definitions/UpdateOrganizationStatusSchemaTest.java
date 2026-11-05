package com.pulsepass.organizationservice.unit.api.schemas.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.api.schemas.definitions.UpdateOrganizationStatusSchema;
import org.junit.jupiter.api.Test;

class UpdateOrganizationStatusSchemaTest {

    @Test
    void values_all_matchAllowedTargetStatuses() {
        assertThat(UpdateOrganizationStatusSchema.values()).extracting(Enum::name)
                .containsExactly("APPROVED", "REJECTED");
    }
}
