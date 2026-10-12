package com.pulsepass.organizationservice.api.controllers;

import com.pulsepass.organizationservice.api.responses.CreateOrganizationResponse;
import com.pulsepass.organizationservice.api.schemas.CreateOrganizationSchema;
import com.pulsepass.organizationservice.api.validators.InternalRequestValidator;
import com.pulsepass.organizationservice.application.services.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrganizationController {

    private final OrganizationService organizationService;
    private final InternalRequestValidator internalRequestValidator;

    public OrganizationController(
            OrganizationService organizationService,
            InternalRequestValidator internalRequestValidator
    ) {
        this.organizationService = organizationService;
        this.internalRequestValidator = internalRequestValidator;
    }

    @PostMapping("/internal/organizations")
    public ResponseEntity<CreateOrganizationResponse> createOrganization(
            @RequestHeader(value = "internal-token", required = false) String internalToken,
            @Valid @RequestBody CreateOrganizationSchema request
    ) {
        internalRequestValidator.validateInternalRequest(internalToken);
        var organization = organizationService.createOrganization(request.toDTO());
        return ResponseEntity.status(201).body(CreateOrganizationResponse.fromEntity(organization));
    }
}
