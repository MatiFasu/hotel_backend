package com.hackacode.hotel.security;

import com.hackacode.hotel.model.Empleado;
import com.hackacode.hotel.model.RolEmpleado;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String TEST_SECRET = "TestJwtSecretKeyForAutomatedTestingOnlyLongerThan256Bits!";
    private JwtService jwtService;
    private Empleado empleadoAdmin;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET);
        empleadoAdmin = new Empleado(
                10L, "Valeria", "Gómez", "Av. Santa Fe 2100", "29111222",
                LocalDate.of(1985, 4, 10), "Argentina", "1144441111", "valeria@hackacode.com",
                "Gerente General", new BigDecimal("1250000.00"), "admin",
                "$2a$12$hash", RolEmpleado.ADMIN, true
        );
    }

    @Test
    @DisplayName("generarToken() crea un token JWT válido con claims de usuario, rol, emisor y expiración")
    void testGenerarYValidarToken() {
        String token = jwtService.generarToken(empleadoAdmin);

        assertNotNull(token);
        assertFalse(token.isBlank());

        Claims claims = jwtService.validarToken(token);
        assertEquals("admin", claims.getSubject());
        assertEquals("agencia-turismo-api", claims.getIssuer());
        assertEquals(10L, jwtService.extraerEmpleadoId(token));
        assertEquals(RolEmpleado.ADMIN, jwtService.extraerRol(token));
        assertNotNull(claims.getExpiration());
        assertNotNull(claims.getIssuedAt());
    }

    @Test
    @DisplayName("validarToken() rechaza tokens adulterados o firmados con otra clave")
    void testTokenAdulteradoLanzaExcepcion() {
        String token = jwtService.generarToken(empleadoAdmin);
        String tokenAdulterado = token.substring(0, token.length() - 5) + "abcde";

        assertThrows(JwtException.class, () -> jwtService.validarToken(tokenAdulterado));
    }

    @Test
    @DisplayName("Constructor rechaza secretos nulos o menores a 256 bits")
    void testConstructorRechazaSecretInseguro() {
        assertThrows(IllegalStateException.class, () -> new JwtService(null));
        assertThrows(IllegalStateException.class, () -> new JwtService("   "));
        assertThrows(IllegalStateException.class, () -> new JwtService("clave_muy_corta"));
    }
}
