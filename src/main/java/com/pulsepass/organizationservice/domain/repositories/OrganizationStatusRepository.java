package com.pulsepass.organizationservice.domain.repositories;

import com.pulsepass.organizationservice.domain.entities.OrganizationStatus;
import java.util.Optional;

public interface OrganizationStatusRepository {

    Optional<OrganizationStatus> findByEnumerator(String enumerator);
}
