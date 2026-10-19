package com.hackacode.hotel.controller;

import com.hackacode.hotel.dto.ClienteDTO;
import com.hackacode.hotel.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    private final ClienteService clienteService;

    @Autowired
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteDTO> crearCliente(@Valid @RequestBody ClienteDTO clienteDTO) {
        ClienteDTO resultado = clienteService.crearCliente(clienteDTO);
        if (Boolean.TRUE.equals(resultado.getReactivado())) {
            return ResponseEntity.ok(resultado);
        }
        return new ResponseEntity<>(resultado, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> obtenerTodos(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String q) {
        if (activo == null && (q == null || q.isBlank())) {
            return ResponseEntity.ok(clienteService.obtenerTodosLosClientes());
        }
        return ResponseEntity.ok(clienteService.listarClientes(activo, q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerClientePorId(id));
    }

    @GetMapping("/dni/{dni}")
    public ResponseEntity<ClienteDTO> obtenerPorDni(@PathVariable String dni) {
        return ResponseEntity.ok(clienteService.obtenerClientePorDni(dni));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> actualizarCliente(@PathVariable Long id, @Valid @RequestBody ClienteDTO clienteDTO) {
        return ResponseEntity.ok(clienteService.actualizarCliente(id, clienteDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        clienteService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ClienteDTO> reactivarCliente(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.reactivarCliente(id));
    }
}
