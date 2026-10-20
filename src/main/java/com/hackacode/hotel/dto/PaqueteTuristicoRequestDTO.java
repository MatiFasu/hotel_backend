package com.hackacode.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class PaqueteTuristicoRequestDTO {

    @NotBlank(message = "El nombre del paquete es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre del paquete debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotEmpty(message = "Debe seleccionar los servicios turísticos para componer el paquete")
    private List<Long> servicioIds;

    public PaqueteTuristicoRequestDTO() {
    }

    public PaqueteTuristicoRequestDTO(String nombre, List<Long> servicioIds) {
        this.nombre = nombre;
        this.servicioIds = servicioIds;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Long> getServicioIds() {
        return servicioIds;
    }

    public void setServicioIds(List<Long> servicioIds) {
        this.servicioIds = servicioIds;
    }
}
