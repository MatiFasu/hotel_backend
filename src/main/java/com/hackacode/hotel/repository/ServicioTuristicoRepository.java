package com.hackacode.hotel.repository;

import com.hackacode.hotel.model.ServicioTuristico;
import com.hackacode.hotel.model.TipoServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicioTuristicoRepository extends JpaRepository<ServicioTuristico, Long> {

    List<ServicioTuristico> findByActivoTrue();

    List<ServicioTuristico> findByActivo(Boolean activo);

    List<ServicioTuristico> findByTipoServicioAndActivoTrue(TipoServicio tipoServicio);
}
