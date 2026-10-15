package com.hackacode.hotel.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDate;

@MappedSuperclass
public abstract class Persona {

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    @Column(name = "fecha_nac", nullable = false)
    private LocalDate fechaNac;

    @Column(nullable = false, length = 50)
    private String nacionalidad;

    @Column(nullable = false, length = 30)
    private String celular;

    @Column(nullable = false, length = 100)
    private String email;

    public Persona() {
    }

    public Persona(String nombre, String apellido, String direccion, String dni, LocalDate fechaNac, String nacionalidad, String celular, String email) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.direccion = direccion;
        this.dni = dni;
        this.fechaNac = fechaNac;
        this.nacionalidad = nacionalidad;
        this.celular = celular;
        this.email = email;
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
}
