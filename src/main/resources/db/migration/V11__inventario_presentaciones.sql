-- =============================================================
-- Presentaciones de Producto
-- =============================================================
-- Un producto puede tener varias presentaciones (UNIDAD, PACK, CAJA, etc.).
-- Cada presentacion tiene su propio precio de venta y su propio codigo de barras.
-- Las subcategorias de productos ya estan soportadas mediante la
-- auto-referencia inventario.categoria_producto.id_categoria_padre (ver V10),
-- por eso aqui solo se agrega el indice de apoyo para esa jerarquia.

CREATE TABLE IF NOT EXISTS inventario.presentacion_producto (
    id_presentacion_producto BIGSERIAL PRIMARY KEY,
    id_producto BIGINT NOT NULL REFERENCES inventario.producto(id_producto) ON DELETE CASCADE,
    descripcion VARCHAR(100) NOT NULL,
    tipo VARCHAR(30) NOT NULL DEFAULT 'UNIDAD',
    cantidad DECIMAL(15,2) NOT NULL DEFAULT 1,
    codigo_barras VARCHAR(60),
    precio DECIMAL(15,2) NOT NULL DEFAULT 0,
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    estado BOOLEAN NOT NULL DEFAULT TRUE
);

-- El codigo de barras debe ser unico cuando esta definido.
CREATE UNIQUE INDEX IF NOT EXISTS ux_presentacion_codigo_barras
    ON inventario.presentacion_producto (codigo_barras)
    WHERE codigo_barras IS NOT NULL;

-- Indice de apoyo para listar presentaciones por producto.
CREATE INDEX IF NOT EXISTS ix_presentacion_producto
    ON inventario.presentacion_producto (id_producto);

-- Indice de apoyo para la jerarquia de categorias (subcategorias).
CREATE INDEX IF NOT EXISTS ix_categoria_producto_padre
    ON inventario.categoria_producto (id_categoria_padre);
