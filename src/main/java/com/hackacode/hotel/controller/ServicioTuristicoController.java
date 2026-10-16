package com.hackacode.hotel.controller;

import com.hackacode.hotel.dto.ServicioTuristicoRequestDTO;
import com.hackacode.hotel.dto.ServicioTuristicoResponseDTO;
import com.hackacode.hotel.model.TipoServicio;
import com.hackacode.hotel.service.ServicioTuristicoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@CrossOrigin(origins = "*")
public class ServicioTuristicoController {

    private final ServicioTuristicoService servicioService;

    @Autowired
    public ServicioTuristicoController(ServicioTuristicoService servicioService) {
        this.servicioService = servicioService;
    }

    @PostMapping
    public ResponseEntity<ServicioTuristicoResponseDTO> crearServicio(@Valid @RequestBody ServicioTuristicoRequestDTO requestDTO) {
        ServicioTuristicoResponseDTO response = servicioService.crearServicio(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ServicioTuristicoResponseDTO>> listarServicios(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) TipoServicio tipoServicio) {
        List<ServicioTuristicoResponseDTO> lista = servicioService.listarServicios(activo, tipoServicio);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicioTuristicoResponseDTO> obtenerPorCodigo(@PathVariable Long id) {
        ServicioTuristicoResponseDTO response = servicioService.obtenerPorCodigo(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicioTuristicoResponseDTO> actualizarServicio(
            @PathVariable Long id,
            @Valid @RequestBody ServicioTuristicoRequestDTO requestDTO) {
        ServicioTuristicoResponseDTO response = servicioService.actualizarServicio(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarServicio(@PathVariable Long id) {
        servicioService.eliminarServicio(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ServicioTuristicoResponseDTO> reactivarServicio(@PathVariable Long id) {
        ServicioTuristicoResponseDTO response = servicioService.reactivarServicio(id);
        return ResponseEntity.ok(response);
    }
}
