package com.hackacode.hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackacode.hotel.AbstractIntegrationTest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReporteControllerTest extends AbstractIntegrationTest {

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

    @BeforeEach
    void setUp() {
        ventaRepository.deleteAll();
        paqueteRepository.deleteAll();
        servicioRepository.deleteAll();
        clienteRepository.deleteAll();
        empleadoRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/reportes/ganancias-diarias y efecto retroactivo de anulación (Decisión 9 y 23)")
    void testReporteDiarioYAnulacionRetroactivaHTTP() throws Exception {
        Cliente cliente = clienteRepository.save(new Cliente(
                null, "Elena", "Ríos", "Av. Belgrano 400", "36777888",
                LocalDate.of(1991, 3, 14), "Argentina", "1133445566", "elena@mail.com", true
        ));
        Empleado empleado = empleadoRepository.save(new Empleado(
                null, "Pablo", "Sánchez", "Calle 8", "30999888",
                LocalDate.of(1986, 12, 1), "Argentina", "1166778899", "pablo@agencia.com",
                "Vendedor", new BigDecimal("790000.00"), "psanchez", "$sha256$hash", RolEmpleado.VENDEDOR, true
        ));
        ServicioTuristico servicio = servicioRepository.save(new ServicioTuristico(
                null, "Excursión Glaciar Perito Moreno", "Pasarelas y navegación",
                "El Calafate", LocalDate.now().plusDays(15), new BigDecimal("100000.00"), TipoServicio.EXCURSION, true
        ));

        // 1. Emitir venta con TARJETA_CREDITO (9% comisión -> neto 91000.00)
        VentaRequestDTO ventaDto = new VentaRequestDTO(
                cliente.getIdCliente(), empleado.getIdEmpleado(), MedioPago.TARJETA_CREDITO, servicio.getCodigoServicio(), null
        );
        String resp = mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ventaDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long numVenta = objectMapper.readTree(resp).get("numVenta").asLong();

        // 2. Consultar reporte diario -> totalVentasActivas = 1, gananciaTotalNeta = 91000.00
        mockMvc.perform(get("/api/reportes/ganancias-diarias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVentasActivas", is(1)))
                .andExpect(jsonPath("$.montoTotalBruto", is(100000.00)))
                .andExpect(jsonPath("$.comisionesTotales", is(9000.00)))
                .andExpect(jsonPath("$.gananciaTotalNeta", is(91000.00)));

        // 3. Anular venta y verificar que el reporte diario descuenta retroactivamente la operación
        mockMvc.perform(patch("/api/ventas/" + numVenta + "/anular"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reportes/ganancias-diarias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVentasActivas", is(0)))
                .andExpect(jsonPath("$.gananciaTotalNeta", is(0.00)));
    }

    @Test
    @DisplayName("GET /api/reportes/ganancias-mensuales y POST /api/reportes/seed-demo")
    void testReporteMensualYSeedDemoHTTP() throws Exception {
        mockMvc.perform(post("/api/reportes/seed-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seeded", is(true)));

        mockMvc.perform(get("/api/reportes/ganancias-mensuales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVentasActivas", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.serieDiaria", not(empty())))
                .andExpect(jsonPath("$.desglosePorMedioPago", hasSize(5)));
    }
}
