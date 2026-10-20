package com.hackacode.hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackacode.hotel.AbstractIntegrationTest;
import com.hackacode.hotel.dto.PaqueteTuristicoRequestDTO;
import com.hackacode.hotel.dto.ServicioTuristicoRequestDTO;
import com.hackacode.hotel.dto.VentaRequestDTO;
import com.hackacode.hotel.model.*;
import com.hackacode.hotel.repository.*;
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
class VentaControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private PaqueteTuristicoRepository paqueteRepository;

    @Autowired
    private ServicioTuristicoRepository servicioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Cliente cliente;
    private Empleado empleado;
    private ServicioTuristico s1;
    private ServicioTuristico s2;
    private PaqueteTuristico paquete;

    @BeforeEach
    void setUp() {
        ventaRepository.deleteAll();
        paqueteRepository.deleteAll();
        servicioRepository.deleteAll();
        clienteRepository.deleteAll();
        empleadoRepository.deleteAll();

        cliente = clienteRepository.save(new Cliente(
                null, "Ana", "Martínez", "Av. Libertador 500", "38999111",
                LocalDate.of(1994, 7, 22), "Argentina", "1144556677", "ana@mail.com", true
        ));

        empleado = empleadoRepository.save(new Empleado(
                null, "Diego", "Torres", "Calle 12", "31222333",
                LocalDate.of(1987, 11, 5), "Argentina", "1177889900", "diego@agencia.com",
                "Vendedor", new BigDecimal("800000.00"), "dtorres", "$sha256$hash", RolEmpleado.VENDEDOR, true
        ));

        s1 = servicioRepository.save(new ServicioTuristico(
                null, "Hotel Mendoza Wine Lodge", "3 noches con degustación", "Mendoza",
                LocalDate.now().plusDays(20), new BigDecimal("100000.00"), TipoServicio.HOTEL, true
        ));

        s2 = servicioRepository.save(new ServicioTuristico(
                null, "Excursión Alta Montaña", "Full day con guía", "Mendoza",
                LocalDate.now().plusDays(21), new BigDecimal("50000.00"), TipoServicio.EXCURSION, true
        ));

        paquete = paqueteRepository.save(new PaqueteTuristico(
                null, "Experiencia Mendoza Completa", List.of(s1, s2), true
        ));
    }

    @Test
    @DisplayName("GET /api/medios-pago - Devuelve los 5 medios de pago canónicos con sus comisiones")
    void testListarMediosPagoHTTP() throws Exception {
        mockMvc.perform(get("/api/medios-pago"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].codigo", is("EFECTIVO")));
    }

    @Test
    @DisplayName("POST /api/ventas - Emitir venta de paquete con TARJETA_DEBITO (3% comisión) y luego anularla")
    void testRegistrarYAnularVentaPaqueteHTTP() throws Exception {
        VentaRequestDTO dto = new VentaRequestDTO(
                cliente.getIdCliente(),
                empleado.getIdEmpleado(),
                MedioPago.TARJETA_DEBITO,
                null,
                paquete.getCodigoPaquete()
        );

        String responseJson = mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numVenta", notNullValue()))
                // Paquete: (100000 + 50000) * 0.90 = 135000.00
                .andExpect(jsonPath("$.montoTotal", is(135000.00)))
                // Comisión 3% de 135000 = 4050.00
                .andExpect(jsonPath("$.porcentajeComision", is(3.00)))
                .andExpect(jsonPath("$.montoComision", is(4050.00)))
                .andExpect(jsonPath("$.gananciaNeta", is(130950.00)))
                .andExpect(jsonPath("$.estado", is("ACTIVA")))
                .andExpect(jsonPath("$.tipoProducto", is("PAQUETE")))
                .andReturn().getResponse().getContentAsString();

        Long numVenta = objectMapper.readTree(responseJson).get("numVenta").asLong();

        // Anular la venta (PATCH /api/ventas/{id}/anular)
        mockMvc.perform(patch("/api/ventas/" + numVenta + "/anular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("ANULADA")))
                .andExpect(jsonPath("$.fechaAnulacion", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/ventas - Rechazar con 422 si se viola la exclusión mutua XOR")
    void testRechazarViolacionExclusionMutuaHTTP() throws Exception {
        VentaRequestDTO dto = new VentaRequestDTO(
                cliente.getIdCliente(),
                empleado.getIdEmpleado(),
                MedioPago.EFECTIVO,
                s1.getCodigoServicio(),
                paquete.getCodigoPaquete()
        );

        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("Exclusión mutua")));
    }

    @Test
    @DisplayName("Decisión 5 y 14: Bloquear cambio de itinerario en servicio vendido y de servicios en paquete vendido")
    void testInmutabilidadDeProductosVendidosHTTP() throws Exception {
        // Emitir venta sobre el paquete (que incluye s1 y s2)
        VentaRequestDTO ventaDto = new VentaRequestDTO(
                cliente.getIdCliente(),
                empleado.getIdEmpleado(),
                MedioPago.EFECTIVO,
                null,
                paquete.getCodigoPaquete()
        );
        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ventaDto)))
                .andExpect(status().isCreated());

        // Intentar cambiar el destino de s1 (Decisión 14) -> 422
        ServicioTuristicoRequestDTO cambioItinerario = new ServicioTuristicoRequestDTO(
                s1.getNombre(),
                s1.getDescripcionBreve(),
                "Otro Destino Distinto",
                s1.getFechaServicio(),
                s1.getCostoServicio(),
                s1.getTipoServicio()
        );
        mockMvc.perform(put("/api/servicios/" + s1.getCodigoServicio())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cambioItinerario)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("itinerario")));

        // Intentar cambiar la lista de servicios del paquete vendido (Decisión 5) -> 422
        ServicioTuristico s3 = servicioRepository.save(new ServicioTuristico(
                null, "Alquiler Auto Mendoza", "Sedán", "Mendoza",
                LocalDate.now().plusDays(22), new BigDecimal("40000.00"), TipoServicio.ALQUILER_AUTO, true
        ));
        PaqueteTuristicoRequestDTO cambioServiciosPaquete = new PaqueteTuristicoRequestDTO(
                "Experiencia Mendoza Renombrada",
                List.of(s1.getCodigoServicio(), s3.getCodigoServicio())
        );
        mockMvc.perform(put("/api/paquetes/" + paquete.getCodigoPaquete())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cambioServiciosPaquete)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("ventas asociadas")));
    }
}
