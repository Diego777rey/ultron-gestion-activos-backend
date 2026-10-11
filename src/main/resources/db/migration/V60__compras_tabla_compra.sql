-- =============================================================
-- MÓDULO DE COMPRAS - Tablas de Compras
-- Schema: compras
-- Descripción: Cabecera y detalle de compras, asociación producto-proveedor
-- =============================================================

-- =============================================================
-- Tabla: producto_proveedor
-- Descripción: Asociación entre productos y proveedores con historial de precios
-- =============================================================
CREATE TABLE compras.producto_proveedor (
    id_producto_proveedor   BIGSERIAL PRIMARY KEY,
    id_producto             BIGINT NOT NULL REFERENCES inventario.producto(id_producto) ON DELETE CASCADE,
    id_proveedor            BIGINT NOT NULL REFERENCES compras.proveedor(id_proveedor) ON DELETE CASCADE,
    
    precio_ultima_compra    DECIMAL(15, 2),
    fecha_ultima_compra     TIMESTAMP,
    observaciones           TEXT,
    activo                  BOOLEAN DEFAULT true,
    fecha_registro          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_producto_proveedor UNIQUE (id_producto, id_proveedor)
);

CREATE INDEX idx_producto_proveedor_producto ON compras.producto_proveedor(id_producto);
CREATE INDEX idx_producto_proveedor_proveedor ON compras.producto_proveedor(id_proveedor);
CREATE INDEX idx_producto_proveedor_activo ON compras.producto_proveedor(activo);

COMMENT ON TABLE compras.producto_proveedor IS 'Asociación entre productos y sus proveedores con historial de compras';
COMMENT ON COLUMN compras.producto_proveedor.precio_ultima_compra IS 'Último precio de compra registrado';
COMMENT ON COLUMN compras.producto_proveedor.fecha_ultima_compra IS 'Fecha de la última compra de este producto a este proveedor';

-- =============================================================
-- Tabla: compra
-- Descripción: Cabecera de compras realizadas a proveedores
-- =============================================================
CREATE TABLE compras.compra (
    id_compra               BIGSERIAL PRIMARY KEY,
    id_proveedor            BIGINT NOT NULL REFERENCES compras.proveedor(id_proveedor),
    id_vendedor             BIGINT REFERENCES compras.vendedor(id_vendedor),
    
    numero_factura          VARCHAR(100),
    timbrado                VARCHAR(100),
    fecha_factura           DATE,
    fecha_compra            TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    -- Información financiera
    forma_pago              VARCHAR(50) NOT NULL,
    moneda                  VARCHAR(20) NOT NULL DEFAULT 'PYG',
    cotizacion              DECIMAL(15, 4) DEFAULT 1,
    
    subtotal                DECIMAL(15, 2) NOT NULL DEFAULT 0,
    total_iva               DECIMAL(15, 2) DEFAULT 0,
    total                   DECIMAL(15, 2) NOT NULL DEFAULT 0,
    
    -- Estado y control
    estado                  VARCHAR(50) DEFAULT 'BORRADOR' NOT NULL,
    observaciones           TEXT,
    fecha_registro          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_ultima_modificacion TIMESTAMP,
    
    CONSTRAINT chk_compra_estado CHECK (estado IN ('BORRADOR', 'CONFIRMADA', 'RECIBIDA', 'CANCELADA'))
);

CREATE INDEX idx_compra_proveedor ON compras.compra(id_proveedor);
CREATE INDEX idx_compra_vendedor ON compras.compra(id_vendedor);
CREATE INDEX idx_compra_fecha ON compras.compra(fecha_compra);
CREATE INDEX idx_compra_estado ON compras.compra(estado);
CREATE INDEX idx_compra_numero_factura ON compras.compra(numero_factura);

COMMENT ON TABLE compras.compra IS 'Cabecera de compras realizadas a proveedores';
COMMENT ON COLUMN compras.compra.numero_factura IS 'Número de factura del proveedor';
COMMENT ON COLUMN compras.compra.timbrado IS 'Timbrado de la factura';
COMMENT ON COLUMN compras.compra.fecha_factura IS 'Fecha de la factura del proveedor';
COMMENT ON COLUMN compras.compra.forma_pago IS 'Forma de pago: EFECTIVO, TARJETA, TRANSFERENCIA, CREDITO, etc.';
COMMENT ON COLUMN compras.compra.moneda IS 'Moneda de la compra: PYG, USD, etc.';
COMMENT ON COLUMN compras.compra.cotizacion IS 'Cotización de la moneda al momento de la compra';
COMMENT ON COLUMN compras.compra.estado IS 'Estado de la compra: BORRADOR, CONFIRMADA, RECIBIDA, CANCELADA';

-- =============================================================
-- Tabla: item_compra
-- Descripción: Detalle de items en cada compra
-- =============================================================
CREATE TABLE compras.item_compra (
    id_item_compra          BIGSERIAL PRIMARY KEY,
    id_compra               BIGINT NOT NULL REFERENCES compras.compra(id_compra) ON DELETE CASCADE,
    id_producto             BIGINT NOT NULL REFERENCES inventario.producto(id_producto),
    
    cantidad                DECIMAL(15, 3) NOT NULL,
    precio_costo            DECIMAL(15, 2) NOT NULL,
    subtotal                DECIMAL(15, 2) NOT NULL,
    
    -- IVA
    tipo_iva                VARCHAR(20),
    iva                     DECIMAL(15, 2) DEFAULT 0,
    
    -- Información adicional del producto
    fecha_vencimiento       DATE,
    lote                    VARCHAR(100),
    observaciones           TEXT,
    
    fecha_registro          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_cantidad_positiva CHECK (cantidad > 0),
    CONSTRAINT chk_precio_positivo CHECK (precio_costo >= 0)
);

CREATE INDEX idx_item_compra_compra ON compras.item_compra(id_compra);
CREATE INDEX idx_item_compra_producto ON compras.item_compra(id_producto);
CREATE INDEX idx_item_compra_vencimiento ON compras.item_compra(fecha_vencimiento);

COMMENT ON TABLE compras.item_compra IS 'Detalle de items en cada compra';
COMMENT ON COLUMN compras.item_compra.cantidad IS 'Cantidad de unidades compradas';
COMMENT ON COLUMN compras.item_compra.precio_costo IS 'Precio de costo unitario';
COMMENT ON COLUMN compras.item_compra.tipo_iva IS 'Tipo de IVA: 10, 5, EXENTA';
COMMENT ON COLUMN compras.item_compra.fecha_vencimiento IS 'Fecha de vencimiento del producto (si aplica)';
COMMENT ON COLUMN compras.item_compra.lote IS 'Número de lote del producto';

-- =============================================================
-- Trigger para actualizar producto_proveedor después de insertar item_compra
-- =============================================================
CREATE OR REPLACE FUNCTION compras.actualizar_producto_proveedor()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO compras.producto_proveedor (id_producto, id_proveedor, precio_ultima_compra, fecha_ultima_compra, activo)
    SELECT 
        NEW.id_producto,
        c.id_proveedor,
        NEW.precio_costo,
        c.fecha_compra,
        true
    FROM compras.compra c
    WHERE c.id_compra = NEW.id_compra
    ON CONFLICT (id_producto, id_proveedor) 
    DO UPDATE SET
        precio_ultima_compra = EXCLUDED.precio_ultima_compra,
        fecha_ultima_compra = EXCLUDED.fecha_ultima_compra;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_actualizar_producto_proveedor
AFTER INSERT ON compras.item_compra
FOR EACH ROW
EXECUTE FUNCTION compras.actualizar_producto_proveedor();

COMMENT ON FUNCTION compras.actualizar_producto_proveedor() IS 'Actualiza la asociación producto-proveedor cuando se inserta un item de compra';
