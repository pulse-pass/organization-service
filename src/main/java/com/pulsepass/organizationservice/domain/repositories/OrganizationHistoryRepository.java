package com.pulsepass.organizationservice.domain.repositories;

import com.pulsepass.organizationservice.domain.entities.OrganizationHistory;

public interface OrganizationHistoryRepository {

    OrganizationHistory save(OrganizationHistory organizationHistory);
}
