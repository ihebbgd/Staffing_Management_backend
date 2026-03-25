package com.demo.staffing_management_backend.security;

import java.security.SecureRandom;

/**
 * Generates temporary passwords for admin-provisioned logins. The alphabet omits
 * easily-confused characters (0/O, 1/l/I) so a password can be read out reliably.
 */
public final class PasswordGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    private PasswordGenerator() {
    }

    public static String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
