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

    private final InternalRequestValidator validator = new InternalRequestValidator("secret-token");

    @Test
    void validateInternalTokenOrAdmin_matchingTokenWithoutRoles_doesNotThrow() {
        assertThatCode(() -> validator.validateInternalTokenOrAdmin("secret-token", null))
                .doesNotThrowAnyException();
    }

    @Test
    void validateInternalTokenOrAdmin_matchingTokenWithOrganizerRole_doesNotThrow() {
        assertThatCode(() -> validator.validateInternalTokenOrAdmin("secret-token", "organizer"))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin", "organizer,admin", "organizer , admin ", " admin"})
    void validateInternalTokenOrAdmin_adminRoleWithoutToken_doesNotThrow(String roles) {
        assertThatCode(() -> validator.validateInternalTokenOrAdmin(null, roles)).doesNotThrowAnyException();
    }

    @Test
    void validateInternalTokenOrAdmin_wrongTokenAndAdminRole_doesNotThrow() {
        assertThatCode(() -> validator.validateInternalTokenOrAdmin("wrong", "admin")).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "organizer", "buyer,organizer", "administrator", "superadmin"})
    void validateInternalTokenOrAdmin_noTokenAndNoAdminRole_throwsUnauthorized(String roles) {
        assertThatThrownBy(() -> validator.validateInternalTokenOrAdmin(null, roles))
                .isInstanceOf(UnauthorizedInternalRequestException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "wrong", "secret"})
    void validateInternalTokenOrAdmin_invalidTokenAndNoRoles_throwsUnauthorized(String token) {
        assertThatThrownBy(() -> validator.validateInternalTokenOrAdmin(token, null))
                .isInstanceOf(UnauthorizedInternalRequestException.class);
    }

    @Test
    void validateInternalTokenOrAdmin_wrongTokenAndOrganizerRole_throwsUnauthorized() {
        assertThatThrownBy(() -> validator.validateInternalTokenOrAdmin("wrong", "organizer"))
                .isInstanceOf(UnauthorizedInternalRequestException.class);
    }

    @Test
    void validateSelectedAgentKey_validKey_doesNotThrow() {
        assertThatCode(() -> validator.validateSelectedAgentKey("7b0f5e6a-1c3d-4e8f-9a2b-0d1e2f3a4b5c"))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  "})
    void validateSelectedAgentKey_missingOrBlankKey_throwsMissingSelectedAgentKey(String selectedAgentKey) {
        assertThatThrownBy(() -> validator.validateSelectedAgentKey(selectedAgentKey))
                .isInstanceOf(MissingSelectedAgentKeyException.class);
    }
}
