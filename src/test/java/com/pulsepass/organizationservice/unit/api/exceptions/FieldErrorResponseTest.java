package com.pulsepass.organizationservice.unit.api.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.api.exceptions.FieldErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.validation.FieldError;

class FieldErrorResponseTest {

    private static FieldError fieldError(String field, String code) {
        return new FieldError("target", field, null, false, new String[]{code}, null, "message");
    }

    @Test
    void fromFieldError_notBlank_returnsRequiredWithSnakeCasePath() {
        var response = FieldErrorResponse.fromFieldError(fieldError("phoneNumber.areaCode", "NotBlank"));

        assertThat(response).isEqualTo(new FieldErrorResponse("phone_number.area_code", "REQUIRED"));
    }

    @Test
    void fromFieldError_notNull_returnsRequired() {
        assertThat(FieldErrorResponse.fromFieldError(fieldError("name", "NotNull")).error()).isEqualTo("REQUIRED");
    }

    @Test
    void fromFieldError_pattern_returnsInvalidFormat() {
        assertThat(FieldErrorResponse.fromFieldError(fieldError("documentNumber", "Pattern")).error())
                .isEqualTo("INVALID_FORMAT");
    }

    @Test
    void fromFieldError_email_returnsInvalidFormat() {
        assertThat(FieldErrorResponse.fromFieldError(fieldError("email", "Email")).error())
                .isEqualTo("INVALID_FORMAT");
    }

    @Test
    void fromFieldError_size_returnsSize() {
        assertThat(FieldErrorResponse.fromFieldError(fieldError("name", "Size")).error()).isEqualTo("SIZE");
    }

    @Test
    void fromFieldError_unknownCode_returnsInvalidFormat() {
        assertThat(FieldErrorResponse.fromFieldError(fieldError("name", "Positive")).error())
                .isEqualTo("INVALID_FORMAT");
    }
}
