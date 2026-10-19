package com.hackacode.hotel.service.impl;

import com.hackacode.hotel.dto.EmpleadoRequestDTO;
import com.hackacode.hotel.dto.EmpleadoResponseDTO;
import com.hackacode.hotel.exception.BusinessException;
import com.hackacode.hotel.exception.DuplicateResourceException;
import com.hackacode.hotel.exception.ResourceNotFoundException;
import com.hackacode.hotel.model.Empleado;
import com.hackacode.hotel.model.RolEmpleado;
import com.hackacode.hotel.repository.EmpleadoRepository;
import com.hackacode.hotel.service.EmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;

    @Autowired
    public EmpleadoServiceImpl(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    @Override
    @Transactional
    public EmpleadoResponseDTO crearEmpleado(EmpleadoRequestDTO requestDTO) {
        if (requestDTO.getSueldo() == null || requestDTO.getSueldo().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El sueldo del empleado debe ser mayor a cero");
        }

        Optional<Empleado> existenteOpt = empleadoRepository.findByDni(requestDTO.getDni());
        if (existenteOpt.isPresent()) {
            Empleado existente = existenteOpt.get();
            if (Boolean.TRUE.equals(existente.getActivo())) {
                throw new DuplicateResourceException("Ya existe un empleado activo registrado con el DNI: " + requestDTO.getDni());
            }

            // Validar colisión de username con otro registro distinto
            Optional<Empleado> porUsername = empleadoRepository.findByUsername(requestDTO.getUsername());
            if (porUsername.isPresent() && !porUsername.get().getIdEmpleado().equals(existente.getIdEmpleado())) {
                throw new DuplicateResourceException("Ya existe otro empleado con el username: " + requestDTO.getUsername());
            }

            // Reactivación de empleado inactivo con actualización de datos
            actualizarCamposEntidad(existente, requestDTO);
            if (requestDTO.getPassword() != null && !requestDTO.getPassword().isBlank()) {
                existente.setPasswordHash(hashPassword(requestDTO.getPassword()));
            }
            existente.setActivo(true);
            Empleado reactivado = empleadoRepository.save(existente);
            return mapToDTO(reactivado, true, true);
        }

        if (empleadoRepository.existsByDni(requestDTO.getDni())) {
            throw new DuplicateResourceException("Ya existe un empleado registrado con el DNI: " + requestDTO.getDni());
        }

        if (empleadoRepository.findByUsername(requestDTO.getUsername()).isPresent()
                || empleadoRepository.existsByUsername(requestDTO.getUsername())) {
            throw new DuplicateResourceException("Ya existe un empleado registrado con el username: " + requestDTO.getUsername());
        }

        if (requestDTO.getPassword() == null || requestDTO.getPassword().isBlank()) {
            throw new BusinessException("La contraseña es obligatoria para dar de alta un nuevo empleado");
        }

        Empleado nuevo = new Empleado(
                null,
                requestDTO.getNombre(),
                requestDTO.getApellido(),
                requestDTO.getDireccion(),
                requestDTO.getDni(),
                requestDTO.getFechaNac(),
                requestDTO.getNacionalidad(),
                requestDTO.getCelular(),
                requestDTO.getEmail(),
                requestDTO.getCargo(),
                requestDTO.getSueldo(),
                requestDTO.getUsername(),
                hashPassword(requestDTO.getPassword()),
                requestDTO.getRol() != null ? requestDTO.getRol() : RolEmpleado.VENDEDOR,
                true
        );

        Empleado guardado = empleadoRepository.save(nuevo);
        return mapToDTO(guardado, true, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoResponseDTO> listarEmpleados(Boolean activo, String q) {
        List<Empleado> lista;
        if (activo != null) {
            lista = empleadoRepository.findByActivo(activo);
        } else {
            lista = empleadoRepository.findAll();
        }

        if (q != null && !q.isBlank()) {
            String filtro = q.trim().toLowerCase(Locale.ROOT);
            lista = lista.stream()
                    .filter(e -> (e.getNombre() != null && e.getNombre().toLowerCase(Locale.ROOT).contains(filtro))
                            || (e.getApellido() != null && e.getApellido().toLowerCase(Locale.ROOT).contains(filtro))
                            || (e.getDni() != null && e.getDni().toLowerCase(Locale.ROOT).contains(filtro))
                            || (e.getCargo() != null && e.getCargo().toLowerCase(Locale.ROOT).contains(filtro)))
                    .collect(Collectors.toList());
        }

        // Protección salarial (Decisión 17): en listados públicos el sueldo siempre se oculta (null)
        return lista.stream()
                .map(e -> mapToDTO(e, false, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoResponseDTO obtenerPorId(Long id, boolean incluirSueldo) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con ID: " + id));
        return mapToDTO(empleado, incluirSueldo, null);
    }

    @Override
    @Transactional
    public EmpleadoResponseDTO actualizarEmpleado(Long id, EmpleadoRequestDTO requestDTO) {
        Empleado existente = empleadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con ID: " + id));

        if (!Boolean.TRUE.equals(existente.getActivo())) {
            throw new BusinessException("No se puede modificar un empleado inactivo. Debe reactivarlo previamente.");
        }

        if (!existente.getDni().equals(requestDTO.getDni())) {
            if (empleadoRepository.findByDni(requestDTO.getDni()).isPresent()
                    || empleadoRepository.existsByDni(requestDTO.getDni())) {
                throw new DuplicateResourceException("Ya existe otro empleado con el DNI: " + requestDTO.getDni());
            }
        }

        if (!existente.getUsername().equals(requestDTO.getUsername())) {
            if (empleadoRepository.findByUsername(requestDTO.getUsername()).isPresent()
                    || empleadoRepository.existsByUsername(requestDTO.getUsername())) {
                throw new DuplicateResourceException("Ya existe otro empleado con el username: " + requestDTO.getUsername());
            }
        }

        actualizarCamposEntidad(existente, requestDTO);
        if (requestDTO.getPassword() != null && !requestDTO.getPassword().isBlank()) {
            existente.setPasswordHash(hashPassword(requestDTO.getPassword()));
        }

        Empleado actualizado = empleadoRepository.save(existente);
        return mapToDTO(actualizado, true, null);
    }

    @Override
    @Transactional
    public void eliminarEmpleado(Long id) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con ID: " + id));
        empleado.setActivo(false);
        empleadoRepository.save(empleado);
    }

    @Override
    @Transactional
    public EmpleadoResponseDTO reactivarEmpleado(Long id) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con ID: " + id));

        if (Boolean.TRUE.equals(empleado.getActivo())) {
            throw new BusinessException("El empleado ya se encuentra activo");
        }

        empleado.setActivo(true);
        Empleado reactivado = empleadoRepository.save(empleado);
        return mapToDTO(reactivado, true, true);
    }

    private void actualizarCamposEntidad(Empleado entidad, EmpleadoRequestDTO dto) {
        entidad.setNombre(dto.getNombre());
        entidad.setApellido(dto.getApellido());
        entidad.setDireccion(dto.getDireccion());
        entidad.setDni(dto.getDni());
        entidad.setFechaNac(dto.getFechaNac());
        entidad.setNacionalidad(dto.getNacionalidad());
        entidad.setCelular(dto.getCelular());
        entidad.setEmail(dto.getEmail());
        entidad.setCargo(dto.getCargo());
        entidad.setSueldo(dto.getSueldo());
        entidad.setUsername(dto.getUsername());
        if (dto.getRol() != null) {
            entidad.setRol(dto.getRol());
        }
    }

    private EmpleadoResponseDTO mapToDTO(Empleado entity, boolean incluirSueldo, Boolean reactivado) {
        return new EmpleadoResponseDTO(
                entity.getIdEmpleado(),
                entity.getNombre(),
                entity.getApellido(),
                entity.getDireccion(),
                entity.getDni(),
                entity.getFechaNac(),
                entity.getNacionalidad(),
                entity.getCelular(),
                entity.getEmail(),
                entity.getCargo(),
                incluirSueldo ? entity.getSueldo() : null,
                entity.getUsername(),
                entity.getRol(),
                entity.getActivo(),
                reactivado
        );
    }

    private String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return "$sha256$" + HexFormat.of().formatHex(encoded);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo inicializar el algoritmo de hash", e);
        }
    }
}
