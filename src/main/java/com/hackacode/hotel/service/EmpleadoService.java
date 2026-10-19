package com.hackacode.hotel.service;

import com.hackacode.hotel.dto.EmpleadoRequestDTO;
import com.hackacode.hotel.dto.EmpleadoResponseDTO;

import java.util.List;

public interface EmpleadoService {
    EmpleadoResponseDTO crearEmpleado(EmpleadoRequestDTO requestDTO);
    List<EmpleadoResponseDTO> listarEmpleados(Boolean activo, String q);
    EmpleadoResponseDTO obtenerPorId(Long id, boolean incluirSueldo);
    EmpleadoResponseDTO actualizarEmpleado(Long id, EmpleadoRequestDTO requestDTO);
    void eliminarEmpleado(Long id);
    EmpleadoResponseDTO reactivarEmpleado(Long id);
}
