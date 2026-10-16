-- =============================================================
-- MÓDULO DE COMPRAS
-- Schema: compras
-- =============================================================

-- Crear schema si no existe
CREATE SCHEMA IF NOT EXISTS compras;

-- =============================================================
-- Tabla: proveedor
-- Descripción: Proveedores que pueden ser persona física o jurídica
-- =============================================================
CREATE TABLE compras.proveedor (
    id_proveedor            BIGSERIAL PRIMARY KEY,
    tipo_persona            VARCHAR(20) NOT NULL CHECK (tipo_persona IN ('FISICA', 'JURIDICA')),
    
    -- Campos para persona física
    nombre                  VARCHAR(255),
    apellido                VARCHAR(255),
    
    -- Campos para persona jurídica
    razon_social            VARCHAR(255),
    nombre_comercial        VARCHAR(255),
    nombre_fantasia         VARCHAR(255),
    
    -- Campos comunes
    ruc                     VARCHAR(50) NOT NULL UNIQUE,
    documento               VARCHAR(50),
    direccion               VARCHAR(500),
    telefono                VARCHAR(50),
    email                   VARCHAR(255),
    ciudad                  VARCHAR(255),
    pais                    VARCHAR(255),
    codigo_postal           VARCHAR(20),
    sitio_web               VARCHAR(255),
    observaciones           TEXT,
    
    -- Control
    activo                  BOOLEAN DEFAULT true,
    fecha_registro          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_ultima_modificacion TIMESTAMP,
    
    CONSTRAINT chk_persona_fisica CHECK (
        tipo_persona = 'JURIDICA' OR (nombre IS NOT NULL AND apellido IS NOT NULL)
    ),
    CONSTRAINT chk_persona_juridica CHECK (
        tipo_persona = 'FISICA' OR razon_social IS NOT NULL
    )
);

-- Índices para mejorar el rendimiento
CREATE INDEX idx_proveedor_ruc ON compras.proveedor(ruc);
CREATE INDEX idx_proveedor_tipo_persona ON compras.proveedor(tipo_persona);
CREATE INDEX idx_proveedor_activo ON compras.proveedor(activo);

-- Comentarios en la tabla y columnas
COMMENT ON TABLE compras.proveedor IS 'Proveedores del sistema, pueden ser personas físicas o jurídicas';
COMMENT ON COLUMN compras.proveedor.tipo_persona IS 'Tipo de proveedor: FISICA o JURIDICA';
COMMENT ON COLUMN compras.proveedor.nombre IS 'Nombre (para persona física)';
COMMENT ON COLUMN compras.proveedor.apellido IS 'Apellido (para persona física)';
COMMENT ON COLUMN compras.proveedor.razon_social IS 'Razón social (para persona jurídica)';
COMMENT ON COLUMN compras.proveedor.nombre_comercial IS 'Nombre comercial (para persona jurídica)';
COMMENT ON COLUMN compras.proveedor.nombre_fantasia IS 'Nombre de fantasía (para persona jurídica)';
COMMENT ON COLUMN compras.proveedor.ruc IS 'RUC único del proveedor';
COMMENT ON COLUMN compras.proveedor.documento IS 'Documento de identidad';
COMMENT ON COLUMN compras.proveedor.activo IS 'Indica si el proveedor está activo en el sistema';

-- =============================================================
-- Tabla: vendedor
-- Descripción: Vendedores asociados a proveedores
-- =============================================================
CREATE TABLE compras.vendedor (
    id_vendedor             BIGSERIAL PRIMARY KEY,
    id_proveedor            BIGINT NOT NULL REFERENCES compras.proveedor(id_proveedor) ON DELETE CASCADE,
    
    nombre                  VARCHAR(255) NOT NULL,
    apellido                VARCHAR(255) NOT NULL,
    telefono                VARCHAR(50),
    email                   VARCHAR(255),
    celular                 VARCHAR(50),
    cargo                   VARCHAR(255),
    observaciones           TEXT,
    
    -- Control
    activo                  BOOLEAN DEFAULT true,
    fecha_registro          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_ultima_modificacion TIMESTAMP,
    
    CONSTRAINT chk_telefono_celular CHECK (telefono IS NOT NULL OR celular IS NOT NULL)
);

-- Índices para mejorar el rendimiento
CREATE INDEX idx_vendedor_proveedor ON compras.vendedor(id_proveedor);
CREATE INDEX idx_vendedor_activo ON compras.vendedor(activo);

-- Comentarios en la tabla y columnas
COMMENT ON TABLE compras.vendedor IS 'Vendedores asociados a proveedores';
COMMENT ON COLUMN compras.vendedor.id_proveedor IS 'Referencia al proveedor al que pertenece el vendedor';
COMMENT ON COLUMN compras.vendedor.nombre IS 'Nombre del vendedor';
COMMENT ON COLUMN compras.vendedor.apellido IS 'Apellido del vendedor';
COMMENT ON COLUMN compras.vendedor.cargo IS 'Cargo o posición del vendedor en la empresa proveedora';
COMMENT ON COLUMN compras.vendedor.activo IS 'Indica si el vendedor está activo';
