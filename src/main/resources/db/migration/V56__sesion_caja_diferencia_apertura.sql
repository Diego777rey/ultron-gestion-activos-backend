-- La diferencia de una sesión compara su conteo de apertura con el último cierre del mismo maletín:
-- si el cajero abre con un monto distinto al que se dejó al cerrar, esa es la diferencia.
-- Antes se calculaba al cerrar como cierre - (apertura + ventas); se recalcula todo el historial.

ALTER TABLE financiero.sesion_caja
    ADD COLUMN id_sesion_anterior BIGINT REFERENCES financiero.sesion_caja (id_sesion_caja);

UPDATE financiero.sesion_caja s
SET id_sesion_anterior = (
    SELECT p.id_sesion_caja
    FROM financiero.sesion_caja p
    WHERE p.id_maletin = s.id_maletin
      AND p.estado = 'CERRADA'
      AND p.id_sesion_caja <> s.id_sesion_caja
      AND p.fecha_cierre <= s.fecha_apertura
    ORDER BY p.fecha_cierre DESC, p.id_sesion_caja DESC
    LIMIT 1
);

UPDATE financiero.sesion_caja s
SET diferencia_pyg = s.monto_inicial_pyg - COALESCE(p.monto_final_pyg, 0),
    diferencia_usd = s.monto_inicial_usd - COALESCE(p.monto_final_usd, 0),
    diferencia_brl = s.monto_inicial_brl - COALESCE(p.monto_final_brl, 0)
FROM financiero.sesion_caja p
WHERE p.id_sesion_caja = s.id_sesion_anterior;

UPDATE financiero.sesion_caja
SET diferencia_pyg = 0,
    diferencia_usd = 0,
    diferencia_brl = 0
WHERE id_sesion_anterior IS NULL;
