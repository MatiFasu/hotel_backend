package com.hackacode.hotel.controller;

import com.hackacode.hotel.dto.LoginRequestDTO;
import com.hackacode.hotel.dto.LoginResponseDTO;
import com.hackacode.hotel.exception.UnauthorizedException;
import com.hackacode.hotel.model.Empleado;
import com.hackacode.hotel.repository.EmpleadoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final String HMAC_SECRET = "HackaCode2026SecretKeyForJwtSigning256Bits!";

    private final EmpleadoRepository empleadoRepository;

    @Autowired
    public AuthController(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO requestDTO) {
        Empleado empleado = empleadoRepository.findByUsername(requestDTO.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas: usuario o contraseña incorrectos."));

        if (!Boolean.TRUE.equals(empleado.getActivo())) {
            throw new UnauthorizedException("La cuenta del empleado se encuentra dada de baja (inactiva).");
        }

        String hashIngresado = hashSha256(requestDTO.getPassword());
        String storedHash = empleado.getPasswordHash();

        boolean passwordValida = storedHash != null && (
                storedHash.equals(hashIngresado)
                || storedHash.equals("$sha256$" + requestDTO.getPassword())
                || storedHash.equals(requestDTO.getPassword())
        );

        if (!passwordValida) {
            throw new UnauthorizedException("Credenciales inválidas: usuario o contraseña incorrectos.");
        }

        String token = generarJwtToken(empleado);
        String nombreCompleto = empleado.getNombre() + " " + empleado.getApellido();

        LoginResponseDTO response = new LoginResponseDTO(
                token,
                "Bearer",
                empleado.getIdEmpleado(),
                nombreCompleto,
                empleado.getUsername(),
                empleado.getRol()
        );

        return ResponseEntity.ok(response);
    }

    public static String hashSha256(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return "$sha256$" + HexFormat.of().formatHex(encoded);
        } catch (Exception e) {
            throw new IllegalStateException("Error calculando hash SHA-256", e);
        }
    }

    private String generarJwtToken(Empleado empleado) {
        try {
            String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            long iat = Instant.now().getEpochSecond();
            long exp = iat + (8 * 3600);
            String payloadJson = String.format(
                    "{\"sub\":\"%s\",\"empleadoId\":%d,\"rol\":\"%s\",\"iat\":%d,\"exp\":%d}",
                    empleado.getUsername(),
                    empleado.getIdEmpleado(),
                    empleado.getRol().name(),
                    iat,
                    exp
            );

            Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
            String headerB64 = encoder.encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
            String payloadB64 = encoder.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
            String data = headerB64 + "." + payloadB64;

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(HMAC_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String signatureB64 = encoder.encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));

            return data + "." + signatureB64;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el token JWT", e);
        }
    }
}
