package com.pulsepass.organizationservice.unit.api.schemas;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.api.schemas.CreateOrganizationSchema;
import com.pulsepass.organizationservice.application.dtos.input.CreateOrganizationDTO;
import org.junit.jupiter.api.Test;

class CreateOrganizationSchemaTest {

    @Test
    void toDTO_schema_copiesAllFields() {
        var schema = new CreateOrganizationSchema("legal-key", "12345678000195", "Acme");

        CreateOrganizationDTO dto = schema.toDTO();

        assertThat(dto.legalPersonKey()).isEqualTo("legal-key");
        assertThat(dto.documentNumber()).isEqualTo("12345678000195");
        assertThat(dto.name()).isEqualTo("Acme");
    }
}
