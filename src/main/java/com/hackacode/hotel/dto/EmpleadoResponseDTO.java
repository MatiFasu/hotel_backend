package com.hackacode.hotel.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hackacode.hotel.model.RolEmpleado;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmpleadoResponseDTO {

    private Long idEmpleado;
    private String nombre;
    private String apellido;
    private String direccion;
    private String dni;
    private LocalDate fechaNac;
    private String nacionalidad;
    private String celular;
    private String email;
    private String cargo;
    private BigDecimal sueldo;
    private String username;
    private RolEmpleado rol;
    private Boolean activo;
    private Boolean reactivado;

    public EmpleadoResponseDTO() {
    }

    public EmpleadoResponseDTO(Long idEmpleado, String nombre, String apellido, String direccion, String dni,
                               LocalDate fechaNac, String nacionalidad, String celular, String email,
                               String cargo, BigDecimal sueldo, String username, RolEmpleado rol,
                               Boolean activo, Boolean reactivado) {
        this.idEmpleado = idEmpleado;
        this.nombre = nombre;
        this.apellido = apellido;
        this.direccion = direccion;
        this.dni = dni;
        this.fechaNac = fechaNac;
        this.nacionalidad = nacionalidad;
        this.celular = celular;
        this.email = email;
        this.cargo = cargo;
        this.sueldo = sueldo;
        this.username = username;
        this.rol = rol;
        this.activo = activo;
        this.reactivado = reactivado;
    }

    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Long idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public LocalDate getFechaNac() {
        return fechaNac;
    }

    public void setFechaNac(LocalDate fechaNac) {
        this.fechaNac = fechaNac;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public BigDecimal getSueldo() {
        return sueldo;
    }

    public void setSueldo(BigDecimal sueldo) {
        this.sueldo = sueldo;
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Boolean getReactivado() {
        return reactivado;
    }

    public void setReactivado(Boolean reactivado) {
        this.reactivado = reactivado;
    }
}
