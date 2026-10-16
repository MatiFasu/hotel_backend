package com.hackacode.hotel.dto;

import com.hackacode.hotel.model.TipoServicio;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ServicioTuristicoResponseDTO {

    private Long codigoServicio;
    private String nombre;
    private String descripcionBreve;
    private String destinoServicio;
    private LocalDate fechaServicio;
    private BigDecimal costoServicio;
    private TipoServicio tipoServicio;
    private Boolean activo;

    public ServicioTuristicoResponseDTO() {
    }

    public ServicioTuristicoResponseDTO(Long codigoServicio, String nombre, String descripcionBreve, String destinoServicio, LocalDate fechaServicio, BigDecimal costoServicio, TipoServicio tipoServicio, Boolean activo) {
        this.codigoServicio = codigoServicio;
        this.nombre = nombre;
        this.descripcionBreve = descripcionBreve;
        this.destinoServicio = destinoServicio;
        this.fechaServicio = fechaServicio;
        this.costoServicio = costoServicio;
        this.tipoServicio = tipoServicio;
        this.activo = activo;
    }

    public Long getCodigoServicio() {
        return codigoServicio;
    }

    public void setCodigoServicio(Long codigoServicio) {
        this.codigoServicio = codigoServicio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcionBreve() {
        return descripcionBreve;
    }

    public void setDescripcionBreve(String descripcionBreve) {
        this.descripcionBreve = descripcionBreve;
    }

    public String getDestinoServicio() {
        return destinoServicio;
    }

    public void setDestinoServicio(String destinoServicio) {
        this.destinoServicio = destinoServicio;
    }

    public LocalDate getFechaServicio() {
        return fechaServicio;
    }

    public void setFechaServicio(LocalDate fechaServicio) {
        this.fechaServicio = fechaServicio;
    }

    public BigDecimal getCostoServicio() {
        return costoServicio;
    }

    public void setCostoServicio(BigDecimal costoServicio) {
        this.costoServicio = costoServicio;
    }

    public TipoServicio getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(TipoServicio tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
