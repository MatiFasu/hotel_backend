package com.hackacode.hotel.service;

import com.hackacode.hotel.dto.VentaRequestDTO;
import com.hackacode.hotel.dto.VentaResponseDTO;
import com.hackacode.hotel.exception.BusinessException;
import com.hackacode.hotel.model.*;
import com.hackacode.hotel.repository.ClienteRepository;
import com.hackacode.hotel.repository.EmpleadoRepository;
import com.hackacode.hotel.repository.PaqueteTuristicoRepository;
import com.hackacode.hotel.repository.ServicioTuristicoRepository;
import com.hackacode.hotel.repository.VentaRepository;
import com.hackacode.hotel.service.impl.VentaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private EmpleadoRepository empleadoRepository;
    @Mock
    private ServicioTuristicoRepository servicioRepository;
    @Mock
    private PaqueteTuristicoRepository paqueteRepository;

    private Clock fixedClock;
    private VentaServiceImpl ventaService;

    private Cliente clienteActivo;
    private Empleado empleadoActivo;
    private ServicioTuristico servicioVigente;
    private ServicioTuristico servicioDos;
    private PaqueteTuristico paqueteVigente;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-10-20T15:00:00Z"), ZoneId.of("America/Argentina/Buenos_Aires"));
        ventaService = new VentaServiceImpl(
                ventaRepository,
                clienteRepository,
                empleadoRepository,
                servicioRepository,
                paqueteRepository,
                fixedClock
        );

        clienteActivo = new Cliente(
                1L, "Martín", "Pérez", "Av. Corrientes 1234", "35123456",
                LocalDate.of(1990, 5, 15), "Argentina", "1123456789", "martin@mail.com", true
        );

        empleadoActivo = new Empleado(
                1L, "Laura", "Gómez", "Calle 50", "30111222",
                LocalDate.of(1988, 3, 10), "Argentina", "1199887766", "laura@agencia.com",
                "Ejecutiva de Ventas", new BigDecimal("850000.00"), "lgomez", "$sha256$hash", RolEmpleado.VENDEDOR, true
        );

        servicioVigente = new ServicioTuristico(
                10L, "Hotel Cataratas 5 Estrellas", "Suite con desayuno", "Puerto Iguazú",
                LocalDate.of(2026, 11, 15), new BigDecimal("100000.00"), TipoServicio.HOTEL, true
        );

        servicioDos = new ServicioTuristico(
                11L, "Vuelo Aeroparque - Iguazú", "Ida y vuelta", "Puerto Iguazú",
                LocalDate.of(2026, 11, 15), new BigDecimal("50000.00"), TipoServicio.PASAJE_AVION, true
        );

        paqueteVigente = new PaqueteTuristico(
                20L, "Escapada Iguazú Total", List.of(servicioVigente, servicioDos), true
        );
    }

    @Test
    @DisplayName("Registrar venta de servicio individual con TARJETA_CREDITO congela snapshot y 9% comisión")
    void testRegistrarVentaServicioExitoso() {
        VentaRequestDTO request = new VentaRequestDTO(1L, 1L, MedioPago.TARJETA_CREDITO, 10L, null);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleadoActivo));
        when(servicioRepository.findById(10L)).thenReturn(Optional.of(servicioVigente));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setNumVenta(100L);
            return v;
        });

        VentaResponseDTO res = ventaService.registrarVenta(request);

        assertNotNull(res);
        assertEquals(100L, res.getNumVenta());
        assertEquals(new BigDecimal("100000.00"), res.getMontoTotal());
        assertEquals(new BigDecimal("9.00"), res.getPorcentajeComision());
        assertEquals(new BigDecimal("9000.00"), res.getMontoComision());
        assertEquals(new BigDecimal("91000.00"), res.getGananciaNeta());
        assertEquals(EstadoVenta.ACTIVA, res.getEstado());
        assertEquals("SERVICIO", res.getTipoProducto());
    }

    @Test
    @DisplayName("Registrar venta de paquete turístico aplica 10% descuento y calcula comisión de TRANSFERENCIA (2.45%)")
    void testRegistrarVentaPaqueteExitoso() {
        VentaRequestDTO request = new VentaRequestDTO(1L, 1L, MedioPago.TRANSFERENCIA, null, 20L);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleadoActivo));
        when(paqueteRepository.findById(20L)).thenReturn(Optional.of(paqueteVigente));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setNumVenta(101L);
            return v;
        });

        VentaResponseDTO res = ventaService.registrarVenta(request);

        assertNotNull(res);
        // (100000 + 50000) * 0.90 = 135000.00
        assertEquals(new BigDecimal("135000.00"), res.getMontoTotal());
        // 135000.00 * 2.45% = 3307.50
        assertEquals(new BigDecimal("2.45"), res.getPorcentajeComision());
        assertEquals(new BigDecimal("3307.50"), res.getMontoComision());
        assertEquals(new BigDecimal("131692.50"), res.getGananciaNeta());
        assertEquals("PAQUETE", res.getTipoProducto());
    }

    @Test
    @DisplayName("Exclusión mutua XOR: enviar tanto servicioId como paqueteId lanza BusinessException (HTTP 422)")
    void testExclusionMutuaAmbosProductos() {
        VentaRequestDTO request = new VentaRequestDTO(1L, 1L, MedioPago.EFECTIVO, 10L, 20L);

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            ventaService.registrarVenta(request);
        });

        assertTrue(ex.getMessage().contains("Exclusión mutua"));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    @DisplayName("Exclusión mutua XOR: no enviar ni servicioId ni paqueteId lanza BusinessException (HTTP 422)")
    void testExclusionMutuaNingunProducto() {
        VentaRequestDTO request = new VentaRequestDTO(1L, 1L, MedioPago.EFECTIVO, null, null);

        assertThrows(BusinessException.class, () -> {
            ventaService.registrarVenta(request);
        });
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    @DisplayName("Rechazar venta de servicio con fecha vencida respecto a Clock (Decisión 12)")
    void testRechazarServicioVencido() {
        servicioVigente.setFechaServicio(LocalDate.of(2026, 10, 10)); // anterior a 2026-10-20
        VentaRequestDTO request = new VentaRequestDTO(1L, 1L, MedioPago.EFECTIVO, 10L, null);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleadoActivo));
        when(servicioRepository.findById(10L)).thenReturn(Optional.of(servicioVigente));

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            ventaService.registrarVenta(request);
        });

        assertTrue(ex.getMessage().contains("vencida"));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    @DisplayName("Anular venta activa pasa a ANULADA y registra fechaAnulacion; anular de nuevo lanza 422")
    void testAnularVentaYRechazarDobleAnulacion() {
        Venta venta = new Venta(
                50L,
                LocalDate.of(2026, 10, 20).atStartOfDay(),
                MedioPago.EFECTIVO,
                new BigDecimal("100000.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                EstadoVenta.ACTIVA,
                null,
                clienteActivo,
                empleadoActivo,
                servicioVigente,
                null
        );

        when(ventaRepository.findById(50L)).thenReturn(Optional.of(venta));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        VentaResponseDTO anulada = ventaService.anularVenta(50L);

        assertEquals(EstadoVenta.ANULADA, anulada.getEstado());
        assertNotNull(anulada.getFechaAnulacion());

        // Segundo intento sobre venta ya anulada -> BusinessException (422)
        assertThrows(BusinessException.class, () -> {
            ventaService.anularVenta(50L);
        });
    }
}
