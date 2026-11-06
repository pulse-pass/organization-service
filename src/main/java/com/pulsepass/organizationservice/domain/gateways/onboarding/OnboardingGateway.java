package com.pulsepass.organizationservice.domain.gateways.onboarding;

import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.GetLegalPersonInputDTO;
import com.pulsepass.organizationservice.domain.gateways.onboarding.dtos.GetLegalPersonOutputDTO;

public interface OnboardingGateway {

    GetLegalPersonOutputDTO getLegalPerson(GetLegalPersonInputDTO input);
}
