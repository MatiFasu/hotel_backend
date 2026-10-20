package com.hackacode.hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackacode.hotel.AbstractIntegrationTest;
import com.hackacode.hotel.dto.PaqueteTuristicoRequestDTO;
import com.hackacode.hotel.model.PaqueteTuristico;
import com.hackacode.hotel.model.ServicioTuristico;
import com.hackacode.hotel.model.TipoServicio;
import com.hackacode.hotel.repository.PaqueteTuristicoRepository;
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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaqueteTuristicoControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaqueteTuristicoRepository paqueteRepository;

    @Autowired
    private ServicioTuristicoRepository servicioRepository;

    @Autowired
    private com.hackacode.hotel.repository.VentaRepository ventaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ServicioTuristico s1;
    private ServicioTuristico s2;

    @BeforeEach
    void setUp() {
        ventaRepository.deleteAll();
        paqueteRepository.deleteAll();
        servicioRepository.deleteAll();

        s1 = servicioRepository.save(new ServicioTuristico(
                null,
                "Hotel Llao Llao Suite",
                "Alojamiento 3 noches",
                "Bariloche",
                LocalDate.now().plusDays(15),
                new BigDecimal("100000.00"),
                TipoServicio.HOTEL,
                true
        ));

        s2 = servicioRepository.save(new ServicioTuristico(
                null,
                "Excursión Circuito Chico",
                "Paseo guiado",
                "Bariloche",
                LocalDate.now().plusDays(16),
                new BigDecimal("50000.00"),
                TipoServicio.EXCURSION,
                true
        ));
    }

    @Test
    @DisplayName("POST /api/paquetes - Crear paquete turístico válido calcula 10% de descuento")
    void testCrearPaqueteHTTP() throws Exception {
        PaqueteTuristicoRequestDTO dto = new PaqueteTuristicoRequestDTO(
                "Pack Bariloche Premium",
                List.of(s1.getCodigoServicio(), s2.getCodigoServicio())
        );

        mockMvc.perform(post("/api/paquetes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoPaquete", notNullValue()))
                .andExpect(jsonPath("$.nombre", is("Pack Bariloche Premium")))
                // (100000 + 50000) * 0.90 = 135000.00
                .andExpect(jsonPath("$.costoPaquete", is(135000.00)))
                .andExpect(jsonPath("$.activo", is(true)))
                .andExpect(jsonPath("$.servicios", hasSize(2)));
    }

    @Test
    @DisplayName("POST /api/paquetes - Rechazar con 422 si se envían servicios duplicados")
    void testCrearPaqueteDuplicadosHTTP() throws Exception {
        PaqueteTuristicoRequestDTO dto = new PaqueteTuristicoRequestDTO(
                "Pack Duplicado",
                List.of(s1.getCodigoServicio(), s1.getCodigoServicio())
        );

        mockMvc.perform(post("/api/paquetes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("duplicados")));
    }

    @Test
    @DisplayName("DELETE /api/servicios/{id} - Bloquear con 422 baja de servicio en paquete activo (Decisión 6)")
    void testBloquearBajaServicioEnPaqueteActivoHTTP() throws Exception {
        paqueteRepository.save(new PaqueteTuristico(
                null,
                "Pack Activo Bariloche",
                List.of(s1, s2),
                true
        ));

        mockMvc.perform(delete("/api/servicios/" + s1.getCodigoServicio()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("paquetes turísticos activos")));
    }

    @Test
    @DisplayName("DELETE /api/paquetes/{id} y PATCH /api/paquetes/{id}/reactivar - Baja lógica y reactivación")
    void testBajaYReactivacionPaqueteHTTP() throws Exception {
        PaqueteTuristico guardado = paqueteRepository.save(new PaqueteTuristico(
                null,
                "Pack Nieve",
                List.of(s1, s2),
                true
        ));

        mockMvc.perform(delete("/api/paquetes/" + guardado.getCodigoPaquete()))
                .andExpect(status().isNoContent());

        mockMvc.perform(patch("/api/paquetes/" + guardado.getCodigoPaquete() + "/reactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo", is(true)));
    }
}
