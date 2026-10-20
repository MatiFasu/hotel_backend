package com.hackacode.hotel.service;

import com.hackacode.hotel.dto.MedioPagoDTO;
import com.hackacode.hotel.dto.VentaRequestDTO;
import com.hackacode.hotel.dto.VentaResponseDTO;
import com.hackacode.hotel.model.EstadoVenta;

import java.util.List;

public interface VentaService {

    VentaResponseDTO registrarVenta(VentaRequestDTO requestDTO);

    List<VentaResponseDTO> listarVentas(EstadoVenta estado, Long clienteId, String tipoProducto);

    VentaResponseDTO obtenerVentaPorNumero(Long numVenta);

    VentaResponseDTO anularVenta(Long numVenta);

    List<MedioPagoDTO> listarMediosPago();
}
