package com.pulsepass.organizationservice.unit.api.validators;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.organizationservice.api.validators.InternalRequestValidator;
import com.pulsepass.organizationservice.domain.exceptions.MissingSelectedAgentKeyException;
import com.pulsepass.organizationservice.domain.exceptions.UnauthorizedInternalRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class InternalRequestValidatorTest {

    private static final String TOKEN = "secret-token";

    @Test
    void validateInternalToken_matchingToken_doesNotThrow() {
        var validator = new InternalRequestValidator(TOKEN);

        assertThatCode(() -> validator.validateInternalToken(TOKEN)).doesNotThrowAnyException();
    }

    @Test
    void validateInternalToken_nullToken_throwsUnauthorized() {
        var validator = new InternalRequestValidator(TOKEN);

        assertThatThrownBy(() -> validator.validateInternalToken(null))
                .isInstanceOf(UnauthorizedInternalRequestException.class);
    }

    @Test
    void validateInternalToken_emptyToken_throwsUnauthorized() {
        var validator = new InternalRequestValidator(TOKEN);

        assertThatThrownBy(() -> validator.validateInternalToken(""))
                .isInstanceOf(UnauthorizedInternalRequestException.class);
    }

    @Test
    void validateInternalToken_differentToken_throwsUnauthorized() {
        var validator = new InternalRequestValidator(TOKEN);

        assertThatThrownBy(() -> validator.validateInternalToken("other"))
                .isInstanceOf(UnauthorizedInternalRequestException.class);
    }

    @Test
    void validateInternalToken_prefixOfToken_throwsUnauthorized() {
        var validator = new InternalRequestValidator(TOKEN);

        assertThatThrownBy(() -> validator.validateInternalToken("secret"))
                .isInstanceOf(UnauthorizedInternalRequestException.class);
    }

    @Test
    void validateSelectedAgentKey_validKey_doesNotThrow() {
        var validator = new InternalRequestValidator(TOKEN);

        assertThatCode(() -> validator.validateSelectedAgentKey("7b0f5e6a-1c3d-4e8f-9a2b-0d1e2f3a4b5c"))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  "})
    void validateSelectedAgentKey_missingOrBlankKey_throwsMissingSelectedAgentKey(String selectedAgentKey) {
        var validator = new InternalRequestValidator(TOKEN);

        assertThatThrownBy(() -> validator.validateSelectedAgentKey(selectedAgentKey))
                .isInstanceOf(MissingSelectedAgentKeyException.class);
    }
}
