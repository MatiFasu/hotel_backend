package com.hackacode.hotel.dto;

import com.hackacode.hotel.model.RolEmpleado;

public class LoginResponseDTO {

    private String token;
    private String tipo;
    private Long empleadoId;
    private String nombreCompleto;
    private String username;
    private RolEmpleado rol;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String token, String tipo, Long empleadoId, String nombreCompleto, String username, RolEmpleado rol) {
        this.token = token;
        this.tipo = tipo;
        this.empleadoId = empleadoId;
        this.nombreCompleto = nombreCompleto;
        this.username = username;
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getEmpleadoId() {
        return empleadoId;
    }

    public void setEmpleadoId(Long empleadoId) {
        this.empleadoId = empleadoId;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public RolEmpleado getRol() {
        return rol;
    }

    public void setRol(RolEmpleado rol) {
        this.rol = rol;
    }
}
