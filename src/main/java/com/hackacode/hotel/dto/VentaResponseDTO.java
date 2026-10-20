package com.hackacode.hotel.dto;

import com.hackacode.hotel.model.EstadoVenta;
import com.hackacode.hotel.model.MedioPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VentaResponseDTO {

    private Long numVenta;
    private LocalDateTime fechaVenta;
    private MedioPago medioPago;
    private BigDecimal montoTotal;
    private BigDecimal porcentajeComision;
    private BigDecimal montoComision;
    private BigDecimal gananciaNeta;
    private EstadoVenta estado;
    private LocalDateTime fechaAnulacion;

    private Long clienteId;
    private String clienteNombreCompleto;
    private String clienteDni;

    private Long empleadoId;
    private String empleadoNombreCompleto;

    private Long servicioId;
    private String servicioNombre;

    private Long paqueteId;
    private String paqueteNombre;

    private String tipoProducto;

    public VentaResponseDTO() {
    }

    public VentaResponseDTO(
            Long numVenta,
            LocalDateTime fechaVenta,
            MedioPago medioPago,
            BigDecimal montoTotal,
            BigDecimal porcentajeComision,
            BigDecimal montoComision,
            BigDecimal gananciaNeta,
            EstadoVenta estado,
            LocalDateTime fechaAnulacion,
            Long clienteId,
            String clienteNombreCompleto,
            String clienteDni,
            Long empleadoId,
            String empleadoNombreCompleto,
            Long servicioId,
            String servicioNombre,
            Long paqueteId,
            String paqueteNombre,
            String tipoProducto) {
        this.numVenta = numVenta;
        this.fechaVenta = fechaVenta;
        this.medioPago = medioPago;
        this.montoTotal = montoTotal;
        this.porcentajeComision = porcentajeComision;
        this.montoComision = montoComision;
        this.gananciaNeta = gananciaNeta;
        this.estado = estado;
        this.fechaAnulacion = fechaAnulacion;
        this.clienteId = clienteId;
        this.clienteNombreCompleto = clienteNombreCompleto;
        this.clienteDni = clienteDni;
        this.empleadoId = empleadoId;
        this.empleadoNombreCompleto = empleadoNombreCompleto;
        this.servicioId = servicioId;
        this.servicioNombre = servicioNombre;
        this.paqueteId = paqueteId;
        this.paqueteNombre = paqueteNombre;
        this.tipoProducto = tipoProducto;
    }

    public Long getNumVenta() {
        return numVenta;
    }

    public void setNumVenta(Long numVenta) {
        this.numVenta = numVenta;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public MedioPago getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(MedioPago medioPago) {
        this.medioPago = medioPago;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public BigDecimal getPorcentajeComision() {
        return porcentajeComision;
    }

    public void setPorcentajeComision(BigDecimal porcentajeComision) {
        this.porcentajeComision = porcentajeComision;
    }

    public BigDecimal getMontoComision() {
        return montoComision;
    }

    public void setMontoComision(BigDecimal montoComision) {
        this.montoComision = montoComision;
    }

    public BigDecimal getGananciaNeta() {
        return gananciaNeta;
    }

    public void setGananciaNeta(BigDecimal gananciaNeta) {
        this.gananciaNeta = gananciaNeta;
    }

    public EstadoVenta getEstado() {
        return estado;
    }

    public void setEstado(EstadoVenta estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaAnulacion() {
        return fechaAnulacion;
    }

    public void setFechaAnulacion(LocalDateTime fechaAnulacion) {
        this.fechaAnulacion = fechaAnulacion;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNombreCompleto() {
        return clienteNombreCompleto;
    }

    public void setClienteNombreCompleto(String clienteNombreCompleto) {
        this.clienteNombreCompleto = clienteNombreCompleto;
    }

    public String getClienteDni() {
        return clienteDni;
    }

    public void setClienteDni(String clienteDni) {
        this.clienteDni = clienteDni;
    }

    public Long getEmpleadoId() {
        return empleadoId;
    }

    public void setEmpleadoId(Long empleadoId) {
        this.empleadoId = empleadoId;
    }

    public String getEmpleadoNombreCompleto() {
        return empleadoNombreCompleto;
    }

    public void setEmpleadoNombreCompleto(String empleadoNombreCompleto) {
        this.empleadoNombreCompleto = empleadoNombreCompleto;
    }

    public Long getServicioId() {
        return servicioId;
    }

    public void setServicioId(Long servicioId) {
        this.servicioId = servicioId;
    }

    public String getServicioNombre() {
        return servicioNombre;
    }

    public void setServicioNombre(String servicioNombre) {
        this.servicioNombre = servicioNombre;
    }

    public Long getPaqueteId() {
        return paqueteId;
    }

    public void setPaqueteId(Long paqueteId) {
        this.paqueteId = paqueteId;
    }

    public String getPaqueteNombre() {
        return paqueteNombre;
    }

    public void setPaqueteNombre(String paqueteNombre) {
        this.paqueteNombre = paqueteNombre;
    }

    public String getTipoProducto() {
        return tipoProducto;
    }

    public void setTipoProducto(String tipoProducto) {
        this.tipoProducto = tipoProducto;
    }
}
