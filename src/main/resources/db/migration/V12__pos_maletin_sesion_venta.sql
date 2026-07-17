-- POS: maletín, sesión de caja, conteos, ventas y extensión de movimientos

CREATE TABLE financiero.maletin (
    id_maletin       BIGSERIAL PRIMARY KEY,
    nombre           VARCHAR(255) NOT NULL,
    estado           VARCHAR(20)  NOT NULL DEFAULT 'CERRADO',
    balance_pyg      NUMERIC(19, 2) NOT NULL DEFAULT 0,
    balance_usd      NUMERIC(19, 2) NOT NULL DEFAULT 0,
    balance_brl      NUMERIC(19, 2) NOT NULL DEFAULT 0,
    id_responsable   BIGINT REFERENCES personas.persona (id_persona),
    activo           BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE financiero.sesion_caja (
    id_sesion_caja     BIGSERIAL PRIMARY KEY,
    id_caja            BIGINT NOT NULL REFERENCES financiero.caja (id_caja),
    id_maletin         BIGINT NOT NULL REFERENCES financiero.maletin (id_maletin),
    id_persona         BIGINT REFERENCES personas.persona (id_persona),
    estado             VARCHAR(20) NOT NULL DEFAULT 'ABIERTA',
    monto_inicial_pyg  NUMERIC(19, 2) NOT NULL DEFAULT 0,
    monto_inicial_usd  NUMERIC(19, 2) NOT NULL DEFAULT 0,
    monto_inicial_brl  NUMERIC(19, 2) NOT NULL DEFAULT 0,
    monto_final_pyg    NUMERIC(19, 2),
    monto_final_usd    NUMERIC(19, 2),
    monto_final_brl    NUMERIC(19, 2),
    diferencia_pyg     NUMERIC(19, 2),
    diferencia_usd     NUMERIC(19, 2),
    diferencia_brl     NUMERIC(19, 2),
    total_ventas_pyg   NUMERIC(19, 2) NOT NULL DEFAULT 0,
    fecha_apertura     TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_cierre       TIMESTAMP
);

CREATE UNIQUE INDEX uq_sesion_caja_abierta
    ON financiero.sesion_caja (id_caja)
    WHERE estado = 'ABIERTA';

CREATE TABLE financiero.conteo_denominacion (
    id_conteo          BIGSERIAL PRIMARY KEY,
    id_sesion_caja     BIGINT NOT NULL REFERENCES financiero.sesion_caja (id_sesion_caja) ON DELETE CASCADE,
    tipo               VARCHAR(20) NOT NULL,
    moneda             VARCHAR(3)  NOT NULL,
    valor_denominacion NUMERIC(19, 2) NOT NULL,
    cantidad           INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE financiero.venta (
    id_venta           BIGSERIAL PRIMARY KEY,
    numero             VARCHAR(50) NOT NULL UNIQUE,
    fecha              TIMESTAMP NOT NULL DEFAULT NOW(),
    id_sesion_caja     BIGINT NOT NULL REFERENCES financiero.sesion_caja (id_sesion_caja),
    id_cliente         BIGINT REFERENCES personas.cliente (id_cliente),
    subtotal           NUMERIC(19, 2) NOT NULL DEFAULT 0,
    descuento          NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total              NUMERIC(19, 2) NOT NULL DEFAULT 0,
    estado             VARCHAR(20) NOT NULL DEFAULT 'PAGADA'
);

CREATE TABLE financiero.detalle_venta (
    id_detalle_venta   BIGSERIAL PRIMARY KEY,
    id_venta           BIGINT NOT NULL REFERENCES financiero.venta (id_venta) ON DELETE CASCADE,
    id_producto        BIGINT NOT NULL REFERENCES inventario.producto (id_producto),
    id_presentacion    BIGINT REFERENCES inventario.presentacion_producto (id_presentacion_producto),
    cantidad           NUMERIC(15, 2) NOT NULL,
    precio_unitario    NUMERIC(19, 2) NOT NULL,
    subtotal           NUMERIC(19, 2) NOT NULL
);

ALTER TABLE financiero.movimiento_caja
    ADD COLUMN moneda VARCHAR(3) DEFAULT 'PYG',
    ADD COLUMN id_maletin BIGINT REFERENCES financiero.maletin (id_maletin),
    ADD COLUMN id_sesion_caja BIGINT REFERENCES financiero.sesion_caja (id_sesion_caja);
