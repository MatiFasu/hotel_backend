package com.hackacode.hotel.controller;

import com.hackacode.hotel.dto.VentaRequestDTO;
import com.hackacode.hotel.dto.VentaResponseDTO;
import com.hackacode.hotel.model.EstadoVenta;
import com.hackacode.hotel.service.VentaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "*")
public class VentaController {

    private final VentaService ventaService;

    @Autowired
    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping
    public ResponseEntity<VentaResponseDTO> registrarVenta(@Valid @RequestBody VentaRequestDTO requestDTO) {
        VentaResponseDTO response = ventaService.registrarVenta(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> listarVentas(
            @RequestParam(required = false) EstadoVenta estado,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String tipoProducto) {
        List<VentaResponseDTO> lista = ventaService.listarVentas(estado, clienteId, tipoProducto);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> obtenerVentaPorNumero(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.obtenerVentaPorNumero(id));
    }

    @PatchMapping("/{id}/anular")
    public ResponseEntity<VentaResponseDTO> anularVenta(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.anularVenta(id));
    }
}
