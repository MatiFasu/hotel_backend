package com.hackacode.hotel.service;

import com.hackacode.hotel.dto.ClienteDTO;
import java.util.List;

public interface ClienteService {
    ClienteDTO crearCliente(ClienteDTO clienteDTO);
    List<ClienteDTO> obtenerTodosLosClientes();
    ClienteDTO obtenerClientePorId(Long id);
    ClienteDTO obtenerClientePorDni(String dni);
    ClienteDTO actualizarCliente(Long id, ClienteDTO clienteDTO);
    void eliminarCliente(Long id);
}
