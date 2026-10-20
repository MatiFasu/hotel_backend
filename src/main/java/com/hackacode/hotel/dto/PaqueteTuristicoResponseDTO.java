package com.hackacode.hotel.dto;

import java.math.BigDecimal;
import java.util.List;

public class PaqueteTuristicoResponseDTO {

    private Long codigoPaquete;
    private String nombre;
    private BigDecimal costoPaquete;
    private Boolean activo;
    private Long ventasCount = 0L;
    private Boolean editable = true;
    private List<ServicioTuristicoResponseDTO> servicios;

    public PaqueteTuristicoResponseDTO() {
    }

    public PaqueteTuristicoResponseDTO(Long codigoPaquete, String nombre, BigDecimal costoPaquete, Boolean activo, List<ServicioTuristicoResponseDTO> servicios) {
        this(codigoPaquete, nombre, costoPaquete, activo, 0L, true, servicios);
    }

    public PaqueteTuristicoResponseDTO(
            Long codigoPaquete,
            String nombre,
            BigDecimal costoPaquete,
            Boolean activo,
            Long ventasCount,
            Boolean editable,
            List<ServicioTuristicoResponseDTO> servicios) {
        this.codigoPaquete = codigoPaquete;
        this.nombre = nombre;
        this.costoPaquete = costoPaquete;
        this.activo = activo;
        this.ventasCount = ventasCount != null ? ventasCount : 0L;
        this.editable = editable != null ? editable : true;
        this.servicios = servicios;
    }

    public Long getCodigoPaquete() {
        return codigoPaquete;
    }

    public void setCodigoPaquete(Long codigoPaquete) {
        this.codigoPaquete = codigoPaquete;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getCostoPaquete() {
        return costoPaquete;
    }

    public void setCostoPaquete(BigDecimal costoPaquete) {
        this.costoPaquete = costoPaquete;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Long getVentasCount() {
        return ventasCount;
    }

    public void setVentasCount(Long ventasCount) {
        this.ventasCount = ventasCount;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public List<ServicioTuristicoResponseDTO> getServicios() {
        return servicios;
    }

    public void setServicios(List<ServicioTuristicoResponseDTO> servicios) {
        this.servicios = servicios;
    }
}
