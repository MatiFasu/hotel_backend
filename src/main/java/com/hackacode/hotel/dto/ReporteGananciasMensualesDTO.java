package com.hackacode.hotel.dto;

import java.math.BigDecimal;
import java.util.List;

public class ReporteGananciasMensualesDTO {

    private int anio;
    private int mes;
    private long totalVentasActivas;
    private BigDecimal montoTotalBruto;
    private BigDecimal comisionesTotales;
    private BigDecimal gananciaTotalNeta;
    private List<DesgloseMedioPagoDTO> desglosePorMedioPago;
    private List<DiaGananciaDTO> serieDiaria;

    public ReporteGananciasMensualesDTO() {
    }

    public ReporteGananciasMensualesDTO(
            int anio,
            int mes,
            long totalVentasActivas,
            BigDecimal montoTotalBruto,
            BigDecimal comisionesTotales,
            BigDecimal gananciaTotalNeta,
            List<DesgloseMedioPagoDTO> desglosePorMedioPago,
            List<DiaGananciaDTO> serieDiaria) {
        this.anio = anio;
        this.mes = mes;
        this.totalVentasActivas = totalVentasActivas;
        this.montoTotalBruto = montoTotalBruto;
        this.comisionesTotales = comisionesTotales;
        this.gananciaTotalNeta = gananciaTotalNeta;
        this.desglosePorMedioPago = desglosePorMedioPago;
        this.serieDiaria = serieDiaria;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public int getMes() {
        return mes;
    }

    public void setMes(int mes) {
        this.mes = mes;
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

    public List<DiaGananciaDTO> getSerieDiaria() {
        return serieDiaria;
    }

    public void setSerieDiaria(List<DiaGananciaDTO> serieDiaria) {
        this.serieDiaria = serieDiaria;
    }
}
