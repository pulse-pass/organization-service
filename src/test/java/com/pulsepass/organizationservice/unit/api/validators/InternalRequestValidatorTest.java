package com.pulsepass.organizationservice.unit.api.validators;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.organizationservice.api.validators.InternalRequestValidator;
import com.pulsepass.organizationservice.domain.exceptions.UnauthorizedInternalRequestException;
import org.junit.jupiter.api.Test;

class InternalRequestValidatorTest {

    private static final String TOKEN = "secret-token";

    @Test
    void validateInternalRequest_matchingToken_doesNotThrow() {
        var validator = new InternalRequestValidator(TOKEN);

        assertThatCode(() -> validator.validateInternalRequest(TOKEN)).doesNotThrowAnyException();
    }

    @Test
    void validateInternalRequest_nullToken_throwsUnauthorized() {
        var validator = new InternalRequestValidator(TOKEN);

        assertThatThrownBy(() -> validator.validateInternalRequest(null))
                .isInstanceOf(UnauthorizedInternalRequestException.class);
    }

    @Test
    void validateInternalRequest_emptyToken_throwsUnauthorized() {
        var validator = new InternalRequestValidator(TOKEN);

        assertThatThrownBy(() -> validator.validateInternalRequest(""))
                .isInstanceOf(UnauthorizedInternalRequestException.class);
    }

    @Test
    void validateInternalRequest_differentToken_throwsUnauthorized() {
        var validator = new InternalRequestValidator(TOKEN);

        assertThatThrownBy(() -> validator.validateInternalRequest("other"))
                .isInstanceOf(UnauthorizedInternalRequestException.class);
    }

    @Test
    void validateInternalRequest_prefixOfToken_throwsUnauthorized() {
        var validator = new InternalRequestValidator(TOKEN);

        assertThatThrownBy(() -> validator.validateInternalRequest("secret"))
                .isInstanceOf(UnauthorizedInternalRequestException.class);
    }
}
