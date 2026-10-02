-- ============================================================================
-- Migración V49: Módulo de Facturación en Papel
-- ============================================================================
-- Tablas de timbrado, factura y detalle. Idempotente: se puede volver a
-- ejecutar sin fallar si los objetos ya existen.
-- Las columnas de contacto de personas.empresa las agrega V48.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Tabla: financiero.timbrado
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS financiero.timbrado (
    id_timbrado             BIGSERIAL PRIMARY KEY,
    numero_timbrado         VARCHAR(20) NOT NULL,
    establecimiento         VARCHAR(3) NOT NULL,
    punto_expedicion        VARCHAR(3) NOT NULL,
    numero_inicial          INTEGER NOT NULL,
    numero_final            INTEGER NOT NULL,
    numero_actual           INTEGER NOT NULL,
    fecha_inicio_vigencia   DATE NOT NULL,
    fecha_fin_vigencia      DATE NOT NULL,
    id_empresa              BIGINT NOT NULL,
    tipo_factura            VARCHAR(20) NOT NULL DEFAULT 'PAPEL',
    activo                  BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion          TIMESTAMP NOT NULL DEFAULT NOW(),
    id_usuario_creador      BIGINT
);

DO $$
BEGIN
    ALTER TABLE financiero.timbrado
        ADD CONSTRAINT fk_timbrado_empresa
        FOREIGN KEY (id_empresa) REFERENCES personas.empresa (id_empresa);
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE financiero.timbrado
        ADD CONSTRAINT fk_timbrado_usuario_creador
        FOREIGN KEY (id_usuario_creador) REFERENCES personas.usuario (id);
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE financiero.timbrado
        ADD CONSTRAINT chk_timbrado_rango CHECK (numero_final >= numero_inicial);
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE financiero.timbrado
        ADD CONSTRAINT chk_timbrado_actual
        CHECK (numero_actual >= numero_inicial AND numero_actual <= numero_final + 1);
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE financiero.timbrado
        ADD CONSTRAINT chk_timbrado_tipo CHECK (tipo_factura IN ('PAPEL', 'ELECTRONICA'));
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE financiero.timbrado
        ADD CONSTRAINT uq_timbrado_numero UNIQUE (numero_timbrado);
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

CREATE INDEX IF NOT EXISTS idx_timbrado_empresa ON financiero.timbrado (id_empresa);
CREATE INDEX IF NOT EXISTS idx_timbrado_activo ON financiero.timbrado (activo);
CREATE INDEX IF NOT EXISTS idx_timbrado_vigencia ON financiero.timbrado (fecha_inicio_vigencia, fecha_fin_vigencia);

COMMENT ON TABLE financiero.timbrado IS 'Control de timbrados SET para facturación legal';
COMMENT ON COLUMN financiero.timbrado.numero_timbrado IS 'Número de timbrado autorizado por la SET';
COMMENT ON COLUMN financiero.timbrado.numero_actual IS 'Próximo número de factura a emitir (control interno)';
COMMENT ON COLUMN financiero.timbrado.establecimiento IS 'Código de establecimiento (001, 002, etc)';
COMMENT ON COLUMN financiero.timbrado.punto_expedicion IS 'Código de punto de expedición (001, 002, etc)';

-- ----------------------------------------------------------------------------
-- Tabla: financiero.factura
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS financiero.factura (
    id_factura              BIGSERIAL PRIMARY KEY,
    numero_factura          VARCHAR(20) NOT NULL,
    timbrado                VARCHAR(20) NOT NULL,
    fecha_emision           TIMESTAMP NOT NULL DEFAULT NOW(),
    id_timbrado             BIGINT NOT NULL,
    id_cliente              BIGINT,
    id_venta                BIGINT,
    id_sesion_caja          BIGINT,
    id_empresa              BIGINT NOT NULL,
    cliente_nombre          VARCHAR(255),
    cliente_documento       VARCHAR(20),
    cliente_ruc             VARCHAR(20),
    cliente_direccion       VARCHAR(500),
    subtotal                NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_iva_5             NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_iva_10            NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_exenta            NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_iva               NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total                   NUMERIC(19, 2) NOT NULL DEFAULT 0,
    forma_pago              VARCHAR(50) NOT NULL,
    moneda                  VARCHAR(3) NOT NULL DEFAULT 'PYG',
    estado                  VARCHAR(20) NOT NULL DEFAULT 'EMITIDA',
    fecha_anulacion         TIMESTAMP,
    motivo_anulacion        TEXT,
    id_usuario_anulacion    BIGINT,
    observaciones           TEXT,
    id_usuario_emisor       BIGINT NOT NULL,
    fecha_creacion          TIMESTAMP NOT NULL DEFAULT NOW()
);

DO $$
BEGIN
    ALTER TABLE financiero.factura
        ADD CONSTRAINT fk_factura_timbrado
        FOREIGN KEY (id_timbrado) REFERENCES financiero.timbrado (id_timbrado);
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE financiero.factura
        ADD CONSTRAINT fk_factura_cliente
        FOREIGN KEY (id_cliente) REFERENCES personas.cliente (id_cliente);
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE financiero.factura
        ADD CONSTRAINT fk_factura_venta
        FOREIGN KEY (id_venta) REFERENCES financiero.venta (id_venta);
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE financiero.factura
        ADD CONSTRAINT fk_factura_sesion_caja
        FOREIGN KEY (id_sesion_caja) REFERENCES financiero.sesion_caja (id_sesion_caja);
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE financiero.factura
        ADD CONSTRAINT fk_factura_empresa
        FOREIGN KEY (id_empresa) REFERENCES personas.empresa (id_empresa);
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE financiero.factura
        ADD CONSTRAINT fk_factura_usuario_anulacion
        FOREIGN KEY (id_usuario_anulacion) REFERENCES personas.usuario (id);
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE financiero.factura
        ADD CONSTRAINT fk_factura_usuario_emisor
        FOREIGN KEY (id_usuario_emisor) REFERENCES personas.usuario (id);
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE financiero.factura
        ADD CONSTRAINT uq_factura_numero_timbrado UNIQUE (numero_factura, timbrado);
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE financiero.factura
        ADD CONSTRAINT chk_factura_estado CHECK (estado IN ('EMITIDA', 'ANULADA'));
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE financiero.factura
        ADD CONSTRAINT chk_factura_forma_pago
        CHECK (forma_pago IN ('EFECTIVO', 'TARJETA', 'CHEQUE', 'TRANSFERENCIA', 'QR', 'CREDITO'));
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE financiero.factura
        ADD CONSTRAINT chk_factura_moneda
        CHECK (moneda IN ('PYG', 'USD', 'BRL', 'EUR', 'ARS'));
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

CREATE INDEX IF NOT EXISTS idx_factura_numero ON financiero.factura (numero_factura);
CREATE INDEX IF NOT EXISTS idx_factura_timbrado ON financiero.factura (timbrado);
CREATE INDEX IF NOT EXISTS idx_factura_fecha ON financiero.factura (fecha_emision);
CREATE INDEX IF NOT EXISTS idx_factura_cliente ON financiero.factura (id_cliente);
CREATE INDEX IF NOT EXISTS idx_factura_estado ON financiero.factura (estado);
CREATE INDEX IF NOT EXISTS idx_factura_empresa ON financiero.factura (id_empresa);
CREATE INDEX IF NOT EXISTS idx_factura_venta ON financiero.factura (id_venta);

COMMENT ON TABLE financiero.factura IS 'Facturas legales emitidas (papel y electrónicas)';
COMMENT ON COLUMN financiero.factura.numero_factura IS 'Número de factura formato XXX-XXX-XXXXXXX';
COMMENT ON COLUMN financiero.factura.timbrado IS 'Número de timbrado SET (desnormalizado para consultas)';
COMMENT ON COLUMN financiero.factura.cliente_nombre IS 'Snapshot del nombre del cliente al momento de emisión';
COMMENT ON COLUMN financiero.factura.cliente_documento IS 'Snapshot del documento del cliente al momento de emisión';

-- ----------------------------------------------------------------------------
-- Tabla: financiero.detalle_factura
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS financiero.detalle_factura (
    id_detalle_factura      BIGSERIAL PRIMARY KEY,
    id_factura              BIGINT NOT NULL,
    id_producto             BIGINT,
    id_servicio             BIGINT,
    id_presentacion         BIGINT,
    descripcion             VARCHAR(500) NOT NULL,
    codigo                  VARCHAR(50),
    cantidad                NUMERIC(15, 2) NOT NULL,
    precio_unitario         NUMERIC(19, 2) NOT NULL,
    subtotal                NUMERIC(19, 2) NOT NULL,
    tipo_iva                VARCHAR(10) NOT NULL,
    monto_iva               NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_linea             NUMERIC(19, 2) NOT NULL,
    numero_linea            INTEGER NOT NULL
);

DO $$
BEGIN
    ALTER TABLE financiero.detalle_factura
        ADD CONSTRAINT fk_detalle_factura_factura
        FOREIGN KEY (id_factura) REFERENCES financiero.factura (id_factura) ON DELETE CASCADE;
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE financiero.detalle_factura
        ADD CONSTRAINT fk_detalle_factura_producto
        FOREIGN KEY (id_producto) REFERENCES inventario.producto (id_producto);
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE financiero.detalle_factura
        ADD CONSTRAINT fk_detalle_factura_servicio
        FOREIGN KEY (id_servicio) REFERENCES inventario.servicio (id_servicio);
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE financiero.detalle_factura
        ADD CONSTRAINT fk_detalle_factura_presentacion
        FOREIGN KEY (id_presentacion) REFERENCES inventario.presentacion_producto (id_presentacion_producto);
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE financiero.detalle_factura
        ADD CONSTRAINT chk_detalle_tipo_iva CHECK (tipo_iva IN ('EXENTA', '5', '10'));
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE financiero.detalle_factura
        ADD CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0);
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

DO $$
BEGIN
    ALTER TABLE financiero.detalle_factura
        ADD CONSTRAINT chk_detalle_precio CHECK (precio_unitario >= 0);
EXCEPTION
    WHEN duplicate_object OR duplicate_table THEN NULL;
END $$;

CREATE INDEX IF NOT EXISTS idx_detalle_factura_factura ON financiero.detalle_factura (id_factura);
CREATE INDEX IF NOT EXISTS idx_detalle_factura_producto ON financiero.detalle_factura (id_producto);
CREATE INDEX IF NOT EXISTS idx_detalle_factura_servicio ON financiero.detalle_factura (id_servicio);

COMMENT ON TABLE financiero.detalle_factura IS 'Detalle de líneas de cada factura';
COMMENT ON COLUMN financiero.detalle_factura.tipo_iva IS 'Tipo de IVA: EXENTA, 5, 10';
COMMENT ON COLUMN financiero.detalle_factura.descripcion IS 'Snapshot de la descripción del producto/servicio';
