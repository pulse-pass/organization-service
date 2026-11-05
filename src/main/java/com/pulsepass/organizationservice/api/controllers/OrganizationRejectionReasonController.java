package com.pulsepass.organizationservice.api.controllers;

import com.pulsepass.organizationservice.api.responses.ListInternalOrganizationRejectionReasonsResponse;
import com.pulsepass.organizationservice.api.responses.pagination.PaginatedResponse;
import com.pulsepass.organizationservice.api.responses.pagination.PaginationResponse;
import com.pulsepass.organizationservice.api.validators.InternalRequestValidator;
import com.pulsepass.organizationservice.application.services.OrganizationRejectionReasonService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrganizationRejectionReasonController {

    private final OrganizationRejectionReasonService organizationRejectionReasonService;
    private final InternalRequestValidator internalRequestValidator;

    public OrganizationRejectionReasonController(
            OrganizationRejectionReasonService organizationRejectionReasonService,
            InternalRequestValidator internalRequestValidator
    ) {
        this.organizationRejectionReasonService = organizationRejectionReasonService;
        this.internalRequestValidator = internalRequestValidator;
    }

    @GetMapping("/organization/internal/organization_rejection_reasons")
    public ResponseEntity<PaginatedResponse<ListInternalOrganizationRejectionReasonsResponse>>
            listInternalOrganizationRejectionReasons(
            @RequestHeader(value = "internal-token", required = false) String internalToken,
            @RequestHeader(value = "selected-user-roles", required = false) String selectedUserRoles,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(name = "page_size", defaultValue = "30") @Min(1) @Max(100) Integer pageSize
    ) {
        internalRequestValidator.validateInternalTokenOrAdmin(internalToken, selectedUserRoles);
        var organizationRejectionReasons = organizationRejectionReasonService
                .listOrganizationRejectionReasons(page, pageSize);
        var data = organizationRejectionReasons.stream()
                .map(ListInternalOrganizationRejectionReasonsResponse::fromEntity).toList();
        return ResponseEntity.ok(new PaginatedResponse<>(data, new PaginationResponse(page, pageSize)));
    }
}
