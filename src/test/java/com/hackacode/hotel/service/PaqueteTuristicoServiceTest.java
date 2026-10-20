package com.hackacode.hotel.service;

import com.hackacode.hotel.dto.PaqueteTuristicoRequestDTO;
import com.hackacode.hotel.dto.PaqueteTuristicoResponseDTO;
import com.hackacode.hotel.exception.BusinessException;
import com.hackacode.hotel.model.PaqueteTuristico;
import com.hackacode.hotel.model.ServicioTuristico;
import com.hackacode.hotel.model.TipoServicio;
import com.hackacode.hotel.repository.PaqueteTuristicoRepository;
import com.hackacode.hotel.repository.ServicioTuristicoRepository;
import com.hackacode.hotel.service.impl.PaqueteTuristicoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaqueteTuristicoServiceTest {

    @Mock
    private PaqueteTuristicoRepository paqueteRepository;

    @Mock
    private ServicioTuristicoRepository servicioRepository;

    private PaqueteTuristicoServiceImpl paqueteService;

    private ServicioTuristico servicioHotel;
    private ServicioTuristico servicioVuelo;

    @BeforeEach
    void setUp() {
        paqueteService = new PaqueteTuristicoServiceImpl(paqueteRepository, servicioRepository);

        servicioHotel = new ServicioTuristico(
                1L,
                "Noche Hotel Cataratas Suite",
                "Habitación doble con desayuno",
                "Puerto Iguazú",
                LocalDate.of(2026, 11, 20),
                new BigDecimal("75000.00"),
                TipoServicio.HOTEL,
                true
        );

        servicioVuelo = new ServicioTuristico(
                2L,
                "Vuelo Ida y Vuelta Iguazú",
                "Vuelo directo desde Aeroparque",
                "Puerto Iguazú",
                LocalDate.of(2026, 11, 20),
                new BigDecimal("50000.00"),
                TipoServicio.PASAJE_AVION,
                true
        );
    }

    @Test
    @DisplayName("Crear paquete turístico con 2 servicios activos aplica 10% de descuento con HALF_UP")
    void testCrearPaqueteExitosoConDescuento() {
        PaqueteTuristicoRequestDTO request = new PaqueteTuristicoRequestDTO(
                "Escapada Cataratas Clásica",
                List.of(1L, 2L)
        );

        PaqueteTuristico entidadGuardada = new PaqueteTuristico(
                10L,
                "Escapada Cataratas Clásica",
                List.of(servicioHotel, servicioVuelo),
                true
        );

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHotel));
        when(servicioRepository.findById(2L)).thenReturn(Optional.of(servicioVuelo));
        when(paqueteRepository.save(any(PaqueteTuristico.class))).thenReturn(entidadGuardada);

        PaqueteTuristicoResponseDTO resultado = paqueteService.crearPaquete(request);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getCodigoPaquete());
        assertEquals("Escapada Cataratas Clásica", resultado.getNombre());
        // (75000.00 + 50000.00) * 0.90 = 112500.00
        assertEquals(new BigDecimal("112500.00"), resultado.getCostoPaquete());
        assertEquals(2, resultado.getServicios().size());
        assertTrue(resultado.getActivo());
        verify(paqueteRepository, times(1)).save(any(PaqueteTuristico.class));
    }

    @Test
    @DisplayName("Crear paquete con menos de 2 servicios lanza BusinessException (HTTP 422)")
    void testCrearPaqueteMenosDeDosServicios() {
        PaqueteTuristicoRequestDTO request = new PaqueteTuristicoRequestDTO(
                "Paquete Inválido Un Solo Servicio",
                List.of(1L)
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            paqueteService.crearPaquete(request);
        });

        assertTrue(ex.getMessage().contains("al menos 2"));
        verify(paqueteRepository, never()).save(any(PaqueteTuristico.class));
    }

    @Test
    @DisplayName("Crear paquete con IDs de servicio duplicados [1, 1] lanza BusinessException (HTTP 422)")
    void testCrearPaqueteServiciosDuplicados() {
        PaqueteTuristicoRequestDTO request = new PaqueteTuristicoRequestDTO(
                "Paquete con Duplicados",
                List.of(1L, 1L)
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            paqueteService.crearPaquete(request);
        });

        assertTrue(ex.getMessage().contains("duplicados"));
        verify(paqueteRepository, never()).save(any(PaqueteTuristico.class));
    }

    @Test
    @DisplayName("Crear paquete incluyendo un servicio inactivo lanza BusinessException (Decisión 11)")
    void testCrearPaqueteConServicioInactivo() {
        servicioVuelo.setActivo(false);
        PaqueteTuristicoRequestDTO request = new PaqueteTuristicoRequestDTO(
                "Paquete Con Inactivo",
                List.of(1L, 2L)
        );

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHotel));
        when(servicioRepository.findById(2L)).thenReturn(Optional.of(servicioVuelo));

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            paqueteService.crearPaquete(request);
        });

        assertTrue(ex.getMessage().contains("inactivo"));
        verify(paqueteRepository, never()).save(any(PaqueteTuristico.class));
    }

    @Test
    @DisplayName("Eliminar paquete realiza baja lógica (activo = false)")
    void testEliminarPaqueteBajaLogica() {
        PaqueteTuristico paquete = new PaqueteTuristico(
                10L,
                "Escapada Cataratas Clásica",
                List.of(servicioHotel, servicioVuelo),
                true
        );
        when(paqueteRepository.findById(10L)).thenReturn(Optional.of(paquete));
        when(paqueteRepository.save(any(PaqueteTuristico.class))).thenReturn(paquete);

        paqueteService.eliminarPaquete(10L);

        assertFalse(paquete.getActivo());
        verify(paqueteRepository, times(1)).save(paquete);
    }

    @Test
    @DisplayName("Reactivar paquete inactivo cuyos servicios están activos es exitoso")
    void testReactivarPaqueteExitoso() {
        PaqueteTuristico paquete = new PaqueteTuristico(
                10L,
                "Escapada Cataratas Clásica",
                List.of(servicioHotel, servicioVuelo),
                false
        );
        when(paqueteRepository.findById(10L)).thenReturn(Optional.of(paquete));
        when(paqueteRepository.save(any(PaqueteTuristico.class))).thenReturn(paquete);

        PaqueteTuristicoResponseDTO resultado = paqueteService.reactivarPaquete(10L);

        assertTrue(resultado.getActivo());
        verify(paqueteRepository, times(1)).save(paquete);
    }

    @Test
    @DisplayName("Reactivar paquete que contiene un servicio dado de baja lanza BusinessException (Decisión 16)")
    void testReactivarPaqueteConServicioInactivoBloqueado() {
        servicioVuelo.setActivo(false);
        PaqueteTuristico paquete = new PaqueteTuristico(
                10L,
                "Escapada Cataratas Clásica",
                List.of(servicioHotel, servicioVuelo),
                false
        );
        when(paqueteRepository.findById(10L)).thenReturn(Optional.of(paquete));

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            paqueteService.reactivarPaquete(10L);
        });

        assertTrue(ex.getMessage().contains("inactivos"));
        verify(paqueteRepository, never()).save(any(PaqueteTuristico.class));
    }
}
