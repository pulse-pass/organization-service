package com.pulsepass.organizationservice.unit.api.responses.pagination;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.api.responses.pagination.PaginationResponse;
import org.junit.jupiter.api.Test;

class PaginationResponseTest {

    @Test
    void constructor_pageAndPageSize_exposesBothValues() {
        PaginationResponse response = new PaginationResponse(2, 30);

        assertThat(response.page()).isEqualTo(2);
        assertThat(response.pageSize()).isEqualTo(30);
    }
}
