package com.hackacode.hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackacode.hotel.AbstractIntegrationTest;
import com.hackacode.hotel.dto.LoginRequestDTO;
import com.hackacode.hotel.model.Empleado;
import com.hackacode.hotel.model.RolEmpleado;
import com.hackacode.hotel.repository.EmpleadoRepository;
import com.hackacode.hotel.repository.VentaRepository;
import com.hackacode.hotel.security.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.HexFormat;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ventaRepository.deleteAll();
        empleadoRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/auth/login - Credenciales válidas con BCrypt devuelven 200 OK con token JWT y rol")
    void testLoginExitosoConBcryptHTTP() throws Exception {
        empleadoRepository.save(new Empleado(
                null, "Valeria", "Gómez", "Av. Santa Fe 2100", "29111222",
                LocalDate.of(1985, 4, 10), "Argentina", "1144441111", "valeria@hackacode.com",
                "Gerente General", new BigDecimal("1250000.00"), "admin",
                passwordHasher.encode("Admin1234!"), RolEmpleado.ADMIN, true
        ));

        LoginRequestDTO dto = new LoginRequestDTO("admin", "Admin1234!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tipo", is("Bearer")))
                .andExpect(jsonPath("$.username", is("admin")))
                .andExpect(jsonPath("$.rol", is("ADMIN")));
    }

    @Test
    @DisplayName("POST /api/auth/login - Fallback transparente: hash legacy SHA-256 se valida y migra a BCrypt")
    void testLoginConMigracionTransparenteSha256HTTP() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest("Admin1234!".getBytes(StandardCharsets.UTF_8));
        String legacySha256Hash = "$sha256$" + HexFormat.of().formatHex(hashBytes);

        Empleado emp = empleadoRepository.save(new Empleado(
                null, "Valeria", "Gómez", "Av. Santa Fe 2100", "29111222",
                LocalDate.of(1985, 4, 10), "Argentina", "1144441111", "valeria@hackacode.com",
                "Gerente General", new BigDecimal("1250000.00"), "admin",
                legacySha256Hash, RolEmpleado.ADMIN, true
        ));

        LoginRequestDTO dto = new LoginRequestDTO("admin", "Admin1234!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.username", is("admin")));

        // Verificar que en base de datos el hash fue actualizado automáticamente a formato BCrypt ($2a$...)
        Empleado empActualizado = empleadoRepository.findById(emp.getIdEmpleado()).orElseThrow();
        assertTrue(empActualizado.getPasswordHash().startsWith("$2a$"),
                "El hash almacenado debe haber migrado automáticamente a formato BCrypt ($2a$...)");
    }

    @Test
    @DisplayName("POST /api/auth/login - Contraseña incorrecta devuelve 401 Unauthorized")
    void testLoginCredencialesInvalidasHTTP() throws Exception {
        empleadoRepository.save(new Empleado(
                null, "Valeria", "Gómez", "Av. Santa Fe 2100", "29111222",
                LocalDate.of(1985, 4, 10), "Argentina", "1144441111", "valeria@hackacode.com",
                "Gerente General", new BigDecimal("1250000.00"), "admin",
                passwordHasher.encode("Admin1234!"), RolEmpleado.ADMIN, true
        ));

        LoginRequestDTO dto = new LoginRequestDTO("admin", "ClaveIncorrecta!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Credenciales inválidas")));
    }

    @Test
    @DisplayName("POST /api/auth/login - Empleado inactivo devuelve 401 Unauthorized")
    void testLoginEmpleadoInactivoHTTP() throws Exception {
        empleadoRepository.save(new Empleado(
                null, "Lucas", "Fernández", "Av. Cabildo 1500", "33444555",
                LocalDate.of(1992, 8, 3), "Argentina", "1155552222", "lucas@hackacode.com",
                "Vendedor", new BigDecimal("890000.00"), "lucas",
                passwordHasher.encode("Vendedor1234!"), RolEmpleado.VENDEDOR, false
        ));

        LoginRequestDTO dto = new LoginRequestDTO("lucas", "Vendedor1234!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("inactiva")));
    }
}
