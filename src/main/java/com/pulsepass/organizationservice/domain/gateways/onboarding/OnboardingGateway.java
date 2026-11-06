package com.pulsepass.organizationservice.domain.gateways.onboarding;

import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.GetPersonInputDTO;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.GetPersonOutputDTO;

public interface OnboardingGateway {

    GetPersonOutputDTO getPerson(GetPersonInputDTO input);
}
