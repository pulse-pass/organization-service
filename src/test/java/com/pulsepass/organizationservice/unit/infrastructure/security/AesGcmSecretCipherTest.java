package com.pulsepass.organizationservice.unit.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.organizationservice.infrastructure.security.AesGcmSecretCipher;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class AesGcmSecretCipherTest {

    private static final String KEY = Base64.getEncoder()
            .encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));
    private static final String OTHER_KEY = Base64.getEncoder()
            .encodeToString("fedcba9876543210fedcba9876543210".getBytes(StandardCharsets.UTF_8));
    private static final String SECRET = "$aact_hmlg_sandbox-api-key";

    private final AesGcmSecretCipher cipher = new AesGcmSecretCipher(KEY);

    @Test
    void decrypt_encryptedValue_returnsOriginalSecret() {
        var encrypted = cipher.encrypt(SECRET);

        assertThat(cipher.decrypt(encrypted)).isEqualTo(SECRET);
    }

    @Test
    void encrypt_secret_returnsBase64DifferentFromSecretWithIvAndTag() {
        var encrypted = cipher.encrypt(SECRET);

        assertThat(encrypted).isNotEqualTo(SECRET).doesNotContain(SECRET);
        assertThat(Base64.getDecoder().decode(encrypted)).hasSize(12 + SECRET.length() + 16);
    }

    @Test
    void encrypt_sameSecretTwice_returnsDifferentValues() {
        assertThat(cipher.encrypt(SECRET)).isNotEqualTo(cipher.encrypt(SECRET));
    }

    @Test
    void decrypt_wrongKey_throwsIllegalStateException() {
        var encrypted = cipher.encrypt(SECRET);

        assertThatThrownBy(() -> new AesGcmSecretCipher(OTHER_KEY).decrypt(encrypted))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void decrypt_tamperedValue_throwsIllegalStateException() {
        byte[] payload = Base64.getDecoder().decode(cipher.encrypt(SECRET));
        payload[payload.length - 1] ^= 1;
        var tampered = Base64.getEncoder().encodeToString(payload);

        assertThatThrownBy(() -> cipher.decrypt(tampered)).isInstanceOf(IllegalStateException.class);
    }
}
