package com.hackacode.hotel.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "clientes")
public class Cliente extends Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(nullable = false)
    private Boolean activo = true;

    public Cliente() {
        super();
    }

    public Cliente(Long idCliente, String nombre, String apellido, String direccion, String dni, LocalDate fechaNac, String nacionalidad, String celular, String email) {
        super(nombre, apellido, direccion, dni, fechaNac, nacionalidad, celular, email);
        this.idCliente = idCliente;
        this.activo = true;
    }

    public Cliente(Long idCliente, String nombre, String apellido, String direccion, String dni, LocalDate fechaNac, String nacionalidad, String celular, String email, Boolean activo) {
        super(nombre, apellido, direccion, dni, fechaNac, nacionalidad, celular, email);
        this.idCliente = idCliente;
        this.activo = activo != null ? activo : true;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    // Métodos de conveniencia para retrocompatibilidad
    public LocalDate getFechaNacimiento() {
        return getFechaNac();
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        setFechaNac(fechaNacimiento);
    }
}
