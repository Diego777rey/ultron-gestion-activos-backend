-- Comercio web: pedidos online y suscripciones al boletín.
CREATE SCHEMA IF NOT EXISTS web;

CREATE TABLE web.pedido_web (
    id_pedido_web       BIGSERIAL PRIMARY KEY,
    codigo              VARCHAR(40) NOT NULL UNIQUE,
    estado              VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    forma_pago          VARCHAR(40) NOT NULL,
    moneda              VARCHAR(10) NOT NULL DEFAULT 'PYG',
    total               NUMERIC(18, 2) NOT NULL,
    cliente_nombre      VARCHAR(150) NOT NULL,
    cliente_email       VARCHAR(180) NOT NULL,
    cliente_telefono    VARCHAR(40),
    cliente_documento   VARCHAR(40),
    direccion_envio     VARCHAR(500),
    ciudad              VARCHAR(120),
    notas               VARCHAR(1000),
    pago_transaccion_id VARCHAR(80),
    pago_ultimos_cuatro VARCHAR(4),
    pago_mensaje        VARCHAR(255),
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pedido_web_email ON web.pedido_web (LOWER(cliente_email));
CREATE INDEX idx_pedido_web_creado ON web.pedido_web (creado_en DESC);

CREATE TABLE web.detalle_pedido_web (
    id_detalle_pedido_web BIGSERIAL PRIMARY KEY,
    id_pedido_web         BIGINT NOT NULL REFERENCES web.pedido_web (id_pedido_web) ON DELETE CASCADE,
    id_producto           BIGINT NOT NULL REFERENCES inventario.producto (id_producto),
    id_presentacion_producto BIGINT REFERENCES inventario.presentacion_producto (id_presentacion_producto),
    nombre_producto       VARCHAR(200) NOT NULL,
    presentacion          VARCHAR(150),
    cantidad              NUMERIC(18, 3) NOT NULL,
    precio_unitario       NUMERIC(18, 2) NOT NULL,
    subtotal              NUMERIC(18, 2) NOT NULL
);

CREATE INDEX idx_detalle_pedido_web_pedido ON web.detalle_pedido_web (id_pedido_web);

CREATE TABLE web.suscripcion_newsletter (
    id_suscripcion  BIGSERIAL PRIMARY KEY,
    email           VARCHAR(180) NOT NULL,
    nombre          VARCHAR(150),
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_suscripcion_newsletter_email
    ON web.suscripcion_newsletter (LOWER(email));

COMMENT ON TABLE web.pedido_web IS 'Pedidos realizados desde la tienda web Red Automotriz';
COMMENT ON TABLE web.suscripcion_newsletter IS 'Suscriptores al boletín de ofertas de la tienda web';
