package com.hackacode.hotel.controller;

import com.hackacode.hotel.dto.EmpleadoRequestDTO;
import com.hackacode.hotel.dto.EmpleadoResponseDTO;
import com.hackacode.hotel.service.EmpleadoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
@CrossOrigin(origins = "*")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    @Autowired
    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @PostMapping
    public ResponseEntity<EmpleadoResponseDTO> crearEmpleado(@Valid @RequestBody EmpleadoRequestDTO requestDTO) {
        EmpleadoResponseDTO response = empleadoService.crearEmpleado(requestDTO);
        if (Boolean.TRUE.equals(response.getReactivado())) {
            return ResponseEntity.ok(response);
        }
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EmpleadoResponseDTO>> listarEmpleados(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(empleadoService.listarEmpleados(activo, q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpleadoResponseDTO> obtenerPorId(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "ADMIN") String headerRole,
            @RequestParam(value = "rol", required = false) String paramRole) {
        String effectiveRole = (paramRole != null && !paramRole.isBlank()) ? paramRole : headerRole;
        boolean esAdmin = "ADMIN".equalsIgnoreCase(effectiveRole);
        return ResponseEntity.ok(empleadoService.obtenerPorId(id, esAdmin));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpleadoResponseDTO> actualizarEmpleado(
            @PathVariable Long id,
            @Valid @RequestBody EmpleadoRequestDTO requestDTO) {
        return ResponseEntity.ok(empleadoService.actualizarEmpleado(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEmpleado(@PathVariable Long id) {
        empleadoService.eliminarEmpleado(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<EmpleadoResponseDTO> reactivarEmpleado(@PathVariable Long id) {
        return ResponseEntity.ok(empleadoService.reactivarEmpleado(id));
    }
}
