package com.hackacode.hotel.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackacode.hotel.AbstractIntegrationTest;
import com.hackacode.hotel.dto.EmpleadoRequestDTO;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityFilterChainIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private ObjectMapper objectMapper;

    private Empleado admin;
    private Empleado vendedor;
    private String adminToken;
    private String vendedorToken;

    @BeforeEach
    void setUp() {
        ventaRepository.deleteAll();
        empleadoRepository.deleteAll();

        admin = empleadoRepository.save(new Empleado(
                null, "Valeria", "Gómez", "Av. Santa Fe 2100", "29111222",
                LocalDate.of(1985, 4, 10), "Argentina", "1144441111", "admin@hackacode.com",
                "Gerente", new BigDecimal("1250000.00"), "admin_sec",
                passwordHasher.encode("AdminPass123!"), RolEmpleado.ADMIN, true
        ));

        vendedor = empleadoRepository.save(new Empleado(
                null, "Lucas", "Fernández", "Av. Cabildo 1500", "33444555",
                LocalDate.of(1992, 8, 3), "Argentina", "1155552222", "vendedor@hackacode.com",
                "Vendedor", new BigDecimal("890000.00"), "vendedor_sec",
                passwordHasher.encode("VendedorPass123!"), RolEmpleado.VENDEDOR, true
        ));

        adminToken = jwtService.generarToken(admin);
        vendedorToken = jwtService.generarToken(vendedor);
    }

    @Test
    @DisplayName("Petición sin token a endpoint protegido /api/clientes devuelve 401 Unauthorized")
    void testRequestSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", containsString("se requiere un token Bearer")));
    }

    @Test
    @DisplayName("Petición con token JWT Bearer válido devuelve 200 OK")
    void testRequestConTokenValidoDevuelve200() throws Exception {
        mockMvc.perform(get("/api/clientes")
                .header("Authorization", "Bearer " + vendedorToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Petición con token inválido devuelve 401 Unauthorized")
    void testRequestConTokenInvalidoDevuelve401() throws Exception {
        mockMvc.perform(get("/api/clientes")
                .header("Authorization", "Bearer token_falso_invalido_12345"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("Rol VENDEDOR en endpoint exclusivo de ADMIN devuelve 403 Forbidden")
    void testVendedorEnEndpointAdminDevuelve403() throws Exception {
        EmpleadoRequestDTO nuevoEmp = new EmpleadoRequestDTO(
                "Carlos", "Ruiz", "Calle 50", "36111222",
                LocalDate.of(1993, 3, 10), "Argentina", "1122334455", "carlos@test.com",
                "Vendedor", new BigDecimal("700000.00"), "cruiz",
                "Password123!", RolEmpleado.VENDEDOR
        );

        mockMvc.perform(post("/api/empleados")
                .header("Authorization", "Bearer " + vendedorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoEmp)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("permisos insuficientes")));
    }

    @Test
    @DisplayName("Rol ADMIN en endpoint exclusivo de ADMIN devuelve 201 Created")
    void testAdminEnEndpointAdminDevuelve201() throws Exception {
        EmpleadoRequestDTO nuevoEmp = new EmpleadoRequestDTO(
                "Carlos", "Ruiz", "Calle 50", "36111222",
                LocalDate.of(1993, 3, 10), "Argentina", "1122334455", "carlos@test.com",
                "Vendedor", new BigDecimal("700000.00"), "cruiz",
                "Password123!", RolEmpleado.VENDEDOR
        );

        mockMvc.perform(post("/api/empleados")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoEmp)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("cruiz")));
    }

    @Test
    @DisplayName("Endpoints públicos (/actuator/health y /api/auth/login) son accesibles sin token")
    void testEndpointsPublicosAccesiblesSinToken() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        // /api/auth/login es público (sin token responde con validación o 401 si datos vacíos, nunca rechaza por filtro)
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin_sec\",\"password\":\"AdminPass123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }
}
