package com.hackacode.hotel.service.impl;

import com.hackacode.hotel.dto.MedioPagoDTO;
import com.hackacode.hotel.dto.VentaRequestDTO;
import com.hackacode.hotel.dto.VentaResponseDTO;
import com.hackacode.hotel.exception.BusinessException;
import com.hackacode.hotel.exception.ResourceNotFoundException;
import com.hackacode.hotel.model.*;
import com.hackacode.hotel.repository.ClienteRepository;
import com.hackacode.hotel.repository.EmpleadoRepository;
import com.hackacode.hotel.repository.PaqueteTuristicoRepository;
import com.hackacode.hotel.repository.ServicioTuristicoRepository;
import com.hackacode.hotel.repository.VentaRepository;
import com.hackacode.hotel.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ServicioTuristicoRepository servicioRepository;
    private final PaqueteTuristicoRepository paqueteRepository;
    private final Clock clock;

    @Autowired
    public VentaServiceImpl(
            VentaRepository ventaRepository,
            ClienteRepository clienteRepository,
            EmpleadoRepository empleadoRepository,
            ServicioTuristicoRepository servicioRepository,
            PaqueteTuristicoRepository paqueteRepository,
            Clock clock) {
        this.ventaRepository = ventaRepository;
        this.clienteRepository = clienteRepository;
        this.empleadoRepository = empleadoRepository;
        this.servicioRepository = servicioRepository;
        this.paqueteRepository = paqueteRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public VentaResponseDTO registrarVenta(VentaRequestDTO requestDTO) {
        // 1. Regla de Exclusión Mutua XOR (Decisión 8)
        boolean tieneServicio = requestDTO.getServicioId() != null;
        boolean tienePaquete = requestDTO.getPaqueteId() != null;
        if (tieneServicio == tienePaquete) {
            throw new BusinessException(
                    "Exclusión mutua violada: la venta debe asociar exactamente un servicio turístico o un paquete turístico, nunca ambos ni ninguno."
            );
        }

        // 2. Validar Cliente activo (Decisión 15)
        Cliente cliente = clienteRepository.findById(requestDTO.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + requestDTO.getClienteId()));
        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new BusinessException("No se puede registrar una venta para un cliente inactivo: " + cliente.getNombre() + " " + cliente.getApellido());
        }

        // 3. Validar Empleado activo (Decisión 15)
        Empleado empleado = empleadoRepository.findById(requestDTO.getEmpleadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con ID: " + requestDTO.getEmpleadoId()));
        if (!Boolean.TRUE.equals(empleado.getActivo())) {
            throw new BusinessException("No se puede registrar una venta con un empleado inactivo: " + empleado.getNombre() + " " + empleado.getApellido());
        }

        // 4. Validar Producto activo, vigencia temporal (Decisión 12) y calcular montoTotal
        LocalDate hoy = LocalDate.now(clock);
        ServicioTuristico servicio = null;
        PaqueteTuristico paquete = null;
        BigDecimal montoTotal;

        if (tieneServicio) {
            servicio = servicioRepository.findById(requestDTO.getServicioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Servicio turístico no encontrado con código: " + requestDTO.getServicioId()));
            if (!Boolean.TRUE.equals(servicio.getActivo())) {
                throw new BusinessException("No se puede vender un servicio turístico inactivo: " + servicio.getNombre());
            }
            if (servicio.getFechaServicio().isBefore(hoy)) {
                throw new BusinessException(
                        "No se puede vender un servicio turístico con fecha vencida (" + servicio.getFechaServicio() + "). Fecha actual: " + hoy
                );
            }
            montoTotal = servicio.getCostoServicio().setScale(2, RoundingMode.HALF_UP);
        } else {
            paquete = paqueteRepository.findById(requestDTO.getPaqueteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Paquete turístico no encontrado con código: " + requestDTO.getPaqueteId()));
            if (!Boolean.TRUE.equals(paquete.getActivo())) {
                throw new BusinessException("No se puede vender un paquete turístico inactivo: " + paquete.getNombre());
            }
            for (ServicioTuristico item : paquete.getListaServicios()) {
                if (!Boolean.TRUE.equals(item.getActivo())) {
                    throw new BusinessException(
                            "No se puede vender el paquete porque contiene un servicio inactivo: " + item.getNombre()
                    );
                }
                if (item.getFechaServicio().isBefore(hoy)) {
                    throw new BusinessException(
                            "No se puede vender el paquete porque el servicio \"" + item.getNombre() + "\" tiene fecha vencida (" + item.getFechaServicio() + ")."
                    );
                }
            }
            montoTotal = paquete.calcularCostoPaquete();
        }

        // 5. Snapshot Financiero y Comisiones (Decisiones 4 y 23)
        MedioPago medioPago = requestDTO.getMedioPago();
        BigDecimal porcentajeComision = medioPago.getPorcentajeComision().setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoComision = medioPago.calcularComision(montoTotal);

        Venta venta = new Venta(
                null,
                LocalDateTime.now(clock),
                medioPago,
                montoTotal,
                porcentajeComision,
                montoComision,
                EstadoVenta.ACTIVA,
                null,
                cliente,
                empleado,
                servicio,
                paquete
        );

        Venta guardada = ventaRepository.save(venta);
        return mapToDTO(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listarVentas(EstadoVenta estado, Long clienteId, String tipoProducto) {
        List<Venta> lista = (estado != null)
                ? ventaRepository.findByEstado(estado)
                : ventaRepository.findAll();

        return lista.stream()
                .filter(v -> clienteId == null || (v.getCliente() != null && clienteId.equals(v.getCliente().getIdCliente())))
                .filter(v -> {
                    if (tipoProducto == null || tipoProducto.isBlank()) return true;
                    if ("SERVICIO".equalsIgnoreCase(tipoProducto)) return v.getServicio() != null;
                    if ("PAQUETE".equalsIgnoreCase(tipoProducto)) return v.getPaquete() != null;
                    return true;
                })
                .sorted(Comparator.comparing(Venta::getNumVenta, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO obtenerVentaPorNumero(Long numVenta) {
        Venta venta = ventaRepository.findById(numVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con número: " + numVenta));
        return mapToDTO(venta);
    }

    @Override
    @Transactional
    public VentaResponseDTO anularVenta(Long numVenta) {
        Venta venta = ventaRepository.findById(numVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con número: " + numVenta));

        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new BusinessException("La venta #" + numVenta + " ya se encuentra anulada.");
        }

        venta.setEstado(EstadoVenta.ANULADA);
        venta.setFechaAnulacion(LocalDateTime.now(clock));

        Venta actualizada = ventaRepository.save(venta);
        return mapToDTO(actualizada);
    }

    @Override
    public List<MedioPagoDTO> listarMediosPago() {
        return Arrays.stream(MedioPago.values())
                .map(mp -> new MedioPagoDTO(mp.name(), mp.getDescripcion(), mp.getPorcentajeComision()))
                .collect(Collectors.toList());
    }

    private VentaResponseDTO mapToDTO(Venta v) {
        BigDecimal gananciaNeta = v.getMontoTotal()
                .subtract(v.getMontoComision())
                .setScale(2, RoundingMode.HALF_UP);

        Cliente c = v.getCliente();
        Empleado e = v.getEmpleado();
        ServicioTuristico s = v.getServicio();
        PaqueteTuristico p = v.getPaquete();

        return new VentaResponseDTO(
                v.getNumVenta(),
                v.getFechaVenta(),
                v.getMedioPago(),
                v.getMontoTotal(),
                v.getPorcentajeComision(),
                v.getMontoComision(),
                gananciaNeta,
                v.getEstado(),
                v.getFechaAnulacion(),
                c != null ? c.getIdCliente() : null,
                c != null ? c.getNombre() + " " + c.getApellido() : null,
                c != null ? c.getDni() : null,
                e != null ? e.getIdEmpleado() : null,
                e != null ? e.getNombre() + " " + e.getApellido() : null,
                s != null ? s.getCodigoServicio() : null,
                s != null ? s.getNombre() : null,
                p != null ? p.getCodigoPaquete() : null,
                p != null ? p.getNombre() : null,
                s != null ? "SERVICIO" : "PAQUETE"
        );
    }
}
