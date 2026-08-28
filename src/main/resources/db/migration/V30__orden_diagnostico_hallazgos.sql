-- Diagnóstico: duración estimada + hallazgos (fallos / defectos) del mecánico

ALTER TABLE taller.orden_diagnostico
    ADD COLUMN IF NOT EXISTS duracion_estimada_dias INTEGER;

COMMENT ON COLUMN taller.orden_diagnostico.duracion_estimada_dias
    IS 'Días calendario estimados (inclusivos) para completar el trabajo';

-- Completar duración a partir de fechas ya cargadas
UPDATE taller.orden_diagnostico
SET duracion_estimada_dias = GREATEST(
        1,
        (fecha_fin_estimada::date - fecha_inicio_estimada::date) + 1
    )
WHERE fecha_inicio_estimada IS NOT NULL
  AND fecha_fin_estimada IS NOT NULL
  AND duracion_estimada_dias IS NULL;

CREATE TABLE IF NOT EXISTS taller.orden_diagnostico_hallazgo (
    id_hallazgo         BIGSERIAL PRIMARY KEY,
    id_orden_trabajo    BIGINT NOT NULL
        REFERENCES taller.orden_trabajo (id_orden_trabajo) ON DELETE CASCADE,
    tipo                VARCHAR(20) NOT NULL,
    gravedad            VARCHAR(20) NOT NULL DEFAULT 'MEDIA',
    sistema             VARCHAR(30),
    descripcion         TEXT NOT NULL,

    CONSTRAINT ck_hallazgo_tipo CHECK (tipo IN ('FALLO', 'DEFECTO')),
    CONSTRAINT ck_hallazgo_gravedad CHECK (gravedad IN ('BAJA', 'MEDIA', 'ALTA', 'CRITICA'))
);

CREATE INDEX IF NOT EXISTS idx_hallazgo_orden
    ON taller.orden_diagnostico_hallazgo (id_orden_trabajo);

COMMENT ON TABLE taller.orden_diagnostico_hallazgo
    IS 'Fallos y defectos encontrados por el mecánico durante el diagnóstico';
