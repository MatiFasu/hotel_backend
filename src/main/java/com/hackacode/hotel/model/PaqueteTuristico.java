package com.hackacode.hotel.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "paquetes_turisticos")
public class PaqueteTuristico {

    private static final BigDecimal FACTOR_DESCUENTO = new BigDecimal("0.90");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_paquete")
    private Long codigoPaquete;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Boolean activo = true;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "paquete_servicio",
            joinColumns = @JoinColumn(name = "codigo_paquete"),
            inverseJoinColumns = @JoinColumn(name = "codigo_servicio")
    )
    private List<ServicioTuristico> listaServicios = new ArrayList<>();

    public PaqueteTuristico() {
    }

    public PaqueteTuristico(Long codigoPaquete, String nombre, List<ServicioTuristico> listaServicios, Boolean activo) {
        this.codigoPaquete = codigoPaquete;
        this.nombre = nombre;
        this.listaServicios = listaServicios != null ? new ArrayList<>(listaServicios) : new ArrayList<>();
        this.activo = activo != null ? activo : true;
        this.version = 0L;
    }

    /**
     * Calcula dinámicamente en memoria el costo del paquete sumando el costo de sus
     * servicios componentes y aplicando un 10% de descuento con redondeo HALF_UP (Decisión 3).
     */
    public BigDecimal calcularCostoPaquete() {
        if (listaServicios == null || listaServicios.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal suma = listaServicios.stream()
                .map(ServicioTuristico::getCostoServicio)
                .filter(c -> c != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return suma.multiply(FACTOR_DESCUENTO).setScale(2, RoundingMode.HALF_UP);
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public List<ServicioTuristico> getListaServicios() {
        return listaServicios;
    }

    public void setListaServicios(List<ServicioTuristico> listaServicios) {
        this.listaServicios = listaServicios != null ? new ArrayList<>(listaServicios) : new ArrayList<>();
    }
}
