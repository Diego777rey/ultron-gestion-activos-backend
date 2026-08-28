-- Distingue hallazgos del diagnóstico de defectos descubiertos en En Proceso

ALTER TABLE taller.orden_diagnostico_hallazgo
    ADD COLUMN IF NOT EXISTS etapa_origen VARCHAR(30) NOT NULL DEFAULT 'DIAGNOSTICO';

UPDATE taller.orden_diagnostico_hallazgo
SET etapa_origen = 'DIAGNOSTICO'
WHERE etapa_origen IS NULL OR etapa_origen = '';

COMMENT ON COLUMN taller.orden_diagnostico_hallazgo.etapa_origen
    IS 'Etapa en la que se registró el hallazgo (DIAGNOSTICO o EN_PROCESO)';
