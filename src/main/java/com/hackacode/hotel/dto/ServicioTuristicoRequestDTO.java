package com.hackacode.hotel.dto;

import com.hackacode.hotel.model.TipoServicio;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ServicioTuristicoRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcionBreve;

    @NotBlank(message = "El destino del servicio es obligatorio")
    private String destinoServicio;

    @NotNull(message = "La fecha del servicio es obligatoria")
    private LocalDate fechaServicio;

    @NotNull(message = "El costo del servicio es obligatorio")
    @DecimalMin(value = "0.01", message = "El costo del servicio debe ser mayor a cero")
    private BigDecimal costoServicio;

    @NotNull(message = "El tipo de servicio es obligatorio")
    private TipoServicio tipoServicio;

    public ServicioTuristicoRequestDTO() {
    }

    public ServicioTuristicoRequestDTO(String nombre, String descripcionBreve, String destinoServicio, LocalDate fechaServicio, BigDecimal costoServicio, TipoServicio tipoServicio) {
        this.nombre = nombre;
        this.descripcionBreve = descripcionBreve;
        this.destinoServicio = destinoServicio;
        this.fechaServicio = fechaServicio;
        this.costoServicio = costoServicio;
        this.tipoServicio = tipoServicio;
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
}
