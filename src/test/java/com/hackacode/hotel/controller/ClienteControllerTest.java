package com.hackacode.hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackacode.hotel.AbstractIntegrationTest;
import com.hackacode.hotel.dto.ClienteDTO;
import com.hackacode.hotel.model.Cliente;
import com.hackacode.hotel.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClienteControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        clienteRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/clientes - Crear cliente con datos válidos")
    void testCrearClienteHTTP() throws Exception {
        ClienteDTO dto = new ClienteDTO(null, "Maria", "Gomez", "Calle Falsa 123", "87654321", LocalDate.of(1995, 10, 20), "Argentina", "1199887766", "maria@example.com");

        mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCliente", notNullValue()))
                .andExpect(jsonPath("$.nombre", is("Maria")))
                .andExpect(jsonPath("$.dni", is("87654321")));
    }

    @Test
    @DisplayName("GET /api/clientes - Obtener lista de clientes")
    void testObtenerTodosHTTP() throws Exception {
        Cliente cliente = new Cliente(null, "Carlos", "Lopez", "Calle 1", "11223344", LocalDate.of(1988, 3, 12), "Chile", "99887766", "carlos@example.com");
        clienteRepository.save(cliente);

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("Carlos")));
    }

    @Test
    @DisplayName("GET /api/clientes/{id} - Cliente no encontrado")
    void testObtenerPorIdNoEncontradoHTTP() throws Exception {
        mockMvc.perform(get("/api/clientes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Cliente no encontrado")));
    }
}
