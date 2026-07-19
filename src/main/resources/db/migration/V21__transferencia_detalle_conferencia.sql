-- Conferencia por ítem: aceptar / rechazar con motivo antes de conferir la cabecera

ALTER TABLE operaciones.transferencia_detalle
    ADD COLUMN IF NOT EXISTS estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    ADD COLUMN IF NOT EXISTS motivo_rechazo VARCHAR(30),
    ADD COLUMN IF NOT EXISTS motivo_rechazo_detalle VARCHAR(255);

-- Detalles de transferencias ya conferidas o recepcionadas quedan como verificados
UPDATE operaciones.transferencia_detalle d
SET estado = 'VERIFICADO'
FROM operaciones.transferencia t
WHERE d.id_transferencia = t.id_transferencia
  AND UPPER(t.estado) IN ('CONFERIDO', 'RECEPCIONADO')
  AND UPPER(COALESCE(d.estado, 'PENDIENTE')) = 'PENDIENTE';
