CREATE SCHEMA IF NOT EXISTS inventario;

-- Categoria de Productos (con auto-referencia para subcategorías)
CREATE TABLE IF NOT EXISTS inventario.categoria_producto (
    id_categoria_producto BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    id_categoria_padre BIGINT REFERENCES inventario.categoria_producto(id_categoria_producto)
);

-- Productos
CREATE TABLE IF NOT EXISTS inventario.producto (
    id_producto BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio_compra DECIMAL(15,2),
    precio_venta DECIMAL(15,2) NOT NULL,
    stock DECIMAL(15,2) NOT NULL DEFAULT 0,
    stock_minimo DECIMAL(15,2) NOT NULL DEFAULT 0,
    ubicacion VARCHAR(100),
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    id_categoria_producto BIGINT NOT NULL REFERENCES inventario.categoria_producto(id_categoria_producto)
);

-- Categoria de Servicios (con auto-referencia para subcategorías)
CREATE TABLE IF NOT EXISTS inventario.categoria_servicio (
    id_categoria_servicio BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    id_categoria_padre BIGINT REFERENCES inventario.categoria_servicio(id_categoria_servicio)
);

-- Servicios
CREATE TABLE IF NOT EXISTS inventario.servicio (
    id_servicio BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(15,2) NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    id_categoria_servicio BIGINT NOT NULL REFERENCES inventario.categoria_servicio(id_categoria_servicio)
);
