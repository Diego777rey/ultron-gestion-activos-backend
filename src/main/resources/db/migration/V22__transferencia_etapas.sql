-- Etapas de transferencia:
-- CREACION -> PENDIENTE_CONFERIR -> CONFERIDO -> RECEPCIONADO

ALTER TABLE operaciones.transferencia
    ALTER COLUMN estado SET DEFAULT 'CREACION';

-- Transferencias que aún estaban en el estado legado PENDIENTE pasan a CREACION
UPDATE operaciones.transferencia
SET estado = 'CREACION'
WHERE UPPER(estado) = 'PENDIENTE';
