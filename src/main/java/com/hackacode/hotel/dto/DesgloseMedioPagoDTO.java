package com.hackacode.hotel.dto;

import java.math.BigDecimal;

public class DesgloseMedioPagoDTO {

    private String medioPago;
    private String descripcion;
    private BigDecimal porcentajeComision;
    private long cantidadVentas;
    private BigDecimal montoBruto;
    private BigDecimal comisionRetenida;
    private BigDecimal gananciaNeta;

    public DesgloseMedioPagoDTO() {
    }

    public DesgloseMedioPagoDTO(
            String medioPago,
            String descripcion,
            BigDecimal porcentajeComision,
            long cantidadVentas,
            BigDecimal montoBruto,
            BigDecimal comisionRetenida,
            BigDecimal gananciaNeta) {
        this.medioPago = medioPago;
        this.descripcion = descripcion;
        this.porcentajeComision = porcentajeComision;
        this.cantidadVentas = cantidadVentas;
        this.montoBruto = montoBruto;
        this.comisionRetenida = comisionRetenida;
        this.gananciaNeta = gananciaNeta;
    }

    public String getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(String medioPago) {
        this.medioPago = medioPago;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPorcentajeComision() {
        return porcentajeComision;
    }

    public void setPorcentajeComision(BigDecimal porcentajeComision) {
        this.porcentajeComision = porcentajeComision;
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

    public BigDecimal getGananciaNeta() {
        return gananciaNeta;
    }

    public void setGananciaNeta(BigDecimal gananciaNeta) {
        this.gananciaNeta = gananciaNeta;
    }
}
