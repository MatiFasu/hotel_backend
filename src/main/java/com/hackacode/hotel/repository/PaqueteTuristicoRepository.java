package com.hackacode.hotel.repository;

import com.hackacode.hotel.model.PaqueteTuristico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaqueteTuristicoRepository extends JpaRepository<PaqueteTuristico, Long> {

    List<PaqueteTuristico> findByActivoTrue();

    List<PaqueteTuristico> findByActivo(Boolean activo);

    @Query("SELECT COUNT(p) > 0 FROM PaqueteTuristico p JOIN p.listaServicios s WHERE p.activo = true AND s.codigoServicio = :codigoServicio")
    boolean existsPaqueteActivoConServicio(@Param("codigoServicio") Long codigoServicio);
}
