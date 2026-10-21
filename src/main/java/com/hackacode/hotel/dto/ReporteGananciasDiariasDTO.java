package com.hackacode.hotel.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReporteGananciasDiariasDTO {

    private LocalDate fecha;
    private long totalVentasActivas;
    private BigDecimal montoTotalBruto;
    private BigDecimal comisionesTotales;
    private BigDecimal gananciaTotalNeta;
    private List<DesgloseMedioPagoDTO> desglosePorMedioPago;

    public ReporteGananciasDiariasDTO() {
    }

    public ReporteGananciasDiariasDTO(
            LocalDate fecha,
            long totalVentasActivas,
            BigDecimal montoTotalBruto,
            BigDecimal comisionesTotales,
            BigDecimal gananciaTotalNeta,
            List<DesgloseMedioPagoDTO> desglosePorMedioPago) {
        this.fecha = fecha;
        this.totalVentasActivas = totalVentasActivas;
        this.montoTotalBruto = montoTotalBruto;
        this.comisionesTotales = comisionesTotales;
        this.gananciaTotalNeta = gananciaTotalNeta;
        this.desglosePorMedioPago = desglosePorMedioPago;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public long getTotalVentasActivas() {
        return totalVentasActivas;
    }

    public void setTotalVentasActivas(long totalVentasActivas) {
        this.totalVentasActivas = totalVentasActivas;
    }

    public BigDecimal getMontoTotalBruto() {
        return montoTotalBruto;
    }

    public void setMontoTotalBruto(BigDecimal montoTotalBruto) {
        this.montoTotalBruto = montoTotalBruto;
    }

    public BigDecimal getComisionesTotales() {
        return comisionesTotales;
    }

    public void setComisionesTotales(BigDecimal comisionesTotales) {
        this.comisionesTotales = comisionesTotales;
    }

    public BigDecimal getGananciaTotalNeta() {
        return gananciaTotalNeta;
    }

    public void setGananciaTotalNeta(BigDecimal gananciaTotalNeta) {
        this.gananciaTotalNeta = gananciaTotalNeta;
    }

    public List<DesgloseMedioPagoDTO> getDesglosePorMedioPago() {
        return desglosePorMedioPago;
    }

    public void setDesglosePorMedioPago(List<DesgloseMedioPagoDTO> desglosePorMedioPago) {
        this.desglosePorMedioPago = desglosePorMedioPago;
    }
}
