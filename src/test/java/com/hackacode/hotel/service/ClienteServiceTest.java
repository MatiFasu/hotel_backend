package com.hackacode.hotel.service;

import com.hackacode.hotel.dto.ClienteDTO;
import com.hackacode.hotel.exception.BusinessException;
import com.hackacode.hotel.exception.DuplicateResourceException;
import com.hackacode.hotel.exception.ResourceNotFoundException;
import com.hackacode.hotel.model.Cliente;
import com.hackacode.hotel.repository.ClienteRepository;
import com.hackacode.hotel.service.impl.ClienteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private Cliente cliente;
    private ClienteDTO clienteDTO;

    @BeforeEach
    void setUp() {
        cliente = new Cliente(1L, "Juan", "Perez", "Av. Siempreviva 123", "12345678", LocalDate.of(1990, 5, 15), "Argentina", "1122334455", "juan@example.com", true);
        clienteDTO = new ClienteDTO(1L, "Juan", "Perez", "Av. Siempreviva 123", "12345678", LocalDate.of(1990, 5, 15), "Argentina", "1122334455", "juan@example.com");
    }

    @Test
    @DisplayName("Crear cliente exitoso - Caso Feliz")
    void testCrearClienteExitoso() {
        when(clienteRepository.findByDni("12345678")).thenReturn(Optional.empty());
        when(clienteRepository.existsByDni("12345678")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        ClienteDTO resultado = clienteService.crearCliente(clienteDTO);

        assertNotNull(resultado);
        assertEquals("Juan", resultado.getNombre());
        assertEquals("12345678", resultado.getDni());
        assertFalse(resultado.getReactivado());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Crear cliente con DNI activo duplicado lanza DuplicateResourceException (409)")
    void testCrearClienteDniDuplicado() {
        when(clienteRepository.findByDni("12345678")).thenReturn(Optional.of(cliente));

        assertThrows(DuplicateResourceException.class, () -> {
            clienteService.crearCliente(clienteDTO);
        });

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Crear cliente con DNI inactivo existente lo reactiva (reactivado = true)")
    void testReactivarClientePorDniInactivo() {
        cliente.setActivo(false);
        when(clienteRepository.findByDni("12345678")).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        clienteDTO.setDireccion("Nueva Direccion 456");
        ClienteDTO resultado = clienteService.crearCliente(clienteDTO);

        assertNotNull(resultado);
        assertTrue(resultado.getActivo());
        assertTrue(resultado.getReactivado());
        assertEquals("Nueva Direccion 456", resultado.getDireccion());
        verify(clienteRepository, times(1)).save(cliente);
    }

    @Test
    @DisplayName("Obtener cliente por ID existente")
    void testObtenerClientePorIdExistente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        ClienteDTO resultado = clienteService.obtenerClientePorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdCliente());
        assertEquals("Perez", resultado.getApellido());
    }

    @Test
    @DisplayName("Obtener cliente por ID inexistente - Excepción")
    void testObtenerClientePorIdInexistente() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            clienteService.obtenerClientePorId(99L);
        });
    }

    @Test
    @DisplayName("Obtener todos los clientes")
    void testObtenerTodosLosClientes() {
        when(clienteRepository.findAll()).thenReturn(List.of(cliente));

        List<ClienteDTO> lista = clienteService.obtenerTodosLosClientes();

        assertNotNull(lista);
        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("Eliminar cliente realiza baja lógica (activo = false)")
    void testEliminarClienteExitoso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        clienteService.eliminarCliente(1L);

        assertFalse(cliente.getActivo());
        verify(clienteRepository, times(1)).save(cliente);
    }

    @Test
    @DisplayName("Actualizar cliente inactivo lanza BusinessException (422)")
    void testActualizarClienteInactivoLanzaExcepcion() {
        cliente.setActivo(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        assertThrows(BusinessException.class, () -> {
            clienteService.actualizarCliente(1L, clienteDTO);
        });

        verify(clienteRepository, never()).save(any(Cliente.class));
    }
}
