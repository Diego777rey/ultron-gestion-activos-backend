-- Estado inicial del vehículo en recepción de orden de trabajo

ALTER TABLE taller.orden_trabajo
    ADD COLUMN IF NOT EXISTS falla_mecanica BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS falla_electrica BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS estado_llantas BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS estado_pintura BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS estado_rayones BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS estado_golpes BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS estado_vidrios BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS nivel_combustible VARCHAR(20),
    ADD COLUMN IF NOT EXISTS kilometraje INTEGER,
    ADD COLUMN IF NOT EXISTS observaciones_estado TEXT;

COMMENT ON COLUMN taller.orden_trabajo.falla_mecanica IS 'Tipo de falla: mecánica';
COMMENT ON COLUMN taller.orden_trabajo.falla_electrica IS 'Tipo de falla: eléctrica';
COMMENT ON COLUMN taller.orden_trabajo.estado_llantas IS 'Observación: llantas en mal estado / dañadas';
COMMENT ON COLUMN taller.orden_trabajo.estado_pintura IS 'Observación: pintura dañada / manchada';
COMMENT ON COLUMN taller.orden_trabajo.estado_rayones IS 'Observación: rayones visibles';
COMMENT ON COLUMN taller.orden_trabajo.estado_golpes IS 'Observación: golpes / abolladuras';
COMMENT ON COLUMN taller.orden_trabajo.estado_vidrios IS 'Observación: vidrios dañados';
COMMENT ON COLUMN taller.orden_trabajo.nivel_combustible IS 'Nivel de combustible al ingreso: VACIO, CUARTO, MEDIO, TRES_CUARTOS, LLENO';
COMMENT ON COLUMN taller.orden_trabajo.kilometraje IS 'Kilometraje reportado al ingreso';
COMMENT ON COLUMN taller.orden_trabajo.observaciones_estado IS 'Notas libres del estado inicial del vehículo';
