package com.pulsepass.organizationservice.api.schemas;

import com.pulsepass.organizationservice.application.dtos.input.CreateOrganizationDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.UUID;

public record CreateOrganizationSchema(
        @NotBlank @UUID(allowNil = false, letterCase = UUID.LetterCase.INSENSITIVE) String legalPersonKey,
        @NotBlank @Pattern(regexp = "^\\d{14}$") String documentNumber,
        @NotBlank @Size(max = 255) String name
) {

    public CreateOrganizationDTO toDTO() {
        return new CreateOrganizationDTO(legalPersonKey, documentNumber, name);
    }
}
