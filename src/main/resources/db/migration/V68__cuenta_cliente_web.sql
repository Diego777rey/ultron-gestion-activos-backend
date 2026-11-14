-- Cuenta de acceso a la tienda web, vinculada a personas.cliente.
CREATE TABLE web.cuenta_cliente (
    id_cuenta_cliente BIGSERIAL PRIMARY KEY,
    id_cliente        BIGINT NOT NULL UNIQUE REFERENCES personas.cliente (id_cliente),
    email             VARCHAR(180) NOT NULL,
    password_hash     VARCHAR(100) NOT NULL,
    activo            BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_cuenta_cliente_email
    ON web.cuenta_cliente (LOWER(email));

CREATE INDEX idx_cuenta_cliente_cliente ON web.cuenta_cliente (id_cliente);

COMMENT ON TABLE web.cuenta_cliente IS
    'Credenciales de clientes de la tienda Red Automotriz (separadas del Usuario interno/ERP)';
