package com.pulsepass.organizationservice.domain.repositories;

import com.pulsepass.organizationservice.domain.entities.OrganizationRejectionReason;
import java.util.Optional;

public interface OrganizationRejectionReasonRepository {

    Optional<OrganizationRejectionReason> findByEnumerator(String enumerator);
}
