package com.pulsepass.organizationservice.api.responses.pagination;

public record PaginationResponse(
        Integer page,
        Integer pageSize
) {
}
