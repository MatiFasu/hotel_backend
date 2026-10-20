package com.hackacode.hotel.repository;

import com.hackacode.hotel.model.EstadoVenta;
import com.hackacode.hotel.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findByEstado(EstadoVenta estado);

    long countByPaquete_CodigoPaquete(Long codigoPaquete);

    @Query("SELECT COUNT(v) > 0 FROM Venta v LEFT JOIN v.paquete p LEFT JOIN p.listaServicios ps WHERE v.servicio.codigoServicio = :codigoServicio OR ps.codigoServicio = :codigoServicio")
    boolean existsVentaAsociadaAServicio(@Param("codigoServicio") Long codigoServicio);
}
