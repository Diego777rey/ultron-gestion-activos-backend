-- ============================================================================
-- Migración V48: Módulo de Facturación en Papel
-- ============================================================================
-- Crea las tablas necesarias para el sistema de facturación legal en papel
-- Compatible con normativa paraguaya de la SET
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Tabla: financiero.timbrado
-- Descripción: Control de timbrados SET para facturación legal
-- ----------------------------------------------------------------------------
CREATE TABLE financiero.timbrado (
    id_timbrado             BIGSERIAL PRIMARY KEY,
    
    -- Información del timbrado
    numero_timbrado         VARCHAR(20) NOT NULL,
    
    -- Rango autorizado
    establecimiento         VARCHAR(3) NOT NULL,
    punto_expedicion        VARCHAR(3) NOT NULL,
    numero_inicial          INTEGER NOT NULL,
    numero_final            INTEGER NOT NULL,
    numero_actual           INTEGER NOT NULL,
    
    -- Vigencia
    fecha_inicio_vigencia   DATE NOT NULL,
    fecha_fin_vigencia      DATE NOT NULL,
    
    -- Relaciones
    id_empresa              BIGINT NOT NULL REFERENCES personas.empresa(id_empresa),
    
    -- Tipo de factura
    tipo_factura            VARCHAR(20) NOT NULL DEFAULT 'PAPEL',
    
    -- Estado
    activo                  BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Auditoría
    fecha_creacion          TIMESTAMP NOT NULL DEFAULT NOW(),
    id_usuario_creador      BIGINT REFERENCES personas.usuario(id_usuario),
    
    CONSTRAINT chk_timbrado_rango CHECK (numero_final >= numero_inicial),
    CONSTRAINT chk_timbrado_actual CHECK (numero_actual >= numero_inicial AND numero_actual <= numero_final + 1),
    CONSTRAINT chk_timbrado_tipo CHECK (tipo_factura IN ('PAPEL', 'ELECTRONICA')),
    CONSTRAINT uq_timbrado_numero UNIQUE(numero_timbrado)
);

CREATE INDEX idx_timbrado_empresa ON financiero.timbrado(id_empresa);
CREATE INDEX idx_timbrado_activo ON financiero.timbrado(activo);
CREATE INDEX idx_timbrado_vigencia ON financiero.timbrado(fecha_inicio_vigencia, fecha_fin_vigencia);

COMMENT ON TABLE financiero.timbrado IS 'Control de timbrados SET para facturación legal';
COMMENT ON COLUMN financiero.timbrado.numero_timbrado IS 'Número de timbrado autorizado por la SET';
COMMENT ON COLUMN financiero.timbrado.numero_actual IS 'Próximo número de factura a emitir (control interno)';
COMMENT ON COLUMN financiero.timbrado.establecimiento IS 'Código de establecimiento (001, 002, etc)';
COMMENT ON COLUMN financiero.timbrado.punto_expedicion IS 'Código de punto de expedición (001, 002, etc)';

-- ----------------------------------------------------------------------------
-- Tabla: financiero.factura
-- Descripción: Facturas legales emitidas (papel y futuras electrónicas)
-- ----------------------------------------------------------------------------
CREATE TABLE financiero.factura (
    id_factura              BIGSERIAL PRIMARY KEY,
    
    -- Numeración fiscal
    numero_factura          VARCHAR(20) NOT NULL,
    timbrado                VARCHAR(20) NOT NULL,
    fecha_emision           TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- Relaciones
    id_timbrado             BIGINT NOT NULL REFERENCES financiero.timbrado(id_timbrado),
    id_cliente              BIGINT REFERENCES personas.cliente(id_cliente),
    id_venta                BIGINT REFERENCES financiero.venta(id_venta),
    id_sesion_caja          BIGINT REFERENCES financiero.sesion_caja(id_sesion_caja),
    id_empresa              BIGINT NOT NULL REFERENCES personas.empresa(id_empresa),
    
    -- Información del cliente (snapshot para auditoría)
    cliente_nombre          VARCHAR(255),
    cliente_documento       VARCHAR(20),
    cliente_ruc             VARCHAR(20),
    cliente_direccion       VARCHAR(500),
    
    -- Montos
    subtotal                NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_iva_5             NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_iva_10            NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_exenta            NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_iva               NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total                   NUMERIC(19, 2) NOT NULL DEFAULT 0,
    
    -- Forma de pago
    forma_pago              VARCHAR(50) NOT NULL,
    moneda                  VARCHAR(3) NOT NULL DEFAULT 'PYG',
    
    -- Estado y control
    estado                  VARCHAR(20) NOT NULL DEFAULT 'EMITIDA',
    fecha_anulacion         TIMESTAMP,
    motivo_anulacion        TEXT,
    id_usuario_anulacion    BIGINT REFERENCES personas.usuario(id_usuario),
    
    -- Observaciones
    observaciones           TEXT,
    
    -- Auditoría
    id_usuario_emisor       BIGINT NOT NULL REFERENCES personas.usuario(id_usuario),
    fecha_creacion          TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT uq_factura_numero_timbrado UNIQUE(numero_factura, timbrado),
    CONSTRAINT chk_factura_estado CHECK (estado IN ('EMITIDA', 'ANULADA')),
    CONSTRAINT chk_factura_forma_pago CHECK (forma_pago IN ('EFECTIVO', 'TARJETA', 'CHEQUE', 'TRANSFERENCIA', 'QR', 'CREDITO')),
    CONSTRAINT chk_factura_moneda CHECK (moneda IN ('PYG', 'USD', 'BRL', 'EUR', 'ARS'))
);

CREATE INDEX idx_factura_numero ON financiero.factura(numero_factura);
CREATE INDEX idx_factura_timbrado ON financiero.factura(timbrado);
CREATE INDEX idx_factura_fecha ON financiero.factura(fecha_emision);
CREATE INDEX idx_factura_cliente ON financiero.factura(id_cliente);
CREATE INDEX idx_factura_estado ON financiero.factura(estado);
CREATE INDEX idx_factura_empresa ON financiero.factura(id_empresa);
CREATE INDEX idx_factura_venta ON financiero.factura(id_venta);

COMMENT ON TABLE financiero.factura IS 'Facturas legales emitidas (papel y electrónicas)';
COMMENT ON COLUMN financiero.factura.numero_factura IS 'Número de factura formato XXX-XXX-XXXXXXX';
COMMENT ON COLUMN financiero.factura.timbrado IS 'Número de timbrado SET (desnormalizado para consultas)';
COMMENT ON COLUMN financiero.factura.cliente_nombre IS 'Snapshot del nombre del cliente al momento de emisión';
COMMENT ON COLUMN financiero.factura.cliente_documento IS 'Snapshot del documento del cliente al momento de emisión';

-- ----------------------------------------------------------------------------
-- Tabla: financiero.detalle_factura
-- Descripción: Detalle de líneas de cada factura
-- ----------------------------------------------------------------------------
CREATE TABLE financiero.detalle_factura (
    id_detalle_factura      BIGSERIAL PRIMARY KEY,
    
    -- Relación
    id_factura              BIGINT NOT NULL REFERENCES financiero.factura(id_factura) ON DELETE CASCADE,
    
    -- Producto/Servicio (opcional - puede ser línea de texto libre)
    id_producto             BIGINT REFERENCES inventario.producto(id_producto),
    id_servicio             BIGINT REFERENCES inventario.servicio(id_servicio),
    id_presentacion         BIGINT REFERENCES inventario.presentacion_producto(id_presentacion_producto),
    
    -- Descripción (snapshot para auditoría)
    descripcion             VARCHAR(500) NOT NULL,
    codigo                  VARCHAR(50),
    
    -- Cantidades y precios
    cantidad                NUMERIC(15, 2) NOT NULL,
    precio_unitario         NUMERIC(19, 2) NOT NULL,
    subtotal                NUMERIC(19, 2) NOT NULL,
    
    -- IVA
    tipo_iva                VARCHAR(10) NOT NULL,
    monto_iva               NUMERIC(19, 2) NOT NULL DEFAULT 0,
    
    -- Total de línea
    total_linea             NUMERIC(19, 2) NOT NULL,
    
    -- Orden
    numero_linea            INTEGER NOT NULL,
    
    CONSTRAINT chk_detalle_tipo_iva CHECK (tipo_iva IN ('EXENTA', '5', '10')),
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_precio CHECK (precio_unitario >= 0)
);

CREATE INDEX idx_detalle_factura_factura ON financiero.detalle_factura(id_factura);
CREATE INDEX idx_detalle_factura_producto ON financiero.detalle_factura(id_producto);
CREATE INDEX idx_detalle_factura_servicio ON financiero.detalle_factura(id_servicio);

COMMENT ON TABLE financiero.detalle_factura IS 'Detalle de líneas de cada factura';
COMMENT ON COLUMN financiero.detalle_factura.tipo_iva IS 'Tipo de IVA: EXENTA, 5, 10';
COMMENT ON COLUMN financiero.detalle_factura.descripcion IS 'Snapshot de la descripción del producto/servicio';

-- ----------------------------------------------------------------------------
-- Ampliación de tabla: personas.empresa
-- Descripción: Agregar campos necesarios para facturación
-- ----------------------------------------------------------------------------
ALTER TABLE personas.empresa 
    ADD COLUMN IF NOT EXISTS telefono VARCHAR(50),
    ADD COLUMN IF NOT EXISTS email VARCHAR(255),
    ADD COLUMN IF NOT EXISTS actividad_economica VARCHAR(500),
    ADD COLUMN IF NOT EXISTS logo VARCHAR(500),
    ADD COLUMN IF NOT EXISTS activa BOOLEAN DEFAULT TRUE;

COMMENT ON COLUMN personas.empresa.telefono IS 'Teléfono de contacto de la empresa';
COMMENT ON COLUMN personas.empresa.email IS 'Email de contacto de la empresa';
COMMENT ON COLUMN personas.empresa.actividad_economica IS 'Descripción de la actividad económica';
COMMENT ON COLUMN personas.empresa.logo IS 'Ruta relativa del logo de la empresa para facturas';
COMMENT ON COLUMN personas.empresa.activa IS 'Indica si la empresa está activa';

-- ----------------------------------------------------------------------------
-- Datos iniciales de ejemplo
-- ----------------------------------------------------------------------------
-- NOTA: Estos son datos de ejemplo. Deben reemplazarse con datos reales
-- antes de usar en producción.
-- ----------------------------------------------------------------------------

-- Actualizar empresa existente con datos de ejemplo
UPDATE personas.empresa 
SET 
    telefono = '(021) 123-4567',
    email = 'info@miempresa.com.py',
    actividad_economica = 'Servicios de mantenimiento y reparación',
    activa = TRUE
WHERE id_empresa = 1;

-- Insertar timbrado de ejemplo (DEBE REEMPLAZARSE CON DATOS REALES)
-- Este timbrado es solo para pruebas y desarrollo
INSERT INTO financiero.timbrado 
    (numero_timbrado, establecimiento, punto_expedicion, numero_inicial, numero_final, 
     numero_actual, fecha_inicio_vigencia, fecha_fin_vigencia, id_empresa, tipo_factura, activo)
SELECT 
    '12345678' as numero_timbrado,
    '001' as establecimiento,
    '001' as punto_expedicion,
    1 as numero_inicial,
    50000 as numero_final,
    1 as numero_actual,
    CURRENT_DATE as fecha_inicio_vigencia,
    CURRENT_DATE + INTERVAL '365 days' as fecha_fin_vigencia,
    1 as id_empresa,
    'PAPEL' as tipo_factura,
    TRUE as activo
WHERE EXISTS (SELECT 1 FROM personas.empresa WHERE id_empresa = 1)
AND NOT EXISTS (SELECT 1 FROM financiero.timbrado WHERE numero_timbrado = '12345678');

-- ============================================================================
-- Fin de la migración V48
-- ============================================================================
