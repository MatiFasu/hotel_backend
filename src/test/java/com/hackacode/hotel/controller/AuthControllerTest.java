package com.hackacode.hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackacode.hotel.AbstractIntegrationTest;
import com.hackacode.hotel.dto.LoginRequestDTO;
import com.hackacode.hotel.model.Empleado;
import com.hackacode.hotel.model.RolEmpleado;
import com.hackacode.hotel.repository.EmpleadoRepository;
import com.hackacode.hotel.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
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
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ventaRepository.deleteAll();
        empleadoRepository.deleteAll();

        empleadoRepository.save(new Empleado(
                null, "Valeria", "Gómez", "Av. Santa Fe 2100", "29111222",
                LocalDate.of(1985, 4, 10), "Argentina", "1144441111", "valeria@hackacode.com",
                "Gerente General", new BigDecimal("1250000.00"), "admin",
                AuthController.hashSha256("Admin1234!"), RolEmpleado.ADMIN, true
        ));
    }

    @Test
    @DisplayName("POST /api/auth/login - Credenciales válidas devuelven 200 OK con token JWT y rol")
    void testLoginExitosoHTTP() throws Exception {
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
    @DisplayName("POST /api/auth/login - Contraseña incorrecta devuelve 401 Unauthorized")
    void testLoginCredencialesInvalidasHTTP() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO("admin", "ClaveIncorrecta!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Credenciales inválidas")));
    }
}
