package com.hackacode.hotel.service;

import com.hackacode.hotel.dto.ReporteGananciasDiariasDTO;
import com.hackacode.hotel.dto.ReporteGananciasMensualesDTO;

import java.time.LocalDate;

public interface ReporteService {

    ReporteGananciasDiariasDTO obtenerGananciasDiarias(LocalDate fecha);

    ReporteGananciasMensualesDTO obtenerGananciasMensuales(Integer anio, Integer mes);
}
