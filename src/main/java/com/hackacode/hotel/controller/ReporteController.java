package com.hackacode.hotel.controller;

import com.hackacode.hotel.config.DemoSeederService;
import com.hackacode.hotel.dto.ReporteGananciasDiariasDTO;
import com.hackacode.hotel.dto.ReporteGananciasMensualesDTO;
import com.hackacode.hotel.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*")
public class ReporteController {

    private final ReporteService reporteService;
    private final DemoSeederService demoSeederService;

    @Autowired
    public ReporteController(ReporteService reporteService, DemoSeederService demoSeederService) {
        this.reporteService = reporteService;
        this.demoSeederService = demoSeederService;
    }

    @GetMapping("/ganancias-diarias")
    public ResponseEntity<ReporteGananciasDiariasDTO> obtenerGananciasDiarias(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(reporteService.obtenerGananciasDiarias(fecha));
    }

    @GetMapping("/ganancias-mensuales")
    public ResponseEntity<ReporteGananciasMensualesDTO> obtenerGananciasMensuales(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes) {
        return ResponseEntity.ok(reporteService.obtenerGananciasMensuales(anio, mes));
    }

    @PostMapping("/seed-demo")
    public ResponseEntity<Map<String, Object>> cargarDatosDemo() {
        return ResponseEntity.ok(demoSeederService.cargarDatosDemoSiVacio());
    }
}
