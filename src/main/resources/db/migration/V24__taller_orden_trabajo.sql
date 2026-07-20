-- Módulo de Orden de Trabajo (schema taller)

CREATE SCHEMA IF NOT EXISTS taller;

-- ======================================================================
-- Tabla principal: orden_trabajo
-- ======================================================================
CREATE TABLE IF NOT EXISTS taller.orden_trabajo (
    id_orden_trabajo    BIGSERIAL PRIMARY KEY,
    numero_orden        VARCHAR(50) NOT NULL,
    etapa               VARCHAR(30) NOT NULL DEFAULT 'RECEPCION',

    -- Relaciones con cliente y vehículo
    id_cliente          BIGINT REFERENCES personas.cliente (id_cliente),
    id_vehiculo         BIGINT REFERENCES patrimonio.vehiculo (id_vehiculo),

    -- Mecánico asignado y responsable
    id_mecanico         BIGINT REFERENCES personas.funcionario (id_funcionario),
    id_sector           BIGINT REFERENCES sectores.sector (id_sector),
    id_responsable      BIGINT REFERENCES personas.usuario (id),

    -- Datos de recepción
    descripcion_falla   TEXT,

    -- Tiempos estimados (diagnóstico)
    fecha_inicio_estimada   TIMESTAMP,
    fecha_fin_estimada      TIMESTAMP,

    -- Fechas del ciclo de vida
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_finalizacion  TIMESTAMP,

    -- Presupuesto
    presupuesto_aprobado BOOLEAN NOT NULL DEFAULT FALSE,
    total_presupuesto   NUMERIC(15, 2) NOT NULL DEFAULT 0,

    -- Observaciones generales
    observaciones       TEXT,

    -- Caja asignada para facturación
    id_caja             BIGINT REFERENCES financiero.caja (id_caja),

    CONSTRAINT ck_orden_trabajo_etapa CHECK (
        etapa IN ('RECEPCION', 'DIAGNOSTICO', 'EN_PROCESO', 'FINALIZADA', 'FACTURADO')
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_orden_trabajo_numero ON taller.orden_trabajo (numero_orden);
CREATE INDEX IF NOT EXISTS idx_ot_cliente ON taller.orden_trabajo (id_cliente);
CREATE INDEX IF NOT EXISTS idx_ot_vehiculo ON taller.orden_trabajo (id_vehiculo);
CREATE INDEX IF NOT EXISTS idx_ot_mecanico ON taller.orden_trabajo (id_mecanico);
CREATE INDEX IF NOT EXISTS idx_ot_etapa ON taller.orden_trabajo (etapa);
CREATE INDEX IF NOT EXISTS idx_ot_fecha_creacion ON taller.orden_trabajo (fecha_creacion DESC);

-- ======================================================================
-- Detalle: productos y servicios asociados a la orden
-- ======================================================================
CREATE TABLE IF NOT EXISTS taller.orden_trabajo_detalle (
    id_detalle          BIGSERIAL PRIMARY KEY,
    id_orden_trabajo    BIGINT NOT NULL REFERENCES taller.orden_trabajo (id_orden_trabajo) ON DELETE CASCADE,

    -- Tipo de ítem: PRODUCTO o SERVICIO
    tipo                VARCHAR(20) NOT NULL,
    id_producto         BIGINT REFERENCES inventario.producto (id_producto),
    id_servicio         BIGINT REFERENCES inventario.servicio (id_servicio),

    descripcion         VARCHAR(255),
    cantidad            NUMERIC(15, 2) NOT NULL DEFAULT 1,
    precio_unitario     NUMERIC(15, 2) NOT NULL DEFAULT 0,
    subtotal            NUMERIC(15, 2) NOT NULL DEFAULT 0,

    -- En qué etapa se agregó el ítem
    etapa_origen        VARCHAR(30) NOT NULL DEFAULT 'DIAGNOSTICO',

    CONSTRAINT ck_detalle_tipo CHECK (tipo IN ('PRODUCTO', 'SERVICIO')),
    CONSTRAINT ck_detalle_cantidad_positiva CHECK (cantidad > 0),
    CONSTRAINT ck_detalle_referencia CHECK (
        (tipo = 'PRODUCTO' AND id_producto IS NOT NULL AND id_servicio IS NULL)
        OR (tipo = 'SERVICIO' AND id_servicio IS NOT NULL AND id_producto IS NULL)
    )
);

CREATE INDEX IF NOT EXISTS idx_ot_detalle_orden ON taller.orden_trabajo_detalle (id_orden_trabajo);
CREATE INDEX IF NOT EXISTS idx_ot_detalle_producto ON taller.orden_trabajo_detalle (id_producto) WHERE id_producto IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_ot_detalle_servicio ON taller.orden_trabajo_detalle (id_servicio) WHERE id_servicio IS NOT NULL;

-- ======================================================================
-- Secuencia para numerar órdenes de trabajo (OT-0001, OT-0002, ...)
-- ======================================================================
CREATE SEQUENCE IF NOT EXISTS taller.orden_trabajo_numero_seq START WITH 1 INCREMENT BY 1;
