-- =========================================================
-- V58: Varios equipos por orden de trabajo
-- =========================================================
-- Una orden que recepciona EQUIPO puede recibir varios (por ejemplo la ECU,
-- el tablero y la llave de un mismo vehículo). Reemplaza a orden_trabajo.id_equipo.

CREATE TABLE IF NOT EXISTS taller.orden_trabajo_equipo (
    id_orden_trabajo   BIGINT NOT NULL REFERENCES taller.orden_trabajo (id_orden_trabajo) ON DELETE CASCADE,
    id_equipo          BIGINT NOT NULL REFERENCES patrimonio.equipo (id_equipo),
    orden_recepcion    INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (id_orden_trabajo, orden_recepcion),
    -- Diferida: al quitar un equipo de la lista, Hibernate reindexa las filas siguientes.
    CONSTRAINT uk_ot_equipo UNIQUE (id_orden_trabajo, id_equipo) DEFERRABLE INITIALLY DEFERRED
);

CREATE INDEX IF NOT EXISTS idx_ot_equipo_recepcionado
    ON taller.orden_trabajo_equipo (id_equipo);

INSERT INTO taller.orden_trabajo_equipo (id_orden_trabajo, id_equipo, orden_recepcion)
SELECT ot.id_orden_trabajo, ot.id_equipo, 0
FROM taller.orden_trabajo ot
WHERE ot.id_equipo IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM taller.orden_trabajo_equipo e
      WHERE e.id_orden_trabajo = ot.id_orden_trabajo
  );

DROP INDEX IF EXISTS taller.idx_ot_equipo;

ALTER TABLE taller.orden_trabajo
    DROP COLUMN IF EXISTS id_equipo;

COMMENT ON TABLE taller.orden_trabajo_equipo IS 'Equipos recepcionados en la orden de trabajo, en el orden en que se cargaron';
