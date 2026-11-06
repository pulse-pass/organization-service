package com.pulsepass.organizationservice.domain.gateways.paymentprovider;

import com.pulsepass.organizationservice.domain.gateways.paymentprovider.dtos.CreatePaymentAccountInputDTO;
import com.pulsepass.organizationservice.domain.gateways.paymentprovider.dtos.CreatePaymentAccountOutputDTO;

public interface PaymentProviderGateway {

    CreatePaymentAccountOutputDTO createPaymentAccount(CreatePaymentAccountInputDTO input);
}
