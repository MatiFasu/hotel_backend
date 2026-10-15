package com.hackacode.hotel.service.impl;

import com.hackacode.hotel.dto.ClienteDTO;
import com.hackacode.hotel.exception.DuplicateResourceException;
import com.hackacode.hotel.exception.ResourceNotFoundException;
import com.hackacode.hotel.model.Cliente;
import com.hackacode.hotel.repository.ClienteRepository;
import com.hackacode.hotel.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Autowired
    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public ClienteDTO crearCliente(ClienteDTO clienteDTO) {
        if (clienteRepository.existsByDni(clienteDTO.getDni())) {
            throw new DuplicateResourceException("Ya existe un cliente registrado con el DNI: " + clienteDTO.getDni());
        }

        Cliente cliente = mapToEntity(clienteDTO);
        Cliente clienteGuardado = clienteRepository.save(cliente);
        return mapToDTO(clienteGuardado);
    }

    @Override
    public List<ClienteDTO> obtenerTodosLosClientes() {
        return clienteRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ClienteDTO obtenerClientePorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
        return mapToDTO(cliente);
    }

    @Override
    public ClienteDTO obtenerClientePorDni(String dni) {
        Cliente cliente = clienteRepository.findByDni(dni)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con DNI: " + dni));
        return mapToDTO(cliente);
    }

    @Override
    public ClienteDTO actualizarCliente(Long id, ClienteDTO clienteDTO) {
        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));

        // Si cambia de DNI, verificar que el nuevo DNI no esté en uso por otro cliente
        if (!clienteExistente.getDni().equals(clienteDTO.getDni()) && clienteRepository.existsByDni(clienteDTO.getDni())) {
            throw new DuplicateResourceException("Ya existe otro cliente con el DNI: " + clienteDTO.getDni());
        }

        clienteExistente.setNombre(clienteDTO.getNombre());
        clienteExistente.setApellido(clienteDTO.getApellido());
        clienteExistente.setDireccion(clienteDTO.getDireccion());
        clienteExistente.setDni(clienteDTO.getDni());
        clienteExistente.setFechaNacimiento(clienteDTO.getFechaNacimiento());
        clienteExistente.setNacionalidad(clienteDTO.getNacionalidad());
        clienteExistente.setCelular(clienteDTO.getCelular());
        clienteExistente.setEmail(clienteDTO.getEmail());

        Cliente clienteActualizado = clienteRepository.save(clienteExistente);
        return mapToDTO(clienteActualizado);
    }

    @Override
    public void eliminarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
        clienteRepository.delete(cliente);
    }

    private ClienteDTO mapToDTO(Cliente cliente) {
        return new ClienteDTO(
                cliente.getIdCliente(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getDireccion(),
                cliente.getDni(),
                cliente.getFechaNacimiento(),
                cliente.getNacionalidad(),
                cliente.getCelular(),
                cliente.getEmail()
        );
    }

    private Cliente mapToEntity(ClienteDTO dto) {
        return new Cliente(
                dto.getIdCliente(),
                dto.getNombre(),
                dto.getApellido(),
                dto.getDireccion(),
                dto.getDni(),
                dto.getFechaNacimiento(),
                dto.getNacionalidad(),
                dto.getCelular(),
                dto.getEmail()
        );
    }
}
