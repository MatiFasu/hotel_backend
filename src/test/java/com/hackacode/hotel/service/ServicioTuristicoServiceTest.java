package com.hackacode.hotel.service;

import com.hackacode.hotel.dto.ServicioTuristicoRequestDTO;
import com.hackacode.hotel.dto.ServicioTuristicoResponseDTO;
import com.hackacode.hotel.exception.BusinessException;
import com.hackacode.hotel.exception.ResourceNotFoundException;
import com.hackacode.hotel.model.ServicioTuristico;
import com.hackacode.hotel.model.TipoServicio;
import com.hackacode.hotel.repository.PaqueteTuristicoRepository;
import com.hackacode.hotel.repository.ServicioTuristicoRepository;
import com.hackacode.hotel.service.impl.ServicioTuristicoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioTuristicoServiceTest {

    @Mock
    private ServicioTuristicoRepository servicioRepository;

    @Mock
    private PaqueteTuristicoRepository paqueteRepository;

    private Clock fixedClock;
    private ServicioTuristicoServiceImpl servicioService;

    private ServicioTuristico servicio;
    private ServicioTuristicoRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        // Fijar reloj en 2026-10-15
        fixedClock = Clock.fixed(Instant.parse("2026-10-15T12:00:00Z"), ZoneId.of("America/Argentina/Buenos_Aires"));
        servicioService = new ServicioTuristicoServiceImpl(servicioRepository, paqueteRepository, fixedClock);

        servicio = new ServicioTuristico(
                1L,
                "Noche Hotel Costa Galana",
                "Habitación doble con vista al mar",
                "Mar del Plata",
                LocalDate.of(2026, 11, 20),
                new BigDecimal("45000.00"),
                TipoServicio.HOTEL,
                true
        );

        requestDTO = new ServicioTuristicoRequestDTO(
                "Noche Hotel Costa Galana",
                "Habitación doble con vista al mar",
                "Mar del Plata",
                LocalDate.of(2026, 11, 20),
                new BigDecimal("45000.00"),
                TipoServicio.HOTEL
        );
    }

    @Test
    @DisplayName("Crear servicio turístico con fecha futura exitoso")
    void testCrearServicioExitoso() {
        when(servicioRepository.save(any(ServicioTuristico.class))).thenReturn(servicio);

        ServicioTuristicoResponseDTO resultado = servicioService.crearServicio(requestDTO);

        assertNotNull(resultado);
        assertEquals("Noche Hotel Costa Galana", resultado.getNombre());
        assertEquals(TipoServicio.HOTEL, resultado.getTipoServicio());
        assertTrue(resultado.getActivo());
        verify(servicioRepository, times(1)).save(any(ServicioTuristico.class));
    }

    @Test
    @DisplayName("Crear servicio turístico con fecha en el pasado lanza BusinessException")
    void testCrearServicioFechaPasada() {
        requestDTO.setFechaServicio(LocalDate.of(2026, 10, 10)); // anterior a 2026-10-15

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            servicioService.crearServicio(requestDTO);
        });

        assertTrue(ex.getMessage().contains("anterior"));
        verify(servicioRepository, never()).save(any(ServicioTuristico.class));
    }

    @Test
    @DisplayName("Obtener servicio por código existente")
    void testObtenerPorCodigoExistente() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

        ServicioTuristicoResponseDTO resultado = servicioService.obtenerPorCodigo(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getCodigoServicio());
    }

    @Test
    @DisplayName("Obtener servicio por código inexistente lanza ResourceNotFoundException")
    void testObtenerPorCodigoInexistente() {
        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            servicioService.obtenerPorCodigo(99L);
        });
    }

    @Test
    @DisplayName("Eliminar servicio realiza baja lógica (activo = false)")
    void testEliminarServicioBajaLogica() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(paqueteRepository.existsPaqueteActivoConServicio(1L)).thenReturn(false);
        when(servicioRepository.save(any(ServicioTuristico.class))).thenReturn(servicio);

        servicioService.eliminarServicio(1L);

        assertFalse(servicio.getActivo());
        verify(servicioRepository, times(1)).save(servicio);
    }

    @Test
    @DisplayName("Eliminar servicio que integra un paquete activo lanza BusinessException (Decisión 6)")
    void testEliminarServicioEnPaqueteActivoBloqueado() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(paqueteRepository.existsPaqueteActivoConServicio(1L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            servicioService.eliminarServicio(1L);
        });

        assertTrue(ex.getMessage().contains("paquetes turísticos activos"));
        assertTrue(servicio.getActivo());
        verify(servicioRepository, never()).save(any(ServicioTuristico.class));
    }

    @Test
    @DisplayName("Reactivar servicio previamente inactivo exitoso")
    void testReactivarServicioExitoso() {
        servicio.setActivo(false);
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(servicioRepository.save(any(ServicioTuristico.class))).thenReturn(servicio);

        ServicioTuristicoResponseDTO resultado = servicioService.reactivarServicio(1L);

        assertNotNull(resultado);
        assertTrue(servicio.getActivo());
        verify(servicioRepository, times(1)).save(servicio);
    }

    @Test
    @DisplayName("Reactivar servicio que ya está activo lanza BusinessException")
    void testReactivarServicioYaActivo() {
        servicio.setActivo(true);
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

        assertThrows(BusinessException.class, () -> {
            servicioService.reactivarServicio(1L);
        });

        verify(servicioRepository, never()).save(any(ServicioTuristico.class));
    }
}
