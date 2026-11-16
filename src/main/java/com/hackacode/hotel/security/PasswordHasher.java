package com.hackacode.hotel.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Componente de hashing de contraseñas utilizando BCrypt con factor de costo 12.
 * Proporciona además utilidades para la migración transparente de hashes legacy SHA-256 ($sha256$...).
 */
@Component
public class PasswordHasher {

    private static final int BCRYPT_STRENGTH = 12;
    private final BCryptPasswordEncoder passwordEncoder;

    public PasswordHasher() {
        this.passwordEncoder = new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }

    /**
     * Genera un hash seguro BCrypt (costo 12) a partir de una contraseña en texto plano.
     */
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("La contraseña en texto plano no puede ser nula.");
        }
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Verifica si una contraseña en texto plano coincide con un hash BCrypt almacenado.
     */
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Comprueba si un hash almacenado utiliza el formato legacy SHA-256.
     */
    public boolean isLegacySha256(String storedHash) {
        return storedHash != null && storedHash.startsWith("$sha256$");
    }

    /**
     * Verifica en tiempo constante si la contraseña coincide con un hash legacy SHA-256.
     */
    public boolean matchesLegacySha256(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null || !isLegacySha256(storedHash)) {
            return false;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(rawPassword.toString().getBytes(StandardCharsets.UTF_8));
            String expectedHash = "$sha256$" + HexFormat.of().formatHex(encoded);
            return MessageDigest.isEqual(
                    storedHash.getBytes(StandardCharsets.UTF_8),
                    expectedHash.getBytes(StandardCharsets.UTF_8)
            );
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Error al verificar hash SHA-256 legacy", e);
        }
    }
}
