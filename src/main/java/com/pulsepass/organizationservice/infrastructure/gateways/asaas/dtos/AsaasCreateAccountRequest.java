package com.pulsepass.organizationservice.infrastructure.gateways.asaas.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record AsaasCreateAccountRequest(
        @JsonProperty("name") String name,
        @JsonProperty("email") String email,
        @JsonProperty("loginEmail") String loginEmail,
        @JsonProperty("cpfCnpj") String cpfCnpj,
        @JsonProperty("mobilePhone") String mobilePhone,
        @JsonProperty("companyType") String companyType,
        @JsonProperty("incomeValue") BigDecimal incomeValue,
        @JsonProperty("address") String address,
        @JsonProperty("addressNumber") String addressNumber,
        @JsonProperty("complement") @JsonInclude(JsonInclude.Include.NON_NULL) String complement,
        @JsonProperty("province") String province,
        @JsonProperty("postalCode") String postalCode
) {
}
