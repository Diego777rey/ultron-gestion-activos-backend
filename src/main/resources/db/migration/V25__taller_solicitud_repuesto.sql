-- Solicitud de repuesto vinculada a Orden de Trabajo

CREATE TABLE IF NOT EXISTS taller.solicitud_repuesto (
    id_solicitud_repuesto   BIGSERIAL PRIMARY KEY,
    id_orden_trabajo        BIGINT NOT NULL REFERENCES taller.orden_trabajo (id_orden_trabajo) ON DELETE CASCADE,
    id_sector_origen        BIGINT NOT NULL REFERENCES sectores.sector (id_sector),
    id_sector_destino       BIGINT NOT NULL REFERENCES sectores.sector (id_sector),
    estado                  VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    observacion             TEXT,
    motivo_rechazo          TEXT,
    fecha                   TIMESTAMP NOT NULL DEFAULT NOW(),
    id_transferencia        BIGINT REFERENCES operaciones.transferencia (id_transferencia),

    CONSTRAINT ck_solicitud_repuesto_estado CHECK (
        estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA')
    ),
    CONSTRAINT ck_solicitud_repuesto_sectores CHECK (
        id_sector_origen <> id_sector_destino
    )
);

CREATE INDEX IF NOT EXISTS idx_solicitud_repuesto_orden
    ON taller.solicitud_repuesto (id_orden_trabajo);
CREATE INDEX IF NOT EXISTS idx_solicitud_repuesto_estado
    ON taller.solicitud_repuesto (estado);

CREATE TABLE IF NOT EXISTS taller.solicitud_repuesto_detalle (
    id_detalle              BIGSERIAL PRIMARY KEY,
    id_solicitud_repuesto   BIGINT NOT NULL REFERENCES taller.solicitud_repuesto (id_solicitud_repuesto) ON DELETE CASCADE,
    id_producto             BIGINT NOT NULL REFERENCES inventario.producto (id_producto),
    cantidad                NUMERIC(15, 2) NOT NULL DEFAULT 1,

    CONSTRAINT ck_solicitud_repuesto_detalle_cantidad CHECK (cantidad > 0)
);

CREATE INDEX IF NOT EXISTS idx_solicitud_repuesto_detalle_solicitud
    ON taller.solicitud_repuesto_detalle (id_solicitud_repuesto);
