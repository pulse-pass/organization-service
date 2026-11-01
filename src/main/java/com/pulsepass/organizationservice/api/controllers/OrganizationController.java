package com.pulsepass.organizationservice.api.controllers;

import com.pulsepass.organizationservice.api.responses.CreateOrganizationResponse;
import com.pulsepass.organizationservice.api.responses.ListOrganizationsResponse;
import com.pulsepass.organizationservice.api.responses.pagination.PaginatedResponse;
import com.pulsepass.organizationservice.api.responses.pagination.PaginationResponse;
import com.pulsepass.organizationservice.api.schemas.CreateOrganizationSchema;
import com.pulsepass.organizationservice.api.validators.InternalRequestValidator;
import com.pulsepass.organizationservice.application.services.OrganizationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
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

    @PostMapping("/organization/internal/organizations")
    public ResponseEntity<CreateOrganizationResponse> createOrganization(
            @RequestHeader(value = "internal-token", required = false) String internalToken,
            @Valid @RequestBody CreateOrganizationSchema request
    ) {
        internalRequestValidator.validateInternalToken(internalToken);
        var organization = organizationService.createOrganization(request.toDTO());
        return ResponseEntity.status(201).body(CreateOrganizationResponse.fromEntity(organization));
    }

    @GetMapping("/organization/organizations")
    public ResponseEntity<PaginatedResponse<ListOrganizationsResponse>> listOrganizations(
            @RequestHeader(value = "selected-agent-key", required = false) String selectedAgentKey,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(name = "page_size", defaultValue = "30") @Min(1) @Max(100) Integer pageSize
    ) {
        internalRequestValidator.validateSelectedAgentKey(selectedAgentKey);
        var organizations = organizationService.listOrganizations(selectedAgentKey, page, pageSize);
        var data = organizations.stream().map(ListOrganizationsResponse::fromEntity).toList();
        return ResponseEntity.ok(new PaginatedResponse<>(data, new PaginationResponse(page, pageSize)));
    }
}
