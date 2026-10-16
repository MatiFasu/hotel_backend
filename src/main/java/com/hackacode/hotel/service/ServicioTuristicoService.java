package com.hackacode.hotel.service;

import com.hackacode.hotel.dto.ServicioTuristicoRequestDTO;
import com.hackacode.hotel.dto.ServicioTuristicoResponseDTO;
import com.hackacode.hotel.model.TipoServicio;

import java.util.List;

public interface ServicioTuristicoService {

    ServicioTuristicoResponseDTO crearServicio(ServicioTuristicoRequestDTO requestDTO);

    List<ServicioTuristicoResponseDTO> listarServicios(Boolean activo, TipoServicio tipoServicio);

    ServicioTuristicoResponseDTO obtenerPorCodigo(Long codigo);

    ServicioTuristicoResponseDTO actualizarServicio(Long codigo, ServicioTuristicoRequestDTO requestDTO);

    void eliminarServicio(Long codigo);

    ServicioTuristicoResponseDTO reactivarServicio(Long codigo);
}
