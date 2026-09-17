package com.banking.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class CredentialHasher {
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private CredentialHasher() {
    }

    public static String hash(String password) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(salt);
            byte[] derived = derive(password.toCharArray(), salt);
            return "pbkdf2$" + Base64.getEncoder().encodeToString(salt)
                    + "$" + Base64.getEncoder().encodeToString(derived);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not hash credential.", exception);
        }
    }

    public static boolean matches(String password, String storedValue) {
        try {
            String[] parts = storedValue.split("\\$", -1);
            if (parts.length != 3 || !"pbkdf2".equals(parts[0])) return false;
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            return MessageDigest.isEqual(expected, derive(password.toCharArray(), salt));
        } catch (Exception exception) {
            return false;
        }
    }

    private static byte[] derive(char[] password, byte[] salt) throws Exception {
        PBEKeySpec specification = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(specification).getEncoded();
        } finally {
            specification.clearPassword();
        }
    }
}