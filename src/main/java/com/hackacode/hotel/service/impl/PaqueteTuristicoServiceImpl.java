package com.hackacode.hotel.service.impl;

import com.hackacode.hotel.dto.PaqueteTuristicoRequestDTO;
import com.hackacode.hotel.dto.PaqueteTuristicoResponseDTO;
import com.hackacode.hotel.dto.ServicioTuristicoResponseDTO;
import com.hackacode.hotel.exception.BusinessException;
import com.hackacode.hotel.exception.ResourceNotFoundException;
import com.hackacode.hotel.model.PaqueteTuristico;
import com.hackacode.hotel.model.ServicioTuristico;
import com.hackacode.hotel.repository.PaqueteTuristicoRepository;
import com.hackacode.hotel.repository.ServicioTuristicoRepository;
import com.hackacode.hotel.repository.VentaRepository;
import com.hackacode.hotel.service.PaqueteTuristicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PaqueteTuristicoServiceImpl implements PaqueteTuristicoService {

    private final PaqueteTuristicoRepository paqueteRepository;
    private final ServicioTuristicoRepository servicioRepository;
    private final VentaRepository ventaRepository;

    public PaqueteTuristicoServiceImpl(
            PaqueteTuristicoRepository paqueteRepository,
            ServicioTuristicoRepository servicioRepository) {
        this(paqueteRepository, servicioRepository, null);
    }

    @Autowired
    public PaqueteTuristicoServiceImpl(
            PaqueteTuristicoRepository paqueteRepository,
            ServicioTuristicoRepository servicioRepository,
            VentaRepository ventaRepository) {
        this.paqueteRepository = paqueteRepository;
        this.servicioRepository = servicioRepository;
        this.ventaRepository = ventaRepository;
    }

    @Override
    @Transactional
    public PaqueteTuristicoResponseDTO crearPaquete(PaqueteTuristicoRequestDTO requestDTO) {
        List<ServicioTuristico> servicios = validarYObtenerServiciosParaPaquete(requestDTO.getServicioIds());

        PaqueteTuristico paquete = new PaqueteTuristico(
                null,
                requestDTO.getNombre(),
                servicios,
                true
        );

        PaqueteTuristico guardado = paqueteRepository.save(paquete);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaqueteTuristicoResponseDTO> listarPaquetes(Boolean activo) {
        List<PaqueteTuristico> lista;
        if (activo != null) {
            lista = paqueteRepository.findByActivo(activo);
        } else {
            lista = paqueteRepository.findAll();
        }
        return lista.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaqueteTuristicoResponseDTO obtenerPorCodigo(Long codigo) {
        PaqueteTuristico paquete = paqueteRepository.findById(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Paquete turístico no encontrado con código: " + codigo));
        return mapToDTO(paquete);
    }

    @Override
    @Transactional
    public PaqueteTuristicoResponseDTO actualizarPaquete(Long codigo, PaqueteTuristicoRequestDTO requestDTO) {
        PaqueteTuristico paquete = paqueteRepository.findById(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Paquete turístico no encontrado con código: " + codigo));

        if (!Boolean.TRUE.equals(paquete.getActivo())) {
            throw new BusinessException("No se puede modificar un paquete turístico inactivo. Debe reactivarlo previamente.");
        }

        // Decisión 5: Inmutabilidad de composición en paquetes con ventas asociadas (incluye ANULADAS)
        if (ventaRepository != null && ventaRepository.countByPaquete_CodigoPaquete(codigo) > 0) {
            Set<Long> actuales = paquete.getListaServicios().stream()
                    .map(ServicioTuristico::getCodigoServicio)
                    .collect(Collectors.toSet());
            Set<Long> nuevos = requestDTO.getServicioIds() != null
                    ? new HashSet<>(requestDTO.getServicioIds())
                    : Set.of();
            if (!actuales.equals(nuevos)) {
                throw new BusinessException(
                        "No se pueden modificar los servicios de un paquete turístico que ya registra ventas asociadas. Solo se permite cambiar su nombre."
                );
            }
        }

        List<ServicioTuristico> servicios = validarYObtenerServiciosParaPaquete(requestDTO.getServicioIds());

        paquete.setNombre(requestDTO.getNombre());
        paquete.setListaServicios(servicios);

        PaqueteTuristico actualizado = paqueteRepository.save(paquete);
        return mapToDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminarPaquete(Long codigo) {
        PaqueteTuristico paquete = paqueteRepository.findById(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Paquete turístico no encontrado con código: " + codigo));

        // Baja lógica (Decisión 1)
        paquete.setActivo(false);
        paqueteRepository.save(paquete);
    }

    @Override
    @Transactional
    public PaqueteTuristicoResponseDTO reactivarPaquete(Long codigo) {
        PaqueteTuristico paquete = paqueteRepository.findById(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Paquete turístico no encontrado con código: " + codigo));

        if (Boolean.TRUE.equals(paquete.getActivo())) {
            throw new BusinessException("El paquete turístico ya se encuentra activo");
        }

        // Decisión 11 y 16: Verificar que todos los servicios asociados estén activos antes de reactivar
        for (ServicioTuristico servicio : paquete.getListaServicios()) {
            if (!Boolean.TRUE.equals(servicio.getActivo())) {
                throw new BusinessException(
                        "No se puede reactivar el paquete porque contiene servicios inactivos (ej. \"" + servicio.getNombre() + "\"). Reactive primero todos sus servicios."
                );
            }
        }

        paquete.setActivo(true);
        PaqueteTuristico reactivado = paqueteRepository.save(paquete);
        return mapToDTO(reactivado);
    }

    /**
     * Valida la regla de negocio de composición de paquetes (Decisión 3 y Decisión 11):
     * - Mínimo 2 servicios turísticos.
     * - Sin identificadores repetidos.
     * - Todos los servicios existentes y en estado activo = true.
     */
    private List<ServicioTuristico> validarYObtenerServiciosParaPaquete(List<Long> servicioIds) {
        if (servicioIds == null || servicioIds.size() < 2) {
            throw new BusinessException("Un paquete turístico debe contener al menos 2 servicios turísticos distintos.");
        }

        Set<Long> unicos = new HashSet<>(servicioIds);
        if (unicos.size() != servicioIds.size()) {
            throw new BusinessException("No se permiten servicios duplicados dentro de un mismo paquete turístico.");
        }

        List<ServicioTuristico> servicios = new ArrayList<>();
        for (Long idServicio : servicioIds) {
            ServicioTuristico servicio = servicioRepository.findById(idServicio)
                    .orElseThrow(() -> new ResourceNotFoundException("Servicio turístico no encontrado con código: " + idServicio));

            if (!Boolean.TRUE.equals(servicio.getActivo())) {
                throw new BusinessException("No se puede incluir un servicio inactivo en un paquete turístico: " + servicio.getNombre());
            }
            servicios.add(servicio);
        }

        return servicios;
    }

    private PaqueteTuristicoResponseDTO mapToDTO(PaqueteTuristico entity) {
        List<ServicioTuristicoResponseDTO> serviciosDTO = entity.getListaServicios().stream()
                .map(s -> new ServicioTuristicoResponseDTO(
                        s.getCodigoServicio(),
                        s.getNombre(),
                        s.getDescripcionBreve(),
                        s.getDestinoServicio(),
                        s.getFechaServicio(),
                        s.getCostoServicio(),
                        s.getTipoServicio(),
                        s.getActivo()
                ))
                .collect(Collectors.toList());

        long ventasCount = (ventaRepository != null && entity.getCodigoPaquete() != null)
                ? ventaRepository.countByPaquete_CodigoPaquete(entity.getCodigoPaquete())
                : 0L;

        return new PaqueteTuristicoResponseDTO(
                entity.getCodigoPaquete(),
                entity.getNombre(),
                entity.calcularCostoPaquete(),
                entity.getActivo(),
                ventasCount,
                ventasCount == 0L,
                serviciosDTO
        );
    }
}
