package org.integritylens.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecretsTest {
    @Test
    void retainsPasswordWhitespaceAndEveryUnicodeCharacter() {
        String password = "  " + "\uD83D\uDE42".repeat(125) + "Ж";
        String hash = Secrets.passwordHash(password);

        assertTrue(Secrets.passwordMatches(password, hash));
        assertFalse(Secrets.passwordMatches(password.trim(), hash));
        assertFalse(Secrets.passwordMatches(password.substring(0, password.length() - 1) + "ж", hash));
        assertNotEquals(hash, Secrets.passwordHash(password));
    }
}
