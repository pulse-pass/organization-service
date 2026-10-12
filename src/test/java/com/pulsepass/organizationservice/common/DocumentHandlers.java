package com.pulsepass.organizationservice.common;

import java.util.Random;

public final class DocumentHandlers {

    private static final Random RANDOM = new Random();

    private DocumentHandlers() {
    }

    private static int calculateDigit(int[] digits, int length) {
        int weight = length - 7;
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += digits[i] * weight;
            weight = weight == 2 ? 9 : weight - 1;
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    public static String generateCnpj() {
        int[] digits = new int[14];
        for (int i = 0; i < 8; i++) {
            digits[i] = RANDOM.nextInt(10);
        }
        digits[11] = 1;
        digits[12] = calculateDigit(digits, 12);
        digits[13] = calculateDigit(digits, 13);

        StringBuilder sb = new StringBuilder();
        for (int digit : digits) {
            sb.append(digit);
        }
        return sb.toString();
    }
}
