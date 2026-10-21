package com.hackacode.hotel.service;

import com.hackacode.hotel.dto.ReporteGananciasDiariasDTO;
import com.hackacode.hotel.dto.ReporteGananciasMensualesDTO;
import com.hackacode.hotel.exception.BusinessException;
import com.hackacode.hotel.model.EstadoVenta;
import com.hackacode.hotel.model.MedioPago;
import com.hackacode.hotel.model.Venta;
import com.hackacode.hotel.repository.VentaRepository;
import com.hackacode.hotel.service.impl.ReporteServiceImpl;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    private Clock fixedClock;
    private ReporteServiceImpl reporteService;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-10-20T15:00:00Z"), ZoneId.of("America/Argentina/Buenos_Aires"));
        reporteService = new ReporteServiceImpl(ventaRepository, fixedClock);
    }

    @Test
    @DisplayName("Ganancias diarias consolida ventas activas, comisiones y ganancia neta por medio de pago")
    void testObtenerGananciasDiarias() {
        Venta v1 = new Venta(
                1L, LocalDateTime.of(2026, 10, 20, 10, 0), MedioPago.TARJETA_CREDITO,
                new BigDecimal("100000.00"), new BigDecimal("9.00"), new BigDecimal("9000.00"),
                EstadoVenta.ACTIVA, null, null, null, null, null
        );
        Venta v2 = new Venta(
                2L, LocalDateTime.of(2026, 10, 20, 12, 30), MedioPago.EFECTIVO,
                new BigDecimal("50000.00"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                EstadoVenta.ACTIVA, null, null, null, null, null
        );

        when(ventaRepository.findByEstadoAndFechaVentaBetween(eq(EstadoVenta.ACTIVA), any(), any()))
                .thenReturn(List.of(v1, v2));

        ReporteGananciasDiariasDTO reporte = reporteService.obtenerGananciasDiarias(LocalDate.of(2026, 10, 20));

        assertEquals(2L, reporte.getTotalVentasActivas());
        assertEquals(new BigDecimal("150000.00"), reporte.getMontoTotalBruto());
        assertEquals(new BigDecimal("9000.00"), reporte.getComisionesTotales());
        assertEquals(new BigDecimal("141000.00"), reporte.getGananciaTotalNeta());
        assertEquals(5, reporte.getDesglosePorMedioPago().size());
    }

    @Test
    @DisplayName("Ganancias mensuales genera serie diaria continua para los 31 días de Octubre (Decisión 23)")
    void testObtenerGananciasMensualesSerieContinua() {
        Venta v1 = new Venta(
                1L, LocalDateTime.of(2026, 10, 5, 14, 0), MedioPago.TARJETA_DEBITO,
                new BigDecimal("200000.00"), new BigDecimal("3.00"), new BigDecimal("6000.00"),
                EstadoVenta.ACTIVA, null, null, null, null, null
        );

        when(ventaRepository.findByEstadoAndFechaVentaBetween(eq(EstadoVenta.ACTIVA), any(), any()))
                .thenReturn(List.of(v1));

        ReporteGananciasMensualesDTO reporte = reporteService.obtenerGananciasMensuales(2026, 10);

        assertEquals(2026, reporte.getAnio());
        assertEquals(10, reporte.getMes());
        assertEquals(1L, reporte.getTotalVentasActivas());
        assertEquals(new BigDecimal("200000.00"), reporte.getMontoTotalBruto());
        assertEquals(new BigDecimal("6000.00"), reporte.getComisionesTotales());
        assertEquals(new BigDecimal("194000.00"), reporte.getGananciaTotalNeta());
        // Octubre tiene 31 días continuos
        assertEquals(31, reporte.getSerieDiaria().size());
        // Día 5 tiene la venta
        assertEquals(new BigDecimal("194000.00"), reporte.getSerieDiaria().get(4).getMontoNeto());
        // Día 1 no tiene ventas -> 0.00
        assertEquals(new BigDecimal("0.00"), reporte.getSerieDiaria().get(0).getMontoNeto());
    }

    @Test
    @DisplayName("Mes fuera de rango (1..12) lanza BusinessException (HTTP 422)")
    void testMesInvalidoLanzaBusinessException() {
        assertThrows(BusinessException.class, () -> {
            reporteService.obtenerGananciasMensuales(2026, 13);
        });
    }
}
