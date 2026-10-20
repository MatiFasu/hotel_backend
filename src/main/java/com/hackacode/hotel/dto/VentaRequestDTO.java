package com.hackacode.hotel.dto;

import com.hackacode.hotel.model.MedioPago;
import jakarta.validation.constraints.NotNull;

public class VentaRequestDTO {

    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;

    @NotNull(message = "El empleado vendedor es obligatorio")
    private Long empleadoId;

    @NotNull(message = "El medio de pago es obligatorio")
    private MedioPago medioPago;

    private Long servicioId;

    private Long paqueteId;

    public VentaRequestDTO() {
    }

    public VentaRequestDTO(Long clienteId, Long empleadoId, MedioPago medioPago, Long servicioId, Long paqueteId) {
        this.clienteId = clienteId;
        this.empleadoId = empleadoId;
        this.medioPago = medioPago;
        this.servicioId = servicioId;
        this.paqueteId = paqueteId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getEmpleadoId() {
        return empleadoId;
    }

    public void setEmpleadoId(Long empleadoId) {
        this.empleadoId = empleadoId;
    }

    public MedioPago getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(MedioPago medioPago) {
        this.medioPago = medioPago;
    }

    public Long getServicioId() {
        return servicioId;
    }

    public void setServicioId(Long servicioId) {
        this.servicioId = servicioId;
    }

    public Long getPaqueteId() {
        return paqueteId;
    }

    public void setPaqueteId(Long paqueteId) {
        this.paqueteId = paqueteId;
    }
}
