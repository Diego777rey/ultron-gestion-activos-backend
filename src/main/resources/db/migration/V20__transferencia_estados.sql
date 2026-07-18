-- Flujo profesional: PENDIENTE -> CONFERIDO -> RECEPCIONADO

ALTER TABLE operaciones.transferencia
    ALTER COLUMN estado SET DEFAULT 'PENDIENTE';

UPDATE operaciones.transferencia
SET estado = 'RECEPCIONADO'
WHERE UPPER(estado) = 'COMPLETADA';
