package com.hackacode.hotel.controller;

import com.hackacode.hotel.dto.MedioPagoDTO;
import com.hackacode.hotel.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medios-pago")
@CrossOrigin(origins = "*")
public class MedioPagoController {

    private final VentaService ventaService;

    @Autowired
    public MedioPagoController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @GetMapping
    public ResponseEntity<List<MedioPagoDTO>> listarMediosPago() {
        return ResponseEntity.ok(ventaService.listarMediosPago());
    }
}
