package com.hackacode.hotel.repository;

import com.hackacode.hotel.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    Optional<Empleado> findByDni(String dni);
    boolean existsByDni(String dni);
    Optional<Empleado> findByUsername(String username);
    boolean existsByUsername(String username);
    List<Empleado> findByActivo(Boolean activo);
    List<Empleado> findByActivoTrue();
}
