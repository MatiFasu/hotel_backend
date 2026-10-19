package com.hackacode.hotel.service.impl;

import com.hackacode.hotel.dto.ServicioTuristicoRequestDTO;
import com.hackacode.hotel.dto.ServicioTuristicoResponseDTO;
import com.hackacode.hotel.exception.BusinessException;
import com.hackacode.hotel.exception.ResourceNotFoundException;
import com.hackacode.hotel.model.ServicioTuristico;
import com.hackacode.hotel.model.TipoServicio;
import com.hackacode.hotel.repository.ServicioTuristicoRepository;
import com.hackacode.hotel.service.ServicioTuristicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioTuristicoServiceImpl implements ServicioTuristicoService {

    private final ServicioTuristicoRepository servicioRepository;
    private final Clock clock;

    @Autowired
    public ServicioTuristicoServiceImpl(ServicioTuristicoRepository servicioRepository, Clock clock) {
        this.servicioRepository = servicioRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ServicioTuristicoResponseDTO crearServicio(ServicioTuristicoRequestDTO requestDTO) {
        LocalDate hoy = LocalDate.now(clock);
        if (requestDTO.getFechaServicio().isBefore(hoy)) {
            throw new BusinessException("La fecha del servicio no puede ser anterior a la fecha actual: " + hoy);
        }

        ServicioTuristico servicio = new ServicioTuristico(
                null,
                requestDTO.getNombre(),
                requestDTO.getDescripcionBreve(),
                requestDTO.getDestinoServicio(),
                requestDTO.getFechaServicio(),
                requestDTO.getCostoServicio(),
                requestDTO.getTipoServicio(),
                true
        );

        ServicioTuristico guardado = servicioRepository.save(servicio);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioTuristicoResponseDTO> listarServicios(Boolean activo, TipoServicio tipoServicio) {
        List<ServicioTuristico> lista;

        if (activo == null && tipoServicio == null) {
            lista = servicioRepository.findAll();
        } else if (activo != null && tipoServicio == null) {
            lista = servicioRepository.findByActivo(activo);
        } else if (activo == null && tipoServicio != null) {
            lista = servicioRepository.findAll().stream()
                    .filter(s -> s.getTipoServicio() == tipoServicio)
                    .collect(Collectors.toList());
        } else {
            lista = servicioRepository.findByActivo(activo).stream()
                    .filter(s -> s.getTipoServicio() == tipoServicio)
                    .collect(Collectors.toList());
        }

        return lista.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ServicioTuristicoResponseDTO obtenerPorCodigo(Long codigo) {
        ServicioTuristico servicio = servicioRepository.findById(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio turístico no encontrado con código: " + codigo));
        return mapToDTO(servicio);
    }

    @Override
    @Transactional
    public ServicioTuristicoResponseDTO actualizarServicio(Long codigo, ServicioTuristicoRequestDTO requestDTO) {
        ServicioTuristico servicio = servicioRepository.findById(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio turístico no encontrado con código: " + codigo));

        servicio.setNombre(requestDTO.getNombre());
        servicio.setDescripcionBreve(requestDTO.getDescripcionBreve());
        servicio.setDestinoServicio(requestDTO.getDestinoServicio());
        servicio.setFechaServicio(requestDTO.getFechaServicio());
        servicio.setCostoServicio(requestDTO.getCostoServicio());
        servicio.setTipoServicio(requestDTO.getTipoServicio());

        ServicioTuristico actualizado = servicioRepository.save(servicio);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminarServicio(Long codigo) {
        ServicioTuristico servicio = servicioRepository.findById(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio turístico no encontrado con código: " + codigo));

        // Baja lógica
        servicio.setActivo(false);
        servicioRepository.save(servicio);
    }

    @Override
    @Transactional
    public ServicioTuristicoResponseDTO reactivarServicio(Long codigo) {
        ServicioTuristico servicio = servicioRepository.findById(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio turístico no encontrado con código: " + codigo));

        if (Boolean.TRUE.equals(servicio.getActivo())) {
            throw new BusinessException("El servicio turístico ya se encuentra activo");
        }

        servicio.setActivo(true);
        ServicioTuristico reactivado = servicioRepository.save(servicio);
        return mapToDTO(reactivado);
    }

    private ServicioTuristicoResponseDTO mapToDTO(ServicioTuristico entity) {
        return new ServicioTuristicoResponseDTO(
                entity.getCodigoServicio(),
                entity.getNombre(),
                entity.getDescripcionBreve(),
                entity.getDestinoServicio(),
                entity.getFechaServicio(),
                entity.getCostoServicio(),
                entity.getTipoServicio(),
                entity.getActivo()
        );
    }
}
