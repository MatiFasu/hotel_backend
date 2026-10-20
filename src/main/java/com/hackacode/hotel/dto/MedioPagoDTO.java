package com.hackacode.hotel.dto;

import java.math.BigDecimal;

public class MedioPagoDTO {

    private String codigo;
    private String descripcion;
    private BigDecimal porcentajeComision;

    public MedioPagoDTO() {
    }

    public MedioPagoDTO(String codigo, String descripcion, BigDecimal porcentajeComision) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.porcentajeComision = porcentajeComision;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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
}
