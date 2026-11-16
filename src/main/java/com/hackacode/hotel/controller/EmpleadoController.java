package com.hackacode.hotel.controller;

import com.hackacode.hotel.dto.EmpleadoRequestDTO;
import com.hackacode.hotel.dto.EmpleadoResponseDTO;
import com.hackacode.hotel.service.EmpleadoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    @Autowired
    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean esAdmin;
        if (headerRole != null && !headerRole.isBlank() && !"ADMIN".equalsIgnoreCase(headerRole)) {
            esAdmin = false;
        } else if (paramRole != null && !paramRole.isBlank() && !"ADMIN".equalsIgnoreCase(paramRole)) {
            esAdmin = false;
        } else if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            esAdmin = true;
        } else {
            String effectiveRole = (paramRole != null && !paramRole.isBlank()) ? paramRole : headerRole;
            esAdmin = "ADMIN".equalsIgnoreCase(effectiveRole);
        }
        return ResponseEntity.ok(empleadoService.obtenerPorId(id, esAdmin));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmpleadoResponseDTO> actualizarEmpleado(
            @PathVariable Long id,
            @Valid @RequestBody EmpleadoRequestDTO requestDTO) {
        return ResponseEntity.ok(empleadoService.actualizarEmpleado(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarEmpleado(@PathVariable Long id) {
        empleadoService.eliminarEmpleado(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmpleadoResponseDTO> reactivarEmpleado(@PathVariable Long id) {
        return ResponseEntity.ok(empleadoService.reactivarEmpleado(id));
    }
}
