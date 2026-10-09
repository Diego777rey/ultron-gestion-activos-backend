-- Retiros de efectivo durante la sesión y arqueo al cierre.
-- Arqueo por moneda: esperado = apertura + cobros en efectivo - vueltos - retiros;
-- diferencia de arqueo = contado al cierre - esperado (negativo: faltante, positivo: sobrante).

CREATE TABLE financiero.retiro_caja (
    id_retiro_caja          BIGSERIAL PRIMARY KEY,
    id_sesion_caja          BIGINT         NOT NULL REFERENCES financiero.sesion_caja (id_sesion_caja),
    moneda                  VARCHAR(3)     NOT NULL,
    monto                   NUMERIC(19, 2) NOT NULL CHECK (monto > 0),
    observacion             VARCHAR(500)   NOT NULL,
    id_usuario_responsable  BIGINT         NOT NULL REFERENCES personas.usuario (id),
    id_usuario_registro     BIGINT REFERENCES personas.usuario (id),
    fecha                   TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_retiro_caja_sesion ON financiero.retiro_caja (id_sesion_caja);

ALTER TABLE financiero.sesion_caja
    ADD COLUMN esperado_cierre_pyg    NUMERIC(19, 2),
    ADD COLUMN esperado_cierre_usd    NUMERIC(19, 2),
    ADD COLUMN esperado_cierre_brl    NUMERIC(19, 2),
    ADD COLUMN diferencia_arqueo_pyg  NUMERIC(19, 2),
    ADD COLUMN diferencia_arqueo_usd  NUMERIC(19, 2),
    ADD COLUMN diferencia_arqueo_brl  NUMERIC(19, 2);

-- Historial: todavía no había retiros, así que el esperado sale de apertura, cobros y vueltos.
UPDATE financiero.sesion_caja
SET esperado_cierre_pyg = monto_inicial_pyg,
    esperado_cierre_usd = monto_inicial_usd,
    esperado_cierre_brl = monto_inicial_brl
WHERE estado = 'CERRADA';

WITH efectivo AS (
    SELECT v.id_sesion_caja,
           CASE
               WHEN v.moneda IS NULL OR UPPER(TRIM(v.moneda)) IN ('PYG', 'GS', 'GUARANI', 'GUARANIES', 'GUARANÍ', 'GUARANÍES') THEN 'PYG'
               WHEN UPPER(TRIM(v.moneda)) IN ('USD', 'DOLAR', 'DOLARES', 'DÓLAR', 'DÓLARES') THEN 'USD'
               WHEN UPPER(TRIM(v.moneda)) IN ('BRL', 'REAL', 'REALES') THEN 'BRL'
               ELSE UPPER(TRIM(v.moneda))
           END AS moneda_cobro,
           v.monto_recibido,
           v.total,
           v.monto_moneda_original,
           CASE
               WHEN v.moneda_vuelto IS NULL OR UPPER(TRIM(v.moneda_vuelto)) IN ('PYG', 'GS', 'GUARANI', 'GUARANIES', 'GUARANÍ', 'GUARANÍES') THEN 'PYG'
               WHEN UPPER(TRIM(v.moneda_vuelto)) IN ('USD', 'DOLAR', 'DOLARES', 'DÓLAR', 'DÓLARES') THEN 'USD'
               WHEN UPPER(TRIM(v.moneda_vuelto)) IN ('BRL', 'REAL', 'REALES') THEN 'BRL'
               ELSE UPPER(TRIM(v.moneda_vuelto))
           END AS moneda_vuelto,
           COALESCE(v.vuelto, 0) AS vuelto
    FROM financiero.venta v
    WHERE COALESCE(v.forma_pago, 'EFECTIVO') = 'EFECTIVO'
      AND (v.estado IS NULL OR v.estado <> 'ANULADA')
),
movimientos AS (
    SELECT id_sesion_caja,
           moneda_cobro AS moneda,
           CASE
               WHEN monto_recibido IS NOT NULL THEN monto_recibido
               WHEN moneda_cobro = 'PYG' THEN total
               ELSE COALESCE(monto_moneda_original, 0)
           END AS monto
    FROM efectivo
    UNION ALL
    SELECT id_sesion_caja, moneda_vuelto, -vuelto
    FROM efectivo
),
neto AS (
    SELECT id_sesion_caja,
           COALESCE(SUM(monto) FILTER (WHERE moneda = 'PYG'), 0) AS pyg,
           COALESCE(SUM(monto) FILTER (WHERE moneda = 'USD'), 0) AS usd,
           COALESCE(SUM(monto) FILTER (WHERE moneda = 'BRL'), 0) AS brl
    FROM movimientos
    GROUP BY id_sesion_caja
)
UPDATE financiero.sesion_caja s
SET esperado_cierre_pyg = s.esperado_cierre_pyg + n.pyg,
    esperado_cierre_usd = s.esperado_cierre_usd + n.usd,
    esperado_cierre_brl = s.esperado_cierre_brl + n.brl
FROM neto n
WHERE n.id_sesion_caja = s.id_sesion_caja
  AND s.estado = 'CERRADA';

UPDATE financiero.sesion_caja
SET diferencia_arqueo_pyg = COALESCE(monto_final_pyg, 0) - esperado_cierre_pyg,
    diferencia_arqueo_usd = COALESCE(monto_final_usd, 0) - esperado_cierre_usd,
    diferencia_arqueo_brl = COALESCE(monto_final_brl, 0) - esperado_cierre_brl
WHERE estado = 'CERRADA';
