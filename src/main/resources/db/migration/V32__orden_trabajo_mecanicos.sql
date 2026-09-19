-- Varios mecánicos por orden, y mecánico responsable de cada servicio.

CREATE TABLE IF NOT EXISTS taller.orden_trabajo_mecanico (
    id_orden_trabajo   BIGINT NOT NULL REFERENCES taller.orden_trabajo (id_orden_trabajo) ON DELETE CASCADE,
    id_mecanico        BIGINT NOT NULL REFERENCES personas.funcionario (id_funcionario),
    orden_asignacion   INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (id_orden_trabajo, orden_asignacion),
    CONSTRAINT uk_ot_mecanico UNIQUE (id_orden_trabajo, id_mecanico)
);

CREATE INDEX IF NOT EXISTS idx_ot_mecanico_asignado
    ON taller.orden_trabajo_mecanico (id_mecanico);

INSERT INTO taller.orden_trabajo_mecanico (id_orden_trabajo, id_mecanico, orden_asignacion)
SELECT ot.id_orden_trabajo, ot.id_mecanico, 0
FROM taller.orden_trabajo ot
WHERE ot.id_mecanico IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM taller.orden_trabajo_mecanico m
      WHERE m.id_orden_trabajo = ot.id_orden_trabajo
        AND m.id_mecanico = ot.id_mecanico
  );

ALTER TABLE taller.orden_trabajo_detalle
    ADD COLUMN IF NOT EXISTS id_mecanico BIGINT REFERENCES personas.funcionario (id_funcionario);

CREATE INDEX IF NOT EXISTS idx_ot_detalle_mecanico
    ON taller.orden_trabajo_detalle (id_mecanico);

UPDATE taller.orden_trabajo_detalle d
SET id_mecanico = ot.id_mecanico
FROM taller.orden_trabajo ot
WHERE d.id_orden_trabajo = ot.id_orden_trabajo
  AND d.tipo = 'SERVICIO'
  AND d.id_mecanico IS NULL
  AND ot.id_mecanico IS NOT NULL;
