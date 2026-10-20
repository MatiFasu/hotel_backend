package com.hackacode.hotel.controller;

import com.hackacode.hotel.dto.PaqueteTuristicoRequestDTO;
import com.hackacode.hotel.dto.PaqueteTuristicoResponseDTO;
import com.hackacode.hotel.service.PaqueteTuristicoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paquetes")
@CrossOrigin(origins = "*")
public class PaqueteTuristicoController {

    private final PaqueteTuristicoService paqueteService;

    @Autowired
    public PaqueteTuristicoController(PaqueteTuristicoService paqueteService) {
        this.paqueteService = paqueteService;
    }

    @PostMapping
    public ResponseEntity<PaqueteTuristicoResponseDTO> crearPaquete(@Valid @RequestBody PaqueteTuristicoRequestDTO requestDTO) {
        PaqueteTuristicoResponseDTO response = paqueteService.crearPaquete(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PaqueteTuristicoResponseDTO>> listarPaquetes(
            @RequestParam(required = false) Boolean activo) {
        List<PaqueteTuristicoResponseDTO> lista = paqueteService.listarPaquetes(activo);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaqueteTuristicoResponseDTO> obtenerPorCodigo(@PathVariable Long id) {
        PaqueteTuristicoResponseDTO response = paqueteService.obtenerPorCodigo(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaqueteTuristicoResponseDTO> actualizarPaquete(
            @PathVariable Long id,
            @Valid @RequestBody PaqueteTuristicoRequestDTO requestDTO) {
        PaqueteTuristicoResponseDTO response = paqueteService.actualizarPaquete(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPaquete(@PathVariable Long id) {
        paqueteService.eliminarPaquete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<PaqueteTuristicoResponseDTO> reactivarPaquete(@PathVariable Long id) {
        PaqueteTuristicoResponseDTO response = paqueteService.reactivarPaquete(id);
        return ResponseEntity.ok(response);
    }
}
