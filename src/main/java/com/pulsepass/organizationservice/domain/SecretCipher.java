package com.pulsepass.organizationservice.domain;

public interface SecretCipher {

    String encrypt(String plainText);

    String decrypt(String cipherText);
}
