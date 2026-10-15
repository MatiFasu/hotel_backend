package com.hackacode.hotel.service;

import com.hackacode.hotel.dto.ClienteDTO;
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
        cliente = new Cliente(1L, "Juan", "Perez", "Av. Siempreviva 123", "12345678", LocalDate.of(1990, 5, 15), "Argentina", "1122334455", "juan@example.com");
        clienteDTO = new ClienteDTO(1L, "Juan", "Perez", "Av. Siempreviva 123", "12345678", LocalDate.of(1990, 5, 15), "Argentina", "1122334455", "juan@example.com");
    }

    @Test
    @DisplayName("Crear cliente exitoso - Caso Feliz")
    void testCrearClienteExitoso() {
        when(clienteRepository.existsByDni("12345678")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        ClienteDTO resultado = clienteService.crearCliente(clienteDTO);

        assertNotNull(resultado);
        assertEquals("Juan", resultado.getNombre());
        assertEquals("12345678", resultado.getDni());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Crear cliente con DNI duplicado - Caso Límite / Excepción")
    void testCrearClienteDniDuplicado() {
        when(clienteRepository.existsByDni("12345678")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> {
            clienteService.crearCliente(clienteDTO);
        });

        verify(clienteRepository, never()).save(any(Cliente.class));
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
    @DisplayName("Eliminar cliente existente")
    void testEliminarClienteExitoso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        doNothing().when(clienteRepository).delete(cliente);

        clienteService.eliminarCliente(1L);

        verify(clienteRepository, times(1)).delete(cliente);
    }
}
