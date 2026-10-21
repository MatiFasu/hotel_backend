package com.hackacode.hotel.config;

import com.hackacode.hotel.model.*;
import com.hackacode.hotel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class DemoSeederService {

    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ServicioTuristicoRepository servicioRepository;
    private final PaqueteTuristicoRepository paqueteRepository;
    private final VentaRepository ventaRepository;
    private final Clock clock;

    @Autowired
    public DemoSeederService(
            ClienteRepository clienteRepository,
            EmpleadoRepository empleadoRepository,
            ServicioTuristicoRepository servicioRepository,
            PaqueteTuristicoRepository paqueteRepository,
            VentaRepository ventaRepository,
            Clock clock) {
        this.clienteRepository = clienteRepository;
        this.empleadoRepository = empleadoRepository;
        this.servicioRepository = servicioRepository;
        this.paqueteRepository = paqueteRepository;
        this.ventaRepository = ventaRepository;
        this.clock = clock;
    }

    @Transactional
    public Map<String, Object> cargarDatosDemoSiVacio() {
        if (!ventaRepository.findByEstado(EstadoVenta.ACTIVA).isEmpty()) {
            return Map.of(
                    "seeded", false,
                    "message", "La base de datos ya cuenta con ventas activas registradas. No se duplicaron registros."
            );
        }

        LocalDate hoy = LocalDate.now(clock);
        LocalDateTime ahora = LocalDateTime.now(clock);

        // 1. Clientes de demostración
        Cliente c1 = clienteRepository.findByDni("35123456").orElseGet(() ->
                clienteRepository.save(new Cliente(
                        null, "Martín", "Pérez", "Av. Corrientes 1234, CABA", "35123456",
                        LocalDate.of(1990, 5, 15), "Argentina", "+54 9 11 2345-6789", "martin.perez@email.com", true
                ))
        );
        Cliente c2 = clienteRepository.findByDni("38987654").orElseGet(() ->
                clienteRepository.save(new Cliente(
                        null, "Sofía", "Rodríguez", "Bv. Oroño 850, Rosario", "38987654",
                        LocalDate.of(1994, 9, 22), "Argentina", "+54 9 341 555-0192", "sofia.rodriguez@email.com", true
                ))
        );

        // 2. Empleados de demostración (ADMIN y VENDEDOR)
        Empleado eAdmin = empleadoRepository.findByDni("29111222").orElseGet(() ->
                empleadoRepository.save(new Empleado(
                        null, "Valeria", "Gómez", "Av. Santa Fe 2100, CABA", "29111222",
                        LocalDate.of(1985, 4, 10), "Argentina", "+54 9 11 4444-1111", "valeria.admin@hackacode.com",
                        "Gerente General", new BigDecimal("1250000.00"), "admin",
                        com.hackacode.hotel.controller.AuthController.hashSha256("Admin1234!"), RolEmpleado.ADMIN, true
                ))
        );
        Empleado eVendedor = empleadoRepository.findByDni("33444555").orElseGet(() ->
                empleadoRepository.save(new Empleado(
                        null, "Lucas", "Fernández", "Av. Cabildo 1500, CABA", "33444555",
                        LocalDate.of(1992, 8, 3), "Argentina", "+54 9 11 5555-2222", "lucas.ventas@hackacode.com",
                        "Asesor Comercial Senior", new BigDecimal("890000.00"), "vendedor",
                        com.hackacode.hotel.controller.AuthController.hashSha256("Vendedor1234!"), RolEmpleado.VENDEDOR, true
                ))
        );

        // 3. Servicios Turísticos vigentes (fechas futuras relativas a Clock)
        ServicioTuristico sHotelIguazu = servicioRepository.save(new ServicioTuristico(
                null, "Noche Hotel Cataratas Suite 5*", "Habitación doble vista selva con desayuno buffet",
                "Puerto Iguazú, Misiones", hoy.plusDays(10), new BigDecimal("85000.00"), TipoServicio.HOTEL, true
        ));
        ServicioTuristico sVueloIguazu = servicioRepository.save(new ServicioTuristico(
                null, "Vuelo Directo Aeroparque - Iguazú", "Ida y vuelta con equipaje en bodega",
                "Puerto Iguazú, Misiones", hoy.plusDays(10), new BigDecimal("115000.00"), TipoServicio.PASAJE_AVION, true
        ));
        ServicioTuristico sExcursionGarganta = servicioRepository.save(new ServicioTuristico(
                null, "Excursión Gran Aventura Cataratas", "Paseo en lancha y senderos con guía bilingüe",
                "Puerto Iguazú, Misiones", hoy.plusDays(11), new BigDecimal("45000.00"), TipoServicio.EXCURSION, true
        ));
        ServicioTuristico sHotelBariloche = servicioRepository.save(new ServicioTuristico(
                null, "Estadía Llao Llao Resort", "Suite Lago Moreno con spa",
                "San Carlos de Bariloche", hoy.plusDays(20), new BigDecimal("140000.00"), TipoServicio.HOTEL, true
        ));
        ServicioTuristico sAutoBariloche = servicioRepository.save(new ServicioTuristico(
                null, "Alquiler SUV 4x4 Ruta de los 7 Lagos", "Kilometraje libre y seguro total",
                "San Carlos de Bariloche", hoy.plusDays(20), new BigDecimal("60000.00"), TipoServicio.ALQUILER_AUTO, true
        ));

        // 4. Paquetes Turísticos con 10% de descuento automático
        PaqueteTuristico pIguazu = paqueteRepository.save(new PaqueteTuristico(
                null, "Escapada Cataratas Premium (Hotel + Vuelo + Excursión)",
                List.of(sHotelIguazu, sVueloIguazu, sExcursionGarganta), true
        ));
        PaqueteTuristico pBariloche = paqueteRepository.save(new PaqueteTuristico(
                null, "Pack Patagonia Andina (Hotel + SUV 4x4)",
                List.of(sHotelBariloche, sAutoBariloche), true
        ));

        // 5. Ventas del día actual y del mes actual para poblar reportes diarios y mensuales
        BigDecimal montoPaqueteIguazu = pIguazu.calcularCostoPaquete(); // (85000+115000+45000)*0.90 = 220500.00
        ventaRepository.save(new Venta(
                null, ahora.minusHours(2), MedioPago.TARJETA_CREDITO,
                montoPaqueteIguazu, MedioPago.TARJETA_CREDITO.getPorcentajeComision(),
                MedioPago.TARJETA_CREDITO.calcularComision(montoPaqueteIguazu),
                EstadoVenta.ACTIVA, null, c1, eVendedor, null, pIguazu
        ));

        BigDecimal montoServicioHotel = sHotelBariloche.getCostoServicio(); // 140000.00
        ventaRepository.save(new Venta(
                null, ahora.minusHours(1), MedioPago.TRANSFERENCIA,
                montoServicioHotel, MedioPago.TRANSFERENCIA.getPorcentajeComision(),
                MedioPago.TRANSFERENCIA.calcularComision(montoServicioHotel),
                EstadoVenta.ACTIVA, null, c2, eAdmin, sHotelBariloche, null
        ));

        BigDecimal montoPaqueteBariloche = pBariloche.calcularCostoPaquete(); // (140000+60000)*0.90 = 180000.00
        LocalDateTime fechaMismoMes = hoy.getDayOfMonth() > 2 ? ahora.minusDays(2) : ahora;
        ventaRepository.save(new Venta(
                null, fechaMismoMes, MedioPago.TARJETA_DEBITO,
                montoPaqueteBariloche, MedioPago.TARJETA_DEBITO.getPorcentajeComision(),
                MedioPago.TARJETA_DEBITO.calcularComision(montoPaqueteBariloche),
                EstadoVenta.ACTIVA, null, c1, eVendedor, null, pBariloche
        ));

        return Map.of(
                "seeded", true,
                "message", "¡Datos de demostración cargados con éxito (Clientes, Empleados, 5 Servicios, 2 Paquetes y 3 Ventas)!"
        );
    }
}
