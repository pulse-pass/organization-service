package com.pulsepass.organizationservice.unit.api.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.organizationservice.api.exceptions.ExtraFieldErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.validation.FieldError;

class ExtraFieldErrorResponseTest {

    private static FieldError fieldError(String field, String code) {
        return new FieldError("target", field, null, false, new String[]{code}, null, "message");
    }

    @Test
    void fromFieldError_notBlank_returnsRequiredWithSnakeCasePath() {
        var response = ExtraFieldErrorResponse.fromFieldError(fieldError("phoneNumber.areaCode", "NotBlank"));

        assertThat(response).isEqualTo(new ExtraFieldErrorResponse("phone_number.area_code", "REQUIRED"));
    }

    @Test
    void fromFieldError_notNull_returnsRequired() {
        assertThat(ExtraFieldErrorResponse.fromFieldError(fieldError("name", "NotNull")).error()).isEqualTo("REQUIRED");
    }

    @Test
    void fromFieldError_pattern_returnsInvalidFormat() {
        assertThat(ExtraFieldErrorResponse.fromFieldError(fieldError("documentNumber", "Pattern")).error())
                .isEqualTo("INVALID_FORMAT");
    }

    @Test
    void fromFieldError_email_returnsInvalidFormat() {
        assertThat(ExtraFieldErrorResponse.fromFieldError(fieldError("email", "Email")).error())
                .isEqualTo("INVALID_FORMAT");
    }

    @Test
    void fromFieldError_size_returnsSize() {
        assertThat(ExtraFieldErrorResponse.fromFieldError(fieldError("name", "Size")).error()).isEqualTo("SIZE");
    }

    @Test
    void fromFieldError_unknownCode_returnsInvalidFormat() {
        assertThat(ExtraFieldErrorResponse.fromFieldError(fieldError("name", "Positive")).error())
                .isEqualTo("INVALID_FORMAT");
    }
}
