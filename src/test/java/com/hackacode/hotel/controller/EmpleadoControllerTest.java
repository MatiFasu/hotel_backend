package com.hackacode.hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackacode.hotel.AbstractIntegrationTest;
import com.hackacode.hotel.dto.EmpleadoRequestDTO;
import com.hackacode.hotel.model.Empleado;
import com.hackacode.hotel.model.RolEmpleado;
import com.hackacode.hotel.repository.EmpleadoRepository;
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
class EmpleadoControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        empleadoRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/empleados - Crear empleado nuevo responde 201 Created")
    void testCrearEmpleadoHTTP() throws Exception {
        EmpleadoRequestDTO dto = new EmpleadoRequestDTO(
                "Lucia", "Fernandez", "Av. Santa Fe 1500", "29888777",
                LocalDate.of(1989, 6, 12), "Argentina", "1122334455", "lucia@agencia.com",
                "Administradora", new BigDecimal("950000.00"), "lfernandez",
                "Admin1234!", RolEmpleado.ADMIN
        );

        mockMvc.perform(post("/api/empleados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idEmpleado", notNullValue()))
                .andExpect(jsonPath("$.nombre", is("Lucia")))
                .andExpect(jsonPath("$.rol", is("ADMIN")))
                .andExpect(jsonPath("$.activo", is(true)))
                .andExpect(jsonPath("$.reactivado", is(false)));
    }

    @Test
    @DisplayName("POST /api/empleados - DNI repetido inactivo reactiva y responde 200 OK")
    void testReactivarEmpleadoPorDniHTTP() throws Exception {
        Empleado inactivo = new Empleado(
                null, "Lucia", "Fernandez", "Av. Santa Fe 1500", "29888777",
                LocalDate.of(1989, 6, 12), "Argentina", "1122334455", "lucia@agencia.com",
                "Vendedora", new BigDecimal("700000.00"), "lfernandez",
                "$sha256$hash", RolEmpleado.VENDEDOR, false
        );
        empleadoRepository.save(inactivo);

        EmpleadoRequestDTO dto = new EmpleadoRequestDTO(
                "Lucia", "Fernandez", "Av. Santa Fe 2000", "29888777",
                LocalDate.of(1989, 6, 12), "Argentina", "1122334455", "lucia@agencia.com",
                "Supervisora", new BigDecimal("900000.00"), "lfernandez",
                "NuevaClave123!", RolEmpleado.ADMIN
        );

        mockMvc.perform(post("/api/empleados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cargo", is("Supervisora")))
                .andExpect(jsonPath("$.activo", is(true)))
                .andExpect(jsonPath("$.reactivado", is(true)));
    }

    @Test
    @DisplayName("GET /api/empleados - Listado oculta sueldo, detalle lo expone solo a ADMIN")
    void testProteccionSalarialHTTP() throws Exception {
        Empleado emp = new Empleado(
                null, "Marcos", "Diaz", "Calle 10", "32111222",
                LocalDate.of(1993, 2, 18), "Argentina", "11998877", "marcos@agencia.com",
                "Vendedor Senior", new BigDecimal("780000.00"), "mdiaz",
                "$sha256$hash", RolEmpleado.VENDEDOR, true
        );
        Empleado guardado = empleadoRepository.save(emp);

        // 1. En el listado general, sueldo NO debe estar presente
        mockMvc.perform(get("/api/empleados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sueldo").doesNotExist());

        // 2. En el detalle como VENDEDOR, sueldo NO debe estar presente
        mockMvc.perform(get("/api/empleados/" + guardado.getIdEmpleado())
                .header("X-User-Role", "VENDEDOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sueldo").doesNotExist());

        // 3. En el detalle como ADMIN, sueldo SI debe estar presente
        mockMvc.perform(get("/api/empleados/" + guardado.getIdEmpleado())
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sueldo", is(780000.00)));
    }
}
