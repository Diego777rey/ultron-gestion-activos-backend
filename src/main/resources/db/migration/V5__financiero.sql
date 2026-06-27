CREATE TABLE financiero.caja (
    id_caja         BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(255),
    saldo_actual    NUMERIC(19, 2),
    id_empresa      BIGINT,
    id_responsable  BIGINT REFERENCES personas.persona (id_persona),
    activa          BOOLEAN
);

CREATE TABLE financiero.movimiento_caja (
    id_movimiento  BIGSERIAL PRIMARY KEY,
    id_caja        BIGINT NOT NULL REFERENCES financiero.caja (id_caja),
    tipo           VARCHAR(255),
    monto          NUMERIC(19, 2),
    concepto       VARCHAR(255),
    fecha          TIMESTAMP,
    id_persona     BIGINT REFERENCES personas.persona (id_persona),
    id_autorizador BIGINT REFERENCES personas.persona (id_persona),
    referencia     VARCHAR(255)
);

CREATE TABLE financiero.ingreso (
    id_ingreso       BIGSERIAL PRIMARY KEY,
    id_movimiento    BIGINT UNIQUE REFERENCES financiero.movimiento_caja (id_movimiento),
    descripcion      VARCHAR(255),
    origen           VARCHAR(255),
    cliente_o_fuente VARCHAR(255),
    observaciones    VARCHAR(255)
);

CREATE TABLE financiero.gasto (
    id_gasto       BIGSERIAL PRIMARY KEY,
    id_movimiento  BIGINT UNIQUE REFERENCES financiero.movimiento_caja (id_movimiento),
    categoria      VARCHAR(255),
    proveedor      VARCHAR(255),
    observaciones  VARCHAR(255)
);
