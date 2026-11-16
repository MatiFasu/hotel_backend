package com.hackacode.hotel.security;

import com.hackacode.hotel.model.Empleado;
import com.hackacode.hotel.model.RolEmpleado;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Servicio encargado de la generación, parseo y validación de tokens JWT (HS256)
 * utilizando la biblioteca io.jsonwebtoken (jjwt 0.12.x).
 */
@Service
public class JwtService {

    public static final String ISSUER = "agencia-turismo-api";
    public static final Duration TOKEN_VALIDITY = Duration.ofHours(8);

    private final SecretKey signingKey;

    public JwtService(@Value("${security.jwt.secret:${JWT_SECRET:}}") String jwtSecret) {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("JWT Secret no configurado. Debe definirse la variable de entorno JWT_SECRET o la propiedad security.jwt.secret.");
        }
        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT Secret inseguro: la clave debe tener al menos 256 bits (32 caracteres).");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
    }

    /**
     * Genera un token JWT con firma HS256 para el empleado autenticado.
     */
    public String generarToken(Empleado empleado) {
        Instant now = Instant.now();
        Instant expiration = now.plus(TOKEN_VALIDITY);

        return Jwts.builder()
                .issuer(ISSUER)
                .subject(empleado.getUsername())
                .claim("empleadoId", empleado.getIdEmpleado())
                .claim("rol", empleado.getRol().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Valida el token y retorna los Claims si la firma y la fecha de expiración son correctas.
     * @throws JwtException si el token está expirado, alterado o es inválido.
     */
    public Claims validarToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extrae el username (Subject) del token validándolo.
     */
    public String extraerUsername(String token) {
        return validarToken(token).getSubject();
    }

    /**
     * Extrae el rol del empleado a partir de los claims del token.
     */
    public RolEmpleado extraerRol(String token) {
        Claims claims = validarToken(token);
        String rolStr = claims.get("rol", String.class);
        return RolEmpleado.valueOf(rolStr);
    }

    /**
     * Extrae el ID del empleado a partir de los claims del token.
     */
    public Long extraerEmpleadoId(String token) {
        Claims claims = validarToken(token);
        Number id = claims.get("empleadoId", Number.class);
        return id != null ? id.longValue() : null;
    }
}
