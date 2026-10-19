package com.hackacode.hotel.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "empleados")
public class Empleado extends Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado")
    private Long idEmpleado;

    @Column(nullable = false, length = 100)
    private String cargo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal sueldo;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolEmpleado rol = RolEmpleado.VENDEDOR;

    @Column(nullable = false)
    private Boolean activo = true;

    public Empleado() {
        super();
    }

    public Empleado(Long idEmpleado, String nombre, String apellido, String direccion, String dni,
                    LocalDate fechaNac, String nacionalidad, String celular, String email,
                    String cargo, BigDecimal sueldo, String username, String passwordHash,
                    RolEmpleado rol, Boolean activo) {
        super(nombre, apellido, direccion, dni, fechaNac, nacionalidad, celular, email);
        this.idEmpleado = idEmpleado;
        this.cargo = cargo;
        this.sueldo = sueldo;
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol != null ? rol : RolEmpleado.VENDEDOR;
        this.activo = activo != null ? activo : true;
    }

    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Long idEmpleado) {
        this.idEmpleado = idEmpleado;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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
}
