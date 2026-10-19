package com.hackacode.hotel.service;

import com.hackacode.hotel.dto.EmpleadoRequestDTO;
import com.hackacode.hotel.dto.EmpleadoResponseDTO;
import com.hackacode.hotel.exception.BusinessException;
import com.hackacode.hotel.exception.DuplicateResourceException;
import com.hackacode.hotel.model.Empleado;
import com.hackacode.hotel.model.RolEmpleado;
import com.hackacode.hotel.repository.EmpleadoRepository;
import com.hackacode.hotel.service.impl.EmpleadoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
class EmpleadoServiceTest {

    @Mock
    private EmpleadoRepository empleadoRepository;

    @InjectMocks
    private EmpleadoServiceImpl empleadoService;

    private Empleado empleado;
    private EmpleadoRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        empleado = new Empleado(
                1L, "Ana", "Martinez", "Av. Libertador 500", "30111222",
                LocalDate.of(1991, 8, 22), "Argentina", "1155667788", "ana@agencia.com",
                "Ejecutiva de Ventas", new BigDecimal("850000.00"), "amartinez",
                "$sha256$hash", RolEmpleado.VENDEDOR, true
        );

        requestDTO = new EmpleadoRequestDTO(
                "Ana", "Martinez", "Av. Libertador 500", "30111222",
                LocalDate.of(1991, 8, 22), "Argentina", "1155667788", "ana@agencia.com",
                "Ejecutiva de Ventas", new BigDecimal("850000.00"), "amartinez",
                "Secret123!", RolEmpleado.VENDEDOR
        );
    }

    @Test
    @DisplayName("Crear empleado nuevo exitoso")
    void testCrearEmpleadoExitoso() {
        when(empleadoRepository.findByDni("30111222")).thenReturn(Optional.empty());
        when(empleadoRepository.existsByDni("30111222")).thenReturn(false);
        when(empleadoRepository.findByUsername("amartinez")).thenReturn(Optional.empty());
        when(empleadoRepository.existsByUsername("amartinez")).thenReturn(false);
        when(empleadoRepository.save(any(Empleado.class))).thenReturn(empleado);

        EmpleadoResponseDTO resultado = empleadoService.crearEmpleado(requestDTO);

        assertNotNull(resultado);
        assertEquals("Ana", resultado.getNombre());
        assertEquals(new BigDecimal("850000.00"), resultado.getSueldo());
        assertFalse(resultado.getReactivado());
        verify(empleadoRepository, times(1)).save(any(Empleado.class));
    }

    @Test
    @DisplayName("Crear empleado con DNI inactivo lo reactiva")
    void testReactivarEmpleadoPorDniInactivo() {
        empleado.setActivo(false);
        when(empleadoRepository.findByDni("30111222")).thenReturn(Optional.of(empleado));
        when(empleadoRepository.findByUsername("amartinez")).thenReturn(Optional.of(empleado));
        when(empleadoRepository.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));

        requestDTO.setCargo("Gerente Comercial");
        EmpleadoResponseDTO resultado = empleadoService.crearEmpleado(requestDTO);

        assertNotNull(resultado);
        assertTrue(resultado.getActivo());
        assertTrue(resultado.getReactivado());
        assertEquals("Gerente Comercial", resultado.getCargo());
        verify(empleadoRepository, times(1)).save(empleado);
    }

    @Test
    @DisplayName("Crear empleado con DNI activo duplicado lanza DuplicateResourceException")
    void testCrearEmpleadoDniActivoDuplicado() {
        when(empleadoRepository.findByDni("30111222")).thenReturn(Optional.of(empleado));

        assertThrows(DuplicateResourceException.class, () -> {
            empleadoService.crearEmpleado(requestDTO);
        });

        verify(empleadoRepository, never()).save(any(Empleado.class));
    }

    @Test
    @DisplayName("Listar empleados oculta el campo sueldo")
    void testListarEmpleadosOcultaSueldo() {
        when(empleadoRepository.findAll()).thenReturn(List.of(empleado));

        List<EmpleadoResponseDTO> lista = empleadoService.listarEmpleados(null, null);

        assertEquals(1, lista.size());
        assertNull(lista.get(0).getSueldo());
    }

    @Test
    @DisplayName("Obtener empleado por ID muestra sueldo solo si incluirSueldo es true (ADMIN)")
    void testObtenerEmpleadoPorIdProteccionSalarial() {
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));

        EmpleadoResponseDTO vistaVendedor = empleadoService.obtenerPorId(1L, false);
        assertNull(vistaVendedor.getSueldo());

        EmpleadoResponseDTO vistaAdmin = empleadoService.obtenerPorId(1L, true);
        assertEquals(new BigDecimal("850000.00"), vistaAdmin.getSueldo());
    }

    @Test
    @DisplayName("Baja lógica de empleado cambia activo a false")
    void testEliminarEmpleadoBajaLogica() {
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));
        when(empleadoRepository.save(any(Empleado.class))).thenReturn(empleado);

        empleadoService.eliminarEmpleado(1L);

        assertFalse(empleado.getActivo());
        verify(empleadoRepository, times(1)).save(empleado);
    }

    @Test
    @DisplayName("Actualizar empleado inactivo lanza BusinessException")
    void testActualizarEmpleadoInactivo() {
        empleado.setActivo(false);
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));

        assertThrows(BusinessException.class, () -> {
            empleadoService.actualizarEmpleado(1L, requestDTO);
        });
    }
}
