ALTER TABLE taller.orden_estado_vehiculo
    ADD COLUMN IF NOT EXISTS perdida_aceite BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS luces_danadas BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS espejos_danados BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS accesorios_faltantes BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN taller.orden_estado_vehiculo.perdida_aceite
    IS 'Condición al ingreso: presenta pérdida de aceite';
COMMENT ON COLUMN taller.orden_estado_vehiculo.luces_danadas
    IS 'Condición al ingreso: tiene luces dañadas';
COMMENT ON COLUMN taller.orden_estado_vehiculo.espejos_danados
    IS 'Condición al ingreso: tiene espejos dañados';
COMMENT ON COLUMN taller.orden_estado_vehiculo.accesorios_faltantes
    IS 'Condición al ingreso: tiene piezas o accesorios faltantes';
