-- =============================================================================
-- ESQUEMA INICIAL DE BASE DE DATOS: AGENCIA DE TURISMO
-- ARCHIVO: V1__init_schema.sql
-- =============================================================================

-- 1. TABLA CLIENTES
CREATE TABLE clientes (
    id_cliente BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    dni VARCHAR(20) NOT NULL,
    fecha_nac DATE NOT NULL,
    nacionalidad VARCHAR(50) NOT NULL,
    celular VARCHAR(30) NOT NULL,
    email VARCHAR(100) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_clientes_dni UNIQUE (dni)
);

-- 2. TABLA EMPLEADOS
CREATE TABLE empleados (
    id_empleado BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    dni VARCHAR(20) NOT NULL,
    fecha_nac DATE NOT NULL,
    nacionalidad VARCHAR(50) NOT NULL,
    celular VARCHAR(30) NOT NULL,
    email VARCHAR(100) NOT NULL,
    cargo VARCHAR(100) NOT NULL,
    sueldo NUMERIC(12, 2) NOT NULL,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    rol VARCHAR(20) NOT NULL DEFAULT 'VENDEDOR',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_empleados_dni UNIQUE (dni),
    CONSTRAINT uq_empleados_username UNIQUE (username),
    CONSTRAINT chk_empleados_sueldo CHECK (sueldo > 0),
    CONSTRAINT chk_empleados_rol CHECK (rol IN ('ADMIN', 'VENDEDOR'))
);

-- 3. TABLA SERVICIOS TURISTICOS
CREATE TABLE servicios_turisticos (
    codigo_servicio BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion_breve VARCHAR(255),
    destino_servicio VARCHAR(100) NOT NULL,
    fecha_servicio DATE NOT NULL,
    costo_servicio NUMERIC(12, 2) NOT NULL,
    tipo_servicio VARCHAR(30) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_servicios_costo CHECK (costo_servicio > 0),
    CONSTRAINT chk_servicios_tipo CHECK (tipo_servicio IN (
        'HOTEL', 
        'ALQUILER_AUTO', 
        'PASAJE_COLECTIVO', 
        'PASAJE_AVION', 
        'PASAJE_TREN', 
        'EXCURSION', 
        'ENTRADA_EVENTO'
    ))
);

-- 4. TABLA PAQUETES TURISTICOS
CREATE TABLE paquetes_turisticos (
    codigo_paquete BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0
);

-- 5. TABLA INTERMEDIA PAQUETE - SERVICIO (N:M)
CREATE TABLE paquete_servicio (
    codigo_paquete BIGINT NOT NULL,
    codigo_servicio BIGINT NOT NULL,
    PRIMARY KEY (codigo_paquete, codigo_servicio),
    CONSTRAINT fk_paquete_servicio_paquete 
        FOREIGN KEY (codigo_paquete) REFERENCES paquetes_turisticos(codigo_paquete) ON DELETE RESTRICT,
    CONSTRAINT fk_paquete_servicio_servicio 
        FOREIGN KEY (codigo_servicio) REFERENCES servicios_turisticos(codigo_servicio) ON DELETE RESTRICT
);

-- 6. TABLA VENTAS (SNAPSHOT FINANCIERO, COMISIONES Y RESTRICCIONES)
CREATE TABLE ventas (
    num_venta BIGSERIAL PRIMARY KEY,
    fecha_venta TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    medio_pago VARCHAR(30) NOT NULL,
    monto_total NUMERIC(12, 2) NOT NULL,
    porcentaje_comision NUMERIC(5, 2) NOT NULL,
    monto_comision NUMERIC(12, 2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    fecha_anulacion TIMESTAMP WITHOUT TIME ZONE,
    cliente_id BIGINT NOT NULL,
    empleado_id BIGINT NOT NULL,
    servicio_id BIGINT,
    paquete_id BIGINT,
    CONSTRAINT fk_ventas_cliente 
        FOREIGN KEY (cliente_id) REFERENCES clientes(id_cliente) ON DELETE RESTRICT,
    CONSTRAINT fk_ventas_empleado 
        FOREIGN KEY (empleado_id) REFERENCES empleados(id_empleado) ON DELETE RESTRICT,
    CONSTRAINT fk_ventas_servicio 
        FOREIGN KEY (servicio_id) REFERENCES servicios_turisticos(codigo_servicio) ON DELETE RESTRICT,
    CONSTRAINT fk_ventas_paquete 
        FOREIGN KEY (paquete_id) REFERENCES paquetes_turisticos(codigo_paquete) ON DELETE RESTRICT,
    -- Exclusión mutua física: exactamente un producto por venta
    CONSTRAINT chk_venta_exclusion_mutua 
        CHECK ((servicio_id IS NOT NULL AND paquete_id IS NULL) OR 
               (servicio_id IS NULL AND paquete_id IS NOT NULL)),
    -- Validaciones cuantitativas
    CONSTRAINT chk_venta_monto_total 
        CHECK (monto_total > 0),
    CONSTRAINT chk_venta_porcentaje_comision 
        CHECK (porcentaje_comision >= 0.00 AND porcentaje_comision <= 100.00),
    CONSTRAINT chk_venta_monto_comision 
        CHECK (monto_comision >= 0.00),
    -- Coherencia de enums y estados
    CONSTRAINT chk_venta_medio_pago 
        CHECK (medio_pago IN ('EFECTIVO', 'TARJETA_DEBITO', 'TARJETA_CREDITO', 'MONEDERO_VIRTUAL', 'TRANSFERENCIA')),
    CONSTRAINT chk_venta_estado 
        CHECK (estado IN ('ACTIVA', 'ANULADA')),
    CONSTRAINT chk_venta_coherencia_anulacion 
        CHECK ((estado = 'ACTIVA' AND fecha_anulacion IS NULL) OR 
               (estado = 'ANULADA' AND fecha_anulacion IS NOT NULL))
);

-- =============================================================================
-- INDICES ESTRATEGICOS
-- =============================================================================

CREATE INDEX idx_ventas_paquete ON ventas(paquete_id);
CREATE INDEX idx_ventas_servicio ON ventas(servicio_id);
CREATE INDEX idx_ventas_cliente ON ventas(cliente_id);
CREATE INDEX idx_ventas_empleado ON ventas(empleado_id);
CREATE INDEX idx_ventas_fecha ON ventas(fecha_venta);
CREATE INDEX idx_ventas_estado ON ventas(estado);
CREATE INDEX idx_servicios_fecha ON servicios_turisticos(fecha_servicio);
CREATE INDEX idx_paquete_servicio_servicio ON paquete_servicio(codigo_servicio);
