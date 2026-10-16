package com.hackacode.hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackacode.hotel.AbstractIntegrationTest;
import com.hackacode.hotel.dto.ServicioTuristicoRequestDTO;
import com.hackacode.hotel.model.ServicioTuristico;
import com.hackacode.hotel.model.TipoServicio;
import com.hackacode.hotel.repository.ServicioTuristicoRepository;
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
class ServicioTuristicoControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServicioTuristicoRepository servicioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        servicioRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/servicios - Crear servicio con datos válidos")
    void testCrearServicioHTTP() throws Exception {
        ServicioTuristicoRequestDTO dto = new ServicioTuristicoRequestDTO(
                "Vuelo Buenos Aires - Bariloche",
                "Vuelo directo ida y vuelta con equipaje",
                "San Carlos de Bariloche",
                LocalDate.now().plusMonths(1),
                new BigDecimal("120000.00"),
                TipoServicio.PASAJE_AVION
        );

        mockMvc.perform(post("/api/servicios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoServicio", notNullValue()))
                .andExpect(jsonPath("$.nombre", is("Vuelo Buenos Aires - Bariloche")))
                .andExpect(jsonPath("$.tipoServicio", is("PASAJE_AVION")))
                .andExpect(jsonPath("$.costoServicio", is(120000.00)))
                .andExpect(jsonPath("$.activo", is(true)));
    }

    @Test
    @DisplayName("POST /api/servicios - Rechazar servicio con costo negativo o cero")
    void testCrearServicioCostoInvalidoHTTP() throws Exception {
        ServicioTuristicoRequestDTO dto = new ServicioTuristicoRequestDTO(
                "Excursión Cerro Catedral",
                "Paseo con guía",
                "Bariloche",
                LocalDate.now().plusDays(10),
                new BigDecimal("-50.00"),
                TipoServicio.EXCURSION
        );

        mockMvc.perform(post("/api/servicios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("costo")));
    }

    @Test
    @DisplayName("GET /api/servicios - Listar servicios activos")
    void testListarServiciosHTTP() throws Exception {
        ServicioTuristico servicio = new ServicioTuristico(
                null,
                "Alquiler de Auto SUV",
                "Vehículo 4x4",
                "Mendoza",
                LocalDate.now().plusWeeks(2),
                new BigDecimal("60000.00"),
                TipoServicio.ALQUILER_AUTO,
                true
        );
        servicioRepository.save(servicio);

        mockMvc.perform(get("/api/servicios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("Alquiler de Auto SUV")))
                .andExpect(jsonPath("$[0].tipoServicio", is("ALQUILER_AUTO")));
    }

    @Test
    @DisplayName("DELETE /api/servicios/{id} - Baja lógica exitosa")
    void testEliminarServicioHTTP() throws Exception {
        ServicioTuristico servicio = new ServicioTuristico(
                null,
                "Entrada Show Tango",
                "Cena show",
                "Buenos Aires",
                LocalDate.now().plusDays(5),
                new BigDecimal("30000.00"),
                TipoServicio.ENTRADA_EVENTO,
                true
        );
        ServicioTuristico guardado = servicioRepository.save(servicio);

        mockMvc.perform(delete("/api/servicios/" + guardado.getCodigoServicio()))
                .andExpect(status().isNoContent());

        // Verificar que quedó inactivo
        ServicioTuristico enDb = servicioRepository.findById(guardado.getCodigoServicio()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertFalse(enDb.getActivo());
    }

    @Test
    @DisplayName("PATCH /api/servicios/{id}/reactivar - Reactivar servicio inactivo")
    void testReactivarServicioHTTP() throws Exception {
        ServicioTuristico servicio = new ServicioTuristico(
                null,
                "Pasaje Tren a las Nubes",
                "Paseo turístico en tren",
                "Salta",
                LocalDate.now().plusMonths(2),
                new BigDecimal("85000.00"),
                TipoServicio.PASAJE_TREN,
                false
        );
        ServicioTuristico guardado = servicioRepository.save(servicio);

        mockMvc.perform(patch("/api/servicios/" + guardado.getCodigoServicio() + "/reactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo", is(true)));
    }
}
