package com.hackacode.hotel.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClienteDTO {

    private Long idCliente;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    private String direccion;

    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    @JsonAlias({"fechaNac", "fechaNacimiento"})
    private LocalDate fechaNacimiento;

    private String nacionalidad;
    private String celular;

    @Email(message = "Debe proporcionar un email válido")
    private String email;

    private Boolean activo;
    private Boolean reactivado;

    public ClienteDTO() {
    }

    public ClienteDTO(Long idCliente, String nombre, String apellido, String direccion, String dni, LocalDate fechaNacimiento, String nacionalidad, String celular, String email) {
        this(idCliente, nombre, apellido, direccion, dni, fechaNacimiento, nacionalidad, celular, email, true, null);
    }

    public ClienteDTO(Long idCliente, String nombre, String apellido, String direccion, String dni, LocalDate fechaNacimiento, String nacionalidad, String celular, String email, Boolean activo, Boolean reactivado) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.apellido = apellido;
        this.direccion = direccion;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.nacionalidad = nacionalidad;
        this.celular = celular;
        this.email = email;
        this.activo = activo;
        this.reactivado = reactivado;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
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

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
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
