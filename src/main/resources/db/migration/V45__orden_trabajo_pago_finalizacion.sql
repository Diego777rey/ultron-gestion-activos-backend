-- Agregar campos de pago y observaciones en la finalización de orden de trabajo
ALTER TABLE taller.orden_trabajo
    ADD COLUMN monto_pago NUMERIC(15,2),
    ADD COLUMN observaciones_finalizacion VARCHAR(1000);

COMMENT ON COLUMN taller.orden_trabajo.monto_pago IS 'Monto cobrado/pagado al finalizar la orden';
COMMENT ON COLUMN taller.orden_trabajo.observaciones_finalizacion IS 'Observaciones adicionales al finalizar la orden';
