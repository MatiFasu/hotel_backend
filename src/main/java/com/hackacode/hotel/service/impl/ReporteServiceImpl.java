package com.hackacode.hotel.service.impl;

import com.hackacode.hotel.dto.DesgloseMedioPagoDTO;
import com.hackacode.hotel.dto.DiaGananciaDTO;
import com.hackacode.hotel.dto.ReporteGananciasDiariasDTO;
import com.hackacode.hotel.dto.ReporteGananciasMensualesDTO;
import com.hackacode.hotel.exception.BusinessException;
import com.hackacode.hotel.model.EstadoVenta;
import com.hackacode.hotel.model.MedioPago;
import com.hackacode.hotel.model.Venta;
import com.hackacode.hotel.repository.VentaRepository;
import com.hackacode.hotel.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReporteServiceImpl implements ReporteService {

    private final VentaRepository ventaRepository;
    private final Clock clock;

    @Autowired
    public ReporteServiceImpl(VentaRepository ventaRepository, Clock clock) {
        this.ventaRepository = ventaRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteGananciasDiariasDTO obtenerGananciasDiarias(LocalDate fecha) {
        LocalDate fechaObjetivo = (fecha != null) ? fecha : LocalDate.now(clock);
        LocalDateTime inicio = fechaObjetivo.atStartOfDay();
        LocalDateTime fin = fechaObjetivo.atTime(LocalTime.MAX);

        List<Venta> ventasActivas = ventaRepository.findByEstadoAndFechaVentaBetween(EstadoVenta.ACTIVA, inicio, fin);

        BigDecimal montoTotalBruto = sumarBruto(ventasActivas);
        BigDecimal comisionesTotales = sumarComisiones(ventasActivas);
        BigDecimal gananciaTotalNeta = montoTotalBruto.subtract(comisionesTotales).setScale(2, RoundingMode.HALF_UP);
        List<DesgloseMedioPagoDTO> desglose = calcularDesglosePorMedioPago(ventasActivas);

        return new ReporteGananciasDiariasDTO(
                fechaObjetivo,
                ventasActivas.size(),
                montoTotalBruto,
                comisionesTotales,
                gananciaTotalNeta,
                desglose
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteGananciasMensualesDTO obtenerGananciasMensuales(Integer anio, Integer mes) {
        LocalDate hoy = LocalDate.now(clock);
        int anioObjetivo = (anio != null) ? anio : hoy.getYear();
        int mesObjetivo = (mes != null) ? mes : hoy.getMonthValue();

        if (mesObjetivo < 1 || mesObjetivo > 12) {
            throw new BusinessException("El mes debe estar comprendido entre 1 y 12.");
        }
        if (anioObjetivo < 2000 || anioObjetivo > 2100) {
            throw new BusinessException("El año debe estar comprendido entre 2000 y 2100.");
        }

        YearMonth yearMonth = YearMonth.of(anioObjetivo, mesObjetivo);
        LocalDateTime inicioMes = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime finMes = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);

        List<Venta> ventasActivas = ventaRepository.findByEstadoAndFechaVentaBetween(EstadoVenta.ACTIVA, inicioMes, finMes);

        BigDecimal montoTotalBruto = sumarBruto(ventasActivas);
        BigDecimal comisionesTotales = sumarComisiones(ventasActivas);
        BigDecimal gananciaTotalNeta = montoTotalBruto.subtract(comisionesTotales).setScale(2, RoundingMode.HALF_UP);
        List<DesgloseMedioPagoDTO> desglose = calcularDesglosePorMedioPago(ventasActivas);

        // Agrupar ventas activas por fecha (LocalDate)
        Map<LocalDate, List<Venta>> porDia = ventasActivas.stream()
                .collect(Collectors.groupingBy(v -> v.getFechaVenta().toLocalDate()));

        // Decisión 23: Serie diaria continua del día 1 al último día del mes calendario
        List<DiaGananciaDTO> serieDiaria = new ArrayList<>();
        int diasDelMes = yearMonth.lengthOfMonth();
        for (int dia = 1; dia <= diasDelMes; dia++) {
            LocalDate fechaDia = yearMonth.atDay(dia);
            List<Venta> ventasDia = porDia.getOrDefault(fechaDia, List.of());

            BigDecimal brutoDia = sumarBruto(ventasDia);
            BigDecimal comisionDia = sumarComisiones(ventasDia);
            BigDecimal netoDia = brutoDia.subtract(comisionDia).setScale(2, RoundingMode.HALF_UP);

            serieDiaria.add(new DiaGananciaDTO(
                    fechaDia,
                    ventasDia.size(),
                    brutoDia,
                    comisionDia,
                    netoDia
            ));
        }

        return new ReporteGananciasMensualesDTO(
                anioObjetivo,
                mesObjetivo,
                ventasActivas.size(),
                montoTotalBruto,
                comisionesTotales,
                gananciaTotalNeta,
                desglose,
                serieDiaria
        );
    }

    private List<DesgloseMedioPagoDTO> calcularDesglosePorMedioPago(List<Venta> ventasActivas) {
        Map<MedioPago, List<Venta>> agrupado = ventasActivas.stream()
                .collect(Collectors.groupingBy(Venta::getMedioPago));

        List<DesgloseMedioPagoDTO> resultado = new ArrayList<>();
        for (MedioPago mp : MedioPago.values()) {
            List<Venta> ventasMedio = agrupado.getOrDefault(mp, List.of());
            BigDecimal bruto = sumarBruto(ventasMedio);
            BigDecimal comision = sumarComisiones(ventasMedio);
            BigDecimal neto = bruto.subtract(comision).setScale(2, RoundingMode.HALF_UP);

            resultado.add(new DesgloseMedioPagoDTO(
                    mp.name(),
                    mp.getDescripcion(),
                    mp.getPorcentajeComision(),
                    ventasMedio.size(),
                    bruto,
                    comision,
                    neto
            ));
        }
        return resultado;
    }

    private BigDecimal sumarBruto(List<Venta> ventas) {
        return ventas.stream()
                .map(Venta::getMontoTotal)
                .filter(m -> m != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumarComisiones(List<Venta> ventas) {
        return ventas.stream()
                .map(Venta::getMontoComision)
                .filter(m -> m != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
