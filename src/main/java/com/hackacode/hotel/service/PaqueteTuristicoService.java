package com.hackacode.hotel.service;

import com.hackacode.hotel.dto.PaqueteTuristicoRequestDTO;
import com.hackacode.hotel.dto.PaqueteTuristicoResponseDTO;

import java.util.List;

public interface PaqueteTuristicoService {

    PaqueteTuristicoResponseDTO crearPaquete(PaqueteTuristicoRequestDTO requestDTO);

    List<PaqueteTuristicoResponseDTO> listarPaquetes(Boolean activo);

    PaqueteTuristicoResponseDTO obtenerPorCodigo(Long codigo);

    PaqueteTuristicoResponseDTO actualizarPaquete(Long codigo, PaqueteTuristicoRequestDTO requestDTO);

    void eliminarPaquete(Long codigo);

    PaqueteTuristicoResponseDTO reactivarPaquete(Long codigo);
}
