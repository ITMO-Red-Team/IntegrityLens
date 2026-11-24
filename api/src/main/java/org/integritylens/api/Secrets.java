package org.integritylens.api;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

final class Secrets {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ITERATIONS = 600_000;
    private static final Base64.Encoder ENCODE = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODE = Base64.getUrlDecoder();

    static String token() {
        return ENCODE.encodeToString(random(32));
    }

    static byte[] tokenHash(String token) {
        return sha256(token.getBytes(StandardCharsets.US_ASCII));
    }

    static byte[] sha256(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }

    static String passwordHash(String password) {
        byte[] salt = random(16);
        return "pbkdf2-sha256$" + ITERATIONS + "$" + ENCODE.encodeToString(salt)
                + "$" + ENCODE.encodeToString(derive(password, salt, ITERATIONS));
    }

    static boolean passwordMatches(String password, String hash) {
        String[] fields = hash.split("\\$");
        return MessageDigest.isEqual(DECODE.decode(fields[3]),
                derive(password, DECODE.decode(fields[2]), Integer.parseInt(fields[1])));
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        var spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(ex);
        } finally {
            spec.clearPassword();
        }
    }

    private static byte[] random(int size) {
        byte[] bytes = new byte[size];
        RANDOM.nextBytes(bytes);
        return bytes;
    }
}
