package com.hackacode.hotel.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum MedioPago {

    EFECTIVO("Efectivo", new BigDecimal("0.00")),
    MONEDERO_VIRTUAL("Monedero Virtual", new BigDecimal("0.00")),
    TARJETA_DEBITO("Tarjeta de Débito", new BigDecimal("3.00")),
    TARJETA_CREDITO("Tarjeta de Crédito", new BigDecimal("9.00")),
    TRANSFERENCIA("Transferencia Bancaria", new BigDecimal("2.45"));

    private final String descripcion;
    private final BigDecimal porcentajeComision;

    MedioPago(String descripcion, BigDecimal porcentajeComision) {
        this.descripcion = descripcion;
        this.porcentajeComision = porcentajeComision;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getPorcentajeComision() {
        return porcentajeComision;
    }

    public BigDecimal calcularComision(BigDecimal montoTotal) {
        if (montoTotal == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return montoTotal.multiply(porcentajeComision)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}
