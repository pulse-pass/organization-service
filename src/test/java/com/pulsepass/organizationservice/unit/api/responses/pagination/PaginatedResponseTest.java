package com.pulsepass.organizationservice.unit.api.responses.pagination;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.api.responses.ListOrganizationsResponse;
import com.pulsepass.organizationservice.api.responses.pagination.PaginatedResponse;
import com.pulsepass.organizationservice.api.responses.pagination.PaginationResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

class PaginatedResponseTest {

    @Test
    void constructor_dataAndPagination_exposesBothValues() {
        ListOrganizationsResponse item = new ListOrganizationsResponse(
                "org-key", "legal-key", "Acme", "12345678000195", "PENDING", null, null, null, null);
        PaginationResponse pagination = new PaginationResponse(1, 30);

        PaginatedResponse<ListOrganizationsResponse> response = new PaginatedResponse<>(List.of(item), pagination);

        assertThat(response.data()).containsExactly(item);
        assertThat(response.pagination()).isSameAs(pagination);
    }
}
