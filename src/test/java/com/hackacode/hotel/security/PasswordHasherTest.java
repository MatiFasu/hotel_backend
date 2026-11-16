package com.hackacode.hotel.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    private PasswordHasher passwordHasher;

    @BeforeEach
    void setUp() {
        passwordHasher = new PasswordHasher();
    }

    @Test
    @DisplayName("encode() genera hashes BCrypt no deterministas con salt aleatorio")
    void testEncodeNoDeterminista() {
        String rawPassword = "MiPasswordSeguro123!";
        String hash1 = passwordHasher.encode(rawPassword);
        String hash2 = passwordHasher.encode(rawPassword);

        assertNotNull(hash1);
        assertNotNull(hash2);
        assertNotEquals(hash1, hash2, "Dos hashes de la misma contraseña deben ser distintos debido al salt de BCrypt");
        assertTrue(hash1.startsWith("$2a$12$"), "El hash debe tener prefijo $2a$ y costo 12");
        assertTrue(hash2.startsWith("$2a$12$"), "El hash debe tener prefijo $2a$ y costo 12");
    }

    @Test
    @DisplayName("matches() valida correctamente contraseñas válidas e inválidas")
    void testMatches() {
        String rawPassword = "AdminPassword2026";
        String hash = passwordHasher.encode(rawPassword);

        assertTrue(passwordHasher.matches(rawPassword, hash));
        assertFalse(passwordHasher.matches("PasswordIncorrecta", hash));
        assertFalse(passwordHasher.matches(null, hash));
        assertFalse(passwordHasher.matches(rawPassword, null));
    }

    @Test
    @DisplayName("Soporte de migración: detección y validación de hash legacy SHA-256")
    void testLegacySha256Support() throws Exception {
        String rawPassword = "LegacyPassword123";
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encoded = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
        String legacyHash = "$sha256$" + HexFormat.of().formatHex(encoded);

        assertTrue(passwordHasher.isLegacySha256(legacyHash));
        assertFalse(passwordHasher.isLegacySha256("$2a$12$someBcryptHash"));

        assertTrue(passwordHasher.matchesLegacySha256(rawPassword, legacyHash));
        assertFalse(passwordHasher.matchesLegacySha256("OtraPassword", legacyHash));
    }
}
