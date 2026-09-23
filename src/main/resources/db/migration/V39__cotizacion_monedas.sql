CREATE TABLE financiero.cotizacion (
    id_cotizacion       BIGSERIAL PRIMARY KEY,
    moneda              VARCHAR(20) NOT NULL,
    valor               NUMERIC(15, 4) NOT NULL,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activa              BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_cotizacion_moneda ON financiero.cotizacion(moneda);
CREATE INDEX idx_cotizacion_activa ON financiero.cotizacion(activa);

COMMENT ON TABLE financiero.cotizacion IS 'Tabla para almacenar las cotizaciones de monedas (Real, Guaraní, Dólar)';
COMMENT ON COLUMN financiero.cotizacion.moneda IS 'Tipo de moneda: REAL, GUARANI, DOLAR';
COMMENT ON COLUMN financiero.cotizacion.valor IS 'Valor de la cotización';
COMMENT ON COLUMN financiero.cotizacion.fecha_actualizacion IS 'Fecha y hora de la última actualización';
COMMENT ON COLUMN financiero.cotizacion.activa IS 'Indica si la cotización está activa';
