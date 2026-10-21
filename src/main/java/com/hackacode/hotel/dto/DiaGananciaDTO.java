package com.hackacode.hotel.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DiaGananciaDTO {

    private LocalDate fecha;
    private long cantidadVentas;
    private BigDecimal montoBruto;
    private BigDecimal comisionRetenida;
    private BigDecimal montoNeto;

    public DiaGananciaDTO() {
    }

    public DiaGananciaDTO(
            LocalDate fecha,
            long cantidadVentas,
            BigDecimal montoBruto,
            BigDecimal comisionRetenida,
            BigDecimal montoNeto) {
        this.fecha = fecha;
        this.cantidadVentas = cantidadVentas;
        this.montoBruto = montoBruto;
        this.comisionRetenida = comisionRetenida;
        this.montoNeto = montoNeto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public long getCantidadVentas() {
        return cantidadVentas;
    }

    public void setCantidadVentas(long cantidadVentas) {
        this.cantidadVentas = cantidadVentas;
    }

    public BigDecimal getMontoBruto() {
        return montoBruto;
    }

    public void setMontoBruto(BigDecimal montoBruto) {
        this.montoBruto = montoBruto;
    }

    public BigDecimal getComisionRetenida() {
        return comisionRetenida;
    }

    public void setComisionRetenida(BigDecimal comisionRetenida) {
        this.comisionRetenida = comisionRetenida;
    }

    public BigDecimal getMontoNeto() {
        return montoNeto;
    }

    public void setMontoNeto(BigDecimal montoNeto) {
        this.montoNeto = montoNeto;
    }
}
