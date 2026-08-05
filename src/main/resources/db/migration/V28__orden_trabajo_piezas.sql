-- Separar orden_trabajo en piezas 1:1: recepción, estado vehículo, diagnóstico

-- ======================================================================
-- 1. Crear tablas hijas
-- ======================================================================

CREATE TABLE IF NOT EXISTS taller.orden_recepcion (
    id_orden_trabajo    BIGINT PRIMARY KEY
        REFERENCES taller.orden_trabajo (id_orden_trabajo) ON DELETE CASCADE,
    descripcion_falla   TEXT
);

CREATE TABLE IF NOT EXISTS taller.orden_estado_vehiculo (
    id_orden_trabajo        BIGINT PRIMARY KEY
        REFERENCES taller.orden_trabajo (id_orden_trabajo) ON DELETE CASCADE,
    falla_mecanica          BOOLEAN NOT NULL DEFAULT FALSE,
    falla_electrica         BOOLEAN NOT NULL DEFAULT FALSE,
    estado_llantas          BOOLEAN NOT NULL DEFAULT FALSE,
    estado_pintura          BOOLEAN NOT NULL DEFAULT FALSE,
    estado_rayones          BOOLEAN NOT NULL DEFAULT FALSE,
    estado_golpes           BOOLEAN NOT NULL DEFAULT FALSE,
    estado_vidrios          BOOLEAN NOT NULL DEFAULT FALSE,
    nivel_combustible       VARCHAR(20),
    kilometraje             INTEGER,
    observaciones_estado    TEXT
);

CREATE TABLE IF NOT EXISTS taller.orden_diagnostico (
    id_orden_trabajo        BIGINT PRIMARY KEY
        REFERENCES taller.orden_trabajo (id_orden_trabajo) ON DELETE CASCADE,
    fecha_inicio_estimada   TIMESTAMP,
    fecha_fin_estimada      TIMESTAMP,
    presupuesto_aprobado    BOOLEAN NOT NULL DEFAULT FALSE,
    total_presupuesto       NUMERIC(15, 2) NOT NULL DEFAULT 0,
    observaciones           TEXT
);

-- ======================================================================
-- 2. Migrar datos existentes
-- ======================================================================

INSERT INTO taller.orden_recepcion (id_orden_trabajo, descripcion_falla)
SELECT id_orden_trabajo, descripcion_falla
FROM taller.orden_trabajo
WHERE descripcion_falla IS NOT NULL
ON CONFLICT (id_orden_trabajo) DO NOTHING;

INSERT INTO taller.orden_estado_vehiculo (
    id_orden_trabajo,
    falla_mecanica, falla_electrica,
    estado_llantas, estado_pintura, estado_rayones, estado_golpes, estado_vidrios,
    nivel_combustible, kilometraje, observaciones_estado
)
SELECT
    id_orden_trabajo,
    COALESCE(falla_mecanica, FALSE),
    COALESCE(falla_electrica, FALSE),
    COALESCE(estado_llantas, FALSE),
    COALESCE(estado_pintura, FALSE),
    COALESCE(estado_rayones, FALSE),
    COALESCE(estado_golpes, FALSE),
    COALESCE(estado_vidrios, FALSE),
    nivel_combustible,
    kilometraje,
    observaciones_estado
FROM taller.orden_trabajo
WHERE COALESCE(falla_mecanica, FALSE)
   OR COALESCE(falla_electrica, FALSE)
   OR COALESCE(estado_llantas, FALSE)
   OR COALESCE(estado_pintura, FALSE)
   OR COALESCE(estado_rayones, FALSE)
   OR COALESCE(estado_golpes, FALSE)
   OR COALESCE(estado_vidrios, FALSE)
   OR nivel_combustible IS NOT NULL
   OR kilometraje IS NOT NULL
   OR observaciones_estado IS NOT NULL
ON CONFLICT (id_orden_trabajo) DO NOTHING;

INSERT INTO taller.orden_diagnostico (
    id_orden_trabajo,
    fecha_inicio_estimada, fecha_fin_estimada,
    presupuesto_aprobado, total_presupuesto, observaciones
)
SELECT
    id_orden_trabajo,
    fecha_inicio_estimada,
    fecha_fin_estimada,
    COALESCE(presupuesto_aprobado, FALSE),
    COALESCE(total_presupuesto, 0),
    observaciones
FROM taller.orden_trabajo
WHERE fecha_inicio_estimada IS NOT NULL
   OR fecha_fin_estimada IS NOT NULL
   OR COALESCE(presupuesto_aprobado, FALSE)
   OR COALESCE(total_presupuesto, 0) <> 0
   OR observaciones IS NOT NULL
ON CONFLICT (id_orden_trabajo) DO NOTHING;

-- También crear fila de diagnóstico vacía si hay detalles (para total_presupuesto)
INSERT INTO taller.orden_diagnostico (
    id_orden_trabajo, presupuesto_aprobado, total_presupuesto
)
SELECT ot.id_orden_trabajo, COALESCE(ot.presupuesto_aprobado, FALSE), COALESCE(ot.total_presupuesto, 0)
FROM taller.orden_trabajo ot
WHERE EXISTS (
    SELECT 1 FROM taller.orden_trabajo_detalle d WHERE d.id_orden_trabajo = ot.id_orden_trabajo
)
ON CONFLICT (id_orden_trabajo) DO UPDATE
SET total_presupuesto = EXCLUDED.total_presupuesto,
    presupuesto_aprobado = EXCLUDED.presupuesto_aprobado;

-- ======================================================================
-- 3. Quitar columnas movidas de orden_trabajo
-- ======================================================================

ALTER TABLE taller.orden_trabajo
    DROP COLUMN IF EXISTS descripcion_falla,
    DROP COLUMN IF EXISTS falla_mecanica,
    DROP COLUMN IF EXISTS falla_electrica,
    DROP COLUMN IF EXISTS estado_llantas,
    DROP COLUMN IF EXISTS estado_pintura,
    DROP COLUMN IF EXISTS estado_rayones,
    DROP COLUMN IF EXISTS estado_golpes,
    DROP COLUMN IF EXISTS estado_vidrios,
    DROP COLUMN IF EXISTS nivel_combustible,
    DROP COLUMN IF EXISTS kilometraje,
    DROP COLUMN IF EXISTS observaciones_estado,
    DROP COLUMN IF EXISTS fecha_inicio_estimada,
    DROP COLUMN IF EXISTS fecha_fin_estimada,
    DROP COLUMN IF EXISTS presupuesto_aprobado,
    DROP COLUMN IF EXISTS total_presupuesto,
    DROP COLUMN IF EXISTS observaciones;
