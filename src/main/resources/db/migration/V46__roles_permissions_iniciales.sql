-- =====================================================
-- Migración: Roles y Permisos Iniciales
-- Descripción: Crea roles (Admin, Cajero, Recepcionar vehículos) 
--              con sus permisos correspondientes
-- =====================================================

-- Insertar permisos por módulo
INSERT INTO personas.permiso (modulo, accion, descripcion) VALUES
    -- Ventas
    ('VENTAS', 'VER', 'Ver módulo de ventas'),
    ('VENTAS', 'CREAR', 'Crear ventas'),
    ('VENTAS', 'EDITAR', 'Editar ventas'),
    ('VENTAS', 'ELIMINAR', 'Eliminar ventas'),
    
    -- Taller
    ('TALLER', 'VER', 'Ver módulo de taller'),
    ('TALLER', 'CREAR', 'Crear en taller'),
    ('TALLER', 'EDITAR', 'Editar en taller'),
    ('TALLER', 'ELIMINAR', 'Eliminar en taller'),
    
    -- Orden de Trabajo
    ('ORDEN_TRABAJO', 'VER', 'Ver órdenes de trabajo'),
    ('ORDEN_TRABAJO', 'CREAR', 'Crear órdenes de trabajo'),
    ('ORDEN_TRABAJO', 'EDITAR', 'Editar órdenes de trabajo'),
    ('ORDEN_TRABAJO', 'ELIMINAR', 'Eliminar órdenes de trabajo'),
    
    -- Operaciones
    ('OPERACIONES', 'VER', 'Ver módulo de operaciones'),
    ('OPERACIONES', 'CREAR', 'Crear operaciones'),
    ('OPERACIONES', 'EDITAR', 'Editar operaciones'),
    ('OPERACIONES', 'ELIMINAR', 'Eliminar operaciones'),
    
    -- Transferencias
    ('TRANSFERENCIAS', 'VER', 'Ver transferencias'),
    ('TRANSFERENCIAS', 'CREAR', 'Crear transferencias'),
    ('TRANSFERENCIAS', 'EDITAR', 'Editar transferencias'),
    ('TRANSFERENCIAS', 'ELIMINAR', 'Eliminar transferencias'),
    
    -- Solicitudes de Repuesto
    ('SOLICITUDES_REPUESTO', 'VER', 'Ver solicitudes de repuesto'),
    ('SOLICITUDES_REPUESTO', 'CREAR', 'Crear solicitudes de repuesto'),
    ('SOLICITUDES_REPUESTO', 'EDITAR', 'Editar solicitudes de repuesto'),
    ('SOLICITUDES_REPUESTO', 'ELIMINAR', 'Eliminar solicitudes de repuesto'),
    
    -- Vehículos
    ('VEHICULOS', 'VER', 'Ver vehículos'),
    ('VEHICULOS', 'CREAR', 'Crear vehículos'),
    ('VEHICULOS', 'EDITAR', 'Editar vehículos'),
    ('VEHICULOS', 'ELIMINAR', 'Eliminar vehículos'),
    
    -- Financiero
    ('FINANCIERO', 'VER', 'Ver módulo financiero'),
    ('FINANCIERO', 'CREAR', 'Crear en financiero'),
    ('FINANCIERO', 'EDITAR', 'Editar en financiero'),
    ('FINANCIERO', 'ELIMINAR', 'Eliminar en financiero'),
    
    -- Maletines
    ('MALETINES', 'VER', 'Ver maletines'),
    ('MALETINES', 'CREAR', 'Crear maletines'),
    ('MALETINES', 'EDITAR', 'Editar maletines'),
    ('MALETINES', 'ELIMINAR', 'Eliminar maletines'),
    
    -- Cajas
    ('CAJAS', 'VER', 'Ver cajas'),
    ('CAJAS', 'CREAR', 'Crear cajas'),
    ('CAJAS', 'EDITAR', 'Editar cajas'),
    ('CAJAS', 'ELIMINAR', 'Eliminar cajas'),
    
    -- Cotizaciones
    ('COTIZACIONES', 'VER', 'Ver cotizaciones'),
    ('COTIZACIONES', 'CREAR', 'Crear cotizaciones'),
    ('COTIZACIONES', 'EDITAR', 'Editar cotizaciones'),
    ('COTIZACIONES', 'ELIMINAR', 'Eliminar cotizaciones'),
    
    -- Servicios
    ('SERVICIOS', 'VER', 'Ver servicios'),
    ('SERVICIOS', 'CREAR', 'Crear servicios'),
    ('SERVICIOS', 'EDITAR', 'Editar servicios'),
    ('SERVICIOS', 'ELIMINAR', 'Eliminar servicios'),
    
    -- Productos
    ('PRODUCTOS', 'VER', 'Ver productos'),
    ('PRODUCTOS', 'CREAR', 'Crear productos'),
    ('PRODUCTOS', 'EDITAR', 'Editar productos'),
    ('PRODUCTOS', 'ELIMINAR', 'Eliminar productos'),
    
    -- RRHH (Recursos Humanos)
    ('RRHH', 'VER', 'Ver módulo de recursos humanos'),
    ('RRHH', 'CREAR', 'Crear en recursos humanos'),
    ('RRHH', 'EDITAR', 'Editar en recursos humanos'),
    ('RRHH', 'ELIMINAR', 'Eliminar en recursos humanos'),
    
    -- Clientes
    ('CLIENTES', 'VER', 'Ver clientes'),
    ('CLIENTES', 'CREAR', 'Crear clientes'),
    ('CLIENTES', 'EDITAR', 'Editar clientes'),
    ('CLIENTES', 'ELIMINAR', 'Eliminar clientes'),
    
    -- Funcionarios
    ('FUNCIONARIOS', 'VER', 'Ver funcionarios'),
    ('FUNCIONARIOS', 'CREAR', 'Crear funcionarios'),
    ('FUNCIONARIOS', 'EDITAR', 'Editar funcionarios'),
    ('FUNCIONARIOS', 'ELIMINAR', 'Eliminar funcionarios'),
    
    -- Usuarios
    ('USUARIOS', 'VER', 'Ver usuarios'),
    ('USUARIOS', 'CREAR', 'Crear usuarios'),
    ('USUARIOS', 'EDITAR', 'Editar usuarios'),
    ('USUARIOS', 'ELIMINAR', 'Eliminar usuarios'),
    
    -- Roles
    ('ROLES', 'VER', 'Ver roles'),
    ('ROLES', 'CREAR', 'Crear roles'),
    ('ROLES', 'EDITAR', 'Editar roles'),
    ('ROLES', 'ELIMINAR', 'Eliminar roles'),
    
    -- Sectores
    ('SECTORES', 'VER', 'Ver sectores'),
    ('SECTORES', 'CREAR', 'Crear sectores'),
    ('SECTORES', 'EDITAR', 'Editar sectores'),
    ('SECTORES', 'ELIMINAR', 'Eliminar sectores'),
    
    -- Reportes
    ('REPORTES', 'VER', 'Ver reportes'),
    ('REPORTES', 'GENERAR', 'Generar reportes'),
    ('REPORTES', 'EXPORTAR', 'Exportar reportes');

-- Insertar roles
INSERT INTO personas.role (descripcion, activo) VALUES
    ('Admin', 'S'),
    ('Cajero', 'S'),
    ('Recepcionar vehículos', 'S');

-- =====================================================
-- Asignar permisos al rol ADMIN (acceso completo)
-- =====================================================
INSERT INTO personas.role_permiso (role_id, permiso_id)
SELECT 
    (SELECT id FROM personas.role WHERE descripcion = 'Admin'),
    id
FROM personas.permiso;

-- =====================================================
-- Asignar permisos al rol CAJERO (solo ventas)
-- =====================================================
INSERT INTO personas.role_permiso (role_id, permiso_id)
SELECT 
    (SELECT id FROM personas.role WHERE descripcion = 'Cajero'),
    id
FROM personas.permiso
WHERE modulo IN ('VENTAS');

-- =====================================================
-- Asignar permisos al rol RECEPCIONAR VEHÍCULOS
-- (Taller, Orden de Trabajo, Operaciones y todo el proceso)
-- =====================================================
INSERT INTO personas.role_permiso (role_id, permiso_id)
SELECT 
    (SELECT id FROM personas.role WHERE descripcion = 'Recepcionar vehículos'),
    id
FROM personas.permiso
WHERE modulo IN (
    'TALLER',
    'ORDEN_TRABAJO',
    'OPERACIONES',
    'TRANSFERENCIAS',
    'SOLICITUDES_REPUESTO',
    'VEHICULOS'
);
