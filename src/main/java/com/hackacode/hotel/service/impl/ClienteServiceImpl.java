package com.hackacode.hotel.service.impl;

import com.hackacode.hotel.dto.ClienteDTO;
import com.hackacode.hotel.exception.BusinessException;
import com.hackacode.hotel.exception.DuplicateResourceException;
import com.hackacode.hotel.exception.ResourceNotFoundException;
import com.hackacode.hotel.model.Cliente;
import com.hackacode.hotel.repository.ClienteRepository;
import com.hackacode.hotel.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Autowired
    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    public ClienteDTO crearCliente(ClienteDTO clienteDTO) {
        Optional<Cliente> existenteOpt = clienteRepository.findByDni(clienteDTO.getDni());
        if (existenteOpt.isPresent()) {
            Cliente existente = existenteOpt.get();
            if (Boolean.TRUE.equals(existente.getActivo())) {
                throw new DuplicateResourceException("Ya existe un cliente activo registrado con el DNI: " + clienteDTO.getDni());
            }

            // Reactivación de cliente previamente dado de baja lógica (Decisión 7)
            existente.setNombre(clienteDTO.getNombre());
            existente.setApellido(clienteDTO.getApellido());
            existente.setDireccion(clienteDTO.getDireccion());
            existente.setFechaNacimiento(clienteDTO.getFechaNacimiento());
            existente.setNacionalidad(clienteDTO.getNacionalidad());
            existente.setCelular(clienteDTO.getCelular());
            existente.setEmail(clienteDTO.getEmail());
            existente.setActivo(true);

            Cliente reactivado = clienteRepository.save(existente);
            return mapToDTO(reactivado, true);
        }

        if (clienteRepository.existsByDni(clienteDTO.getDni())) {
            throw new DuplicateResourceException("Ya existe un cliente registrado con el DNI: " + clienteDTO.getDni());
        }

        Cliente cliente = mapToEntity(clienteDTO);
        cliente.setActivo(true);
        Cliente clienteGuardado = clienteRepository.save(cliente);
        return mapToDTO(clienteGuardado, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDTO> obtenerTodosLosClientes() {
        return listarClientes(null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDTO> listarClientes(Boolean activo, String q) {
        List<Cliente> lista;
        if (activo != null) {
            lista = clienteRepository.findByActivo(activo);
        } else {
            lista = clienteRepository.findAll();
        }

        if (q != null && !q.isBlank()) {
            String filtro = q.trim().toLowerCase(Locale.ROOT);
            lista = lista.stream()
                    .filter(c -> (c.getNombre() != null && c.getNombre().toLowerCase(Locale.ROOT).contains(filtro))
                            || (c.getApellido() != null && c.getApellido().toLowerCase(Locale.ROOT).contains(filtro))
                            || (c.getDni() != null && c.getDni().toLowerCase(Locale.ROOT).contains(filtro)))
                    .collect(Collectors.toList());
        }

        return lista.stream()
                .map(c -> mapToDTO(c, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteDTO obtenerClientePorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
        return mapToDTO(cliente, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteDTO obtenerClientePorDni(String dni) {
        Cliente cliente = clienteRepository.findByDni(dni)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con DNI: " + dni));
        return mapToDTO(cliente, null);
    }

    @Override
    @Transactional
    public ClienteDTO actualizarCliente(Long id, ClienteDTO clienteDTO) {
        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));

        if (!Boolean.TRUE.equals(clienteExistente.getActivo())) {
            throw new BusinessException("No se puede modificar un cliente inactivo. Debe reactivarlo previamente.");
        }

        // Si cambia de DNI, verificar que el nuevo DNI no esté en uso por otro cliente
        if (!clienteExistente.getDni().equals(clienteDTO.getDni())
                && (clienteRepository.findByDni(clienteDTO.getDni()).isPresent()
                    || clienteRepository.existsByDni(clienteDTO.getDni()))) {
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
        return mapToDTO(clienteActualizado, null);
    }

    @Override
    @Transactional
    public void eliminarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
        // Baja lógica (Decisión 1)
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public ClienteDTO reactivarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));

        if (Boolean.TRUE.equals(cliente.getActivo())) {
            throw new BusinessException("El cliente ya se encuentra activo");
        }

        cliente.setActivo(true);
        Cliente reactivado = clienteRepository.save(cliente);
        return mapToDTO(reactivado, true);
    }

    private ClienteDTO mapToDTO(Cliente cliente, Boolean reactivado) {
        return new ClienteDTO(
                cliente.getIdCliente(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getDireccion(),
                cliente.getDni(),
                cliente.getFechaNacimiento(),
                cliente.getNacionalidad(),
                cliente.getCelular(),
                cliente.getEmail(),
                cliente.getActivo(),
                reactivado
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
                dto.getEmail(),
                dto.getActivo() != null ? dto.getActivo() : true
        );
    }
}
