package com.hackacode.hotel.controller;

import com.hackacode.hotel.dto.LoginRequestDTO;
import com.hackacode.hotel.dto.LoginResponseDTO;
import com.hackacode.hotel.exception.UnauthorizedException;
import com.hackacode.hotel.model.Empleado;
import com.hackacode.hotel.repository.EmpleadoRepository;
import com.hackacode.hotel.security.JwtService;
import com.hackacode.hotel.security.PasswordHasher;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de autenticación para la emisión de tokens JWT.
 * Realiza la validación de credenciales con BCrypt y migración transparente de hashes legacy SHA-256.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final EmpleadoRepository empleadoRepository;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;

    @Autowired
    public AuthController(
            EmpleadoRepository empleadoRepository,
            PasswordHasher passwordHasher,
            JwtService jwtService) {
        this.empleadoRepository = empleadoRepository;
        this.passwordHasher = passwordHasher;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO requestDTO) {
        Empleado empleado = empleadoRepository.findByUsername(requestDTO.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas: usuario o contraseña incorrectos."));

        if (!Boolean.TRUE.equals(empleado.getActivo())) {
            throw new UnauthorizedException("La cuenta del empleado se encuentra dada de baja (inactiva).");
        }

        String rawPassword = requestDTO.getPassword();
        String storedHash = empleado.getPasswordHash();

        boolean passwordValida = passwordHasher.matches(rawPassword, storedHash);

        // Migración transparente: si coincide con hash legacy SHA-256, se re-hashea con BCrypt y se persiste
        if (!passwordValida && passwordHasher.isLegacySha256(storedHash)
                && passwordHasher.matchesLegacySha256(rawPassword, storedHash)) {
            passwordValida = true;
            empleado.setPasswordHash(passwordHasher.encode(rawPassword));
            empleadoRepository.save(empleado);
        }

        if (!passwordValida) {
            throw new UnauthorizedException("Credenciales inválidas: usuario o contraseña incorrectos.");
        }

        String token = jwtService.generarToken(empleado);
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
}
